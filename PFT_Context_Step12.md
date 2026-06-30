# Personal Finance Tracker — Project Context (Step 12)

> Paste file này vào đầu conversation mới để Claude hiểu đầy đủ context

---

## Thông tin developer

- **Tên:** HaiDuc
- **Skill chính:** Java Backend (intermediate)
- **Mục tiêu:** Portfolio project cho phỏng vấn vị trí Java Backend tại ngân hàng/fintech
- **Package gốc:** `com.haiduc.personalfinancetracker`

---

## Tech Stack

- **Backend:** Spring Boot **4.1.0**, Java 21, Maven
- **Database:** PostgreSQL 15 + Flyway migration
- **Cache:** Redis 7
- **Security:** Spring Security + JWT (JJWT 0.12.6)
- **Messaging:** Apache Kafka (KRaft mode, image `apache/kafka:3.7.0`)
- **Docs:** SpringDoc OpenAPI 3.0.0
- **Deploy:** Docker + Docker Compose

---

## Tiến độ

| Step | Mô tả | Trạng thái |
|------|-------|-----------|
| 1–5  | Foundation (Auth, Security, JWT) | ✅ Hoàn thành |
| 6    | Category API | ✅ Hoàn thành |
| 7    | Transaction API | ✅ Hoàn thành |
| 8    | Budget API | ✅ Hoàn thành |
| 9    | Redis Caching | ⏭ Bỏ qua — làm sau |
| 10   | Dashboard API | ✅ Hoàn thành |
| 11   | Kafka Budget Alert | ✅ Hoàn thành |
| **12** | **Email Notification** | **⬅ ĐANG LÀM** |
| 13   | PDF + CSV Export | 🔲 Chưa làm |
| 14+  | Docker, CI/CD, Frontend | 🔲 Chưa làm |

---

## Package Structure

```
com.haiduc.personalfinancetracker/
├── auth/
├── user/
├── transaction/        (TransactionService có budget alert trigger)
├── category/
├── budget/
│   ├── Budget.java
│   ├── BudgetRepository.java
│   ├── BudgetService.java
│   ├── BudgetController.java
│   ├── BudgetAlertProducer.java
│   ├── BudgetAlertConsumer.java   ← Step 12 sẽ gọi email từ đây
│   └── event/
│       └── BudgetAlertEvent.java
├── dashboard/
│   ├── DashboardService.java
│   ├── DashboardController.java
│   └── dto/
│       ├── DashboardResponse.java
│       ├── MonthlySummary.java
│       ├── BudgetSummary.java
│       └── ExpenseByCategory.java
└── config/
    ├── SecurityConfig.java
    ├── OpenApiConfig.java
    ├── CustomAuthEntryPoint.java
    └── KafkaProducerConfig.java
```

---

## Các class quan trọng đã implement

### BudgetAlertEvent

```java
package com.haiduc.personalfinancetracker.budget.event;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BudgetAlertEvent {
    private UUID userId;
    private String userEmail;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private Double percentage;
    private AlertType alertType;
    private int month;
    private int year;

    public enum AlertType {
        ALERT_80, ALERT_100
    }
}
```

### BudgetAlertProducer

```java
@Component @RequiredArgsConstructor @Slf4j
public class BudgetAlertProducer {
    private static final String TOPIC = "budget-alerts";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAlert(BudgetAlertEvent event) {
        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) log.error("Failed to send budget alert: {}", ex.getMessage());
                    else log.info("Budget alert sent: userId={}, type={}, category={}",
                            event.getUserId(), event.getAlertType(), event.getCategoryName());
                });
    }
}
```

### BudgetAlertConsumer (Step 12 sẽ thêm email vào đây)

```java
@Component @Slf4j
public class BudgetAlertConsumer {

    @KafkaListener(topics = "budget-alerts", groupId = "pft-budget-alert-group")
    public void consume(BudgetAlertEvent event) {
        log.info("Received budget alert: userId={}, type={}, category={}, percentage={}%",
                event.getUserId(), event.getAlertType(),
                event.getCategoryName(), event.getPercentage());

        // TODO Step 12: gửi email ở đây
    }
}
```

### KafkaProducerConfig (đã fix xong — dùng lambda Serializer)

```java
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper kafkaObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper kafkaObjectMapper) {
        Serializer<Object> valueSerializer = (topic, data) -> {
            try {
                return kafkaObjectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize Kafka message", e);
            }
        };

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), valueSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
```

---

