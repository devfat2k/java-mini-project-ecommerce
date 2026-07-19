# 📖 PROJECT OVERVIEW V2 — Mini Ecommerce (Deep-Dive Edition)

> **Mục đích tài liệu:** Đây là tài liệu chuyên sâu nhất của dự án. Bất kỳ AI Agent hay Developer nào khi bắt đầu hoặc tiếp tục công việc trên project này đều cần đọc file này ĐẦU TIÊN để nắm rõ toàn bộ kiến trúc, quy ước, flow nghiệp vụ, và trạng thái hiện tại của codebase.
>
> **Cập nhật lần cuối:** 2026-07-15

---

## 📋 MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Tech Stack & Dependencies](#2-tech-stack--dependencies)
3. [Cấu hình (Configuration)](#3-cấu-hình-configuration)
4. [Cấu trúc thư mục chi tiết](#4-cấu-trúc-thư-mục-chi-tiết)
5. [Database Schema & Migrations](#5-database-schema--migrations)
6. [Entity Layer — Chi tiết từng Entity](#6-entity-layer--chi-tiết-từng-entity)
7. [Repository Layer — Data Access](#7-repository-layer--data-access)
8. [DTO Layer — Request & Response](#8-dto-layer--request--response)
9. [Service Layer — Business Logic](#9-service-layer--business-logic)
10. [Controller Layer — API Endpoints](#10-controller-layer--api-endpoints)
11. [Security & Authentication (JWT)](#11-security--authentication-jwt)
12. [Exception Handling](#12-exception-handling)
13. [Common — API Response Format](#13-common--api-response-format)
14. [Business Flows (Luồng nghiệp vụ)](#14-business-flows-luồng-nghiệp-vụ)
15. [Coding Conventions & Patterns](#15-coding-conventions--patterns)
16. [Trạng thái phát triển & TODO](#16-trạng-thái-phát-triển--todo)

---

## 1. Tổng quan dự án

| Thuộc tính | Giá trị |
|---|---|
| **Tên dự án** | Mini Ecommerce |
| **Group ID** | `com.devfat` |
| **Artifact ID** | `mini-ecommerce` |
| **Version** | `0.0.1-SNAPSHOT` |
| **Main class** | `MiniEcommerceApplication` |
| **Base package** | `com.devfat.mini_ecommerce` |
| **Mục tiêu** | Side project cá nhân — rèn luyện Java Backend, áp dụng best practices thực tế, làm portfolio cho vị trí Java Backend Developer |
| **Kiến trúc** | Layered Architecture (Controller → Service → Repository → Entity) |
| **API Style** | RESTful JSON API, Stateless (JWT, không session) |

---

## 2. Tech Stack & Dependencies

### 2.1. Core

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| **Java** | 21 | Ngôn ngữ chính |
| **Spring Boot** | 3.5.16 | Framework core |
| **Maven** | (wrapper mvnw) | Build tool |

### 2.2. Dependencies (pom.xml)

| Dependency | Mục đích | Scope |
|---|---|---|
| `spring-boot-starter-web` | REST API, Tomcat embedded | compile |
| `spring-boot-starter-data-jpa` | ORM (Hibernate), JpaRepository | compile |
| `spring-boot-starter-validation` | Bean Validation (`@NotNull`, `@Email`, ...) | compile |
| `spring-boot-starter-security` | Spring Security framework | compile |
| `spring-boot-devtools` | Hot reload khi dev | runtime, optional |
| `postgresql` | PostgreSQL JDBC driver | runtime |
| `flyway-core` + `flyway-database-postgresql` | Database migration versioning | compile |
| `springdoc-openapi-starter-webmvc-ui` **2.7.0** | Swagger UI + OpenAPI 3 docs | compile |
| `jjwt-api` **0.12.6** | JWT API (tạo & verify token) | compile |
| `jjwt-impl` **0.12.6** | JWT implementation | runtime |
| `jjwt-jackson` **0.12.6** | JWT JSON serialization | runtime |
| `lombok` | Giảm boilerplate (`@Data`, `@Builder`, ...) | compile, optional |
| `spring-boot-starter-test` | Unit/Integration testing | test |

### 2.3. Build Plugins

- **spring-boot-maven-plugin**: Exclude Lombok khỏi fat JAR.
- **maven-compiler-plugin**: Cấu hình Lombok annotation processor cho cả compile & test-compile.

---

## 3. Cấu hình (Configuration)

### 3.1. application.yaml (file chính)

| Nhóm cấu hình | Key | Giá trị / Mô tả |
|---|---|---|
| **Profile** | `spring.profiles.active` | `dev` (mặc định) |
| **Database** | `spring.datasource.url` | `jdbc:postgresql://localhost:5432/mini_shop` |
| | `spring.datasource.driver-class-name` | `org.postgresql.Driver` |
| | `spring.datasource.hikari.maximum-pool-size` | `10` |
| | `spring.datasource.hikari.connection-timeout` | `3600000` (1 giờ) |
| **JPA** | `spring.jpa.database` | `postgresql` |
| | `spring.jpa.show-sql` | `true` |
| | `spring.jpa.open-in-view` | `false` (tắt — tránh lazy query không kiểm soát) |
| | `spring.jpa.hibernate.ddl-auto` | `update` |
| | `spring.jpa.properties.hibernate.format_sql` | `true` |
| | `spring.jpa.properties.hibernate.use_sql_comments` | `true` |
| **Flyway** | `spring.flyway.enabled` | `true` |
| | `spring.flyway.locations` | `classpath:db/migration` |
| | `spring.flyway.baseline-on-migrate` | `true` |
| | `spring.flyway.schemas` | `public` |
| **Mail** | `spring.mail.host` | `smtp.gmail.com` |
| | `spring.mail.port` | `587` (STARTTLS) |
| **JWT** | `app.jwt.secret-key` | Base64-encoded HMAC key (tự tạo) |
| | `app.jwt.expiration` | `3600000` (1 giờ, tính bằng ms) |
| | `app.jwt.refresh-expiration-days` | `7` (7 ngày) |
| **Swagger** | `springdoc.api-docs.path` | `/v1/api-docs` |
| | `springdoc.swagger-ui.path` | `/swagger-ui.html` |
| **Logging** | `logging.level.org.springframework.web` | `DEBUG` |
| | `logging.level.org.hibernate.SQL` | `DEBUG` |
| | `logging.level.org.hibernate.type` | `TRACE` |

### 3.2. Profile-specific files

| File | Server Port |
|---|---|
| `application-dev.yaml` | `8085` |
| `application-test.yaml` | `8082` |
| `application-prod.yaml` | `8083` |

> **Lưu ý quan trọng:** Namespace `app:`, `springdoc:`, `logging:` là NGANG HÀNG với `spring:`, KHÔNG lồng bên trong.

---

## 4. Cấu trúc thư mục chi tiết

```
mini-ecommerce/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/devfat/mini_ecommerce/
│   │   │   ├── MiniEcommerceApplication.java          # Entry point
│   │   │   ├── base/                                  # (RỖNG) — Dự kiến cho BaseEntity
│   │   │   ├── common/                                # Các class dùng chung
│   │   │   │   └── ApiResponse.java                   # Generic API response wrapper
│   │   │   ├── config/                                # Spring Bean configuration
│   │   │   │   ├── SecurityConfig.java                # Security filter chain, CORS, BCrypt
│   │   │   │   └── OpenApiConfig.java                 # Swagger/OpenAPI + Bearer Auth scheme
│   │   │   ├── controller/                            # REST Controllers (6 files)
│   │   │   │   ├── AuthController.java                # /api/v1/auth/**
│   │   │   │   ├── CategoryController.java            # /api/v1/categories/**
│   │   │   │   ├── ProductController.java             # /api/v1/products/**
│   │   │   │   ├── OrderController.java               # /api/v1/orders/**
│   │   │   │   ├── UserController.java                # /api/v1/users/** (RỖNG)
│   │   │   │   └── PingController.java                # /api/v1/health (health check)
│   │   │   ├── docs/                                  # Tài liệu dự án (markdown)
│   │   │   │   ├── PROJECT_OVERVIEW.md                # Bản tổng quan V1
│   │   │   │   ├── PROJECT_OVERVIEW_V2.md             # ★ BẢN NÀY
│   │   │   │   ├── auth_security_jwt_plan.md          # Plan JWT v1
│   │   │   │   ├── auth_security_jwt_plan_v2.md       # Plan JWT v2 (chi tiết)
│   │   │   │   └── d6_d7_plan.md                      # Swagger Bearer + E2E test checklist
│   │   │   ├── dto/                                   # Data Transfer Objects
│   │   │   │   ├── request/                           # 9 request DTOs (Java Records)
│   │   │   │   │   ├── RegisterRequestDto.java
│   │   │   │   │   ├── LoginRequestDto.java
│   │   │   │   │   ├── RefreshTokenRequestDto.java
│   │   │   │   │   ├── CreateProductRequestDto.java
│   │   │   │   │   ├── UpdateProductRequestDto.java
│   │   │   │   │   ├── CreateCategoryRequestDto.java
│   │   │   │   │   ├── CreateOrderRequestDto.java     # Nested OrderItemRequest record
│   │   │   │   │   ├── OrderItemRequestDto.java       # Standalone (dùng riêng)
│   │   │   │   │   └── UpdateOrderStatusRequestDto.java
│   │   │   │   └── response/                          # 7 response DTOs (Lombok Builder classes)
│   │   │   │       ├── AuthResponseDto.java
│   │   │   │       ├── UserResponseDto.java
│   │   │   │       ├── CategoryResponseDto.java
│   │   │   │       ├── ProductResponseDto.java
│   │   │   │       ├── OrderResponseDto.java
│   │   │   │       ├── OrderItemResponseDto.java
│   │   │   │       └── RefreshTokenResponseDto.java
│   │   │   ├── entity/                                # JPA Entities (6 entities)
│   │   │   │   ├── UserEntity.java
│   │   │   │   ├── CategoryEntity.java
│   │   │   │   ├── ProductEntity.java
│   │   │   │   ├── OrderEntity.java
│   │   │   │   ├── OrderItemEntity.java
│   │   │   │   └── RefreshTokenEntity.java
│   │   │   ├── enums/                                 # (RỖNG) — Enums hiện inline trong Entity
│   │   │   ├── exception/                             # Custom exceptions + Global handler
│   │   │   │   ├── GlobalExceptionHandler.java        # @RestControllerAdvice
│   │   │   │   ├── ErrorResponse.java                 # Record (timestamp, status, error, message)
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── InvalidStatusTransitionException.java
│   │   │   │   ├── CategoryHasProductsException.java
│   │   │   │   ├── InvalidRefreshTokenException.java
│   │   │   │   └── ExpiredJwtException.java           # Class rỗng, chưa sử dụng
│   │   │   ├── repository/                            # Spring Data JPA Repositories (5 repos)
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── ProductRepository.java             # Có custom JPQL queries + Projection Interfaces
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── RefreshTokenRepository.java
│   │   │   ├── security/                              # JWT Security module (7 files)
│   │   │   │   ├── JwtProvider.java                   # Tạo & validate JWT token
│   │   │   │   ├── JwtAuthenticationFilter.java       # OncePerRequestFilter — đọc Bearer token
│   │   │   │   ├── JwtAuthenticationEntryPoint.java   # Trả JSON 401 khi chưa xác thực
│   │   │   │   ├── JwtAccessDeniedHandler.java        # Trả JSON 403 khi thiếu quyền
│   │   │   │   ├── CustomUserDetailsService.java      # Load user từ DB bằng email
│   │   │   │   ├── UserPrincipal.java                 # Implements UserDetails, wrap UserEntity
│   │   │   │   └── RefreshTokenGenerator.java         # SecureRandom → Base64 token
│   │   │   ├── service/                               # Service interfaces + implementations
│   │   │   │   ├── AuthService.java                   # register, login, refreshToken, logout
│   │   │   │   ├── CategoryService.java               # CRUD category
│   │   │   │   ├── ProductService.java                # CRUD product + stock + analytics
│   │   │   │   ├── OrderService.java                  # CRUD order + state machine
│   │   │   │   ├── UserService.java                   # (RỖNG interface)
│   │   │   │   └── impl/
│   │   │   │       ├── AuthServiceImpl.java           # 213 dòng — đầy đủ logic auth
│   │   │   │       ├── CategoryServiceImpl.java       # 82 dòng
│   │   │   │       ├── ProductServiceImpl.java        # 157 dòng
│   │   │   │       ├── OrderServiceImpl.java          # 175 dòng — phức tạp nhất
│   │   │   │       └── UserServiceImpl.java           # Class rỗng, chưa triển khai
│   │   │   └── util/                                  # (CHỈ CÓ .gitkeep) — sẵn sàng cho utilities
│   │   └── resources/
│   │       ├── application.yaml                       # Cấu hình chính
│   │       ├── application-dev.yaml                   # Port 8085
│   │       ├── application-test.yaml                  # Port 8082
│   │       ├── application-prod.yaml                  # Port 8083
│   │       ├── db/migration/
│   │       │   ├── V1__init_mini_shop.sql             # Tạo 5 bảng + seed categories & products
│   │       │   └── V2__init_mini_shop.sql             # Tạo bảng refresh_tokens
│   │       ├── static/                                # (Rỗng)
│   │       └── templates/                             # (Rỗng — dự kiến cho email templates)
│   └── test/                                          # (Chưa có test code)
```

---

## 5. Database Schema & Migrations

### 5.1. Flyway Migration Files

| File | Nội dung |
|---|---|
| `V1__init_mini_shop.sql` | Tạo 5 bảng: `users`, `categories`, `products`, `orders`, `order_items` + 5 indexes + seed 20 categories + 30 products. Users/Orders/OrderItems insert bị comment out. |
| `V2__init_mini_shop.sql` | Tạo bảng `refresh_tokens` + 2 indexes (`user_id`, `token_hash`) |

### 5.2. Database Schema (6 bảng)

```
┌──────────────────────────┐       ┌──────────────────────┐
│         users            │       │     categories       │
├──────────────────────────┤       ├──────────────────────┤
│ id         SERIAL PK     │       │ id       SERIAL PK   │
│ full_name  VARCHAR(100)  │       │ name     VARCHAR(50)  │
│ email      VARCHAR(150)  │──┐    │          UNIQUE       │
│            UNIQUE        │  │    │ created_at TIMESTAMP  │
│ phone_number VARCHAR(15) │  │    └──────────┬───────────┘
│            UNIQUE        │  │               │
│ password   VARCHAR(255)  │  │               │ 1:N
│ role       VARCHAR(20)   │  │               │
│   CHECK(USER, ADMIN)     │  │    ┌──────────┴───────────┐
│ is_active  BOOLEAN       │  │    │      products        │
│ created_at TIMESTAMP     │  │    ├──────────────────────┤
│ updated_at TIMESTAMP     │  │    │ id          SERIAL PK│
└──────────┬───────────────┘  │    │ name        VARCHAR  │
           │                  │    │ description TEXT      │
           │ 1:N              │    │ price       NUMERIC  │
           │                  │    │   (12,2) CHECK>=0    │
┌──────────┴───────────────┐  │    │ stock       INTEGER  │
│         orders           │  │    │   CHECK>=0           │
├──────────────────────────┤  │    │ category_id FK───────┘
│ id          SERIAL PK    │  │    │ is_active   BOOLEAN  │
│ user_id     FK──────────────┘    │ created_at  TIMESTAMP│
│ status      VARCHAR(20)  │       │ updated_at  TIMESTAMP│
│   CHECK(PENDING,         │       └──────────┬───────────┘
│   CONFIRMED,SHIPPED,     │                  │
│   DONE,CANCELLED)        │                  │ 1:N
│ total_amount NUMERIC     │       ┌──────────┴───────────┐
│   (12,2) CHECK>=0        │       │    order_items       │
│ note        TEXT         │       ├──────────────────────┤
│ created_at  TIMESTAMP    │       │ id         SERIAL PK │
│ updated_at  TIMESTAMP    │  ┌────│ order_id   FK        │
└──────────────────────────┘  │    │ product_id FK────────┘
           ▲                  │    │ quantity   INTEGER    │
           │ 1:N              │    │   CHECK>0            │
           └──────────────────┘    │ unit_price NUMERIC   │
                                   │   (12,2) CHECK>=0    │
┌──────────────────────────┐       └──────────────────────┘
│     refresh_tokens       │
├──────────────────────────┤
│ id          SERIAL PK    │
│ user_id     BIGINT FK────┤──→ users(id)
│ token_hash  VARCHAR(255) │
│ expires_at  TIMESTAMP    │
│ revoked     BOOLEAN      │
│ created_at  TIMESTAMP    │
└──────────────────────────┘
```

### 5.3. Indexes

| Index | Bảng | Column |
|---|---|---|
| `idx_products_category` | products | category_id |
| `idx_orders_user` | orders | user_id |
| `idx_orders_status` | orders | status |
| `idx_order_items_order` | order_items | order_id |
| `idx_order_items_product` | order_items | product_id |
| `idx_refresh_tokens_user_id` | refresh_tokens | user_id |
| `idx_refresh_tokens_token_hash` | refresh_tokens | token_hash |

---

## 6. Entity Layer — Chi tiết từng Entity

> **Quy ước chung:** Tất cả entities đều dùng `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@DynamicUpdate`, `@DynamicInsert` (trừ RefreshTokenEntity chỉ có `@Data`). Timestamps dùng `@CreationTimestamp` và `@UpdateTimestamp`.

### 6.1. UserEntity (`users`)

| Field | Type | JPA Annotation | Ghi chú |
|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | PK auto-increment |
| `fullName` | `String` | `@Column(name="full_name", length=150)` | |
| `email` | `String` | `@Column(unique=true, length=150)` | Dùng làm username đăng nhập |
| `phoneNumber` | `String` | `@Column(name="phone_number", unique=true, length=15)` | |
| `password` | `String` | `@Column(nullable=false)` | Lưu BCrypt hash |
| `role` | `Role` enum | `@Enumerated(STRING), length=20` | Enum nội tại: `USER`, `ADMIN` |
| `isActive` | `boolean` | `@Column(name="is_active")` | Soft delete / disable account |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | |

> **⚠️ Lưu ý:** Enum `Role` được khai báo **INLINE** bên trong `UserEntity` (`UserEntity.Role`), không nằm trong package `enums/`.

### 6.2. CategoryEntity (`categories`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `name` | `String` | UNIQUE, length=50 |
| `products` | `List<ProductEntity>` | `@OneToMany(LAZY, mappedBy="category")` — Bi-directional |
| `createdAt` | `LocalDateTime` | |

### 6.3. ProductEntity (`products`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `name` | `String` | NOT NULL |
| `description` | `String` | `@Column(columnDefinition="TEXT")` |
| `price` | `BigDecimal` | precision=12, scale=2 |
| `stock` | `Integer` | Default 0 |
| `category` | `CategoryEntity` | `@ManyToOne(LAZY)`, FK: `category_id` |
| `isActive` | `boolean` | Soft delete |
| `orderItems` | `List<OrderItemEntity>` | `@OneToMany(mappedBy="product")` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |

### 6.4. OrderEntity (`orders`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `user` | `UserEntity` | `@ManyToOne(LAZY)`, FK: `user_id` |
| `status` | `OrderStatus` enum | Inline enum: `DONE, PENDING, SHIPPED, CONFIRMED, CANCELLED` |
| `totalAmount` | `BigDecimal` | precision=12, scale=2. Default `BigDecimal.ZERO` |
| `note` | `String` | length=1000 |
| `items` | `List<OrderItemEntity>` | `@OneToMany(mappedBy="order", cascade=ALL, orphanRemoval=true)`, `@Builder.Default` với `new ArrayList<>()` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |

### 6.5. OrderItemEntity (`order_items`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `order` | `OrderEntity` | `@ManyToOne(LAZY)`, FK: `order_id` |
| `product` | `ProductEntity` | `@ManyToOne(LAZY)`, FK: `product_id` |
| `quantity` | `Integer` | |
| `unitPrice` | `BigDecimal` | Giá **snapshot** tại thời điểm đặt hàng |

### 6.6. RefreshTokenEntity (`refresh_tokens`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `user` | `UserEntity` | `@ManyToOne(LAZY)`, FK: `user_id` |
| `tokenHash` | `String` | SHA-256 hash của raw refresh token |
| `expireAt` | `LocalDateTime` | |
| `revoked` | `boolean` | Đánh dấu token đã bị thu hồi |
| `createdAt` | `LocalDateTime` | |

> **Lưu ý:** RefreshTokenEntity **KHÔNG** dùng `@DynamicUpdate/@DynamicInsert/@Builder` như các entity khác.

---

## 7. Repository Layer — Data Access

### 7.1. UserRepository

```java
extends JpaRepository<UserEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findByEmail(String)` | Tìm user bằng email (dùng cho login & register check) |
| `findByPhoneNumber(String)` | Tìm user bằng SĐT (dùng cho register check) |

### 7.2. CategoryRepository

```java
extends JpaRepository<CategoryEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findByNameContainingIgnoreCase(String, Pageable)` | Tìm category theo tên (pagination + search) |
| `existsByNameIgnoreCase(String)` | Kiểm tra tên category đã tồn tại |

### 7.3. ProductRepository ⭐ (Phức tạp nhất)

```java
extends JpaRepository<ProductEntity, Long>
```

**Derived queries:**

| Method | Mô tả |
|---|---|
| `findByStockGreaterThan(int)` | SP còn hàng (List) |
| `findByCategoryIdAndStockGreaterThan(Long, int)` | SP theo category còn hàng |
| `findByStockGreaterThan(int, Pageable)` | SP còn hàng (phân trang) |
| `findByNameContainsIgnoreCase(String, Pageable)` | Search SP theo tên (phân trang) |

**JPQL custom queries:**

| Method | Query | Mô tả |
|---|---|---|
| `findAvailableWithCategory()` | `JOIN FETCH p.category WHERE p.stock > 0` | SP còn hàng kèm category (tránh N+1) |
| `findByMinPriceWithCategory(BigDecimal)` | `JOIN FETCH p.category WHERE p.price >= :minPrice` | SP theo giá tối thiểu |

**Projection Interfaces (nằm ngay trong ProductRepository):**

| Interface | Fields | Query | Mục đích |
|---|---|---|---|
| `TopProductView` | `name`, `price`, `mostBuy` | LEFT JOIN orderItems, GROUP BY, ORDER BY SUM(quantity) DESC | Top sản phẩm bán chạy |
| `CategoryRevenueView` | `name`, `revenue` | LEFT JOIN categories → products → orderItems, SUM(unitPrice*quantity) | Doanh thu theo category |
| `MonthlyRevenueView` | `month`, `revenue` | DATE_TRUNC('month', o.createdAt), SUM(unitPrice*quantity) | Doanh thu theo tháng |

### 7.4. OrderRepository

```java
extends JpaRepository<OrderEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findAllByUserId(Long)` | Tìm orders theo userId (trả Optional — có thể cần sửa) |
| `findAllByStatus(OrderStatus)` | Tìm orders theo status |
| `findByUserIdAndUserId(Long, Long)` | ⚠️ Method tên có vấn đề (cùng field 2 lần) |
| `findAllByUserIdAndStatus(Long, OrderStatus)` | JPQL: JOIN FETCH user, lọc userId + status |
| `findByUserIdWithDetails(Long)` | JPQL: JOIN FETCH user + LEFT JOIN FETCH items |

### 7.5. RefreshTokenRepository

```java
extends JpaRepository<RefreshTokenEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findByTokenHash(String)` | Tìm refresh token bằng SHA-256 hash |
| `deleteExpiredOrRevokedByUserId(Long, LocalDateTime)` | `@Modifying` DELETE những token đã revoked hoặc expired của 1 user |

---

## 8. DTO Layer — Request & Response

### 8.1. Request DTOs (Java Records — immutable)

| DTO | Fields | Validation |
|---|---|---|
| **RegisterRequestDto** | `fullName`, `email`, `phoneNumber`, `password` | `@NotNull @NotBlank @Email @Size @Pattern(10-11 digits)` |
| **LoginRequestDto** | `email`, `password` | `@NotNull @NotBlank @Email @Size` |
| **RefreshTokenRequestDto** | `refreshToken` | `@NotNull @NotBlank` |
| **CreateProductRequestDto** | `name`, `description`, `price`, `stock`, `categoryId`, `isActive` | `@NotNull @NotBlank @DecimalMin @Min(1)` |
| **UpdateProductRequestDto** | `name`, `description`, `price`, `stock`, `isActive`, `categoryId` | Tất cả nullable (partial update) |
| **CreateCategoryRequestDto** | `name` | `@NotNull @NotBlank` |
| **CreateOrderRequestDto** | `items: List<OrderItemRequest>` | `@NotEmpty @Valid`. Nested record: `productId(@NotNull)`, `quantity(@Min(1))` |
| **OrderItemRequestDto** | `productId`, `quantity` | Standalone record (dùng riêng, không trong CreateOrder) |
| **UpdateOrderStatusRequestDto** | `orderStatus` | `@NotNull OrderEntity.OrderStatus` enum |

> **Quy ước:** Request DTOs là **Java Records** (immutable). Response DTOs là **Lombok classes** với `@Builder`.

### 8.2. Response DTOs (Lombok Builder classes)

| DTO | Fields | Ghi chú |
|---|---|---|
| **UserResponseDto** | `userId`, `fullName`, `email`, `phoneNumber`, `role`, `isActive`, `createdAt` | **KHÔNG** chứa password |
| **AuthResponseDto** | `accessToken`, `refreshToken`, `tokenType("Bearer")`, `expiresIn` | Trả về khi login thành công |
| **RefreshTokenResponseDto** | `accessToken` | Trả về khi refresh thành công |
| **CategoryResponseDto** | `id`, `categoryName` | |
| **ProductResponseDto** | `id`, `name`, `price`, `stock`, `category(CategoryResponseDto)` | Nested category object |
| **OrderResponseDto** | `id`, `status`, `totalAmount`, `createdAt`, `orderItems(List<OrderItemResponseDto>)` | |
| **OrderItemResponseDto** | `productName`, `quantity`, `unitPrice` | |

---

## 9. Service Layer — Business Logic

### 9.1. AuthService / AuthServiceImpl ⭐

| Method | Logic chi tiết |
|---|---|
| **register(RegisterRequestDto)** | 1. Trim + lowercase email. 2. Check trùng email/phone → `DuplicateResourceException`. 3. BCrypt hash password. 4. Tạo UserEntity (role=USER, isActive=true). 5. Save + trả UserResponseDto. |
| **login(LoginRequestDto)** | 1. Trim + lowercase email. 2. Tạo `UsernamePasswordAuthenticationToken`. 3. `authenticationManager.authenticate()` → Spring tự gọi CustomUserDetailsService + PasswordEncoder. 4. Lấy UserPrincipal từ Authentication. 5. `jwtProvider.generateToken()` → accessToken. 6. Xoá expired/revoked refresh tokens của user. 7. Sinh raw refresh token (SecureRandom 64 bytes + Base64). 8. SHA-256 hash → lưu RefreshTokenEntity (expiry = now + 7 days). 9. Trả AuthResponseDto (accessToken + raw refreshToken). |
| **refreshToken(RefreshTokenRequestDto)** | 1. SHA-256 hash raw token. 2. Tìm RefreshTokenEntity by hash → `InvalidRefreshTokenException` nếu không tìm thấy. 3. Check revoked → exception. 4. Check expireAt → exception. 5. Tạo UserPrincipal từ entity.getUser(). 6. Sinh accessToken mới. 7. Trả RefreshTokenResponseDto. |
| **logout(RefreshTokenRequestDto)** | 1. SHA-256 hash. 2. Tìm entity. 3. Set revoked=true + save. 4. Idempotent — không throw nếu không tìm thấy. |

**Helper methods:**
- `hashRefreshToken(String)`: SHA-256 → Base64 (private)
- `toResponseDto(UserEntity)`: Manual mapping (private)

### 9.2. CategoryService / CategoryServiceImpl

| Method | Logic |
|---|---|
| **create(CreateCategoryRequestDto)** | Check tên đã tồn tại (`existsByNameIgnoreCase`) → exception. Tạo + save. |
| **findByNameContainingIgnoreCase(String, Pageable)** | Phân trang + search. Return `Page<CategoryResponseDto>`. |
| **findById(Long)** | findById → map → orElseThrow `ResourceNotFoundException`. |
| **update(Long, CreateCategoryRequestDto)** | findById → setName → save. |
| **deleteById(Long)** | findById → check products.isEmpty() → nếu có SP: `CategoryHasProductsException`. Nếu rỗng: hard delete. |

### 9.3. ProductService / ProductServiceImpl

| Method | Logic |
|---|---|
| **create(CreateProductRequestDto)** | Tìm category → tạo ProductEntity → set tất cả fields → save. |
| **getProductsWithSearch(String, Pageable)** | `findByNameContainsIgnoreCase` + mapping. |
| **findById(Long)** | findById → map → orElseThrow. |
| **update(Long, UpdateProductRequestDto)** | **Partial update**: Chỉ set field nào != null. Check categoryId nếu có. |
| **softDelete(Long)** | findById → check isActive → set false → save. Nếu đã inactive: throw exception. |
| **decreaseStock(Long, int)** | Validate quantity > 0 + stock >= quantity → trừ stock. |
| **increaseStock(Long, int)** | Validate quantity > 0 → cộng stock. |
| **getTopProducts(int)** | Gọi repository projection `getTopViewProduct(PageRequest.of(0, limit))`. |
| **getCategoryRevenue(Pageable)** | Gọi repository projection `getCategoryRevenue()`. |
| **getMonthlyRevenue()** | Gọi repository projection `getMonthlyRevenue()`. |

### 9.4. OrderService / OrderServiceImpl ⭐

**Order Status State Machine:**

```
PENDING ──→ CONFIRMED ──→ SHIPPED ──→ DONE
   │             │
   └──→ CANCELLED └──→ CANCELLED
```

Được implement bằng `Map<OrderStatus, Set<OrderStatus>> ALLOWED_ORDERS`:
- `PENDING` → {CONFIRMED, CANCELLED}
- `CONFIRMED` → {SHIPPED, CANCELLED}
- `SHIPPED` → {DONE}
- `DONE` / `CANCELLED` → Không có entry (terminal states)

| Method | Logic |
|---|---|
| **findByUserIdWithDetails(Long userIdPath, Long userIdInToken, String role)** | 1. Validate null. 2. Check user tồn tại (cả path lẫn token). 3. **Authorization**: Nếu role=USER → path userId phải khớp token userId, ngược lại `AccessDeniedException`. ADMIN thì bypass. 4. Query JOIN FETCH user + items. |
| **findById(Long orderId, Long userIdInToken, String role)** | 1. Validate null. 2. Tìm order. 3. Nếu role=USER → check order.user.id == userIdInToken. 4. Trả OrderResponseDto. |
| **create(Long userId, CreateOrderRequestDto)** | 1. Tìm User. 2. Tạo Order rỗng (PENDING, totalAmount=0). 3. Lặp items: tìm Product, check stock ≥ quantity, trừ stock, tạo OrderItemEntity (unitPrice = product.price), add vào order.items. 4. Tính totalAmount = Σ(unitPrice × quantity). 5. Save order (cascade saves items). |
| **changeStatus(Long id, UpdateOrderStatusRequestDto)** | 1. Tìm order. 2. Check ALLOWED_ORDERS map. 3. Nếu transition không hợp lệ → `InvalidStatusTransitionException`. 4. Set status mới → save. |

### 9.5. UserService / UserServiceImpl

> **⚠️ Hoàn toàn RỖNG** — Interface và class đều chưa có method nào.

---

## 10. Controller Layer — API Endpoints

### 10.1. AuthController (`/api/v1/auth`)

> **Đặc biệt:** Có `@SecurityRequirements({})` → LOẠI BỎ yêu cầu Bearer token trên Swagger cho nhóm API này.

| HTTP | Path | Method | Auth | Request Body | Response |
|---|---|---|---|---|---|
| POST | `/register` | register | ❌ Public | RegisterRequestDto | 201 + UserResponseDto |
| POST | `/login` | login | ❌ Public | LoginRequestDto | 200 + AuthResponseDto |
| POST | `/refresh-token` | refreshToken | ❌ Public | RefreshTokenRequestDto | 200 + RefreshTokenResponseDto |
| POST | `/logout` | logout | ❌ Public | RefreshTokenRequestDto | 200 + null |

### 10.2. CategoryController (`/api/v1/categories`)

| HTTP | Path | Method | Auth | Request | Response |
|---|---|---|---|---|---|
| POST | `/` | createCategory | 🔒 ADMIN | CreateCategoryRequestDto | 201 + CategoryResponseDto |
| GET | `/` | getCategories | ❌ Public (GET) | `?page=&size=&search=&sort=&direction=` | 200 + Page\<CategoryResponseDto\> |
| GET | `/{id}` | getCategoriesById | ❌ Public (GET) | Path: id | 200 + CategoryResponseDto |
| PUT | `/{id}` | updateCategory | 🔒 ADMIN | CreateCategoryRequestDto | 200 + CategoryResponseDto |
| DELETE | `/{id}` | deleteCategory | 🔒 ADMIN | Path: id | 200 + Boolean |

### 10.3. ProductController (`/api/v1/products`) ⭐

> Có `@Operation` annotations cho Swagger descriptions.

| HTTP | Path | Method | Auth | Request | Response |
|---|---|---|---|---|---|
| POST | `/` | createProduct | 🔒 ADMIN | CreateProductRequestDto | 201 + ProductResponseDto |
| GET | `/` | getAll | ❌ Public (GET, `@SecurityRequirements({})`) | `?page=&size=&search=&sort=&direction=` | 200 + Page\<ProductResponseDto\> |
| GET | `/{id}` | getById | 🔑 Authenticated | Path: id | 200 + ProductResponseDto |
| PATCH | `/{id}` | updateProduct | 🔒 ADMIN | UpdateProductRequestDto | 200 + ProductResponseDto |
| DELETE | `/{id}` | deleteProduct | 🔒 ADMIN | Path: id | 200 + Boolean (soft delete) |
| PATCH | `/increase/{id}` | increaseStock | 🔒 ADMIN | `?quantity=` | 200 + ProductResponseDto |
| PATCH | `/decrease/{id}` | decreaseStock | 🔒 ADMIN | `?quantity=` | 200 + ProductResponseDto |
| GET | `/top-buy` | getTopBuyProduct | 🔒 ADMIN | `?limit=10` | 200 + List\<TopProductView\> |
| GET | `/revenue-by-category` | getRevenueByCategory | 🔒 ADMIN | (none) | 200 + List\<CategoryRevenueView\> |
| GET | `/revenue-in-month` | getMonthlyRevenue | 🔒 ADMIN | (none) | 200 + List\<MonthlyRevenueView\> |

### 10.4. OrderController (`/api/v1/orders`)

| HTTP | Path | Method | Auth | Request | Response |
|---|---|---|---|---|---|
| GET | `/user/{userId}` | getOrderResponse | 🔑 Authenticated | Path: userId + `@AuthenticationPrincipal` | 200 + List\<OrderResponseDto\> |
| GET | `/{id}` | getOrderById | 🔑 Authenticated | Path: orderId + `@AuthenticationPrincipal` | 200 + OrderResponseDto |
| POST | `/` | createOrder | 🔑 Authenticated | CreateOrderRequestDto + `@AuthenticationPrincipal` | 201 + OrderResponseDto |
| PATCH | `/{id}/status` | updateOrderStatus | 🔒 ADMIN | UpdateOrderStatusRequestDto | 200 + OrderResponseDto |

> **Quan trọng:** Order APIs lấy `userId` từ JWT token (`@AuthenticationPrincipal UserPrincipal`), KHÔNG từ request body. Điều này ngăn chặn IDOR attack.

### 10.5. PingController (`/api/v1/health`)

| HTTP | Path | Auth | Response |
|---|---|---|---|
| GET | `/` | ❌ Public | "pong" (plain text) |

### 10.6. UserController (`/api/v1/users`)

> **⚠️ RỖNG** — Chưa có endpoint nào.

---

## 11. Security & Authentication (JWT)

### 11.1. Security Architecture Flow

```
Client Request
    │
    ▼
┌─────────────────────────────────────────────────────────────┐
│                  Security Filter Chain                       │
│                                                              │
│  ┌─────────────────────────────────────────┐                │
│  │    JwtAuthenticationFilter              │                │
│  │    (OncePerRequestFilter)               │                │
│  │                                          │                │
│  │  1. Đọc header "Authorization"          │                │
│  │  2. Tách "Bearer <token>"               │                │
│  │  3. jwtProvider.validateToken(token)     │                │
│  │  4. Nếu valid:                           │                │
│  │     - getEmailFromToken()                │                │
│  │     - loadUserByUsername(email) → DB     │                │
│  │     - Tạo Authentication object         │                │
│  │     - Set vào SecurityContextHolder     │                │
│  │  5. LUÔN gọi filterChain.doFilter()     │                │
│  └─────────────────────────────────────────┘                │
│                    │                                         │
│                    ▼                                         │
│  ┌─────────────────────────────────────────┐                │
│  │    Authorization Check                   │                │
│  │    (URL patterns + @PreAuthorize)        │                │
│  │                                          │                │
│  │  PUBLIC_URLS: /auth/register, /login,    │                │
│  │               /refresh-token, /logout    │                │
│  │  PUBLIC_GET:  /products, /products/{id}, │                │
│  │               /categories, /categories/  │                │
│  │               {id}                       │                │
│  │  SWAGGER:     /swagger-ui/**, /v1/api-   │                │
│  │               docs/**                    │                │
│  │  .anyRequest().authenticated()           │                │
│  └─────────────────────────────────────────┘                │
│                    │                                         │
│        ┌───────────┴───────────┐                            │
│        ▼                       ▼                            │
│  Chưa xác thực            Thiếu quyền                      │
│  (No/Invalid Token)       (Role mismatch)                   │
│        │                       │                            │
│        ▼                       ▼                            │
│  JwtAuthentication        JwtAccessDenied                   │
│  EntryPoint               Handler                           │
│  → JSON 401               → JSON 403                       │
└─────────────────────────────────────────────────────────────┘
    │
    ▼
Controller → Service → Repository → Database
```

### 11.2. JWT Token Structure

**Payload claims:**
```json
{
  "userId": 1,
  "role": "USER",
  "sub": "user@example.com",
  "iat": 1720000000,
  "exp": 1720003600
}
```

- **Signing**: HMAC-SHA với secret key Base64-decoded
- **Expiry**: 1 giờ (3600000 ms)
- **Refresh Token**: Opaque (SecureRandom 64 bytes → Base64), KHÔNG phải JWT. Lưu SHA-256 hash trong DB.

### 11.3. SecurityConfig chi tiết

| Bean | Mô tả |
|---|---|
| `PasswordEncoder` | `BCryptPasswordEncoder` (strength=10) |
| `SecurityFilterChain` | CSRF disabled, STATELESS session, CORS configured, JWT filter before UsernamePasswordAuthenticationFilter |
| `CorsConfigurationSource` | AllowedOrigins: `http://localhost:8085`. Methods: GET/POST/PUT/PATCH/DELETE. Headers: Authorization, Content-Type. |
| `AuthenticationManager` | Auto-configured từ `AuthenticationConfiguration` (tự tìm UserDetailsService + PasswordEncoder beans) |

### 11.4. Security Classes

| Class | Responsibility |
|---|---|
| `JwtProvider` | Tạo JWT (`generateToken`), validate (`validateToken`), extract claims (`getEmailFromToken`, `getUserIdFromToken`, `getRoleFromToken`), expose expiration. |
| `JwtAuthenticationFilter` | `OncePerRequestFilter` — intercept mỗi request, set Authentication vào SecurityContext. |
| `JwtAuthenticationEntryPoint` | `AuthenticationEntryPoint` — trả JSON 401 Unauthorized. Dùng `ObjectMapper` + `ApiResponse.error()`. |
| `JwtAccessDeniedHandler` | `AccessDeniedHandler` — trả JSON 403 Forbidden. Dùng `ObjectMapper` + `ApiResponse.error()`. |
| `CustomUserDetailsService` | `UserDetailsService` — load user từ DB bằng email, wrap thành `UserPrincipal`. |
| `UserPrincipal` | `UserDetails` implementation. Wrap `UserEntity`. `getUsername()` = email. Authority = `ROLE_` + role. `isEnabled()` = `isActive`. `isAccountNonExpired/Locked`, `isCredentialsNonExpired` = true (mặc định). |
| `RefreshTokenGenerator` | SecureRandom 64 bytes → Base64 (without padding). |

---

## 12. Exception Handling

### 12.1. GlobalExceptionHandler (`@RestControllerAdvice`)

| Exception | HTTP Status | Response |
|---|---|---|
| `ResourceNotFoundException` | 404 NOT_FOUND | `ApiResponse.error(message)` |
| `InsufficientStockException` | 409 CONFLICT | `ApiResponse.error(message)` |
| `InvalidStatusTransitionException` | 409 CONFLICT | `ApiResponse.error(message)` |
| `CategoryHasProductsException` | 409 CONFLICT | `ApiResponse.error(message)` |
| `DuplicateResourceException` | 409 CONFLICT | `ApiResponse.error(message)` |
| `MethodArgumentNotValidException` | 400 BAD_REQUEST | `ApiResponse<Map<String,String>>` — field → message |
| `DataIntegrityViolationException` | 409 CONFLICT | "Resource already exists" |
| `BadCredentialsException` | 401 UNAUTHORIZED | `ApiResponse.error(message)` |
| `DisabledException` | 403 FORBIDDEN | `ApiResponse.error(message)` |
| `AccessDeniedException` | 403 FORBIDDEN | `ApiResponse.error(message)` |
| `InvalidRefreshTokenException` | 401 UNAUTHORIZED | `ApiResponse.error(message)` |
| `Exception` (catch-all) | 500 INTERNAL_SERVER_ERROR | `ApiResponse.error(message)` |

### 12.2. Custom Exception Classes

Tất cả đều kế thừa `RuntimeException` với constructor `(String message)`:

| Class | Khi nào throw |
|---|---|
| `ResourceNotFoundException` | Không tìm thấy entity trong DB |
| `DuplicateResourceException` | Trùng email/phone khi register |
| `InsufficientStockException` | Không đủ tồn kho, hoặc quantity <= 0 |
| `InvalidStatusTransitionException` | Chuyển trạng thái order không hợp lệ |
| `CategoryHasProductsException` | Xoá category còn chứa sản phẩm |
| `InvalidRefreshTokenException` | Refresh token không hợp lệ hoặc đã hết hạn/revoked |
| `ExpiredJwtException` | ⚠️ Class rỗng, chưa sử dụng (shadow `io.jsonwebtoken.ExpiredJwtException`) |
| `ErrorResponse` | Record `(timestamp, status, error, message)` — ⚠️ chưa được sử dụng trực tiếp trong code |

---

## 13. Common — API Response Format

### ApiResponse<T>

```java
@Getter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
```

**Factory methods:**
- `ApiResponse.success(data, message)` → `success=true`, `data=T`, `timestamp=now()`
- `ApiResponse.error(message)` → `success=false`, `data=null`, `timestamp=now()`

**Ví dụ response thành công:**
```json
{
  "success": true,
  "message": "Create Product Successfully!",
  "data": { "id": 1, "name": "iPhone 16", ... },
  "timestamp": "2026-07-15T22:00:00"
}
```

**Ví dụ response lỗi:**
```json
{
  "success": false,
  "message": "Product id is not found",
  "timestamp": "2026-07-15T22:00:00"
}
```

> `@JsonInclude(NON_NULL)` → field `data` sẽ KHÔNG xuất hiện trong JSON khi nó là null.

---

## 14. Business Flows (Luồng nghiệp vụ)

### 14.1. Luồng Authentication hoàn chỉnh

```
                 ┌────────────────────────────────────┐
                 │           REGISTER                  │
                 │  POST /api/v1/auth/register         │
                 │                                     │
                 │  1. Validate email/phone format      │
                 │  2. Check trùng email → 409          │
                 │  3. Check trùng phone → 409          │
                 │  4. BCrypt hash password              │
                 │  5. Save user (role=USER, active=true)│
                 │  6. Return UserResponseDto (201)      │
                 └───────────────┬─────────────────────┘
                                 │
                                 ▼
                 ┌────────────────────────────────────┐
                 │             LOGIN                    │
                 │  POST /api/v1/auth/login             │
                 │                                      │
                 │  1. AuthenticationManager.authenticate│
                 │     → CustomUserDetailsService       │
                 │     → PasswordEncoder.matches()      │
                 │  2. Sai → BadCredentialsException     │
                 │  3. Account disabled → DisabledException│
                 │  4. Đúng → Generate JWT accessToken   │
                 │  5. Clean expired refresh tokens      │
                 │  6. Generate raw refresh token        │
                 │  7. SHA-256 hash → save to DB         │
                 │  8. Return {accessToken, refreshToken}│
                 └───────────────┬──────────────────────┘
                                 │
                    ┌────────────┴─────────────┐
                    ▼                          ▼
         accessToken (1h)            refreshToken (7 days)
         Gửi trong header            Lưu phía client
         Authorization:              (localStorage/cookie)
         Bearer <token>
                    │                          │
                    │                          │
                    ▼                          ▼
         ┌─────────────────┐      ┌──────────────────────┐
         │  API Calls       │      │  REFRESH TOKEN        │
         │  (mỗi request)   │      │  POST /auth/refresh   │
         │                  │      │                        │
         │  JwtFilter check │      │  1. SHA-256 hash raw   │
         │  → validate      │      │  2. Find by hash in DB │
         │  → set Auth      │      │  3. Check !revoked     │
         │                  │      │  4. Check !expired     │
         └─────────────────┘      │  5. Generate new       │
                                   │     accessToken        │
                                   │  6. Return {accessToken}│
                                   └──────────────────────┘
                                              │
                                              ▼
                                   ┌──────────────────────┐
                                   │      LOGOUT           │
                                   │  POST /auth/logout    │
                                   │                        │
                                   │  1. SHA-256 hash raw   │
                                   │  2. Find entity        │
                                   │  3. Set revoked=true   │
                                   │  4. Save (idempotent)  │
                                   └──────────────────────┘
```

### 14.2. Luồng tạo đơn hàng

```
Client (đã login)
    │
    │ POST /api/v1/orders
    │ Body: { "items": [{ "productId": 1, "quantity": 2 }, ...] }
    │ Header: Authorization: Bearer <token>
    │
    ▼
OrderController.createOrder()
    │ userId lấy từ @AuthenticationPrincipal (KHÔNG từ body)
    │
    ▼
OrderServiceImpl.create(userId, request)
    │
    ├─ 1. userRepository.findById(userId) → user
    │     └── Không tìm thấy → ResourceNotFoundException
    │
    ├─ 2. Tạo OrderEntity rỗng
    │     status = PENDING
    │     totalAmount = 0
    │
    ├─ 3. Lặp qua request.items():
    │     │
    │     ├── productRepository.findById(productId) → product
    │     │   └── Không tìm thấy → ResourceNotFoundException
    │     │
    │     ├── Check: product.stock >= quantity
    │     │   └── Không đủ → InsufficientStockException
    │     │
    │     ├── product.setStock(stock - quantity)  ← TRỪ KHO NGAY
    │     │
    │     └── Tạo OrderItemEntity:
    │         order = this order
    │         product = product
    │         quantity = request quantity
    │         unitPrice = product.getPrice()  ← SNAPSHOT GIÁ
    │
    ├─ 4. Tính totalAmount = Σ(unitPrice × quantity)
    │
    └─ 5. orderRepository.save(order)
          └── Cascade.ALL → save tất cả OrderItems
          └── Return OrderResponseDto (201)
```

### 14.3. Order State Machine

```
    ┌──────────┐
    │ PENDING  │
    └──┬───┬───┘
       │   │
  ┌────┘   └────┐
  ▼              ▼
┌──────────┐ ┌───────────┐
│CONFIRMED │ │ CANCELLED │  ← Terminal state
└────┬──┬──┘ └───────────┘
     │  │
┌────┘  └────┐
▼            ▼
┌────────┐ ┌───────────┐
│SHIPPED │ │ CANCELLED │
└───┬────┘ └───────────┘
    │
    ▼
┌────────┐
│  DONE  │  ← Terminal state
└────────┘
```

**Validation logic:** `ALLOWED_ORDERS` Map — nếu transition không có trong map → `InvalidStatusTransitionException`.

### 14.4. Authorization Logic trong Order

- **Xem orders theo userId (`GET /user/{userId}`):**
  - ADMIN → xem bất kỳ user nào
  - USER → chỉ xem orders của chính mình (so sánh `userIdPath == userIdInToken`)

- **Xem order chi tiết (`GET /{id}`):**
  - ADMIN → xem bất kỳ order nào
  - USER → chỉ xem order nếu `order.user.id == userIdInToken`

- **Tạo order (`POST /`):**
  - userId lấy từ JWT token, không từ body → ngăn IDOR

- **Cập nhật status (`PATCH /{id}/status`):**
  - Chỉ ADMIN (`@PreAuthorize("hasRole('ADMIN')")`)

---

## 15. Coding Conventions & Patterns

### 15.1. Naming Conventions

| Loại | Quy ước | Ví dụ |
|---|---|---|
| Entity class | `<Name>Entity` | `ProductEntity`, `UserEntity` |
| DTO request | `<Action><Entity>RequestDto` | `CreateProductRequestDto` |
| DTO response | `<Entity>ResponseDto` | `ProductResponseDto` |
| Repository | `<Entity>Repository` | `ProductRepository` |
| Service interface | `<Entity>Service` | `ProductService` |
| Service impl | `<Entity>ServiceImpl` | `ProductServiceImpl` |
| Controller | `<Entity>Controller` | `ProductController` |
| Exception | `<Description>Exception` | `ResourceNotFoundException` |

### 15.2. Patterns sử dụng

| Pattern | Áp dụng |
|---|---|
| **Interface-Implementation** | Tất cả Services đều có interface + impl class |
| **Builder Pattern** | Response DTOs dùng Lombok `@Builder` |
| **Java Records** | Request DTOs dùng Java Records (immutable) |
| **Manual Mapping** | Entity ↔ DTO mapping bằng tay (private `toResponse()` methods). Không dùng MapStruct |
| **Soft Delete** | Products (`isActive=false`), Users (`isActive=false`) |
| **Pagination** | `Spring Data Pageable` + `Page<T>` |
| **State Machine** | Order status transition validation bằng static Map |
| **Projection Interface** | Spring Data JPA Projections cho aggregate queries |

### 15.3. DI (Dependency Injection) style

- Controllers & Services dùng **Constructor Injection** qua Lombok (`@AllArgsConstructor` hoặc `@RequiredArgsConstructor` với `final` fields)
- **KHÔNG** dùng `@Autowired` field injection

### 15.4. Transaction Management

- `@Transactional` trên write methods
- `@Transactional(readOnly = true)` trên read-only methods
- Ngoại lệ: một số analytics methods trong ProductServiceImpl không có `@Transactional`

### 15.5. API Versioning

- Base path: `/api/v1/`
- Prefix cho tất cả controllers

---

## 16. Trạng thái phát triển & TODO

### 16.1. Đã hoàn thành ✅

- [x] CRUD Category (create, read, update, hard delete)
- [x] CRUD Product (create, read, partial update, soft delete)
- [x] Product stock management (increase/decrease)
- [x] Analytics: Top products, Revenue by category, Monthly revenue
- [x] Order creation với stock validation + price snapshot
- [x] Order status state machine (PENDING → CONFIRMED → SHIPPED → DONE, + CANCELLED)
- [x] Order viewing with IDOR protection
- [x] User registration (BCrypt password hash)
- [x] JWT Login (access token + refresh token)
- [x] Refresh token flow (SHA-256 hash, DB storage, expiry)
- [x] Logout (revoke refresh token)
- [x] JWT Authentication Filter
- [x] Role-based authorization (USER vs ADMIN)
- [x] Custom 401/403 JSON responses
- [x] Global exception handling
- [x] Swagger UI với Bearer token auth
- [x] Flyway database migrations (V1 + V2)
- [x] Multi-profile configuration (dev/test/prod)
- [x] CORS configuration
- [x] Bean Validation trên tất cả Request DTOs

### 16.2. Chưa triển khai / TODO ⬜

- [ ] **UserService / UserController**: Chưa có logic (profile update, change password, list users, disable user, etc.)
- [ ] **base/ package**: Rỗng — có thể dành cho `BaseEntity` (abstract entity với id, createdAt, updatedAt)
- [ ] **enums/ package**: Rỗng — Role, OrderStatus hiện inline trong Entity
- [ ] **util/ package**: Chỉ có `.gitkeep`
- [ ] **ExpiredJwtException**: Class rỗng, chưa sử dụng
- [ ] **ErrorResponse record**: Khai báo rồi nhưng chưa sử dụng (đang dùng ApiResponse cho mọi thứ)
- [ ] **Email functionality**: Mail config có trong application.yaml nhưng chưa có service/template gửi mail
- [ ] **Unit tests / Integration tests**: Chưa có
- [ ] **Pagination cho Orders**: Orders trả về List, chưa hỗ trợ Page
- [ ] **Product update không có update price**: UpdateProductRequestDto có field `price` nhưng ProductServiceImpl.update() KHÔNG check/set giá
- [ ] **OrderRepository.findByUserIdAndUserId()**: Method tên có vấn đề (2 lần cùng field)
- [ ] **CORS**: Hiện chỉ cho localhost:8085, cần cập nhật khi deploy
- [ ] **ddl-auto: update**: Nên đổi sang `validate` cho production
- [ ] **Secret key**: Hardcoded trong application.yaml, nên chuyển sang environment variables
- [ ] **Hoàn kho khi cancel order**: Hiện tại CANCELLED không hoàn stock lại cho products

---

> **📌 Hướng dẫn cho AI Agent:** Khi nhận task mới, hãy:
> 1. Đọc file này ĐẦU TIÊN
> 2. Xác định task thuộc module/layer nào
> 3. Kiểm tra section "TODO" để biết trạng thái hiện tại
> 4. Tuân thủ naming conventions và patterns đã thiết lập
> 5. Sử dụng `ApiResponse<T>` wrapper cho mọi response
> 6. Request DTOs dùng Java Records, Response DTOs dùng Lombok `@Builder` classes
> 7. Thêm `@Transactional` cho write operations
> 8. Mọi exception đều phải được handle qua `GlobalExceptionHandler`
> 9. Swagger `@Operation` cho các endpoint quan trọng
> 10. Kiểm tra SecurityConfig nếu cần thêm public endpoints
