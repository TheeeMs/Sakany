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

