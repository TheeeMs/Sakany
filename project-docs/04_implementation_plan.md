# Sakany Implementation Guide

**Mode: You build, I review.**

---

## ✅ Phase 1: COMPLETE

- [x] docker-compose.yml (PostgreSQL 17.4 + PgAdmin)
- [x] secrets.properties
- [x] build.gradle (Flyway, Security, JWT, etc.)
- [x] application.properties
- [x] V1__create_users_table.sql

---

## ⏳ Phase 2: Shared Module Base Classes

**Your next assignment.** Create these files, then show me for review.

---

### Step 1: DomainEvent.java

**Path:** `shared/domain/DomainEvent.java`

```java
// Marker interface - all domain events implement this
// Should have: eventId (UUID), occurredAt (Instant)
```

**Think about:** Why include `occurredAt` in every event?

---

### Step 2: AggregateRoot.java

**Path:** `shared/domain/AggregateRoot.java`

```java
// Abstract class (NOT a JPA entity!)
// Fields:
//   - List<DomainEvent> occurredEvents
// Methods:
//   - protected void raiseEvent(DomainEvent event)
//   - public List<DomainEvent> occurredEvents() → returns AND clears list
```

**Think about:** Why does `occurredEvents()` clear the list after returning?

---

### Step 3: BaseEntity.java

**Path:** `shared/jpa/BaseEntity.java`

```java
// @MappedSuperclass - JPA will inherit this to child entities
// Fields:
//   - id: UUID with @Id @GeneratedValue(strategy = GenerationType.UUID)
//   - createdAt: LocalDateTime with @CreationTimestamp
//   - updatedAt: LocalDateTime with @UpdateTimestamp
// Use @Column annotations with proper names (created_at, updated_at)
```

**Think about:** Why is this separate from AggregateRoot?
- Hint: AggregateRoot is pure domain. BaseEntity knows about JPA.
- Hint: Some aggregates might not be JPA entities (in-memory only).

---

### Step 4: CQRS Interfaces

**Path:** `shared/abstractions/`

#### Command.java
```java
// Marker interface - represents a write operation
public interface Command {}
```

#### Query.java
```java
// Marker interface - represents a read operation
public interface Query {}
```

#### CommandHandler.java
```java
// Abstract class with generic types
// public abstract class CommandHandler<C extends Command, R> {
//     public abstract R handle(C command);
// }
```

#### QueryHandler.java
```java
// Same pattern as CommandHandler
// public abstract class QueryHandler<Q extends Query, R> {
//     public abstract R handle(Q query);
// }
```

---

### Step 5: Exceptions

**Path:** `shared/exceptions/`

#### BusinessRuleException.java
```java
// extends RuntimeException
// Used when domain invariants are violated
// Example: "Cannot resolve maintenance request without technician"
```

#### NotFoundException.java
```java
// extends RuntimeException
// Constructor: (String entityName, Object id)
// Message format: "User with id 123 not found"
```

#### GlobalExceptionHandler.java
```java
// @RestControllerAdvice
// Handle BusinessRuleException → 400 Bad Request
// Handle NotFoundException → 404 Not Found
// Handle generic Exception → 500 Internal Server Error
// Return JSON: { "error": "...", "timestamp": "...", "path": "..." }
```

---

## Phase 3: Verification

Run these commands:
```bash
docker-compose up -d
./gradlew build
./gradlew bootRun
```

Check:
- [ ] Application compiles without errors
- [ ] Flyway migration runs: "Migrating schema to version 1"
- [ ] No JPA validation errors
- [ ] App starts on port 8080

---

## Phase 4: Accounts Module

After shared module is done:

### V2__create_profile_tables.sql
```sql
-- resident_profiles table
-- technician_profiles table  
-- admin_profiles table
-- All with user_id foreign key
```

### Domain Entities
- `accounts/internal/domain/User.java` (extends BaseEntity)
- `accounts/internal/domain/ResidentProfile.java`
- `accounts/internal/domain/TechnicianProfile.java`
- `accounts/internal/domain/AdminProfile.java`

### Repository
- `accounts/internal/infrastructure/UserRepository.java`

### Module Declaration
- `accounts/package-info.java` with @ApplicationModule

---

## Phase 5: Property Module

### V3__create_property_tables.sql
```sql
-- compounds table
-- buildings table (compound_id FK)
-- units table (building_id FK)
```

### Domain Entities
- `property/internal/domain/Compound.java`
- `property/internal/domain/Building.java`
- `property/internal/domain/Unit.java`

---

## Phase 6: Maintenance Module

### V4__create_maintenance_tables.sql
```sql
-- maintenance_requests table
-- With all fields from domain design
-- Foreign keys to users and units
```

### Domain
- `MaintenanceRequest.java` with status state machine
- Domain events for status transitions

### Commands
- `CreateMaintenanceRequestCommand`
- `AssignTechnicianCommand`
- `ResolveMaintenanceRequestCommand`

---

## Future Phases

- Phase 7: Access Module (QR codes, visitors)
- Phase 8: Billing Module (payments, invoices)
- Phase 9: Events Module (community events)
- Phase 10: Community Module (alerts, feedback)
- Phase 11: Notifications Module (push notifications)
- Phase 12: Authentication (JWT, Spring Security)
