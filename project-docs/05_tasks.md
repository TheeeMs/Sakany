# Sakany Implementation Checklist

**Last Updated:** December 25, 2025

---

## Phase 1: Foundation ✅ COMPLETE
- [x] Create `docker-compose.yml` for PostgreSQL + PgAdmin
- [x] Create `secrets.properties` for credentials
- [x] Update `build.gradle` with dependencies
- [x] Update `application.properties` with DB + Flyway config
- [x] Create `V1__create_users_table.sql` migration

---

## Phase 2: Shared Module Base Classes ✅ COMPLETE

> **Architecture:** Pure DDD (separate domain models from JPA entities)

### Domain Layer (Pure Java - NO framework dependencies)
- [x] `shared/domain/DomainEvent.java` - marker interface for all domain events
- [x] `shared/domain/AggregateRoot.java` - base class with event collection

### Infrastructure Layer (JPA-aware)
- [x] `shared/jpa/BaseEntity.java` - @MappedSuperclass with id, timestamps

### CQRS Abstractions
- [x] `shared/cqrs/Command.java` - marker interface
- [x] `shared/cqrs/Query.java` - marker interface
- [x] `shared/cqrs/CommandHandler.java` - generic handler interface
- [x] `shared/cqrs/QueryHandler.java` - generic handler interface

### Exceptions
- [x] `shared/exception/BusinessRuleException.java` - for invariant violations
- [x] `shared/exception/NotFoundException.java` - for missing entities
- [x] `shared/exception/GlobalExceptionHandler.java` - @ControllerAdvice

### Logging & Observability (OpenTelemetry → SigNoz)
- [x] Add OpenTelemetry dependencies to build.gradle
- [x] Configure OTLP exporter in application.properties (points to SigNoz)
- [x] Add Spring Modulith observability module
- [x] Configure structured JSON logging with trace context

---

## Phase 3: Verify Foundation ✅ COMPLETE
- [x] `docker-compose up -d` successful
- [x] `./gradlew build` passes
- [x] `./gradlew bootRun` starts without errors
- [x] Flyway migration applied (check logs)
- [x] Connect to PgAdmin at localhost:5433

---

## Phase 4: Accounts Module ⏳ IN PROGRESS

### Domain Layer (Pure Java)
- [x] `accounts/internal/domain/User.java` (aggregate root)
- [x] `accounts/internal/domain/Role.java` (enum)
- [x] `accounts/internal/domain/LoginMethod.java` (enum)
- [x] `accounts/internal/domain/events/UserCreated.java` (domain event - Java record)
- [ ] Add getters to User.java
- [ ] Add domain methods (deactivate, verifyPhone, etc.)
- [ ] `accounts/internal/domain/ResidentProfile.java`
- [ ] `accounts/internal/domain/TechnicianProfile.java`
- [ ] `accounts/internal/domain/AdminProfile.java`

### Database Migrations
- [x] `V1__create_users_table.sql` (updated: name → first_name + last_name)
- [ ] `V3__create_profile_tables.sql` (resident, technician, admin profiles)
- [ ] Reset database to apply updated V1

### Infrastructure Layer (JPA)
- [ ] `accounts/internal/infrastructure/persistence/UserEntity.java`
- [ ] `accounts/internal/infrastructure/persistence/UserMapper.java`
- [ ] `accounts/internal/infrastructure/persistence/UserRepositoryImpl.java`

### Module Configuration
- [ ] `accounts/package-info.java`
- [ ] `shared/package-info.java` (@ApplicationModule type=OPEN)

---

## Phase 5: Property Module
- [ ] `V3__create_property_tables.sql`
- [ ] `property/internal/domain/Compound.java`
- [ ] `property/internal/domain/Building.java`
- [ ] `property/internal/domain/Unit.java`
- [ ] `property/package-info.java`

---

## Phase 6: Maintenance Module
- [ ] `V4__create_maintenance_tables.sql`
- [ ] `maintenance/internal/domain/MaintenanceRequest.java`
- [ ] `maintenance/internal/domain/events/` (domain events)
- [ ] `maintenance/internal/application/commands/CreateMaintenanceRequestCommand.java`
- [ ] `maintenance/internal/application/commands/CreateMaintenanceRequestHandler.java`
- [ ] `maintenance/internal/infrastructure/MaintenanceRequestRepository.java`
- [ ] `maintenance/package-info.java`

---

## Phase 7: Access Module (Visitors & QR)
- [ ] `V5__create_access_tables.sql`
- [ ] Visitor entity
- [ ] AccessCode entity (QR codes)
- [ ] VisitLog entity
- [ ] QR code generation service

---

## Phase 8: Billing Module
- [ ] `V6__create_billing_tables.sql`
- [ ] Invoice entity
- [ ] Payment entity
- [ ] Payment gateway integration

---

## Phase 9: Events Module
- [ ] `V7__create_events_tables.sql`
- [ ] CommunityEvent entity
- [ ] EventRegistration entity

---

## Phase 10: Community Module
- [ ] `V8__create_community_tables.sql`
- [ ] Alert entity
- [ ] Feedback entity (with voting)
- [ ] Announcement entity

---

## Phase 11: Notifications Module
- [ ] `V9__create_notifications_tables.sql`
- [ ] DeviceToken entity
- [ ] NotificationLog entity
- [ ] Firebase FCM integration

---

## Phase 12: Authentication & Security
- [ ] JWT implementation
- [ ] Spring Security configuration
- [ ] Phone OTP flow
- [ ] Google OAuth integration
- [ ] Role-based access control

---

## Phase 13: API & Testing
- [ ] OpenAPI/Swagger documentation
- [ ] ModularityTest for module boundaries
- [ ] Integration tests with Testcontainers
- [ ] Unit tests for domain logic

---

## Phase 14: Deployment
- [ ] Production Dockerfile
- [ ] Environment-specific properties
- [ ] CI/CD pipeline
- [ ] Monitoring & observability