## Budget Entity (quan trọng)

```java
@Entity
@Table(name = "budgets")
public class Budget extends BaseEntity {
    @Column(name = "monthly_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit;      // ← tên field là monthlyLimit (không phải limitAmount)

    @Column(nullable = false) private Short month;
    @Column(nullable = false) private Short year;

    @Column(name = "alert_sent_80", nullable = false)
    private boolean alertSent80 = false;  // ← primitive boolean

    @Column(name = "alert_sent_100", nullable = false)
    private boolean alertSent100 = false; // ← primitive boolean

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") private Category category;
}
```

---

## application.yml (hiện tại)

```yaml
app:
    jwt:
        access-token-expiry: 900000
        refresh-token-expiry: 604800000
        secret: ${JWT_SECRET:...}
server:
    port: 8080
spring:
    datasource:
        driver-class-name: org.postgresql.Driver
        hikari:
            maximum-pool-size: 10
            minimum-idle: 2
        password: ${DB_PASSWORD:root}
        url: jdbc:postgresql://localhost:5432/pft_db
        username: ${DB_USERNAME:postgres}
    flyway:
        baseline-on-migrate: true
        enabled: true
        locations: classpath:db/migration
    jpa:
        hibernate:
            ddl-auto: validate
        properties:
            hibernate:
                dialect: org.hibernate.dialect.PostgreSQLDialect
                format_sql: true
        show-sql: false
    kafka:
        bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
        producer:
            key-serializer: org.apache.kafka.common.serialization.StringSerializer
            value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
        consumer:
            group-id: pft-budget-alert-group
            auto-offset-reset: earliest
            key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
            value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
            properties:
                spring.json.trusted.packages: "com.haiduc.personalfinancetracker.*"
    # Step 12 cần thêm mail config vào đây
```

---

## docker-compose.yml (hiện tại)

```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: pft_postgres
    environment:
      POSTGRES_DB: pft_db
      POSTGRES_USER: pft_user
      POSTGRES_PASSWORD: pft_password
    ports: ["5432:5432"]
    volumes: [postgres_data:/var/lib/postgresql/data]

  redis:
    image: redis:7-alpine
    container_name: pft_redis
    ports: ["6379:6379"]
    command: redis-server --save 60 1
    volumes: [redis_data:/data]

  kafka:
    image: apache/kafka:3.7.0
    container_name: pft_kafka
    ports: ["9092:9092"]
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: true

volumes:
  postgres_data:
  redis_data:
```

---

## pom.xml — dependencies quan trọng

```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.0</version>
</parent>

<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Mail — đã thêm sẵn cho Step 12 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<!-- OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## Business Rules đã xác nhận

| Rule | Quyết định |
|------|-----------|
| Budget alert 80% | Gửi 1 lần duy nhất (alert_sent_80) |
| Budget alert 100% | Gửi 1 lần duy nhất (alert_sent_100) |
| Alert reset | Khi user update budget → reset cả 2 flag |
| Budget chỉ cho | EXPENSE category (không cho INCOME) |
| 1 budget/tháng | Unique per user + category + month + year |
| spentAmount tính theo | transaction_date |

---

## Lưu ý kỹ thuật quan trọng

- Spring Boot **4.1.0** — `org.springframework.kafka.support.serializer.JsonSerializer` đã **deprecated** → dùng lambda `Serializer<Object>` trong `KafkaProducerConfig`
- `FUNCTION('MONTH', ...)` không hoạt động với PostgreSQL → dùng `EXTRACT(MONTH FROM ...)` trong JPQL
- `Category` entity **không có** `isDeleted` → dùng `findById()` thay vì `findByIdAndIsDeletedFalse()`
- `Budget.monthlyLimit` (không phải `limitAmount`) — tên field trong entity
- `Budget.alertSent80/100` là **primitive boolean** → getter là `isAlertSent80()`

---

## Bước tiếp theo: Step 12 — Email Notification

**Việc cần làm:**
1. Thêm mail config vào `application.yml` (Gmail SMTP hoặc Mailtrap cho dev)
2. Tạo `EmailService` — compose và gửi email HTML
3. Update `BudgetAlertConsumer` — gọi `EmailService` sau khi nhận Kafka event
4. Email template cho ALERT_80 và ALERT_100

**Flow:**
```
BudgetAlertConsumer.consume(event)
        ↓
EmailService.sendBudgetAlert(event)
        ↓
JavaMailSender → Gmail SMTP / Mailtrap
```
