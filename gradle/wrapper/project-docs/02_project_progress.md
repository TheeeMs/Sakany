# Sakany - Project Progress & Decisions Log

**Started:** December 25, 2025  
**Architecture:** Modular Monolith with DDD + CQRS

---

## 🎯 Session 1: Domain Modeling & Foundation Setup

### Domain-Driven Design Learning

#### Concepts Covered:
1. **Aggregates** - Cluster of objects treated as a single unit for data changes
2. **Invariants** - Business rules that must ALWAYS be true
3. **Domain Events** - Business-meaningful events (e.g., `MaintenanceRequestCreated`)
4. **Rich vs Anemic Models** - Behavior in the model vs just getters/setters

---

## 📦 Module Structure Decisions

### Final Module Names:
| Module | Purpose | Renamed From |
|--------|---------|--------------|
| `accounts` | Users, Profiles, Authentication | `identity` |
| `property` | Compound, Building, Unit | *(new)* |
| `maintenance` | Maintenance requests | - |
| `access` | Visitor QR codes, gate access | - |
| `billing` | Payments, invoices | `finance` |
| `events` | Community events, RSVPs | *(new)* |
| `community` | Alerts, Feedback, Announcements | - |
| `notifications` | Push notifications, DeviceTokens | `notification` |
| `shared` | Cross-cutting concerns | - |

---

## 👤 Accounts Module Design

### User Aggregate

```
User (Aggregate Root)
├── id: UUID
├── email: String? (nullable for phone-only)
├── passwordHash: String? (nullable for OAuth/OTP)
├── phone: String (required, unique) ← PRIMARY identifier
├── name: String
├── avatarUrl: String?
├── role: RESIDENT | TECHNICIAN | ADMIN | SECURITY_GUARD
├── authProvider: PHONE_OTP | EMAIL_PASSWORD | GOOGLE
├── isPhoneVerified: Boolean
├── isActive: Boolean (soft delete)
├── createdAt, updatedAt: Timestamp
```

### Decision: Composition over Inheritance

Instead of `Resident extends User`, we chose:
- `User` (identity/auth) + separate profile entities
- `ResidentProfile` (unitId, moveInDate, isOwner)
- `TechnicianProfile` (specializations, isAvailable, rating)
- `AdminProfile` (scope/permissions)

**Reasoning:**
- Different roles have different data shapes
- Avoids NULL columns in single table
- Cleaner separation: auth logic vs domain logic
- Each profile has its own lifecycle

### Auth Strategy Decision

| Role | Primary Auth | Secondary |
|------|-------------|-----------|
| Resident | Phone OTP | Optional email |
| Technician | Phone OTP | Optional email |
| Admin | Email + Password | Or Google OAuth |

**Key Invariant:** Phone is ALWAYS required (compound identity).

---

## 🔧 Maintenance Module Design

### MaintenanceRequest Aggregate

```
MaintenanceRequest (Aggregate Root)
├── id: UUID
├── residentId: UUID
├── unitId: UUID
├── technicianId: UUID? (nullable until assigned)
├── title: String
├── description: String
├── category: PLUMBING | ELECTRICAL | HVAC | ELEVATOR | OTHER
├── priority: LOW | NORMAL | URGENT | EMERGENCY
├── isPublic: Boolean
├── photoUrls: List<String>
├── createdAt, updatedAt, resolvedAt: Timestamp
├── status: SUBMITTED | ASSIGNED | IN_PROGRESS | RESOLVED | CANCELLED | REJECTED

Invariants:
1. Cannot RESOLVE without assigned technician
2. Cannot CANCEL after IN_PROGRESS
3. Cannot change technicianId after RESOLVED
4. Cannot transition from terminal states

Domain Events:
- MaintenanceRequestCreated
- MaintenanceRequestAssigned
- MaintenanceRequestStarted
- MaintenanceRequestResolved
- MaintenanceRequestCancelled
- MaintenanceRequestRejected
```

---

## 🏠 Property Module Design

### Aggregates (Separate, not nested)

```
Compound (Aggregate Root)
├── id, name, address, createdAt

Building (Aggregate Root)
├── id, compoundId, name, numberOfFloors

Unit (Aggregate Root)
├── id, buildingId, unitNumber, floor, type
```

**Decision:** Separate aggregates because:
- Different query patterns
- Don't need to load all 500 units when viewing compound info
- Each has independent lifecycle

---

## 🗃️ Database Decisions

