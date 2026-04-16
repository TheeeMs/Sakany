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
User (Aggregate Root) ← extends AggregateRoot (Pure Java)
├── id: UUID (generated in application layer via UUID.randomUUID())
├── firstName: String (split from single 'name' field)
├── lastName: String
├── email: String? (nullable for phone-only users)
├── hashedPassword: String? (nullable for OAuth/OTP)
├── phoneNumber: String (required, unique) ← PRIMARY identifier
├── role: Role enum (RESIDENT | TECHNICIAN | ADMIN | SECURITY_GUARD)
├── loginMethod: LoginMethod enum (PHONE_OTP | EMAIL_PASSWORD | GOOGLE)
├── isPhoneVerified: boolean (default false)
├── isActive: boolean (default true, soft delete)

Creation: Private constructor + static factory method User.create()
Invariant: phoneNumber cannot be null or empty (enforced at creation)
Domain Event: Registers UserCreated on creation
State Changes: Domain methods (e.g., deactivate()) not setters
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
| Soft delete | `is_active` flag + `ON DELETE RESTRICT` | Preserve data, RESTRICT is safety net |
| Password handling | Phone-first, password optional | Mobile-first UX for residential app |
| Profile separation | Composition pattern (separate tables) | No NULL columns, clean data model |
| Profile PK | `user_id` as PK (no separate id) | True 1-to-1, simpler |
| Name field | Split: `first_name` + `last_name` | Flexible for forms, personalization |
| Domain Events | Java Records | Immutable data carriers, auto-generates boilerplate |
| UUID generation | Application layer (`UUID.randomUUID()`) | Need ID before DB save for domain events |
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

### Composition vs Inheritance (for User + Profiles)
- **Inheritance:** `Resident extends User` — rigid, can't be Resident AND Technician
- **Composition:** `User` has-a `ResidentProfile` — flexible, like inventory slots on a game character
- **ResidentProfile is INSIDE User aggregate** — it has no meaning without a User

### Static Factory Methods in DDD
- Private constructor prevents bypassing validation
- `User.create()` enforces invariants before object exists
- Not deprecated — standard pattern (List.of, Optional.of, UUID.randomUUID)

### Domain Methods vs Setters
- No setters on domain models
- Use business-meaningful methods: `deactivate()` not `setActive(false)`
- Methods can enforce rules AND register domain events

### Phone Numbers Are Strings
- Phone numbers are identifiers, not quantities
- Need `+` prefix, leading zeros, dashes — `int` can't store these

---

---

## 🔍 Mass Implementation Audit (April 16, 2026)

An extensive audit was conducted across the entire codebase to evaluate its alignment with the `01_project_abstraction.md` and `03_architecture_blueprint.md`.

### Audit Findings

1. **Massive Progress Validated ✅**
   All core business modules outlined in the abstraction (Accounts, Property, Maintenance, Access, Billing, Events, Community, Notifications) have been implemented. The implementation features correct boundary definitions (`package-info.java`) and complete sets of domains, CQRS commands, migrations (`V1` to `V10`), and persistence mappings.
   
2. **"TRUE Pure DDD" Consistency 🏆**
   The project has fiercely maintained the "TRUE Pure DDD" structure. For every single domain object across all modules (e.g. `AccessCode`, `CommunityEvent`, `Invoice`), there is a clear separation:
   - A pure pure-Java AggregateRoot (e.g. `MaintenanceRequest`) managing its invariants and states.
   - An isolated JPA Entity (e.g. `MaintenanceRequestEntity`) extending `BaseEntity`.
   - A handcrafted Mapper converting between them.
   - Domain Event registration on all state changes (e.g. `InvoicePaid`, `AccessCodeUsed`).

3. **Database Constraints & Relational Integrity 🗃️**
   The PostgreSQL migrations correctly match the established schemas, enforcing relationships securely via foreign keys, cascading deletions, and constraints (e.g., `CHECK (priority IN ('LOW', 'NORMAL', ...))`), negating the need for complex database triggers.

4. **Security & Authentication 🔐**
   The `shared/auth` package completely implements the planned security stack: Stateless JWT generation/resolution, Filter validation mapped via `SecurityConfig`, and integration with an OTP service (currently mocked memory for development, readied for Firebase).

5. **Modulith Eventing 🚌**
   Spring Modulith is properly configured with `@ApplicationModule` annotations limiting dependency pollution, forcing cross-module coordination through asynchronous Modulith event publication instead of direct beans invocation.

### Minor Architectural Deviations Noted
- `SakanyApplication.java` does not declare `@EnableAsync` which is typically required to actually force Spring event listeners into asynchronous background threads when using standard Spring Eventing (Spring Modulith wraps this, but verification is recommended).
- There is an environment mismatch identified where the build toolchain is set to Java 21, but Java 25 was passed locally causing compilation configuration issues. The codebase strictly utilizes Java 21 records and patterns perfectly.

---

*Last updated: April 16, 2026*
