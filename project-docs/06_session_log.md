# Sakany - Session Log

**Append-only log of all sessions, decisions, and learnings.**

> ⚠️ **RULE:** Only APPEND to this file, never edit or delete previous entries.

---

## Session 1: December 25, 2025

### Topics Covered:
- Domain-Driven Design fundamentals (Aggregates, Invariants, Domain Events)
- Modular Monolith architecture
- Project setup and Phase 1 completion

### Domain Models Designed:

#### User Aggregate:
- Phone-first authentication (residents), email for admins
- Composition pattern: User + separate Profile entities
- Roles: RESIDENT, TECHNICIAN, ADMIN, SECURITY_GUARD
- Auth providers: PHONE_OTP, EMAIL_PASSWORD, GOOGLE

#### MaintenanceRequest Aggregate:
- Status flow: SUBMITTED → ASSIGNED → IN_PROGRESS → RESOLVED
- Terminal states: CANCELLED, REJECTED
- Key invariant: Cannot resolve without assigned technician

#### Property Module:
- Separate aggregates: Compound, Building, Unit
- Reason: Different query patterns, independent lifecycles

### Key Decisions:

| Decision | Choice | Reason |
|----------|--------|--------|
| Database | PostgreSQL over MongoDB | Relational data, ACID for payments |
| Migrations | Flyway with ddl-auto=validate | Flyway owns schema |
| Timestamps | JPA @UpdateTimestamp | Simpler, many companies do this |
| User profiles | Composition (not inheritance) | Different data shapes per role |

### Concepts Learned:

1. **Aggregate** = Cluster of objects as single unit for data changes
2. **Invariant** = Business rule that must ALWAYS be true
3. **Domain Event** = Business-meaningful event (not technical event)
4. **Rich Model** = Behavior in the model, not anemic getters/setters
5. **Why unitId instead of unitNumber** = Normalization, referential integrity
6. **Why ddl-auto=validate** = Flyway owns schema, avoid conflicts

### Files Created:
- docker-compose.yml (PostgreSQL 17.4 + PgAdmin)
- secrets.properties (gitignored)
- build.gradle (updated with all dependencies)
- application.properties (DB + Flyway config)
- V1__create_users_table.sql (first migration)
- project-docs/ folder with all documentation

### Next Session:
- Continue with Phase 2: Shared Module Base Classes
- First file: DomainEvent.java

---

## Session 2: January 6, 2026

### Topics Covered:
- Deep dive into DDD concepts (Aggregate vs Aggregate Root)
- Pure DDD architecture vs Pragmatic approach trade-offs
- Domain layer separation from infrastructure layer
- Marker interfaces and their purpose

### Key Decisions:

| Decision | Choice | Reason |
|----------|--------|--------|
| Architecture Style | Pure DDD (separate domain/JPA entities) | Learning enterprise patterns, portfolio value |
| Mappers | Manual mappers (not MapStruct) | Understand fundamentals before automation |
| AggregateRoot | Pure Java class (no JPA) | Framework-agnostic domain layer |
| BaseEntity | JPA @MappedSuperclass | Infrastructure concern, contains id + timestamps |

### Concepts Learned:

1. **Aggregate** = The whole cluster of related objects (tree)
2. **Aggregate Root** = Entry point to that cluster (trunk)
3. **"Does it have a Repository?"** = Yes → Aggregate Root, No → Entity inside aggregate
4. **Marker Interface** = Common type for type-safe generic handling (e.g., `DomainEvent`)
5. **MaintenanceRequest naming** = Domain entity (service ticket), NOT HTTP request
6. **Why AggregateRoot pure Java** = Swap databases without touching business logic
7. **JPA annotations are passive** = Can unit test domain logic without database

### Architecture Decided:

```
DOMAIN LAYER (Pure Java):
├── shared/domain/DomainEvent.java
├── shared/domain/AggregateRoot.java
└── [module]/internal/domain/*.java (pure domain models)

INFRASTRUCTURE LAYER (JPA):
├── shared/jpa/BaseEntity.java
└── [module]/internal/infrastructure/persistence/
    ├── *Entity.java (JPA entities)
    ├── *Mapper.java (domain ↔ entity)
    └── *RepositoryImpl.java
```

