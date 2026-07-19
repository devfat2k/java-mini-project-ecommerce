# 📖 PROJECT OVERVIEW V4 — Mini Ecommerce (Current State Edition)

> **Mục đích tài liệu:** Đây là tài liệu **CHÍNH XÁC NHẤT** phản ánh trạng thái codebase thực tế tính đến ngày cập nhật. Bất kỳ AI Agent hay Developer nào khi bắt đầu hoặc tiếp tục công việc trên project này đều phải đọc file này **ĐẦU TIÊN** — không đọc các file V1, V2, V3 cũ vì có thể bị lỗi thời.
>
> **Cập nhật lần cuối:** 2026-07-19
>
> **Supersedes:** PROJECT_OVERVIEW_V2.md, PROJECT_OVERVIEW_V3.md

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
12. [Payment Integration — VNPay](#12-payment-integration--vnpay)
13. [Exception Handling](#13-exception-handling)
14. [Common — API Response Format](#14-common--api-response-format)
15. [Business Flows (Luồng nghiệp vụ)](#15-business-flows-luồng-nghiệp-vụ)
16. [Coding Conventions & Patterns](#16-coding-conventions--patterns)
17. [Trạng thái phát triển & TODO](#17-trạng-thái-phát-triển--todo)

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

### 1.1. Điểm khác biệt so với V2 (những gì đã thêm mới)

| Module | Trạng thái V2 | Trạng thái V4 (Hiện tại) |
|---|---|---|
| **UserService/UserController** | ⚠️ RỖNG — chưa có gì | ✅ **ĐẦY ĐỦ** — 5 endpoints, 4 use case |
| **Payment (VNPay)** | ❌ Chưa có | ✅ **MỚI HOÀN TOÀN** — tích hợp VNPay đầy đủ |
| **PaymentEntity** | ❌ Chưa có | ✅ **MỚI** — `payments` table với 4 status |
| **Optimistic Locking** | ❌ Chưa có | ✅ **MỚI** — `@Version` trên Product + Order |
| **Scheduled Jobs** | ❌ Chưa có | ✅ **MỚI** — Auto-expire pending payments mỗi 2 phút |
| **Exception mới** | 7 exceptions | ✅ **8 exceptions** (thêm `BadRequestException`) |
| **VNPay Security URLs** | ❌ | ✅ `vnpay-return` và `vnpay-ipn` là PUBLIC URLs |

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
| `spring-boot-starter-validation` | Bean Validation | compile |
| `spring-boot-starter-security` | Spring Security framework | compile |
| `spring-boot-devtools` | Hot reload khi dev | runtime, optional |
| `postgresql` | PostgreSQL JDBC driver | runtime |
| `flyway-core` + `flyway-database-postgresql` | Database migration versioning | compile |
| `springdoc-openapi-starter-webmvc-ui` **2.7.0** | Swagger UI + OpenAPI 3 docs | compile |
| `jjwt-api` **0.12.6** | JWT API | compile |
| `jjwt-impl` **0.12.6** | JWT implementation | runtime |
| `jjwt-jackson` **0.12.6** | JWT JSON serialization | runtime |
| `lombok` | Giảm boilerplate | compile, optional |
| `spring-boot-starter-test` | Unit/Integration testing | test |

> **Lưu ý:** Dự án **KHÔNG** có VNPay SDK dependency. VNPay được tích hợp thủ công bằng HMAC-SHA512 (`javax.crypto.Mac`) — không cần thư viện bên thứ 3.

---

## 3. Cấu hình (Configuration)

### 3.1. application.yaml (file chính)

| Nhóm cấu hình | Key | Giá trị / Mô tả |
|---|---|---|
| **Profile** | `spring.profiles.active` | `dev` (mặc định) |
| **Database** | `spring.datasource.url` | `jdbc:postgresql://localhost:5432/mini_shop` |
| | `spring.datasource.driver-class-name` | `org.postgresql.Driver` |
| | `spring.datasource.hikari.maximum-pool-size` | `10` |
| **JPA** | `spring.jpa.show-sql` | `true` |
| | `spring.jpa.open-in-view` | `false` (tránh lazy query ngoài transaction) |
| | `spring.jpa.hibernate.ddl-auto` | `update` |
| **Flyway** | `spring.flyway.enabled` | `true` |
| | `spring.flyway.locations` | `classpath:db/migration` |
| | `spring.flyway.baseline-on-migrate` | `true` |
| **JWT** | `app.jwt.secret-key` | Base64-encoded HMAC key (từ `.env`) |
| | `app.jwt.expiration` | `3600000` (1 giờ, ms) |
| | `app.jwt.refresh-expiration-days` | `7` (7 ngày) |
| **VNPay** | `vnpay.pay-url` | Sandbox/production URL của VNPay |
| | `vnpay.tmn-code` | Merchant terminal code (từ `.env`) |
| | `vnpay.secret-key` | HMAC secret key (từ `.env`) |
| | `vnpay.return-url` | `/api/v1/payments/vnpay-return` |
| | `vnpay.ipn-url` | IPN webhook URL |
| **Swagger** | `springdoc.api-docs.path` | `/v1/api-docs` |
| | `springdoc.swagger-ui.path` | `/swagger-ui.html` |
| **Logging** | `logging.level.org.springframework.web` | `DEBUG` |

> **⚠️ BẢO MẬT:** `vnpay.*`, `app.jwt.secret-key`, `spring.datasource.*` PHẢI đặt trong `.env` ở root project và load qua environment variables. File `.env` được thêm vào `.gitignore`.

### 3.2. Profile-specific files

| File | Server Port |
|---|---|
| `application-dev.yaml` | `8085` |
| `application-test.yaml` | `8082` |
| `application-prod.yaml` | `8083` |

### 3.3. VNPayConfig Bean

```java
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    private String payUrl;
    private String tmnCode;
    private String secretKey;
    private String returnUrl;
    private String ipnUrl;
    // + static helpers: getCreateDate(), getExpireDate()
}
```

---

## 4. Cấu trúc thư mục chi tiết

```
mini-ecommerce/
├── pom.xml
├── mvnw / mvnw.cmd
├── .env                                           # ⚠️ KHÔNG commit — chứa secrets
├── .gitignore                                     # Ignore .env và application.yaml
├── src/
│   ├── main/
│   │   ├── java/com/devfat/mini_ecommerce/
│   │   │   ├── MiniEcommerceApplication.java      # Entry point
│   │   │   ├── base/                              # (RỖNG) — Dự kiến BaseEntity
│   │   │   ├── common/
│   │   │   │   └── ApiResponse.java               # Generic API response wrapper
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java            # Security filter chain, CORS, BCrypt
│   │   │   │   ├── OpenApiConfig.java             # Swagger/OpenAPI + Bearer Auth scheme
│   │   │   │   ├── VNPayConfig.java               # @ConfigurationProperties(prefix="vnpay")
│   │   │   │   └── VNPayUtil.java                 # HMAC-SHA512, buildQueryAndHash, verifySignature
│   │   │   ├── controller/                        # REST Controllers (7 files)
│   │   │   │   ├── AuthController.java            # /api/v1/auth/**
│   │   │   │   ├── CategoryController.java        # /api/v1/categories/**
│   │   │   │   ├── ProductController.java         # /api/v1/products/**
│   │   │   │   ├── OrderController.java           # /api/v1/orders/**
│   │   │   │   ├── UserController.java            # /api/v1/users/** (5 endpoints)
│   │   │   │   ├── PaymentController.java         # /api/v1/payments/** (MỚI)
│   │   │   │   └── PingController.java            # /api/v1/health
│   │   │   ├── docs/                              # Tài liệu dự án (markdown)
│   │   │   │   ├── PROJECT_OVERVIEW_V2.md         # Phiên bản cũ (deprecated)
│   │   │   │   ├── PROJECT_OVERVIEW_V3.md         # Phiên bản cũ (deprecated)
│   │   │   │   ├── PROJECT_OVERVIEW_V4.md         # ★ BẢN NÀY (hiện tại)
│   │   │   │   ├── plan_tong_hop.md               # Kế hoạch tổng hợp các phase
│   │   │   │   ├── plan_uu_tien_hoc_tap.md        # Roadmap học tập ưu tiên
│   │   │   │   └── auth_security_jwt_plan_v2.md   # Plan JWT chi tiết
│   │   │   ├── dto/
│   │   │   │   ├── request/                       # 11 request DTOs (Java Records)
│   │   │   │   │   ├── RegisterRequestDto.java
│   │   │   │   │   ├── LoginRequestDto.java
│   │   │   │   │   ├── RefreshTokenRequestDto.java
│   │   │   │   │   ├── CreateProductRequestDto.java
│   │   │   │   │   ├── UpdateProductRequestDto.java
│   │   │   │   │   ├── CreateCategoryRequestDto.java
│   │   │   │   │   ├── CreateOrderRequestDto.java
│   │   │   │   │   ├── OrderItemRequestDto.java
│   │   │   │   │   ├── UpdateOrderStatusRequestDto.java
│   │   │   │   │   ├── ChangePasswordRequestDto.java   # MỚI
│   │   │   │   │   └── UpdateProfileRequestDto.java    # MỚI
│   │   │   │   └── response/                      # 9 response DTOs
│   │   │   │       ├── AuthResponseDto.java
│   │   │   │       ├── UserResponseDto.java
│   │   │   │       ├── CategoryResponseDto.java
│   │   │   │       ├── ProductResponseDto.java
│   │   │   │       ├── OrderResponseDto.java
│   │   │   │       ├── OrderItemResponseDto.java
│   │   │   │       ├── RefreshTokenResponseDto.java
│   │   │   │       └── CreatePaymentResponseDto.java   # MỚI — record(String paymentUrl)
│   │   │   ├── entity/                            # JPA Entities (7 entities ← tăng từ 6)
│   │   │   │   ├── UserEntity.java
│   │   │   │   ├── CategoryEntity.java
│   │   │   │   ├── ProductEntity.java             # Thêm @Version
│   │   │   │   ├── OrderEntity.java               # Thêm @Version
│   │   │   │   ├── OrderItemEntity.java
│   │   │   │   ├── RefreshTokenEntity.java
│   │   │   │   └── PaymentEntity.java             # MỚI — 3 inline enums
│   │   │   ├── enums/                             # (RỖNG) — Enums inline trong Entity
│   │   │   ├── exception/                         # 8 exceptions + Global handler
│   │   │   │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice
│   │   │   │   ├── BadRequestException.java       # MỚI (400 Bad Request)
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── InvalidStatusTransitionException.java
│   │   │   │   ├── CategoryHasProductsException.java
│   │   │   │   └── InvalidRefreshTokenException.java
│   │   │   ├── repository/                        # 6 repositories (tăng từ 5)
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   └── PaymentRepository.java         # MỚI
│   │   │   ├── security/                          # JWT Security module (7 files)
│   │   │   │   ├── JwtProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   │   ├── JwtAccessDeniedHandler.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── UserPrincipal.java
│   │   │   │   └── RefreshTokenGenerator.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── UserService.java               # ĐẦY ĐỦ — 4 methods
│   │   │   │   ├── PaymentService.java            # MỚI — 2 methods
│   │   │   │   └── impl/
│   │   │   │       ├── AuthServiceImpl.java
│   │   │   │       ├── CategoryServiceImpl.java
│   │   │   │       ├── ProductServiceImpl.java
│   │   │   │       ├── OrderServiceImpl.java
│   │   │   │       ├── UserServiceImpl.java       # ĐẦY ĐỦ — 4 methods + helper
│   │   │   │       └── PaymentServiceImpl.java    # MỚI — VNPay + Scheduled job
│   │   │   └── util/                              # (CHỈ CÓ .gitkeep)
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml               # Port 8085
│   │       ├── application-test.yaml              # Port 8082
│   │       ├── application-prod.yaml              # Port 8083
│   │       ├── db/migration/
│   │       │   ├── V1__init_mini_shop.sql         # 6 bảng core + indexes
│   │       │   ├── V2__add_version_to_products.sql # Thêm cột version (Optimistic Lock)
│   │       │   ├── V3__create_payments_table.sql  # MỚI — bảng payments
│   │       │   ├── V4__fix_payments_order_id_type.sql # Fix BIGINT cho order_id
│   │       │   └── V5__add_version_to_orders.sql  # MỚI — version cho orders
│   │       ├── static/
│   │       └── templates/
│   └── test/                                      # (Chưa có test code)
```

---

## 5. Database Schema & Migrations

### 5.1. Lịch sử Flyway Migrations

| File | Nội dung |
|---|---|
| `V1__init_mini_shop.sql` | Tạo 6 bảng core: `users`, `categories`, `products`, `orders`, `order_items`, `refresh_tokens` + 7 indexes |
| `V2__add_version_to_products.sql` | `ALTER TABLE products ADD COLUMN version INTEGER NOT NULL DEFAULT 0` |
| `V3__create_payments_table.sql` | Tạo bảng `payments` + 2 indexes |
| `V4__fix_payments_order_id_type.sql` | `ALTER TABLE payments ALTER COLUMN order_id TYPE BIGINT` |
| `V5__add_version_to_orders.sql` | `ALTER TABLE orders ADD COLUMN version INTEGER NOT NULL DEFAULT 0` |

### 5.2. Database Schema (7 bảng)

**users, categories, products, orders, order_items** — không đổi so với V2.

**Bổ sung quan trọng:**
- `products.version INTEGER NOT NULL DEFAULT 0` — Optimistic Lock
- `orders.version INTEGER NOT NULL DEFAULT 0` — Optimistic Lock

**Bảng mới — payments:**

```sql
CREATE TABLE payments (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT NOT NULL REFERENCES orders(id),
    amount                  NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    provider                VARCHAR(20) NOT NULL CHECK (provider IN ('VNPAY','MOMO','ZALOPAY','ACB','VCB')),
    payment_method          VARCHAR(20) NOT NULL CHECK (payment_method IN ('BANK','WALLET','CASH')),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING','SUCCESS','FAILED','EXPIRED')),
    provider_transaction_id VARCHAR(100) UNIQUE,
    paid_at                 TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
```

### 5.3. Tổng hợp Indexes (9 indexes)

| Index | Bảng | Column |
|---|---|---|
| `idx_products_category` | products | category_id |
| `idx_orders_user` | orders | user_id |
| `idx_orders_status` | orders | status |
| `idx_order_items_order` | order_items | order_id |
| `idx_order_items_product` | order_items | product_id |
| `idx_refresh_tokens_user_id` | refresh_tokens | user_id |
| `idx_refresh_tokens_token_hash` | refresh_tokens | token_hash |
| `idx_payments_order` | payments | order_id |
| `idx_payments_status` | payments | status |

---

## 6. Entity Layer — Chi tiết từng Entity

> **Quy ước chung:** Tất cả entities dùng `@Data @NoArgsConstructor @AllArgsConstructor @Builder @DynamicUpdate @DynamicInsert`. Timestamps dùng `@CreationTimestamp` và `@UpdateTimestamp`.

### 6.1. UserEntity (`users`)

| Field | Type | JPA Annotation | Ghi chú |
|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | PK |
| `fullName` | `String` | `@Column(name="full_name", length=150)` | |
| `email` | `String` | `@Column(unique=true, length=150)` | Username đăng nhập |
| `phoneNumber` | `String` | `@Column(name="phone_number", unique=true, length=15)` | |
| `password` | `String` | `@Column(nullable=false)` | BCrypt hash |
| `role` | `Role` enum | `@Enumerated(STRING)` | Inline enum: `USER`, `ADMIN` |
| `isActive` | `boolean` | `@Column(name="is_active")` | Soft disable account |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | |

> **Lưu ý:** Enum `Role` khai báo **INLINE** trong `UserEntity` (`UserEntity.Role`), không nằm trong package `enums/`.

### 6.2. CategoryEntity (`categories`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `name` | `String` | UNIQUE, length=50 |
| `products` | `List<ProductEntity>` | `@OneToMany(LAZY, mappedBy="category")` |
| `createdAt` | `LocalDateTime` | |

### 6.3. ProductEntity (`products`) — Optimistic Lock

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `name` | `String` | NOT NULL |
| `description` | `String` | `@Column(columnDefinition="TEXT")` |
| `price` | `BigDecimal` | precision=12, scale=2 |
| `stock` | `Integer` | `@Builder.Default = 0` |
| `category` | `CategoryEntity` | `@ManyToOne(LAZY)`, FK: `category_id` |
| `isActive` | `boolean` | `@Builder.Default = true` |
| `orderItems` | `List<OrderItemEntity>` | `@OneToMany(mappedBy="product")` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |
| **`version`** | **`Integer`** | **`@Version` — Optimistic Locking** |

> **Lý do `@Version` trên Product:** Ngăn race condition khi nhiều user đặt hàng cùng lúc tranh nhau stock. Nếu 2 transaction cùng đọc `version=5` rồi update → chỉ 1 thành công, cái kia ném `ObjectOptimisticLockingFailureException` → HTTP 409.

### 6.4. OrderEntity (`orders`) — Optimistic Lock

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `user` | `UserEntity` | `@ManyToOne(LAZY)` |
| `status` | `OrderStatus` enum | Inline: `DONE, PENDING, SHIPPED, CONFIRMED, CANCELLED` |
| `totalAmount` | `BigDecimal` | Default `BigDecimal.ZERO` |
| `note` | `String` | length=1000 |
| `items` | `List<OrderItemEntity>` | `@OneToMany(cascade=ALL, orphanRemoval=true)`, `@Builder.Default = new ArrayList<>()` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |
| **`version`** | **`Integer`** | **`@Version` — Optimistic Locking** |

### 6.5. OrderItemEntity (`order_items`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `order` | `OrderEntity` | `@ManyToOne(LAZY)` |
| `product` | `ProductEntity` | `@ManyToOne(LAZY)` |
| `quantity` | `Integer` | |
| `unitPrice` | `BigDecimal` | Giá **snapshot** tại thời điểm đặt hàng |

### 6.6. RefreshTokenEntity (`refresh_tokens`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `user` | `UserEntity` | `@ManyToOne(LAZY)` |
| `tokenHash` | `String` | SHA-256 hash của raw refresh token |
| `expireAt` | `LocalDateTime` | |
| `revoked` | `boolean` | Đánh dấu token bị thu hồi |
| `createdAt` | `LocalDateTime` | |

### 6.7. PaymentEntity (`payments`) — MỚI

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK (BIGSERIAL) |
| `amount` | `BigDecimal` | precision=12, scale=2. NOT NULL |
| `paymentProvider` | `PaymentProvider` enum | Column: `provider`. Inline: `VNPAY, MOMO, ZALOPAY, ACB, VCB` |
| `paymentStatus` | `PaymentStatus` enum | Column: `status`. Inline: `PENDING, SUCCESS, FAILED, EXPIRED` |
| `paymentMethod` | `PaymentMethod` enum | Column: `payment_method`. Inline: `CASH, BANK, WALLET` |
| `providerTransactionId` | `String` | `@Size(1,100)`, UNIQUE. Lưu `vnp_TransactionNo` từ VNPay |
| `order` | `OrderEntity` | `@ManyToOne(LAZY)`, FK: `order_id` |
| `paidAt` | `LocalDateTime` | Set khi payment SUCCESS |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` |

> **3 inline enums trong PaymentEntity:**
> - `PaymentStatus`: `{PENDING, SUCCESS, FAILED, EXPIRED}`
> - `PaymentMethod`: `{CASH, BANK, WALLET}`
> - `PaymentProvider`: `{VNPAY, MOMO, ZALOPAY, ACB, VCB}`

---

## 7. Repository Layer — Data Access

### 7.1. UserRepository

```java
extends JpaRepository<UserEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findByEmail(String)` | Tìm user bằng email (login + check trùng) |
| `findByPhoneNumber(String)` | Tìm user bằng SĐT (check trùng khi register) |

### 7.2. CategoryRepository

```java
extends JpaRepository<CategoryEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findByNameContainingIgnoreCase(String, Pageable)` | Search + phân trang |
| `existsByNameIgnoreCase(String)` | Check tên đã tồn tại |

### 7.3. ProductRepository — Phức tạp nhất

```java
extends JpaRepository<ProductEntity, Long>
```

**Derived queries:**

| Method | Mô tả |
|---|---|
| `findByStockGreaterThan(int)` | SP còn hàng |
| `findByCategoryIdAndStockGreaterThan(Long, int)` | SP theo category còn hàng |
| `findByStockGreaterThan(int, Pageable)` | Phân trang |
| `findByNameContainsIgnoreCase(String, Pageable)` | Search theo tên |

**JPQL custom queries:**

| Method | Mô tả |
|---|---|
| `findAvailableWithCategory()` | `JOIN FETCH p.category WHERE p.stock > 0` — tránh N+1 |
| `findByMinPriceWithCategory(BigDecimal)` | `JOIN FETCH p.category WHERE p.price >= :minPrice` |

**Projection Interfaces (nằm trong ProductRepository):**

| Interface | Fields | Mục đích |
|---|---|---|
| `TopProductView` | `name`, `price`, `mostBuy` | Top sản phẩm bán chạy (SUM quantity) |
| `CategoryRevenueView` | `name`, `revenue` | Doanh thu theo category |
| `MonthlyRevenueView` | `month`, `revenue` | Doanh thu theo tháng (DATE_TRUNC) |

### 7.4. OrderRepository

```java
extends JpaRepository<OrderEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findAllByUserId(Long)` | Tìm orders theo userId |
| `findAllByStatus(OrderStatus)` | Tìm orders theo status |
| `findAllByUserIdAndStatus(Long, OrderStatus)` | JPQL: JOIN FETCH user, lọc userId + status |
| `findByUserIdWithDetails(Long)` | JPQL: JOIN FETCH user + LEFT JOIN FETCH items |

> **⚠️ Known Issue:** Có method `findByUserIdAndUserId(Long, Long)` với tên bị trùng field — cần rename.

### 7.5. RefreshTokenRepository

```java
extends JpaRepository<RefreshTokenEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findByTokenHash(String)` | Tìm bằng SHA-256 hash |
| `deleteExpiredOrRevokedByUserId(Long, LocalDateTime)` | `@Modifying` DELETE expired/revoked tokens |
| `findAllByUserAndRevokedFalse(UserEntity)` | Tìm active tokens (dùng khi disable account) |

### 7.6. PaymentRepository — MỚI

```java
extends JpaRepository<PaymentEntity, Long>
```
| Method | Mô tả |
|---|---|
| `findByOrderId(Long)` | Tìm payment theo orderId |
| `existsByOrderIdAndPaymentStatus(Long, PaymentStatus)` | Check payment SUCCESS đã tồn tại (tránh tạo thêm) |
| `findAllByPaymentStatusAndCreatedAtBefore(PaymentStatus, LocalDateTime)` | Tìm PENDING payments quá hạn (dùng trong Scheduled job) |

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
| **CreateOrderRequestDto** | `items: List<OrderItemRequest>` | `@NotEmpty @Valid`. Nested: `productId(@NotNull)`, `quantity(@Min(1))` |
| **OrderItemRequestDto** | `productId`, `quantity` | Standalone |
| **UpdateOrderStatusRequestDto** | `orderStatus` | `@NotNull OrderEntity.OrderStatus` |
| **ChangePasswordRequestDto** (MỚI) | `oldPassword`, `newPassword` | `@NotNull @NotBlank @Size(8-100)` |
| **UpdateProfileRequestDto** (MỚI) | `fullName`, `phoneNumber` | `@NotBlank @Pattern(10-11 digits)` |

> **⚠️ Note `UpdateProfileRequestDto`:** Cả 2 fields đều `@NotBlank` — không phải partial update. Nếu muốn đổi chỉ tên thì vẫn phải gửi phone và ngược lại.

### 8.2. Response DTOs

| DTO | Fields | Ghi chú |
|---|---|---|
| **UserResponseDto** | `userId`, `fullName`, `email`, `phoneNumber`, `role`, `isActive`, `createdAt` | Không chứa password |
| **AuthResponseDto** | `accessToken`, `refreshToken`, `tokenType("Bearer")`, `expiresIn` | Login thành công |
| **RefreshTokenResponseDto** | `accessToken` | Refresh thành công |
| **CategoryResponseDto** | `id`, `categoryName` | |
| **ProductResponseDto** | `id`, `name`, `price`, `stock`, `category(CategoryResponseDto)` | Nested |
| **OrderResponseDto** | `id`, `status`, `totalAmount`, `createdAt`, `orderItems(List<OrderItemResponseDto>)` | |
| **OrderItemResponseDto** | `productName`, `quantity`, `unitPrice` | |
| **CreatePaymentResponseDto** (MỚI) | `paymentUrl` | Java record — URL redirect VNPay |

---

## 9. Service Layer — Business Logic

### 9.1. AuthService / AuthServiceImpl

| Method | Logic |
|---|---|
| **register** | Trim+lowercase email → check trùng email/phone → BCrypt hash → tạo User (role=USER) → save → UserResponseDto |
| **login** | AuthManager.authenticate() → sinh JWT accessToken → clean expired tokens → sinh raw refresh token (SecureRandom 64B) → SHA-256 hash → save DB → AuthResponseDto |
| **refreshToken** | SHA-256 hash raw → tìm entity → check revoked+expiry → sinh accessToken mới |
| **logout** | SHA-256 hash → tìm entity → set revoked=true. Idempotent. |

### 9.2. CategoryService / CategoryServiceImpl

| Method | Logic |
|---|---|
| **create** | existsByNameIgnoreCase → exception nếu trùng → tạo + save |
| **findByNameContainingIgnoreCase** | Pagination + search → `Page<CategoryResponseDto>` |
| **findById** | findById → orElseThrow |
| **update** | findById → setName → save |
| **deleteById** | findById → check products.isEmpty() → hard delete hoặc CategoryHasProductsException |

### 9.3. ProductService / ProductServiceImpl

| Method | Logic |
|---|---|
| **create** | Tìm category → tạo ProductEntity → save |
| **getProductsWithSearch** | `findByNameContainsIgnoreCase` + mapping |
| **findById** | findById → map → orElseThrow |
| **update** | **Partial update** — chỉ set fields != null, check categoryId nếu có |
| **softDelete** | Check isActive → set false → save |
| **decreaseStock** | validate > 0 + stock >= qty → trừ stock |
| **increaseStock** | validate > 0 → cộng stock |
| **getTopProducts** | Projection `getTopViewProduct(PageRequest)` |
| **getCategoryRevenue** | Projection `getCategoryRevenue()` |
| **getMonthlyRevenue** | Projection `getMonthlyRevenue()` |

### 9.4. OrderService / OrderServiceImpl — State Machine

**Order Status State Machine:**
```
PENDING ──→ CONFIRMED ──→ SHIPPED ──→ DONE
   │             │
   └──→ CANCELLED└──→ CANCELLED
```

> **QUAN TRỌNG với VNPay:** `PENDING → CONFIRMED` **KHÔNG** được gọi thủ công qua `changeStatus()`. Chỉ `PaymentServiceImpl.handleVnpayIpn()` khi nhận `vnp_ResponseCode = "00"` mới được chuyển sang CONFIRMED.

| Method | Logic |
|---|---|
| **findByUserIdWithDetails** | Authorization: USER xem order của mình (path userId == token userId), ADMIN xem all |
| **findById** | Tìm + check ownership cho USER role |
| **create** | Tìm User → Order PENDING → lặp items (findProduct, check stock, decreaseStock, tạo OrderItem, gán unitPrice=product.price) → tính totalAmount → save |
| **changeStatus** | Check ALLOWED_ORDERS map → set status mới (ADMIN only through API) |

### 9.5. UserService / UserServiceImpl — ĐẦY ĐỦ

| Method | Logic |
|---|---|
| **getMe(Long id)** | `findById(id).map(toResponseDto).orElseThrow` |
| **getAllUsers(Pageable)** | `findAll(pageable).map(toResponseDto)`. ADMIN only. |
| **changePassword(Long, ChangePasswordRequestDto)** | Tìm user → `passwordEncoder.matches(old, hash)` → nếu sai: BadRequestException → encode new → save |
| **updateProfile(Long, UpdateProfileRequestDto)** | Tìm user → set phone/fullName nếu != null → save |
| **updateStatusUser(Long idInQuery, Long idInToken, Boolean isActive)** | Tìm user → check không tự disable mình → check status != isActive → nếu disable: revoke ALL active refresh tokens của user đó → save |

**Helper:** `toResponseDto(UserEntity)` — mapping thủ công, dùng bởi nhiều methods.

### 9.6. PaymentService / PaymentServiceImpl — MỚI

| Method | Logic chi tiết |
|---|---|
| **createPayment(userId, orderId, request)** | 1. Tìm Order. 2. Check `order.user.id == userId` (chống IDOR). 3. Check `order.status == PENDING`. 4. Check không có payment SUCCESS nào của order này. 5. Tạo PaymentEntity (PENDING, VNPAY, WALLET). 6. Build VNPay params map → sort theo alphabet → `VNPayUtil.buildQueryAndHash()`. 7. Nối URL. 8. Trả `CreatePaymentResponseDto(paymentUrl)`. |
| **handleVnpayIpn(Map params)** | 1. `VNPayUtil.verifySignature()` → nếu sai → BadRequestException. 2. Lấy paymentId từ `vnp_TxnRef`. 3. Tìm PaymentEntity. 4. **Idempotency**: nếu đã SUCCESS → return. 5. `vnp_ResponseCode == "00"` → SUCCESS, `paidAt=now`, `providerTransactionId=vnp_TransactionNo`, save → order.status=CONFIRMED, save. 6. Khác → FAILED, save. |
| **expiredPayment()** | `@Scheduled(fixedRate=120000)` mỗi 2 phút. Tìm PENDING payments tạo trước `now()-15min` → set EXPIRED + order.status=CANCELLED. |

### 9.7. VNPayUtil (Static Utility)

| Method | Mô tả |
|---|---|
| `hmacSHA512(key, data)` | Sinh chữ ký HMAC-SHA512 |
| `getIpAddress(request)` | Lấy IP thật qua `X-FORWARDED-FOR` |
| `buildQueryAndHash(params, secretKey)` | Sort keys → build query string + hash. Trả `Map<"queryUrl", "secureHash">` |
| `verifySignature(params, secretKey)` | Remove `vnp_SecureHash` → sort → hash → compare. Dùng trong IPN. |

---

## 10. Controller Layer — API Endpoints

### 10.1. AuthController (`/api/v1/auth`)

> `@SecurityRequirements({})` — bỏ yêu cầu Bearer trên Swagger.

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/register` | Public | RegisterRequestDto | 201 + UserResponseDto |
| POST | `/login` | Public | LoginRequestDto | 200 + AuthResponseDto |
| POST | `/refresh-token` | Public | RefreshTokenRequestDto | 200 + RefreshTokenResponseDto |
| POST | `/logout` | Public | RefreshTokenRequestDto | 200 + null |

### 10.2. CategoryController (`/api/v1/categories`)

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateCategoryRequestDto | 201 + CategoryResponseDto |
| GET | `/` | Public | `?page&size&search&sort&direction` | 200 + Page |
| GET | `/{id}` | Public | Path: id | 200 + CategoryResponseDto |
| PUT | `/{id}` | ADMIN | CreateCategoryRequestDto | 200 + CategoryResponseDto |
| DELETE | `/{id}` | ADMIN | Path: id | 200 + Boolean |

### 10.3. ProductController (`/api/v1/products`)

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateProductRequestDto | 201 + ProductResponseDto |
| GET | `/` | Public `@SecurityRequirements({})` | `?page&size&search&sort&direction` | 200 + Page |
| GET | `/{id}` | Authenticated | Path: id | 200 + ProductResponseDto |
| PATCH | `/{id}` | ADMIN | UpdateProductRequestDto | 200 + ProductResponseDto |
| DELETE | `/{id}` | ADMIN | Path: id | 200 + Boolean (soft delete) |
| PATCH | `/increase/{id}` | ADMIN | `?quantity=` | 200 + ProductResponseDto |
| PATCH | `/decrease/{id}` | ADMIN | `?quantity=` | 200 + ProductResponseDto |
| GET | `/top-buy` | ADMIN | `?limit=10` | 200 + List TopProductView |
| GET | `/revenue-by-category` | ADMIN | — | 200 + List CategoryRevenueView |
| GET | `/revenue-in-month` | ADMIN | — | 200 + List MonthlyRevenueView |

### 10.4. OrderController (`/api/v1/orders`)

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| GET | `/user/{userId}` | Authenticated | Path: userId + @AuthPrincipal | 200 + List OrderResponseDto |
| GET | `/{id}` | Authenticated | Path: orderId + @AuthPrincipal | 200 + OrderResponseDto |
| POST | `/` | Authenticated | CreateOrderRequestDto | 201 + OrderResponseDto |
| PATCH | `/{id}/status` | ADMIN | UpdateOrderStatusRequestDto | 200 + OrderResponseDto |

> **IDOR Protection:** userId lấy từ JWT token (`@AuthenticationPrincipal`), KHÔNG từ request body.

### 10.5. UserController (`/api/v1/users`) — MỚI ĐẦY ĐỦ

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| GET | `/me` | Authenticated | @AuthPrincipal | 200 + UserResponseDto |
| PATCH | `/password` | Authenticated | ChangePasswordRequestDto | 204 No Content |
| PATCH | `/me/update` | Authenticated | UpdateProfileRequestDto | 204 No Content |
| GET | `/` | ADMIN `@PreAuthorize` | `?page&size&sort&direction` | 200 + Page UserResponseDto |
| PATCH | `/{userId}/status` | ADMIN `@PreAuthorize` | `?isActive=` | 204 No Content |

> **Admin guard:** Dùng `@PreAuthorize("hasRole('ADMIN')")` cho `/` và `/{userId}/status`.

### 10.6. PaymentController (`/api/v1/payments`) — MỚI

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/{orderId}/create` | Authenticated | @AuthPrincipal + Path: orderId | 200 + `{paymentUrl}` |
| GET | `/vnpay-return` | Public | `@RequestParam Map<String,String>` | 200 + `{status, message}` |
| GET | `/vnpay-ipn` | Public | `@RequestParam Map<String,String>` | 200 + `{RspCode, Message}` |

> **Thiết kế quan trọng:**
> - `vnpay-return`: Người dùng được redirect về sau khi thanh toán. **KHÔNG** xử lý nghiệp vụ — chỉ đọc DB trả trạng thái cho client.
> - `vnpay-ipn`: VNPay server gọi webhook. Toàn bộ logic cập nhật payment/order xảy ra ở đây.
> - Cả hai phải là PUBLIC URLs trong SecurityConfig.

### 10.7. PingController (`/api/v1/health`)

| HTTP | Path | Auth | Response |
|---|---|---|---|
| GET | `/` | Public | "pong" (plain text) |

---

## 11. Security & Authentication (JWT)

### 11.1. Security Filter Chain

```
Client Request
    │
    ▼
JwtAuthenticationFilter (OncePerRequestFilter)
    ├─ Đọc "Authorization: Bearer <token>"
    ├─ jwtProvider.validateToken(token)
    ├─ Nếu valid: getEmailFromToken() → loadUserByUsername() → set SecurityContext
    └─ LUÔN gọi filterChain.doFilter()
    │
    ▼
Authorization Check
    ├─ PUBLIC_URLS: /auth/**, /health, /payments/vnpay-return, /payments/vnpay-ipn
    ├─ PUBLIC_GET: GET /products, GET /products/{id}, GET /categories, GET /categories/{id}
    ├─ SWAGGER: /swagger-ui/**, /v1/api-docs/**
    └─ .anyRequest().authenticated()
    │
    ├──────────────────────┐
    ▼                      ▼
Chưa xác thực         Thiếu quyền
→ JSON 401             → JSON 403
```

### 11.2. JWT Token Structure

```json
{
  "userId": 1,
  "role": "USER",
  "sub": "user@example.com",
  "iat": 1720000000,
  "exp": 1720003600
}
```

- **Access Token expiry:** 1 giờ (3600000 ms)
- **Refresh Token:** Opaque (SecureRandom 64 bytes + Base64), lưu SHA-256 hash, expiry 7 ngày.

### 11.3. Public URLs (SecurityConfig)

```java
private static final String[] PUBLIC_URLS = {
    "/api/v1/auth/register",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh-token",
    "/api/v1/auth/logout",
    "/api/v1/health",
    "/api/v1/payments/vnpay-return",   // MỚI
    "/api/v1/payments/vnpay-ipn"       // MỚI
};
```

### 11.4. Security Classes

| Class | Responsibility |
|---|---|
| `JwtProvider` | Tạo JWT, validate, extract claims (email, userId, role) |
| `JwtAuthenticationFilter` | Intercept request, set Authentication vào SecurityContext |
| `JwtAuthenticationEntryPoint` | Trả JSON 401 |
| `JwtAccessDeniedHandler` | Trả JSON 403 |
| `CustomUserDetailsService` | Load user từ DB bằng email, wrap thành `UserPrincipal` |
| `UserPrincipal` | `UserDetails` impl. `getUsername()` = email. Authority = `ROLE_` + role. `isEnabled()` = `isActive`. |
| `RefreshTokenGenerator` | SecureRandom 64 bytes → Base64 (without padding) |

---

## 12. Payment Integration — VNPay

### 12.1. Luồng thanh toán VNPay

```
Client              Backend                 VNPay Server
  │                    │                        │
  │  POST /payments/{orderId}/create            │
  ├──────────────────>│                        │
  │                    │ 1. Validate order      │
  │                    │ 2. Tạo PaymentEntity   │
  │                    │    (PENDING)            │
  │                    │ 3. Build params         │
  │                    │ 4. HMAC-SHA512 sign     │
  │  {paymentUrl}      │                        │
  │<──────────────────┤                        │
  │                    │                        │
  │  Redirect → VNPay                          │
  ├──────────────────────────────────────────>│
  │                    │                        │ User thanh toán
  │                    │  IPN Webhook           │
  │                    │<──────────────────────┤
  │                    │ 1. verifySignature()   │
  │                    │ 2. Idempotency check   │
  │                    │ 3. "00" → SUCCESS      │
  │                    │    → Order CONFIRMED   │
  │                    │  {RspCode: "00"}       │
  │                    │──────────────────────>│
  │                    │                        │
  │  Redirect /vnpay-return                    │
  │<──────────────────────────────────────────┤
  │  GET /vnpay-return │                        │
  ├──────────────────>│                        │
  │  {status từ DB}    │                        │
  │<──────────────────┤                        │
```

### 12.2. Security của VNPay Integration

| Cơ chế | Mô tả |
|---|---|
| **HMAC-SHA512 Signature** | Tất cả params sort alphabet → ký bằng secretKey |
| **Signature Verification** | `handleVnpayIpn()` verify chữ ký TRƯỚC KHI đọc bất kỳ param nào |
| **Idempotency** | Nếu IPN gọi lần 2 với payment đã SUCCESS → return ngay |
| **State từ DB** | `/vnpay-return` đọc `payment.getStatus()` từ DB, không tin `vnp_ResponseCode` trong URL |
| **IDOR Protection** | `createPayment()` check `order.user.id == userId` |

### 12.3. Auto-Expire Scheduled Job

```java
@Scheduled(fixedRate = 120000)  // mỗi 2 phút
@Transactional
public void expiredPayment() {
    LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
    List<PaymentEntity> expired = paymentRepository
        .findAllByPaymentStatusAndCreatedAtBefore(PENDING, threshold);
    for (PaymentEntity payment : expired) {
        payment.setPaymentStatus(EXPIRED);
        payment.getOrder().setStatus(CANCELLED);
    }
}
```

---

## 13. Exception Handling

### 13.1. GlobalExceptionHandler (`@RestControllerAdvice`)

| Exception | HTTP Status | Khi nào |
|---|---|---|
| `BadRequestException` (MỚI) | 400 BAD_REQUEST | Vi phạm business rule: sai password cũ, sai userId, order không PENDING |
| `ResourceNotFoundException` | 404 NOT_FOUND | Entity không tồn tại trong DB |
| `InsufficientStockException` | 409 CONFLICT | Không đủ stock |
| `InvalidStatusTransitionException` | 409 CONFLICT | Chuyển trạng thái order không hợp lệ |
| `CategoryHasProductsException` | 409 CONFLICT | Xoá category còn sản phẩm |
| `DuplicateResourceException` | 409 CONFLICT | Trùng email/phone |
| `ObjectOptimisticLockingFailureException` (MỚI) | 409 CONFLICT | Race condition trên entity có `@Version` |
| `MethodArgumentNotValidException` | 400 BAD_REQUEST | Validation lỗi → trả `Map<field, message>` |
| `DataIntegrityViolationException` | 409 CONFLICT | DB unique constraint vi phạm |
| `BadCredentialsException` | 401 UNAUTHORIZED | Sai email/password khi login |
| `DisabledException` | 403 FORBIDDEN | Account bị disable |
| `AccessDeniedException` | 403 FORBIDDEN | Thiếu quyền (role) |
| `InvalidRefreshTokenException` | 401 UNAUTHORIZED | Refresh token không hợp lệ/expired/revoked |
| `Exception` (catch-all) | 500 INTERNAL_SERVER_ERROR | Lỗi không dự kiến |

### 13.2. Phân biệt Exception

| Exception | Dùng khi |
|---|---|
| `ResourceNotFoundException` | Entity KHÔNG TỒN TẠI trong DB |
| `BadRequestException` | Entity TỒN TẠI nhưng vi phạm business rule |

---

## 14. Common — API Response Format

### ApiResponse\<T\>

```java
@Getter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;         // null nếu error → không xuất hiện trong JSON
    private LocalDateTime timestamp;
}
```

**Factory methods:**
- `ApiResponse.success(data, message)` → success=true
- `ApiResponse.error(message)` → success=false, data=null

---

## 15. Business Flows (Luồng nghiệp vụ)

### 15.1. Order + Payment Flow (VNPay)

```
1. POST /orders          → Order.status = PENDING (stock bị trừ ngay)
2. POST /payments/create → Nhận paymentUrl → redirect VNPay
3. User thanh toán
4. VNPay IPN callback    → Payment SUCCESS → Order CONFIRMED
                          hoặc Payment FAILED → Order vẫn PENDING
5. Scheduler (2 phút)   → PENDING payments > 15 phút → EXPIRED + Order CANCELLED
```

### 15.2. Order Status với VNPay

```
                           VNPay IPN "00"
                           ┌────────────────────┐
                           │                    │
PENDING ─────────────────> CONFIRMED ──> SHIPPED ──> DONE
   │   (tạo order)              │
   │                            └──> CANCELLED
   │
   └──> CANCELLED (Scheduler 15 phút hoặc Admin manual)
```

> **KHÔNG** thể dùng `changeStatus()` thủ công để PENDING → CONFIRMED. Transition đó chỉ từ VNPay IPN.

### 15.3. Disable Account Flow

```
Admin PATCH /users/{id}/status?isActive=false
→ Revoke ALL active refresh tokens của user đó
→ user.isActive = false
→ Lần sau user dùng access token cũ:
   CustomUserDetailsService → UserPrincipal.isEnabled() = false
   → Spring Security ném DisabledException → 403
```

---

## 16. Coding Conventions & Patterns

### 16.1. Quy ước đặt tên

| Loại | Quy ước | Ví dụ |
|---|---|---|
| Package | lowercase, underscore | `mini_ecommerce` |
| Class | PascalCase | `PaymentServiceImpl` |
| Method | camelCase | `createPayment`, `handleVnpayIpn` |
| Enum constant | UPPER_CASE | `PENDING`, `SUCCESS` |
| URL path | kebab-case | `/vnpay-return`, `/refresh-token` |
| DB column | snake_case | `payment_method`, `created_at` |

### 16.2. Patterns áp dụng

| Pattern | Áp dụng ở đâu |
|---|---|
| **Builder** | Tất cả Entities + Response DTOs — Lombok `@Builder` |
| **Record** | Request DTOs — immutable, tự sinh equals/hashCode/toString |
| **Service Interface** | Mọi Service đều có interface + impl (DI tốt, dễ mock test) |
| **Repository Projection** | `ProductRepository` — interface-based projections cho analytics |
| **Optimistic Locking** | `@Version` trên `ProductEntity` + `OrderEntity` |
| **Scheduled Task** | `PaymentServiceImpl.expiredPayment()` — `@Scheduled(fixedRate)` |
| **IDOR Prevention** | Order + Payment verify `order.user.id == tokenUserId` |
| **Idempotent Webhook** | VNPay IPN check SUCCESS trước khi xử lý |
| **Snapshot Price** | `OrderItemEntity.unitPrice` = giá tại thời điểm order |
| **DynamicUpdate/Insert** | Chỉ UPDATE/INSERT các field thay đổi — tối ưu SQL |

### 16.3. Anti-patterns đã tránh

| Anti-pattern | Cách tránh |
|---|---|
| N+1 Query | `JOIN FETCH` trong JPQL |
| Lazy Loading ngoài Transaction | `open-in-view=false` |
| Password trong Response | `UserResponseDto` không có `password` |
| Trust Return URL | `/vnpay-return` đọc DB thay vì `vnp_ResponseCode` |
| Self-Disable | Admin không disable chính mình |

---

## 17. Trạng thái phát triển & TODO

### 17.1. Đã hoàn thành (tính đến 2026-07-19)

| Feature | Trạng thái |
|---|---|
| Auth (Register/Login/Refresh/Logout) | ✅ |
| JWT Filter + Security Config | ✅ |
| Category CRUD | ✅ |
| Product CRUD + Stock Management | ✅ |
| Product Analytics (Top, Revenue) | ✅ |
| Order CRUD + State Machine | ✅ |
| User Profile Management | ✅ |
| User Admin Management (list/disable) | ✅ |
| VNPay Payment Integration | ✅ |
| Optimistic Locking (Product + Order) | ✅ |
| Auto-expire Payments (Scheduler) | ✅ |
| Exception Handling (14 types) | ✅ |
| Swagger/OpenAPI Documentation | ✅ |
| Flyway Migrations (V1-V5) | ✅ |
| Git Security (.env gitignored) | ✅ |

### 17.2. Known Issues & Technical Debt

| Issue | Mức độ | Mô tả |
|---|---|---|
| **Không có Unit Tests** | HIGH | `src/test` rỗng. Cần tests cho AuthServiceImpl, OrderServiceImpl, PaymentServiceImpl |
| **Stock không hoàn khi payment FAILED/EXPIRED** | HIGH | Stock bị trừ ngay khi tạo Order. Khi EXPIRED/FAILED, stock KHÔNG được hoàn lại |
| **`UpdateProfileRequestDto` không partial** | MEDIUM | `@NotBlank` trên cả 2 fields — phải gửi đủ cả 2 kể cả chỉ đổi 1 |
| **CORS hardcoded localhost** | MEDIUM | `allowedOrigins: http://localhost:8085` — phải đổi khi deploy |
| **Không có rate limiting** | MEDIUM | `/auth/login` không có giới hạn số lần thử — dễ brute force |
| **Enum nằm inline trong Entity** | LOW | Nên chuyển ra package `enums/` riêng |
| **`OrderRepository.findByUserIdAndUserId`** | LOW | Method tên bị trùng field — cần rename |
| **Chưa có email notifications** | LOW | Không gửi email khi order CONFIRMED |
| **`PaymentController` inject repository trực tiếp** | LOW | `/vnpay-return` gọi thẳng `paymentRepository` thay vì qua Service |

### 17.3. Roadmap gợi ý

| Phase | Nội dung |
|---|---|
| **Testing** | Unit Tests cho Service layer (AuthServiceImpl, OrderServiceImpl, PaymentServiceImpl). Mục tiêu 80% coverage. |
| **Stock Recovery** | Hoàn stock khi payment EXPIRED/FAILED. Cần compensating transaction. |
| **Rate Limiting** | `Bucket4j` hoặc Redis Rate Limiting cho `/auth/login`. |
| **Email Notifications** | Gửi email khi: Register, Order CONFIRMED, Payment SUCCESS. Dùng `spring-boot-starter-mail`. |
| **Refactor** | Chuyển enums ra package `enums/`. Fix `UpdateProfileRequestDto`. Fix `OrderRepository`. Chuyển `/vnpay-return` logic vào Service. |
| **Deployment** | Docker Compose (app + postgres). Fix CORS cho production URL. |

---

> **Ghi chú cho AI Agent:** Luôn đọc file này trước khi làm bất kỳ việc gì với project. Nếu có thay đổi lớn, tạo `PROJECT_OVERVIEW_V5.md` và đánh dấu file này là deprecated. Không xóa file cũ.