### PostgreSQL over MongoDB
**Reasoning:**
- Strong relationships (User → Request → Technician)
- ACID transactions needed for payments
- Data is highly relational

### Flyway for Migrations
- Version-controlled database changes
- `spring.jpa.hibernate.ddl-auto=validate` (Flyway owns schema)
- Migrations in `src/main/resources/db/migration/`

### Timestamps: Application Layer Only
- Using JPA `@CreationTimestamp` and `@UpdateTimestamp`
- Decided against database triggers
- Trade-off: Raw SQL bypasses timestamp updates

---

## 📁 Files Created

### Configuration
- [x] `docker-compose.yml` - PostgreSQL 17.4 + PgAdmin
- [x] `secrets.properties` - Database credentials (gitignored)
- [x] `build.gradle` - Updated with Flyway, Security, JWT, etc.
- [x] `application.properties` - DB connection, Flyway config

### Database Migrations
- [x] `V1__create_users_table.sql`
  - UUID primary key with `gen_random_uuid()`
  - CHECK constraints for role and auth_provider enums
  - Indexes on `role` and `is_active`
  - Timezone-aware timestamps (`TIMESTAMPTZ`)

### Pending
- [ ] Shared module base classes (Phase 2)
- [ ] V2 migration for profiles
- [ ] V3 migration for property tables

---

## 🔑 Key Architectural Decisions

| Decision | Choice | Reasoning |
|----------|--------|-----------|
| Module communication | API for sync, Events for async | Loose coupling between modules |
| Enum storage | VARCHAR + CHECK | More flexible than PostgreSQL ENUM |
| Soft delete | `is_active` flag | Preserve referential integrity |
| Password handling | Phone-first, password optional | Mobile-first UX for residential app |
| Profile separation | Composition pattern | Different data shapes per role |
| **Architecture style** | **TRUE Pure DDD** | Complete separation: domain models ≠ JPA entities |
| **Mappers** | **Manual** | Learn fundamentals before MapStruct automation |
| **AggregateRoot** | **Pure Java** | Domain layer has zero framework dependencies |

### TRUE Pure DDD Structure

```
[module]/internal/
├── domain/                          ← PURE JAVA (no frameworks)
│   ├── MaintenanceRequest.java      extends AggregateRoot
│   └── events/
│       └── MaintenanceRequestCreated.java
│
└── infrastructure/
    └── persistence/                 ← JPA LAYER
        ├── MaintenanceRequestEntity.java  extends BaseEntity
        ├── MaintenanceRequestMapper.java  (domain ↔ entity)
        └── MaintenanceRequestRepositoryImpl.java
```

**Key Rule:** Domain classes extend `AggregateRoot` (pure). JPA entities extend `BaseEntity` (has JPA). Mappers convert between them.

---

## 📚 Learning Notes

### Why `unitId: UUID` not `unitNumber: String`?
**Normalization.** If admin renames unit, only Unit table changes. Foreign keys stay valid.

### Why `ddl-auto=validate` with Flyway?
Flyway owns the schema. Hibernate should only validate entities match tables. Using `create` or `update` would conflict with Flyway's migration history.

### Aggregate Root vs Entity
- **Aggregate Root:** Entry point, owns child entities, transaction boundary
- **Entity:** Has identity, but accessed through aggregate root

### TRUE Pure DDD vs Pragmatic DDD
- **TRUE Pure DDD:** 2 classes per aggregate (domain + JPA entity) + mapper. Domain is 100% pure.
- **Pragmatic DDD:** 1 class per aggregate. Domain has JPA annotations. Simpler but less pure.
- **We chose TRUE Pure DDD** for enterprise learning experience.

---

*Last updated: January 8, 2026*


---

## 🎯 Session: Phase 4 Accounts Module Completion

### Progress and Decisions:
- **Profile Segregation:** Completed the true DDD approach by having separate purely java models for `ResidentProfile`, `TechnicianProfile`, and `AdminProfile`. They are properties of the `User` aggregate root but maintained individually to cleanly distribute logic.
- **Migration `V3__create_profile_tables.sql`:** Was written using proper constraints tying foreign keys with `user_id`, handling cascading deletion gracefully. Note that V3 was chosen since a `V2` migration existed for events publication.
- **Persistence Handling:** Overcame `BaseEntity` id mapping limitation by using reflection in the `UserMapper` to write onto the private `id` field. This maintains the true DDD abstraction without exposing JPA specifics.
- **Service Layer (CQRS):** Formally created `CreateUserCommand` and `CreateUserCommandHandler` to encapsulate creation business logic.
