# Sakany - Architecture Blueprint

> **NOTE:** Fill this file with the full Modular Monolith Blueprint.

---

## 🔄 Modifications from Original Blueprint

The following changes were made to adapt the blueprint for Sakany:

### Database: PostgreSQL instead of MongoDB

| Original | Sakany |
|----------|--------|
| MongoDB with Replica Set | PostgreSQL 17.4 |
| `spring-modulith-starter-mongodb` | `spring-modulith-starter-jpa` |
| `@Document` annotations | `@Entity` annotations |
| No migrations needed | Flyway migrations |

### Why PostgreSQL?
- Strong relationships (User → Request → Technician)
- ACID transactions for payments
- Familiar SQL for team
- Better tooling (PgAdmin, etc.)

---

### Migrations: Flyway Added

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_profile_tables.sql
├── V3__create_property_tables.sql
└── ...
```

**Key config:**
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```

---

### JPA Entity Base Class

Instead of MongoDB's document approach, we use:

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

### Transaction Support

PostgreSQL transactions work out of the box:

```java
@Service
public class CreateOrderHandler extends CommandHandler<CreateOrder, OrderResponse> {
    
    @Override
    @Transactional  // Works with PostgreSQL!
    public OrderResponse handle(CreateOrder command) {
        // Atomic operations
    }
}
```

---

### Dependencies Changed

```groovy
// Removed
implementation 'org.springframework.modulith:spring-modulith-starter-mongodb'

// Added
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.modulith:spring-modulith-starter-jpa'
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
runtimeOnly 'org.postgresql:postgresql'
```

---

## 📂 Folder Structure (Adapted for Sakany)

```
sakany/
├── SakanyApplication.java
│
├── shared/
│   ├── abstractions/      ← CQRS (Command, Query, Handlers)
│   ├── auth/              ← JWT, Security
│   ├── domain/            ← AggregateRoot, DomainEvent
│   ├── exceptions/        ← Global handlers
│   ├── infrastructure/    ← Configs
│   └── jpa/               ← BaseEntity
│
├── accounts/              ← Users, Profiles
│   ├── internal/
│   │   ├── api/controllers/
│   │   ├── application/commands/
│   │   ├── domain/model/
│   │   └── infrastructure/repositories/
│   └── shared/            ← Public API
│
├── property/              ← Compound, Building, Unit
├── maintenance/           ← MaintenanceRequest
├── access/                ← Visitors, QR codes
├── billing/               ← Payments, Invoices
├── events/                ← Community events
├── community/             ← Alerts, Feedback, Announcements
└── notifications/         ← Push, DeviceTokens
```

---

<!-- 
Paste the full original Modular Monolith Blueprint below this line.
The modifications above explain what's different for Sakany.
-->
# 🏗️ Modular Monolith Project Blueprint

**A reusable architecture template for any domain**

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    YOUR APPLICATION                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
│  │ Module A │  │ Module B │  │ Module C │  │     SHARED       │ │
│  │          │  │          │  │          │  │                  │ │
│  │ internal │  │ internal │  │ internal │  │ • auth           │ │
│  │ shared → │──│ shared → │──│ shared → │──│ • abstractions   │ │
│  │          │  │          │  │          │  │ • domain base    │ │
│  └──────────┘  └──────────┘  └──────────┘  │ • infrastructure │ │
│       ▲              ▲              ▲       │ • observability  │ │
│       │              │              │       │ • exceptions     │ │
│       └──────────────┴──────────────┘       └──────────────────┘ │
│                      │                                           │
│              EventBus (publish/subscribe)                        │
└─────────────────────────────────────────────────────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │    MongoDB      │
              │  (Replica Set)  │
              └─────────────────┘