### Files Created:
- `shared/domain/DomainEvent.java` - marker interface with `occurredAt()`
- `shared/domain/AggregateRoot.java` - abstract base with event collection

### Clarification Made:
- TRUE Pure DDD confirmed: Domain models extend `AggregateRoot`, JPA entities extend `BaseEntity`
- Two classes per aggregate + manual mappers

---

## Session 3: January 8, 2026

### Topics Covered:
- Clarified TRUE Pure DDD vs Pragmatic DDD
- Confirmed architecture: Domain models ≠ JPA entities
- Reviewed `AggregateRoot.java` implementation (fixed visibility modifiers)

### Key Clarification:

| Class | Extends | Layer | Has JPA? |
|-------|---------|-------|----------|
| `AggregateRoot` | nothing | Domain | ❌ No |
| `MaintenanceRequest` | `AggregateRoot` | Domain | ❌ No |
| `BaseEntity` | nothing | Infrastructure | ✅ Yes |
| `MaintenanceRequestEntity` | `BaseEntity` | Infrastructure | ✅ Yes |

### Files Modified:
- `02_project_progress.md` - Added TRUE Pure DDD structure diagram
- `AggregateRoot.java` - Fixed visibility: `protected registerEvent()`, `public getDomainEvents()`

### Next Steps:
- Create `BaseEntity.java` in shared/jpa
- Continue with CQRS abstractions

---

## Session 4: January 24, 2026

### Topics Covered:
- Completed Shared Module Base Classes (Phase 2)
- Implemented Observability with SigNoz & OpenTelemetry
- Verified application startup (Phase 3)
- Fixed Spring Modulith startup error (missing table)

### Files Created/Modified:
- `shared/jpa/BaseEntity.java` (JPA superclass)
- `shared/cqrs/*` (Command/Query interfaces)
- `shared/exception/*` (Custom exceptions + GlobalExceptionHandler)
- `build.gradle` (added OpenTelemetry dependencies)
- `application.properties` (Configured OTLP exporter, Secrets)
- `V2__create_event_publication.sql` (Fix for Spring Modulith)

### Key Decisions:
| Decision | Choice | Reason |
|----------|--------|--------|
| Observability | SigNoz + OpenTelemetry | Auto-instrumentation of Spring Boot traces/metrics |
| ID Type in Exceptions | `Object` | flexible for UUID, Long, String IDs |
| Exception Handling | `ProblemDetail` | Standard RFC 7807 JSON error responses in Spring 6 |
| Secrets | `spring.config.import` | Keep `application.properties` public, `secrets.properties` gitignored |

### Issues Encountered:
- **Startup Error:** `Schema-validation: missing table [event_publication]`.
    - **Cause:** Spring Modulith requires an event outbox table.
    - **Fix:** Created V2 migration script for `event_publication`.
- **SQL Typo:** `TIMESTAMPWith` in migration script. Fixed to `TIMESTAMPTZ`.
- **Hibernate "Unknown" Driver:** Normal behavior with HikariCP lazy connection.

### Verification (Phase 3):
- ✅ `./gradlew build` passed
- ✅ Docker (Postgres) running
- ✅ App started in 5.8s
- ✅ Schemas V1 & V2 applied

### Next Steps:
- **Phase 4: Accounts Module**
- Implement `User` Aggregate Root (Pure Domain)
- Create V3 migration for Profile tables

---

## Session 5: April 14, 2026

### Topics Covered:
- Composition vs Inheritance pattern (deep dive with real-world analogies)
- Aggregate boundaries: why ResidentProfile is part of User aggregate
- Phone-first auth reasoning (mobile-first UX, Middle East context)
- Database schema: profile tables design (Option A vs B)
- PostgreSQL fundamentals: schemas, CASCADE vs RESTRICT, ON DELETE behavior
- Soft delete strategy (is_active flag, RESTRICT as safety net)
- Docker/psql command breakdown (-it, -U, -d, -c flags)
- Pure DDD: Static factory methods for aggregate creation
- Java Records vs Classes (immutable data carriers)
- Constructor chaining with `this(...)`
- DDD state changes: domain methods vs setters (`deactivate()` not `setActive(false)`)
- Domain Events: what data to carry, who creates/stores/publishes/listens

