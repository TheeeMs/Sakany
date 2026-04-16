# Sakany Implementation Checklist

**Last Updated:** April 16, 2026

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

## Phase 4: Accounts Module ✅ COMPLETE

### Domain Layer (Pure Java)
- [x] `accounts/internal/domain/User.java` (aggregate root)
- [x] `accounts/internal/domain/Role.java` (enum)
- [x] `accounts/internal/domain/LoginMethod.java` (enum)
- [x] `accounts/internal/domain/events/UserCreated.java` (domain event - Java record)
- [x] Add getters to User.java
- [x] Add domain methods (deactivate, verifyPhone, etc.)
- [x] `accounts/internal/domain/ResidentProfile.java`
- [x] `accounts/internal/domain/TechnicianProfile.java`
- [x] `accounts/internal/domain/AdminProfile.java`

### Database Migrations
- [x] `V1__create_users_table.sql` (updated: name → first_name + last_name)
- [x] `V3__create_profile_tables.sql` (resident, technician, admin profiles)
- [x] Reset database to apply updated V1

### Infrastructure Layer (JPA)
- [x] `accounts/internal/infrastructure/persistence/UserEntity.java`
- [x] `accounts/internal/infrastructure/persistence/UserMapper.java`
- [x] `accounts/internal/infrastructure/persistence/UserRepositoryImpl.java`

### Module Configuration
- [x] `accounts/package-info.java`
- [x] `shared/package-info.java` (@ApplicationModule type=OPEN)

---

## Phase 5: Property Module ✅ COMPLETE
- [x] `V4__create_property_tables.sql`
- [x] `property/internal/domain/Compound.java`
- [x] `property/internal/domain/Building.java`
- [x] `property/internal/domain/Unit.java`
- [x] `property/package-info.java`

---

## Phase 6: Maintenance Module ✅ COMPLETE
- [x] `V5__create_maintenance_tables.sql`
- [x] `maintenance/internal/domain/MaintenanceRequest.java`
- [x] `maintenance/internal/domain/events/` (domain events)
- [x] `maintenance/internal/application/commands/CreateMaintenanceRequestCommand.java`
- [x] `maintenance/internal/application/commands/CreateMaintenanceRequestHandler.java`
- [x] `maintenance/internal/infrastructure/MaintenanceRequestRepository.java`
- [x] `maintenance/package-info.java`

---

## Phase 7: Access Module (Visitors & QR) ✅ COMPLETE
- [x] `V6__create_access_tables.sql`
- [x] Visitor entity
- [x] AccessCode entity (QR codes)
- [x] VisitLog entity
- [x] QR code generation service

---

## Phase 8: Billing Module ✅ COMPLETE
- [x] `V7__create_billing_tables.sql`
- [x] Invoice entity
- [x] Payment entity
- [x] Payment gateway integration

---

## Phase 9: Events Module ✅ COMPLETE
- [x] `V8__create_events_tables.sql`
- [x] CommunityEvent entity
- [x] EventRegistration entity

---

## Phase 10: Community Module ✅ COMPLETE
- [x] `V9__create_community_tables.sql`
- [x] Alert entity
- [x] Feedback entity (with voting)
- [x] Announcement entity

---

## Phase 11: Notifications Module ✅ COMPLETE
- [x] `V10__create_notifications_tables.sql`
- [x] DeviceToken entity
- [x] NotificationLog entity
- [x] Firebase FCM integration

---

## Phase 12: Authentication & Security ✅ COMPLETE
- [x] JWT implementation
- [x] Spring Security configuration
- [x] Phone OTP flow
- [x] Google OAuth integration
- [x] Role-based access control

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