```

---

## 📂 Folder Structure Template

```
your-project/
├── src/main/java/com/yourcompany/yourapp/
│   │
│   ├── YourAppApplication.java              ← Spring Boot entry point
│   │
│   ├── shared/                              ← CROSS-CUTTING CONCERNS
│   │   ├── abstractions/                    ← CQRS base interfaces
│   │   │   ├── Command.java
│   │   │   ├── Query.java
│   │   │   ├── CommandHandler.java
│   │   │   └── QueryHandler.java
│   │   ├── auth/                            ← Security
│   │   │   ├── JwtAuthFilter.java
│   │   │   ├── JwtVerifier.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── AccessTokenPayload.java
│   │   ├── domain/                          ← DDD building blocks
│   │   │   ├── IEventBus.java
│   │   │   ├── EventBus.java
│   │   │   └── model/
│   │   │       ├── AggregateRoot.java
│   │   │       ├── Entity.java
│   │   │       ├── ValueObject.java
│   │   │       └── DomainEvent.java
│   │   ├── exceptions/                      ← Global exception handling
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BusinessRuleException.java
│   │   │   ├── NotFoundException.java
│   │   │   └── UnAuthorized.java
│   │   ├── infrastructure/                  ← Configs
│   │   │   ├── MongoDBConfig.java
│   │   │   ├── RateLimitFilter.java
│   │   │   └── CacheConfig.java
│   │   └── observability/                   ← Metrics & tracing
│   │       ├── ObservabilityConfiguration.java
│   │       └── metrics/BusinessMetrics.java
│   │
│   ├── moduleA/                             ← YOUR FIRST MODULE
│   │   ├── package-info.java                ← Module dependencies
│   │   ├── internal/                        ← PRIVATE (hidden from other modules)
│   │   │   ├── api/
│   │   │   │   ├── controllers/
│   │   │   │   └── dtos/
│   │   │   ├── application/
│   │   │   │   ├── commands/
│   │   │   │   ├── queries/
│   │   │   │   ├── eventHandlers/
│   │   │   │   └── contracts/
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   ├── events/
│   │   │   │   └── contracts/
│   │   │   └── infrastructure/
│   │   │       ├── db/
│   │   │       ├── repositories/
│   │   │       └── services/
│   │   └── shared/                          ← PUBLIC API for other modules
│   │       ├── IModuleAAPI.java
│   │       ├── ModuleAAPI.java
│   │       └── dtos/
│   │
│   ├── moduleB/                             ← YOUR SECOND MODULE
│   │   └── (same structure as moduleA)
│   │
│   └── moduleC/                             ← YOUR THIRD MODULE
│       └── (same structure as moduleA)
│
├── src/main/resources/
│   ├── application.properties
│   ├── application-prod.properties
│   ├── application-test.properties
│   └── logback-spring.xml
│
├── src/test/java/
│   ├── ModularityTest.java                  ← Verify module boundaries
│   └── testconfig/
│       └── MongoTestContainerConfig.java
│
├── Dockerfile
├── docker-compose.yml
└── build.gradle
```

---

## 🧩 Pattern 1: Module Structure

### package-info.java (Module Declaration)

```java
/**
 * [Module Name] - [Description]
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
    "shared",                    // Always include shared
    "shared :: auth",
    "shared :: abstractions",
    "shared :: domain",
    "otherModule :: shared"      // Only access other module's PUBLIC API
})
package com.yourcompany.yourapp.moduleA;
```

### Rule: Module Communication

```
✅ moduleA can access: moduleB.shared.IModuleBAPI
❌ moduleA CANNOT access: moduleB.internal.* (compilation fails!)
```

---

## 🧩 Pattern 2: Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│  API LAYER (Controllers, DTOs)                              │
│  - Receives HTTP requests                                    │
│  - Validates input                                           │
│  - Calls Application layer                                   │
├─────────────────────────────────────────────────────────────┤
│  APPLICATION LAYER (Handlers, Use Cases)                    │
│  - Commands: Write operations                                │
│  - Queries: Read operations                                  │
│  - Uses @Transactional for consistency                       │
├─────────────────────────────────────────────────────────────┤
│  DOMAIN LAYER (Models, Events, Business Rules)              │
│  - Aggregates: Transaction boundaries                        │
│  - Entities: Objects with identity                           │
│  - Value Objects: Immutable values                           │
│  - Domain Events: Something happened                         │
├─────────────────────────────────────────────────────────────┤
│  INFRASTRUCTURE LAYER (Repos, External Services)            │
│  - MongoDB repositories                                      │
│  - External API clients                                      │
│  - File storage, etc.                                        │
└─────────────────────────────────────────────────────────────┘

DEPENDENCY RULE: Arrows point inward only!
  API → Application → Domain ← Infrastructure
```

---

## 🧩 Pattern 3: CQRS (Command Query Responsibility Segregation)

### Abstractions (in shared/)

```java
// Command marker
public interface Command {}

// Query marker
public interface Query {}

// Command handler - for write operations
public abstract class CommandHandler<C extends Command, R> {
    public abstract R handle(C command);
}

// Query handler - for read operations
public abstract class QueryHandler<Q extends Query, R> {
    public abstract R handle(Q query);
}
```

### Example Command

```java
// Command record (immutable)
public record CreateOrder(
    UUID customerId,
    List<OrderItem> items,
    String shippingAddress
) implements Command {}

// Handler
@Service
public class CreateOrderHandler extends CommandHandler<CreateOrder, OrderResponse> {
    private final IOrderRepository orderRepo;
    private final IEventBus eventBus;

    @Override
    @Transactional
    @Observed(name = "handler.create_order")
    public OrderResponse handle(CreateOrder command) {
        // 1. Create domain object
        Order order = Order.create(command.customerId(), command.items());

        // 2. Save to database
        orderRepo.save(order);

        // 3. Publish events for other modules
        order.occurredEvents().forEach(eventBus::push);

        return new OrderResponse(order.getId());
    }
}
```

### Example Query

```java
public record GetOrdersByCustomer(UUID customerId) implements Query {}

@Service
public class GetOrdersByCustomerHandler extends QueryHandler<GetOrdersByCustomer, List<OrderDTO>> {
    private final IOrderReadRepository readRepo;  // Separate read repo

    @Override
    @Observed(name = "query.orders_by_customer")
    public List<OrderDTO> handle(GetOrdersByCustomer query) {
        return readRepo.findByCustomerId(query.customerId());
    }
}
```

---

## 🧩 Pattern 4: Domain-Driven Design

### AggregateRoot (Base Class)

```java
public abstract class AggregateRoot<T extends ValueObject> extends Entity<T> {
    private final List<DomainEvent> occurredEvents = new ArrayList<>();

    protected void raiseEvent(DomainEvent event) {
        occurredEvents.add(event);
    }

    public List<DomainEvent> occurredEvents() {
        List<DomainEvent> events = new ArrayList<>(this.occurredEvents);
        this.occurredEvents.clear();
        return events;
    }
}
```

### Example Aggregate

```java
public class Order extends AggregateRoot<OrderId> {
    private CustomerId customerId;
    private List<OrderLine> lines;
    private OrderStatus status;
    private Money totalAmount;

    // Factory method (not public constructor)
    public static Order create(UUID customerId, List<OrderItem> items) {
        Order order = new Order(OrderId.generate());
        order.customerId = new CustomerId(customerId);
        order.lines = items.stream().map(OrderLine::from).toList();
        order.status = OrderStatus.PENDING;
        order.totalAmount = order.calculateTotal();

        // Raise domain event
        order.raiseEvent(new OrderCreated(order.getId(), customerId, order.totalAmount));

        return order;
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new BusinessRuleException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
        raiseEvent(new OrderConfirmed(this.getId()));
    }
}
```

---

## 🧩 Pattern 5: Inter-Module Communication

### Option A: Synchronous API Call

```java
// In moduleA/shared/
public interface IModuleAAPI {
    UserDTO getUserById(UUID userId);
    List<UserDTO> getUsersByIds(List<UUID> userIds);
}

// In moduleB, inject and use:
@Service
public class SomeHandler {
    private final IModuleAAPI moduleAAPI;  // Spring injects implementation

    public void handle() {
        var user = moduleAAPI.getUserById(userId);
    }
}
```

### Option B: Asynchronous Event

```java
// Publisher (in moduleA)
public class Order extends AggregateRoot<OrderId> {
    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        raiseEvent(new OrderConfirmed(this.getId(), this.customerId));
    }
}

// In handler after save:
order.occurredEvents().forEach(eventBus::push);

// Subscriber (in moduleB)
@Service
public class OrderConfirmedHandler {
    private final IEventBus eventBus;

    @PostConstruct
    public void start() {
        eventBus.subscribe(this, OrderConfirmedDTO.class, this::handle);
    }

    private void handle(OrderConfirmedDTO event) {
        // React to order confirmed in another module
        // e.g., Start shipping, update inventory, send notification
    }
}
```

### When to Use What?

| Scenario                | Use API | Use Event |
| ----------------------- | ------- | --------- |
| Need immediate response | ✅      | ❌        |
| Fire and forget         | ❌      | ✅        |
| Multiple modules react  | ❌      | ✅        |
| Query data              | ✅      | ❌        |
| Loose coupling          | ❌      | ✅        |

---

## 🧩 Pattern 6: Event Bus

```java
public interface IEventBus {
    <T> void subscribe(Object owner, Class<T> event, EventCallback<T> callback);
    void unsubscribe(Object owner);
    <T> void push(T event);

    @FunctionalInterface
    interface EventCallback<T> {
        void handle(T event);
    }
}

@Component
public class EventBus implements IEventBus {
    // CopyOnWriteArrayList for virtual thread safety
    private final List<EventListener<?>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public <T> void subscribe(Object owner, Class<T> event, EventCallback<T> callback) {
        listeners.add(new EventListener<>(owner, event, callback));
    }

    @Override
    public <T> void push(T event) {
        listeners.forEach(listener -> {
            if (listener.event().isInstance(event)) {
                ((EventListener<T>) listener).callback().handle(event);
            }
        });
    }
}
```

---

## 🧩 Pattern 7: MongoDB with Transactions

### docker-compose.yml

```yaml
services:
  mongodb:
    image: mongo:7
    command: ['--replSet', 'rs0', '--bind_ip_all']
    ports:
      - '27017:27017'
    volumes:
      - mongodb_data:/data/db

  mongo-init:
    image: mongo:7
    depends_on:
      mongodb:
        condition: service_healthy
    entrypoint: >
      mongosh --host mongodb --eval "
        try { rs.status(); }
        catch(e) { rs.initiate({_id: 'rs0', members: [{_id: 0, host: 'mongodb:27017'}]}); }
      "

volumes:
  mongodb_data:
```

### Why Replica Set?

```
MongoDB Transactions require Replica Set because:
1. Transactions need oplog (operation log)
2. Oplog only exists in replica set
3. Even single node needs to be a "replica set of 1"
```

### Using @Transactional

```java
@Service
public class CreateOrderHandler extends CommandHandler<CreateOrder, OrderResponse> {

    @Override
    @Transactional  // Requires MongoDB replica set!
    public OrderResponse handle(CreateOrder command) {
        // All these operations are atomic:
        orderRepo.save(order);
        inventoryRepo.decreaseStock(items);
        ledgerRepo.recordTransaction(amount);
        // If any fails, ALL roll back
    }
}
```

---

## 🧩 Pattern 8: Controller → Handler Flow

```java
@RestController
@RequestMapping("/v1/orders")
public class CreateOrderController {
    private final CreateOrderHandler handler;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // 1. Convert DTO to Command
        var command = new CreateOrder(
            request.customerId(),
            request.items(),
            request.shippingAddress()
        );

        // 2. Delegate to handler
        return handler.handle(command);
    }
}
```

---

## 📋 Technology Stack Template

### build.gradle

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.5'
    id 'io.spring.dependency-management' version '1.1.6'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

ext {
    set('springModulithVersion', "1.2.2")
    set('bucket4jVersion', "8.10.1")
}

dependencies {
    // Core
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    implementation 'org.springframework.boot:spring-boot-starter-cache'

    // Modulith
    implementation 'org.springframework.modulith:spring-modulith-starter-core'
    implementation 'org.springframework.modulith:spring-modulith-starter-mongodb'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    // Rate Limiting
    implementation "com.bucket4j:bucket4j-core:${bucket4jVersion}"
    implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'

    // Observability
    runtimeOnly 'io.micrometer:micrometer-registry-otlp'
    runtimeOnly 'io.micrometer:micrometer-tracing-bridge-otel'
    implementation 'io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.3.0-alpha'

    // API Docs
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'

    // Lombok
    implementation 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.modulith:spring-modulith-starter-test'
    testImplementation 'org.testcontainers:mongodb:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.modulith:spring-modulith-bom:${springModulithVersion}"
    }
}
```

### application.properties

```properties
spring.application.name=your-app

# MongoDB
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/yourdb?replicaSet=rs0}

# Virtual Threads (Java 21)
spring.threads.virtual.enabled=true

# Security
auth.secretKey=${AUTH_SECRET_KEY}

# Observability
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.tracing.sampling.probability=1.0
management.tracing.enabled=true

# Rate Limiting
rate-limit.enabled=false
rate-limit.max-requests=100
rate-limit.window-seconds=60
```

---

## 🚀 Quick Start Checklist

When starting a new project:

1. **[ ] Define your modules** (bounded contexts)

   - What are the main business areas?
   - Example: `orders`, `inventory`, `customers`, `payments`

2. **[ ] Create shared infrastructure**

   - Copy `shared/` folder structure
   - Set up base classes (AggregateRoot, Entity, etc.)

3. **[ ] For each module:**

   - [ ] Create `package-info.java` with dependencies
   - [ ] Define public API interface in `module/shared/`
   - [ ] Create domain models (Aggregates, Entities)
   - [ ] Create Commands and Queries
   - [ ] Create Handlers with @Transactional and @Observed
   - [ ] Create Controllers that call Handlers
   - [ ] Create Repositories

4. **[ ] Set up inter-module communication**

   - APIs for synchronous calls
   - Events for async notifications

5. **[ ] Add tests**
   - [ ] ModularityTest to verify boundaries
   - [ ] Integration tests with Testcontainers

---

## 📝 Domain Examples

This pattern works for any domain:

| Domain                     | Modules                                          | Events                                    |
| -------------------------- | ------------------------------------------------ | ----------------------------------------- |
| **E-commerce**             | orders, inventory, customers, payments, shipping | OrderPlaced, PaymentReceived, ItemShipped |
| **Healthcare**             | patients, appointments, billing, prescriptions   | AppointmentBooked, PrescriptionIssued     |
| **Education**              | courses, students, enrollments, grades           | StudentEnrolled, AssignmentSubmitted      |
| **Banking**                | accounts, transactions, loans, cards             | TransferCompleted, LoanApproved           |
| **Charity (this project)** | accounts, cases, ledger, notifications           | ContributionMade, CaseOpened              |

---

**Use this blueprint as your starting point for any new project! 🎯**