### Domain Models Designed:
- **User Aggregate Root** (Pure Java, extends AggregateRoot)
  - Private constructor + static factory method `create()`
  - Phone number invariant enforced at creation
  - UUID generated in application layer (not DB)
  - Registers `UserCreated` domain event on creation
  - Smart defaults: isActive=true, isPhoneVerified=false, role=RESIDENT

- **UserCreated Domain Event** (Java Record)
  - Implements DomainEvent interface
  - Immutable with convenience constructor (auto-sets occurredAt)
  - Carries: id, firstName, lastName, phoneNumber, loginMethod

### Key Decisions:
| Decision | Choice | Reason |
|----------|--------|--------|
| Profile tables | Separate tables (Composition - Option B) | Clean data model, no NULL columns, learn enterprise patterns |
| Profile PK | `user_id` as PK (Option A style) | True 1-to-1, simpler than separate id |
| ON DELETE behavior | RESTRICT | Safety net, we use soft delete via is_active |
| Name field | Split to first_name + last_name | More flexible for forms and personalization |
| LoginMethod values | Match DB CHECK exactly | PHONE_OTP, EMAIL_PASSWORD, GOOGLE |
| Domain Events | Java Records | Immutable data carriers, less boilerplate |
| State changes | Domain methods, not setters | `deactivate()` enforces rules + fires events |
| UUID generation | Application layer (UUID.randomUUID()) | Need ID before DB save for domain events |

### Concepts Learned:
1. **Composition vs Inheritance** = "Has-A" vs "Is-A". Video game character with inventory slots, not biology.
2. **Aggregate boundary test** = "Does it make sense without its parent?" No → part of aggregate
3. **CASCADE** = Delete parent → auto-delete children (folder with files)
4. **RESTRICT** = Block parent deletion if children exist (safety net)
5. **Soft delete** = `is_active = false`, never hard delete in production
6. **PostgreSQL Schema** = Like a folder for tables (`public` is default)
7. **Static Factory Method** = Not deprecated, standard in Java (List.of, Optional.of)
8. **Java Record** = Auto-generates constructor, getters, equals, hashCode, toString
9. **Constructor chaining** = `this(...)` calls another constructor in same class
10. **Domain methods > setters** = Methods describe business actions and can enforce invariants
11. **Fail fast** = Validate before doing expensive operations (check phone before generating UUID)

### Files Created:
- `accounts/internal/domain/User.java` — User aggregate root (pure Java)
- `accounts/internal/domain/Role.java` — Enum: RESIDENT, TECHNICIAN, ADMIN, SECURITY_GUARD
- `accounts/internal/domain/LoginMethod.java` — Enum: PHONE_OTP, EMAIL_PASSWORD, GOOGLE
- `accounts/internal/domain/events/UserCreated.java` — Domain event (Java record)

### Files Modified:
- `V1__create_users_table.sql` — Split `name` → `first_name` + `last_name`
- `05_tasks.md` — Marked Phase 2 & 3 as complete
- `.gitignore` — Removed project-docs from ignore (docs should be in git)

### Issues Encountered:
- **Docker container not running:** Must `docker compose up -d` before exec
- **Wrong DB credentials in psql command:** Used `sakany_admin` instead of `sakany`
- **Spring Modulith visibility:** `accounts` can't see `shared.exception` sub-package (fix pending: `@ApplicationModule(type = OPEN)` on shared)

### Pending Items:
- Add getters to User.java (no setters — use domain methods instead)
- Create `shared/package-info.java` with `@ApplicationModule(type = OPEN)`
- Reset database and run migrations with updated V1
- Create V3 migration for profile tables (resident_profiles, technician_profiles, admin_profiles)

### Next Session:
- Add getters to User.java
- Fix Spring Modulith shared module visibility
- Write V3__create_profile_tables.sql
- Create profile domain models (ResidentProfile, TechnicianProfile)
- Reset DB and verify migrations

---

<!-- 
TEMPLATE FOR NEW SESSIONS:

## Session N: [DATE]

### Topics Covered:
- 

### Domain Models Designed:
- 

### Key Decisions:
| Decision | Choice | Reason |
|----------|--------|--------|

### Concepts Learned:
1. 

### Files Created:
- 

### Files Modified:
- 

### Issues Encountered:
- 

### Next Session:
- 

---
-->

