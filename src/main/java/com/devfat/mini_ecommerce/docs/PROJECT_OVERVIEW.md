# 📖 PROJECT OVERVIEW V5 — Mini Ecommerce (Current State Edition)

> **Mục đích tài liệu:** Đây là tài liệu **CHÍNH XÁC NHẤT** phản ánh trạng thái codebase thực tế tính đến ngày cập nhật. Bất kỳ AI Agent hay Developer nào khi bắt đầu hoặc tiếp tục công việc trên project này đều phải đọc file này **ĐẦU TIÊN**.
>
> **Cập nhật lần cuối:** 2026-07-21
>
> **Supersedes:** PROJECT_OVERVIEW_V2.md, PROJECT_OVERVIEW_V3.md, PROJECT_OVERVIEW_V4.md (các bản cũ đã bị xoá)

---

## 📋 MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Tech Stack & Dependencies](#2-tech-stack--dependencies)
3. [DevOps & Containerization](#3-devops--containerization)
4. [Cấu hình (Configuration)](#4-cấu-hình-configuration)
5. [Cấu trúc thư mục chi tiết](#5-cấu-trúc-thư-mục-chi-tiết)
6. [Database Schema & Migrations](#6-database-schema--migrations)
7. [Entity Layer — Chi tiết từng Entity](#7-entity-layer--chi-tiết-từng-entity)
8. [Repository Layer — Data Access](#8-repository-layer--data-access)
9. [DTO Layer — Request & Response](#9-dto-layer--request--response)
10. [Service Layer — Business Logic](#10-service-layer--business-logic)
11. [Controller Layer — API Endpoints](#11-controller-layer--api-endpoints)
12. [Security & Authentication (JWT)](#12-security--authentication-jwt)
13. [File Upload & Storage (MinIO)](#13-file-upload--storage-minio)
14. [Payment Integration — VNPay](#14-payment-integration--vnpay)
15. [Exception Handling](#15-exception-handling)
16. [Common — API Response Format](#16-common--api-response-format)
17. [Business Flows (Luồng nghiệp vụ)](#17-business-flows-luồng-nghiệp-vụ)
18. [Coding Conventions & Patterns](#18-coding-conventions--patterns)
19. [Trạng thái phát triển & TODO](#19-trạng-thái-phát-triển--todo)
20. [Góp ý tối ưu & Kiến thức cần bổ sung](#20-góp-ý-tối-ưu--kiến-thức-cần-bổ-sung)

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
| **Plan giai đoạn** | Hiện tại đến **Giai đoạn 4** (theo `plan_tong_hop.md`) |

### 1.1. Điểm thay đổi so với V4 (những gì đã thêm mới trong V5)

| Module | Trạng thái V4 | Trạng thái V5 (Hiện tại) |
|---|---|---|
| **Docker & Docker Compose** | ❌ Chưa có hoặc comment | ✅ **HOÀN CHỈNH** — Multi-stage build, healthcheck, service_healthy |
| **GitHub Actions CI** | ❌ Chưa có | ✅ **MỚI** — `ci.yml` build + test on push/PR to main |
| **MinIO Object Storage** | ❌ Chưa có | ✅ **MỚI** — `MinioConfig`, `StorageService`, `StorageServiceImpl` |
| **File Upload & Validation** | ❌ Chưa có | ✅ **MỚI** — `FileValidationUtil` (magic bytes), `Thumbnailator` resize |
| **Image URL columns** | ❌ Chưa có | ✅ **MỚI** — `products.image_url`, `users.avatar_url` (V7 migration) |
| **Upload APIs** | ❌ Chưa có | ✅ **MỚI** — `POST /products/{id}/image`, `POST /users/me/avatar`, `POST /test/upload` |
| **PageResponse wrapper** | ❌ Chưa có | ✅ **MỚI** — `PageResponse<T>` cho pagination chuẩn hoá |
| **BasePageRequest** | ❌ Chưa có | ✅ **MỚI** — DTO base cho pagination request |
| **V6 Migration** | ❌ Chưa có | ✅ **MỚI** — Fix tất cả ID columns sang BIGINT |
| **V7 Migration** | ❌ Chưa có | ✅ **MỚI** — Thêm `image_url` cho products, `avatar_url` cho users |
| **JPA ddl-auto** | `update` | ✅ **Đổi sang `validate`** — an toàn production |
| **spring-dotenv** | ❌ | ✅ **MỚI** — Load `.env` tự động |
| **TestUploadController** | ❌ | ✅ **MỚI** — Test endpoint upload file |
| **base/ package** | Dự kiến BaseEntity | **XOÁ** — không còn trong codebase |

---

## 2. Tech Stack & Dependencies

### 2.1. Core

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| **Java** | 21 | Ngôn ngữ chính |
| **Spring Boot** | 3.5.16 | Framework core |
| **Maven** | (wrapper mvnw) | Build tool |
| **Docker** | Multi-stage | Containerization |
| **PostgreSQL** | 16 (Docker image) | Database |

### 2.2. Dependencies (pom.xml) — Đầy đủ

| Dependency | Phiên bản | Mục đích | Scope |
|---|---|---|---|
| `spring-boot-starter-web` | (managed) | REST API, Tomcat embedded | compile |
| `spring-boot-starter-data-jpa` | (managed) | ORM (Hibernate), JpaRepository | compile |
| `spring-boot-starter-validation` | (managed) | Bean Validation (`@NotNull`, `@Size`...) | compile |
| `spring-boot-starter-security` | (managed) | Spring Security framework | compile |
| `spring-boot-devtools` | (managed) | Hot reload khi dev | runtime, optional |
| `postgresql` | (managed) | PostgreSQL JDBC driver | runtime |
| `flyway-core` | (managed) | Database migration versioning | compile |
| `flyway-database-postgresql` | (managed) | Flyway PostgreSQL adapter | compile |
| `springdoc-openapi-starter-webmvc-ui` | **2.7.0** | Swagger UI + OpenAPI 3 docs | compile |
| `jjwt-api` | **0.12.6** | JWT API | compile |
| `jjwt-impl` | **0.12.6** | JWT implementation | runtime |
| `jjwt-jackson` | **0.12.6** | JWT JSON serialization | runtime |
| `lombok` | (managed) | Giảm boilerplate | compile, optional |
| `spring-dotenv` | **4.0.0** | Tự động load file `.env` vào Spring properties | compile |
| `minio` | **8.5.17** | **MỚI** — MinIO Java SDK (S3-compatible object storage) | compile |
| `thumbnailator` | **0.4.20** | **MỚI** — Resize/compress ảnh trước khi upload | compile |
| `spring-boot-starter-test` | (managed) | Unit/Integration testing | test |

> **Lưu ý:** Dự án **KHÔNG** có VNPay SDK dependency. VNPay được tích hợp thủ công bằng HMAC-SHA512 (`javax.crypto.Mac`).

### 2.3. Build Plugins

| Plugin | Vai trò |
|---|---|
| `spring-boot-maven-plugin` | Build JAR, exclude Lombok |
| `maven-compiler-plugin` | Config annotation processor paths cho Lombok ở cả compile + testCompile |

---

## 3. DevOps & Containerization

### 3.1. Dockerfile (Multi-stage build)

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget       ← Cần cho healthcheck
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> **Ưu điểm:** Image nhỏ gọn (JRE-only), tách biệt build & runtime, cache Maven dependencies.

### 3.2. docker-compose.yml

| Service | Image | Port | Healthcheck |
|---|---|---|---|
| **db** | `postgres:16` | `5432:5432` | `pg_isready -U ${DB_USERNAME} -d mini_shop` mỗi 5s |
| **app** | Build từ Dockerfile | `8085:8085` | `wget --spider http://localhost:8085/api/v1/health` mỗi 10s |

**Đặc điểm quan trọng:**
- `app` depends_on `db` với `condition: service_healthy` — chờ DB ready rồi mới start app
- Env từ `.env` + override `SPRING_DATASOURCE_URL` để dùng hostname `db` thay vì `localhost`
- Volume `pgdata` persist data PostgreSQL

### 3.3. GitHub Actions CI (`ci.yml`)

```yaml
on: push/PR to main
jobs:
  build-and-test:
    - Checkout code
    - Setup JDK 21 (Temurin) + Maven cache
    - ./mvnw clean verify -B
```

### 3.4. .dockerignore & .gitignore

| File | Ignore |
|---|---|
| `.dockerignore` | `target/`, `.git/`, `.idea/`, `*.iml`, `.env`, `.mvn/wrapper/maven-wrapper.jar` |
| `.gitignore` | Maven build, IDE files, `.env`, `application.yaml` (chứa secrets) |

### 3.5. .env & .env.example

- `.env` — chứa secrets thực (DB, JWT, Mail, MinIO, VNPay) → **KHÔNG commit**
- `.env.example` — template để developer khác setup:
  ```
  DB_URL, DB_USERNAME, DB_PASSWORD
  MAIL_USERNAME, MAIL_PASSWORD
  JWT_SECRET_KEY
  ```

---

## 4. Cấu hình (Configuration)

### 4.1. application.yaml (file chính)

| Nhóm cấu hình | Key | Giá trị / Mô tả |
|---|---|---|
| **Profile** | `spring.profiles.active` | `dev` (mặc định) |
| **Multipart** | `spring.servlet.multipart.max-file-size` | `5MB` |
| | `spring.servlet.multipart.max-request-size` | `5MB` |
| **Database** | `spring.datasource.url` | `${DB_URL}` (từ `.env`) |
| | `spring.datasource.driver-class-name` | `org.postgresql.Driver` |
| | `spring.datasource.hikari.maximum-pool-size` | `10` |
| | `spring.datasource.hikari.connection-timeout` | `30000` |
| **JPA** | `spring.jpa.show-sql` | `true` |
| | `spring.jpa.open-in-view` | `false` |
| | **`spring.jpa.hibernate.ddl-auto`** | **`validate`** ← Đổi từ `update` (an toàn hơn) |
| | `spring.jpa.properties.hibernate.format_sql` | `true` |
| | `spring.jpa.properties.hibernate.use_sql_comments` | `true` |
| **Flyway** | `spring.flyway.enabled` | `true` |
| | `spring.flyway.locations` | `classpath:db/migration` |
| | `spring.flyway.baseline-on-migrate` | `true` |
| | `spring.flyway.schemas` | `public` |
| **Mail** | `spring.mail.*` | Gmail SMTP (587, TLS) — cấu hình sẵn, chưa dùng |
| **JWT** | `app.jwt.secret-key` | `${JWT_SECRET_KEY}` |
| | `app.jwt.expiration` | `3600000` (1 giờ, ms) |
| | `app.jwt.refresh-expiration-days` | `7` (7 ngày) |
| **Upload** | `app.upload.max-image-size` | `5MB` |
| **MinIO** | `app.minio.endpoint` | `http://localhost:9000` |
| | `app.minio.access-key` | `${MIN_IO_ACCESS_KEY}` |
| | `app.minio.secret-key` | `${MIN_IO_SECRET_KEY}` |
| | `app.minio.bucket` | `${MIN_IO_BUCKET}` |
| **VNPay** | `vnpay.pay-url` | Sandbox URL |
| | `vnpay.tmn-code` | `${VNPAY_TMN_CODE}` |
| | `vnpay.secret-key` | `${VNPAY_SECRET_KEY}` |
| | `vnpay.return-url` | `${VNPAY_RETURN_URL}` |
| | `vnpay.ipn-url` | `${VNPAY_IPN_URL}` |
| **Swagger** | `springdoc.api-docs.path` | `/v1/api-docs` |
| | `springdoc.swagger-ui.path` | `/swagger-ui.html` |
| **Logging** | `logging.level.org.springframework.web` | `DEBUG` |
| | `logging.level.org.hibernate.SQL` | `DEBUG` |
| | `logging.level.org.hibernate.type` | `TRACE` |

### 4.2. Profile-specific files

| File | Server Port |
|---|---|
| `application-dev.yaml` | `8085` |
| `application-test.yaml` | `8082` |
| `application-prod.yaml` | `8083` |

### 4.3. Configuration Beans

| Bean Class | Annotation | Vai trò |
|---|---|---|
| `SecurityConfig` | `@Configuration @EnableWebSecurity @EnableMethodSecurity` | Filter chain, CORS, BCrypt, AuthenticationManager |
| `OpenApiConfig` | `@Configuration` | Swagger/OpenAPI + Bearer Auth scheme |
| `VNPayConfig` | `@Configuration @ConfigurationProperties(prefix="vnpay")` | VNPay settings + helper methods |
| `MinioConfig` | `@Configuration` | **MỚI** — Tạo `MinioClient` bean từ endpoint/access-key/secret-key |

---

## 5. Cấu trúc thư mục chi tiết

```
mini-ecommerce/
├── pom.xml                                        # Maven build config
├── mvnw / mvnw.cmd                                # Maven wrapper
├── Dockerfile                                     # Multi-stage build (builder → JRE-alpine)
├── docker-compose.yml                             # db (postgres:16) + app services
├── .env                                           # ⚠️ KHÔNG commit — chứa ALL secrets
├── .env.example                                   # Template cho developer mới
├── .dockerignore                                  # Ignore target, .git, .idea, .env
├── .gitignore                                     # Ignore .env, application.yaml, IDE
├── .gitattributes                                 # Git line-ending config
├── README.md                                      # Project readme (minimal)
├── HELP.md                                        # Spring Boot generated help
├── .github/
│   └── workflows/
│       └── ci.yml                                 # GitHub Actions: build + test on push/PR
├── .mvn/                                          # Maven wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/devfat/mini_ecommerce/
│   │   │   ├── MiniEcommerceApplication.java      # Entry point (@SpringBootApplication @EnableScheduling)
│   │   │   │
│   │   │   ├── common/                            # Shared response wrappers
│   │   │   │   ├── ApiResponse.java               # Generic API response <T> + success/error factories
│   │   │   │   └── PageResponse.java              # ★ MỚI — Pagination wrapper (content, page, size, totalElements, totalPages, last)
│   │   │   │
│   │   │   ├── config/                            # Spring Configuration beans (5 files)
│   │   │   │   ├── SecurityConfig.java            # Security filter chain, CORS, BCrypt, AuthenticationManager
│   │   │   │   ├── OpenApiConfig.java             # Swagger/OpenAPI + Bearer Auth scheme
│   │   │   │   ├── VNPayConfig.java               # @ConfigurationProperties(prefix="vnpay")
│   │   │   │   ├── VNPayUtil.java                 # HMAC-SHA512, buildQueryAndHash, verifySignature
│   │   │   │   └── MinioConfig.java               # ★ MỚI — MinioClient bean
│   │   │   │
│   │   │   ├── controller/                        # REST Controllers (8 files ← tăng từ 7)
│   │   │   │   ├── AuthController.java            # /api/v1/auth/** (4 endpoints)
│   │   │   │   ├── CategoryController.java        # /api/v1/categories/** (5 endpoints)
│   │   │   │   ├── ProductController.java         # /api/v1/products/** (10+1 endpoints) ← thêm upload image
│   │   │   │   ├── OrderController.java           # /api/v1/orders/** (4 endpoints)
│   │   │   │   ├── UserController.java            # /api/v1/users/** (5+1 endpoints) ← thêm upload avatar
│   │   │   │   ├── PaymentController.java         # /api/v1/payments/** (3 endpoints)
│   │   │   │   ├── PingController.java            # /api/v1/health (1 endpoint)
│   │   │   │   └── TestUploadController.java      # ★ MỚI — /api/v1/test/upload (test MinIO)
│   │   │   │
│   │   │   ├── docs/                              # Tài liệu dự án (markdown)
│   │   │   │   ├── PROJECT_OVERVIEW.md            # ★ BẢN NÀY V5 (hiện tại)
│   │   │   │   ├── plan_tong_hop.md               # Kế hoạch tổng hợp các phase
│   │   │   │   ├── plan_uu_tien_hoc_tap.md        # Roadmap học tập ưu tiên
│   │   │   │   └── auth_security_jwt_plan.md      # Plan JWT chi tiết (5 giai đoạn, A-E)
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/                       # 12 request DTOs (tăng từ 11)
│   │   │   │   │   ├── BasePageRequest.java       # ★ MỚI — Base class cho pagination params
│   │   │   │   │   ├── RegisterRequestDto.java    # (record) Đăng ký user
│   │   │   │   │   ├── LoginRequestDto.java       # (record) Đăng nhập
│   │   │   │   │   ├── RefreshTokenRequestDto.java # (record) Refresh token
│   │   │   │   │   ├── CreateProductRequestDto.java # (record) Tạo sản phẩm
│   │   │   │   │   ├── UpdateProductRequestDto.java # (record) Cập nhật sản phẩm (partial)
│   │   │   │   │   ├── CreateCategoryRequestDto.java # (record) Tạo danh mục
│   │   │   │   │   ├── CreateOrderRequestDto.java  # (record) Tạo đơn hàng
│   │   │   │   │   ├── OrderItemRequestDto.java    # (record) Item trong đơn hàng
│   │   │   │   │   ├── UpdateOrderStatusRequestDto.java # (record) Cập nhật status đơn
│   │   │   │   │   ├── ChangePasswordRequestDto.java # (record) Đổi mật khẩu
│   │   │   │   │   └── UpdateProfileRequestDto.java # (record) Cập nhật profile
│   │   │   │   │
│   │   │   │   └── response/                      # 8 response DTOs
│   │   │   │       ├── AuthResponseDto.java       # Login response (accessToken, refreshToken, tokenType, expiresIn)
│   │   │   │       ├── UserResponseDto.java       # User info (+ avatarUrl mới)
│   │   │   │       ├── CategoryResponseDto.java   # Category info
│   │   │   │       ├── ProductResponseDto.java    # Product info (+ imageUrl, active mới)
│   │   │   │       ├── OrderResponseDto.java      # Order info
│   │   │   │       ├── OrderItemResponseDto.java  # Order item info
│   │   │   │       ├── RefreshTokenResponseDto.java # Refresh response
│   │   │   │       └── CreatePaymentResponseDto.java # Payment URL (record)
│   │   │   │
│   │   │   ├── entity/                            # JPA Entities (7 entities)
│   │   │   │   ├── UserEntity.java                # + avatarUrl field (V7)
│   │   │   │   ├── CategoryEntity.java
│   │   │   │   ├── ProductEntity.java             # + imageUrl field (V7), @Version
│   │   │   │   ├── OrderEntity.java               # @Version
│   │   │   │   ├── OrderItemEntity.java
│   │   │   │   ├── RefreshTokenEntity.java
│   │   │   │   └── PaymentEntity.java             # 3 inline enums
│   │   │   │
│   │   │   ├── enums/                             # (RỖNG) — Enums inline trong Entity
│   │   │   │
│   │   │   ├── exception/                         # 7 custom exceptions + 1 Global handler
│   │   │   │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice (14 handlers)
│   │   │   │   ├── BadRequestException.java       # 400
│   │   │   │   ├── ResourceNotFoundException.java # 404
│   │   │   │   ├── DuplicateResourceException.java # 409
│   │   │   │   ├── InsufficientStockException.java # 409
│   │   │   │   ├── InvalidStatusTransitionException.java # 409
│   │   │   │   ├── CategoryHasProductsException.java # 409
│   │   │   │   └── InvalidRefreshTokenException.java # 401
│   │   │   │
│   │   │   ├── repository/                        # 6 repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── ProductRepository.java         # Phức tạp nhất: JPQL, Projections
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   └── PaymentRepository.java
│   │   │   │
│   │   │   ├── security/                          # JWT Security module (7 files)
│   │   │   │   ├── JwtProvider.java               # generate, validate, extract claims
│   │   │   │   ├── JwtAuthenticationFilter.java   # OncePerRequestFilter — gắn Authentication vào SecurityContext
│   │   │   │   ├── JwtAuthenticationEntryPoint.java # JSON 401 response
│   │   │   │   ├── JwtAccessDeniedHandler.java    # JSON 403 response
│   │   │   │   ├── CustomUserDetailsService.java  # Load user bằng email → UserPrincipal
│   │   │   │   ├── UserPrincipal.java             # UserDetails impl (getUserId, getRole, isEnabled)
│   │   │   │   └── RefreshTokenGenerator.java     # SecureRandom 64 bytes → Base64
│   │   │   │
│   │   │   ├── service/                           # Service interfaces (7 files) + impl/ (7 files)
│   │   │   │   ├── AuthService.java               # register, login, refreshToken, logout
│   │   │   │   ├── CategoryService.java           # CRUD + search
│   │   │   │   ├── ProductService.java            # CRUD + stock + analytics + uploadImage
│   │   │   │   ├── OrderService.java              # CRUD + status machine
│   │   │   │   ├── UserService.java               # getMe, changePassword, updateProfile, getAllUsers, updateStatus, uploadAvatar
│   │   │   │   ├── PaymentService.java            # createPayment, handleVnPayIpn
│   │   │   │   ├── StorageService.java            # ★ MỚI — interface: uploadFile(file, folder, resize)
│   │   │   │   └── impl/
│   │   │   │       ├── AuthServiceImpl.java       # 4 methods + hashRefreshToken helper
│   │   │   │       ├── CategoryServiceImpl.java   # 5 methods
│   │   │   │       ├── ProductServiceImpl.java    # 11 methods (+ uploadProductImage)
│   │   │   │       ├── OrderServiceImpl.java      # 4 methods + state machine
│   │   │   │       ├── UserServiceImpl.java       # 6 methods (+ uploadUserImage)
│   │   │   │       ├── PaymentServiceImpl.java    # 3 methods (+ @Scheduled expiredPayment)
│   │   │   │       └── StorageServiceImpl.java    # ★ MỚI — MinIO upload + Thumbnailator resize
│   │   │   │
│   │   │   └── util/                              # Utility classes
│   │   │       └── FileValidationUtil.java        # ★ MỚI — Magic bytes validation (JPEG, PNG, GIF, WebP)
│   │   │
│   │   └── resources/
│   │       ├── application.yaml                   # Config chính (⚠️ gitignored vì chứa env refs)
│   │       ├── application-dev.yaml               # Port 8085
│   │       ├── application-test.yaml              # Port 8082
│   │       ├── application-prod.yaml              # Port 8083
│   │       ├── db/migration/
│   │       │   ├── V1__init_mini_shop.sql         # 6 bảng core + indexes
│   │       │   ├── V2__add_version_to_products.sql # Optimistic Lock: products.version
│   │       │   ├── V3__create_payments_table.sql  # Bảng payments + indexes
│   │       │   ├── V4__fix_payments_order_id_type.sql # Fix BIGINT cho order_id
│   │       │   ├── V5__add_version_to_orders.sql  # Optimistic Lock: orders.version
│   │       │   ├── V6__fix_id_columns_to_bigint.sql # ★ MỚI — Fix ALL ID columns → BIGINT
│   │       │   └── V7__add_image_url_columns.sql  # ★ MỚI — products.image_url + users.avatar_url
│   │       ├── static/                            # (RỖNG)
│   │       └── templates/                         # (RỖNG)
│   │
│   └── test/java/                                 # (Chưa có test code)
```

---

## 6. Database Schema & Migrations

### 6.1. Lịch sử Flyway Migrations (7 migrations)

| File | Nội dung |
|---|---|
| `V1__init_mini_shop.sql` | Tạo 6 bảng core: `users`, `categories`, `products`, `orders`, `order_items`, `refresh_tokens` + 7 indexes |
| `V2__add_version_to_products.sql` | `ALTER TABLE products ADD COLUMN version INTEGER NOT NULL DEFAULT 0` |
| `V3__create_payments_table.sql` | Tạo bảng `payments` + 2 indexes |
| `V4__fix_payments_order_id_type.sql` | `ALTER TABLE payments ALTER COLUMN order_id TYPE BIGINT` |
| `V5__add_version_to_orders.sql` | `ALTER TABLE orders ADD COLUMN version INTEGER NOT NULL DEFAULT 0` |
| `V6__fix_id_columns_to_bigint.sql` | ★ **MỚI** — Fix ALL `id`, FK columns sang `BIGINT` (users, categories, products, orders, order_items, refresh_tokens, payments) |
| `V7__add_image_url_columns.sql` | ★ **MỚI** — `products ADD image_url VARCHAR(500)`, `users ADD avatar_url VARCHAR(500)` |

### 6.2. Database Schema (7 bảng)

**Bổ sung so với V4:**
- `products.image_url VARCHAR(500)` — URL ảnh sản phẩm (MinIO)
- `users.avatar_url VARCHAR(500)` — URL avatar user (MinIO)
- Tất cả ID và FK columns đã chuẩn hoá sang `BIGINT`

**Bảng payments (không đổi):**

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
```

### 6.3. Tổng hợp Indexes (9 indexes)

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

## 7. Entity Layer — Chi tiết từng Entity

> **Quy ước chung:** Tất cả entities dùng `@Data @NoArgsConstructor @AllArgsConstructor @Builder @DynamicUpdate @DynamicInsert`. Timestamps dùng `@CreationTimestamp` và `@UpdateTimestamp`.

### 7.1. UserEntity (`users`)

| Field | Type | JPA Annotation | Ghi chú |
|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | PK |
| `fullName` | `String` | `@Column(name="full_name", length=150)` | |
| `email` | `String` | `@Column(unique=true, length=150)` | Username đăng nhập |
| **`avatarUrl`** | **`String`** | **`@Column(name="avatar_url", length=500)`** | **★ MỚI — URL avatar (MinIO)** |
| `phoneNumber` | `String` | `@Column(name="phone_number", unique=true, length=15)` | |
| `password` | `String` | `@Column(nullable=false)` | BCrypt hash |
| `role` | `Role` enum | `@Enumerated(STRING)` | Inline enum: `USER`, `ADMIN` |
| `isActive` | `boolean` | `@Column(name="is_active")` | Soft disable account |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | |

### 7.2. CategoryEntity (`categories`) — Không đổi

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `name` | `String` | UNIQUE, length=50 |
| `products` | `List<ProductEntity>` | `@OneToMany(LAZY, mappedBy="category")` |
| `createdAt` | `LocalDateTime` | |

### 7.3. ProductEntity (`products`) — Optimistic Lock + Image

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `name` | `String` | NOT NULL |
| **`imageUrl`** | **`String`** | **★ MỚI — `@Column(name="image_url", length=500)` — URL ảnh (MinIO)** |
| `description` | `String` | `@Column(columnDefinition="TEXT")` |
| `price` | `BigDecimal` | precision=12, scale=2 |
| `stock` | `Integer` | `@Builder.Default = 0` |
| `category` | `CategoryEntity` | `@ManyToOne(LAZY)`, FK: `category_id` |
| `isActive` | `boolean` | `@Builder.Default = true` |
| `orderItems` | `List<OrderItemEntity>` | `@OneToMany(mappedBy="product")` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |
| `version` | `Integer` | `@Version` — Optimistic Locking |

### 7.4. OrderEntity (`orders`) — Optimistic Lock — Không đổi

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `user` | `UserEntity` | `@ManyToOne(LAZY)` |
| `status` | `OrderStatus` enum | Inline: `DONE, PENDING, SHIPPED, CONFIRMED, CANCELLED` |
| `totalAmount` | `BigDecimal` | Default `BigDecimal.ZERO` |
| `note` | `String` | length=1000 |
| `items` | `List<OrderItemEntity>` | `@OneToMany(cascade=ALL, orphanRemoval=true)` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |
| `version` | `Integer` | `@Version` |

### 7.5. OrderItemEntity (`order_items`) — Không đổi

### 7.6. RefreshTokenEntity (`refresh_tokens`) — Không đổi

### 7.7. PaymentEntity (`payments`) — Không đổi

> **3 inline enums trong PaymentEntity:**
> - `PaymentStatus`: `{PENDING, SUCCESS, FAILED, EXPIRED}`
> - `PaymentMethod`: `{CASH, BANK, WALLET}`
> - `PaymentProvider`: `{VNPAY, MOMO, ZALOPAY, ACB, VCB}`

---

## 8. Repository Layer — Data Access

### 8.1. UserRepository — Không đổi

| Method | Mô tả |
|---|---|
| `findByEmail(String)` | Tìm user bằng email |
| `findByPhoneNumber(String)` | Tìm user bằng SĐT |

### 8.2. CategoryRepository — Không đổi

| Method | Mô tả |
|---|---|
| `findByNameContainingIgnoreCase(String, Pageable)` | Search + phân trang |
| `existsByNameIgnoreCase(String)` | Check tên đã tồn tại |

### 8.3. ProductRepository — Phức tạp nhất

**Derived queries:**

| Method | Mô tả |
|---|---|
| `findByStockGreaterThan(int)` | SP còn hàng |
| `findByCategoryIdAndStockGreaterThan(Long, int)` | SP theo category còn hàng |
| `findByStockGreaterThan(int, Pageable)` | Phân trang |

**JPQL custom queries:**

| Method | Mô tả |
|---|---|
| `findAvailableWithCategory()` | `JOIN FETCH p.category WHERE p.stock > 0` |
| `findByMinPriceWithCategory(BigDecimal)` | `JOIN FETCH p.category WHERE p.price >= :minPrice` |
| `findByNameContainsIgnoreCase(String, Pageable)` | **JOIN FETCH + isActive=true + LIKE search** — có countQuery riêng |

**Projection Interfaces:**

| Interface | Fields | Mục đích |
|---|---|---|
| `TopProductView` | `name`, `price`, `mostBuy` | Top sản phẩm bán chạy |
| `CategoryRevenueView` | `name`, `revenue` | Doanh thu theo category |
| `MonthlyRevenueView` | `month`, `revenue` | Doanh thu theo tháng |

### 8.4. OrderRepository

| Method | Mô tả |
|---|---|
| `findAllByUserId(Long)` | Orders theo userId |
| `findByUserId(Long)` | Single order theo userId |
| `findAllByStatus(OrderStatus)` | Orders theo status |
| `findAllByUserIdAndStatus(Long, OrderStatus)` | JPQL JOIN FETCH user |
| `findByUserIdWithDetails(Long)` | JPQL: JOIN FETCH user + LEFT JOIN FETCH items |

### 8.5. RefreshTokenRepository — Không đổi

### 8.6. PaymentRepository — Không đổi

---

## 9. DTO Layer — Request & Response

### 9.1. Request DTOs

| DTO | Type | Fields | Validation | Ghi chú |
|---|---|---|---|---|
| **BasePageRequest** | **Class** | `page, size, sortBy, direction` | — | **★ MỚI** — Base cho pagination, có `toPageable()` method |
| **RegisterRequestDto** | Record | `fullName, email, phoneNumber, password` | `@NotNull @NotBlank @Email @Size @Pattern(10-11 digits)` | |
| **LoginRequestDto** | Record | `email, password` | `@NotNull @NotBlank @Email @Size` | |
| **RefreshTokenRequestDto** | Record | `refreshToken` | `@NotNull @NotBlank` | |
| **CreateProductRequestDto** | Record | `name, description, price, stock, categoryId, isActive` | `@NotNull @NotBlank @DecimalMin @Min(1)` | |
| **UpdateProductRequestDto** | Record | `name, description, price, stock, isActive, categoryId` | Tất cả nullable (partial update) | |
| **CreateCategoryRequestDto** | Record | `name` | `@NotNull @NotBlank` | |
| **CreateOrderRequestDto** | Record | `items: List<OrderItemRequestDto>` | `@NotEmpty @Valid` | |
| **OrderItemRequestDto** | Record | `productId, quantity` | `@NotNull, @Min(1)` | |
| **UpdateOrderStatusRequestDto** | Record | `orderStatus` | `@NotNull` | |
| **ChangePasswordRequestDto** | Record | `oldPassword, newPassword` | `@NotNull @NotBlank @Size(8-100)` | |
| **UpdateProfileRequestDto** | Record | `fullName, phoneNumber` | `@NotBlank @Pattern(10-11 digits)` | |

### 9.2. Response DTOs

| DTO | Fields | Ghi chú |
|---|---|---|
| **UserResponseDto** | `userId, fullName, **avatarUrl**, email, phoneNumber, role, isActive, createdAt` | **★ Thêm `avatarUrl`** |
| **AuthResponseDto** | `accessToken, refreshToken, tokenType("Bearer"), expiresIn` | |
| **RefreshTokenResponseDto** | `accessToken` | |
| **CategoryResponseDto** | `id, categoryName` | |
| **ProductResponseDto** | `id, name, price, stock, description, **imageUrl**, **active**, category(CategoryResponseDto)` | **★ Thêm `imageUrl`, `active`** |
| **OrderResponseDto** | `id, status, totalAmount, createdAt, orderItems(List)` | |
| **OrderItemResponseDto** | `productName, quantity, unitPrice` | |
| **CreatePaymentResponseDto** | `paymentUrl` | Record |

### 9.3. Shared Response Wrapper

| Class | Mô tả |
|---|---|
| **ApiResponse\<T\>** | `success, message, data, timestamp` — `@JsonInclude(NON_NULL)` |
| **PageResponse\<T\>** | **★ MỚI** — `content, page, size, totalElements, totalPages, last` — factory `PageResponse.of(Page<T>)` |

---

## 10. Service Layer — Business Logic

### 10.1. AuthService / AuthServiceImpl — Không đổi

| Method | Logic |
|---|---|
| **register** | Trim+lowercase email → check trùng → BCrypt hash → save → UserResponseDto |
| **login** | AuthManager.authenticate() → JWT accessToken → clean expired tokens → sinh refresh token → save → AuthResponseDto |
| **refreshToken** | SHA-256 hash → tìm entity → check revoked+expiry → sinh accessToken mới |
| **logout** | SHA-256 hash → set revoked=true. Idempotent. |

### 10.2. CategoryService / CategoryServiceImpl — Không đổi (5 methods)

### 10.3. ProductService / ProductServiceImpl — Thêm upload

| Method | Logic |
|---|---|
| **create** | Tìm category → tạo Product → save |
| **getProductsWithSearch** | JPQL search + JOIN FETCH category + pagination |
| **findById** | findById → map → orElseThrow |
| **update** | Partial update (chỉ set fields != null) |
| **softDelete** | Check isActive → set false |
| **decreaseStock** | validate > 0 + check stock >= qty → trừ stock |
| **increaseStock** | validate > 0 → cộng stock |
| **getTopProducts** | Projection query |
| **getCategoryRevenue** | Projection query |
| **getMonthlyRevenue** | Projection query |
| **uploadProductImage** | **★ MỚI** — findById → `storageService.uploadFile(file, "productImage", true)` → set imageUrl → save |

### 10.4. OrderService / OrderServiceImpl — Không đổi (4 methods)

**Order Status State Machine:**
```
PENDING ──→ CONFIRMED ──→ SHIPPED ──→ DONE
   │             │
   └──→ CANCELLED└──→ CANCELLED
```

> **VNPay rule:** `PENDING → CONFIRMED` **CHỈ** từ `PaymentServiceImpl.handleVnpayIpn()` khi `vnp_ResponseCode = "00"`.

### 10.5. UserService / UserServiceImpl — Thêm upload

| Method | Logic |
|---|---|
| **getMe(Long id)** | findById → toResponseDto |
| **getAllUsers(Pageable)** | findAll → map. ADMIN only. |
| **changePassword** | Verify old password → encode new → save |
| **updateProfile** | Set phone/fullName nếu != null → save |
| **updateStatusUser** | Check không tự disable → revoke tokens nếu disable → save |
| **uploadUserImage** | **★ MỚI** — findById → `storageService.uploadFile(file, "UserImage", true)` → set avatarUrl → save |

### 10.6. PaymentService / PaymentServiceImpl — Không đổi (3 methods)

| Method | Logic |
|---|---|
| **createPayment** | Validate → tạo PaymentEntity → build VNPay URL → trả paymentUrl |
| **handleVnpayIpn** | Verify signature → idempotency check → SUCCESS/FAILED → update order |
| **expiredPayment** | `@Scheduled(fixedRate=120000)` — PENDING > 15min → EXPIRED + CANCELLED |

### 10.7. StorageService / StorageServiceImpl — ★ MỚI

| Method | Logic |
|---|---|
| **uploadFile(MultipartFile, String folder, boolean resizeImage)** | 1. `fileValidationUtil.validateImageFile(file)` — check magic bytes. 2. Generate UUID filename. 3. Nếu resize: `Thumbnailator.size(800,800).outputQuality(0.85)`. 4. `minioClient.putObject()`. 5. Return URL: `endpoint/bucket/folder/uuid.ext` |

---

## 11. Controller Layer — API Endpoints

### 11.1. AuthController (`/api/v1/auth`) — 4 endpoints

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/register` | Public | RegisterRequestDto | 201 + UserResponseDto |
| POST | `/login` | Public | LoginRequestDto | 200 + AuthResponseDto |
| POST | `/refresh-token` | Public | RefreshTokenRequestDto | 200 + RefreshTokenResponseDto |
| POST | `/logout` | Public | RefreshTokenRequestDto | 200 + null |

### 11.2. CategoryController (`/api/v1/categories`) — 5 endpoints

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateCategoryRequestDto | 201 + CategoryResponseDto |
| GET | `/` | Public | `?page&size&search&sort&direction` | 200 + Page |
| GET | `/{id}` | Public | Path: id | 200 + CategoryResponseDto |
| PUT | `/{id}` | ADMIN | CreateCategoryRequestDto | 200 + CategoryResponseDto |
| DELETE | `/{id}` | ADMIN | Path: id | 200 + Boolean |

### 11.3. ProductController (`/api/v1/products`) — 11 endpoints

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
| **POST** | **`/{id}/image`** | **ADMIN** | **multipart/form-data `file`** | **200 + ProductResponseDto** ★ MỚI |

### 11.4. OrderController (`/api/v1/orders`) — 4 endpoints

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| GET | `/user/{userId}` | Authenticated | Path: userId + @AuthPrincipal | 200 + List OrderResponseDto |
| GET | `/{id}` | Authenticated | Path: orderId + @AuthPrincipal | 200 + OrderResponseDto |
| POST | `/` | Authenticated | CreateOrderRequestDto | 201 + OrderResponseDto |
| PATCH | `/{id}/status` | ADMIN | UpdateOrderStatusRequestDto | 200 + OrderResponseDto |

### 11.5. UserController (`/api/v1/users`) — 6 endpoints

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| GET | `/me` | Authenticated | @AuthPrincipal | 200 + UserResponseDto |
| PATCH | `/password` | Authenticated | ChangePasswordRequestDto | 204 No Content |
| PATCH | `/me/update` | Authenticated | UpdateProfileRequestDto | 204 No Content |
| GET | `/` | ADMIN `@PreAuthorize` | `?page&size&sort&direction` | 200 + Page UserResponseDto |
| PATCH | `/{userId}/status` | ADMIN `@PreAuthorize` | `?isActive=` | 204 No Content |
| **POST** | **`/me/avatar`** | **Authenticated** | **multipart/form-data `file`** | **200 + UserResponseDto** ★ MỚI |

### 11.6. PaymentController (`/api/v1/payments`) — 3 endpoints — Không đổi

### 11.7. PingController (`/api/v1/health`) — 1 endpoint — Không đổi

### 11.8. TestUploadController (`/api/v1/test`) — ★ MỚI

| HTTP | Path | Auth | Request | Response |
|---|---|---|---|---|
| **POST** | **`/upload`** | **Authenticated** | **multipart/form-data `file`** | **200 + String (URL)** |

> **Lưu ý:** Đây là endpoint test MinIO upload, không nên expose ở production.

### 11.9. Tổng hợp tất cả API Endpoints

| Module | Tổng endpoints | Public | Authenticated | ADMIN only |
|---|---|---|---|---|
| Auth | 4 | 4 | 0 | 0 |
| Category | 5 | 2 (GET) | 0 | 3 |
| Product | 11 | 1 (GET list) | 1 (GET by id) | 9 |
| Order | 4 | 0 | 2 | 2 |
| User | 6 | 0 | 4 | 2 |
| Payment | 3 | 2 (vnpay-return, vnpay-ipn) | 1 | 0 |
| Health | 1 | 1 | 0 | 0 |
| Test Upload | 1 | 0 | 1 | 0 |
| **TỔNG** | **35** | **10** | **9** | **16** |

---

## 12. Security & Authentication (JWT)

### 12.1. Security Filter Chain

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
(EntryPoint)           (AccessDeniedHandler)
```

### 12.2. JWT Token Structure

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
- **Refresh Token:** Opaque (SecureRandom 64 bytes + Base64 without padding), lưu SHA-256 hash, expiry 7 ngày.

### 12.3. CORS Configuration

```java
allowedOrigins: ["http://localhost:8085"]  // TODO: đổi khi deploy
allowedMethods: ["GET", "POST", "PUT", "PATCH", "DELETE"]
allowedHeaders: ["Authorization", "Content-Type"]
```

### 12.4. Security Classes — 7 files (Không đổi)

| Class | Responsibility |
|---|---|
| `JwtProvider` | Tạo JWT, validate, extract claims (email, userId, role) |
| `JwtAuthenticationFilter` | Intercept request, set Authentication vào SecurityContext |
| `JwtAuthenticationEntryPoint` | Trả JSON 401 (dùng `ObjectMapper` + `ApiResponse.error()`) |
| `JwtAccessDeniedHandler` | Trả JSON 403 (dùng `ObjectMapper` + `ApiResponse.error()`) |
| `CustomUserDetailsService` | Load user từ DB bằng email, wrap thành `UserPrincipal` |
| `UserPrincipal` | `UserDetails` impl. Authority = `ROLE_` + role. `isEnabled()` = `isActive`. |
| `RefreshTokenGenerator` | SecureRandom 64 bytes → Base64 (without padding) |

---

## 13. File Upload & Storage (MinIO) — ★ MỚI HOÀN TOÀN

### 13.1. Kiến trúc Upload

```
Client (multipart/form-data)
    │
    ▼
Controller (ProductController / UserController / TestUploadController)
    │
    ▼
StorageService.uploadFile(file, folder, resize)
    ├─ FileValidationUtil.validateImageFile(file)
    │   ├─ Check null/empty
    │   ├─ Check file size <= 5MB (from config)
    │   └─ Check magic bytes (JPEG/PNG/GIF/WebP)
    ├─ Generate UUID filename: folder/UUID.ext
    ├─ (Optional) Thumbnailator.resize(800x800, quality=0.85)
    └─ MinioClient.putObject(bucket, objectName, stream)
    │
    ▼
Return URL: {endpoint}/{bucket}/{folder}/{uuid}.{ext}
```

### 13.2. FileValidationUtil

| Method | Mô tả |
|---|---|
| `validateImageFile(MultipartFile)` | Check null, size, magic bytes |
| `matchesSignature(byte[], byte[])` | So sánh magic bytes header |
| `isWebp(byte[])` | Check RIFF + WEBP markers |

**Supported formats:** JPEG (`FF D8 FF`), PNG (`89 50 4E 47`), GIF87a, GIF89a, WebP (`RIFF...WEBP`)

### 13.3. Config

| Key | Value |
|---|---|
| `app.upload.max-image-size` | `5MB` (inject vào `FileValidationUtil`) |
| `spring.servlet.multipart.max-file-size` | `5MB` |
| `spring.servlet.multipart.max-request-size` | `5MB` |

---

## 14. Payment Integration — VNPay — Không đổi

### 14.1. Luồng thanh toán

```
1. POST /orders          → Order.status = PENDING (stock bị trừ ngay)
2. POST /payments/create → Nhận paymentUrl → redirect VNPay
3. User thanh toán trên VNPay
4. VNPay IPN callback    → Payment SUCCESS → Order CONFIRMED
                          hoặc Payment FAILED → Order vẫn PENDING
5. Scheduler (2 phút)   → PENDING payments > 15 phút → EXPIRED + Order CANCELLED
```

### 14.2. VNPayUtil (Static Utility)

| Method | Mô tả |
|---|---|
| `hmacSHA512(key, data)` | Sinh chữ ký HMAC-SHA512 |
| `getIpAddress(request)` | Lấy IP thật qua `X-FORWARDED-FOR` |
| `buildQueryAndHash(params, secretKey)` | Sort → build query + hash |
| `verifySignature(params, secretKey)` | Verify IPN signature |

---

## 15. Exception Handling

### 15.1. GlobalExceptionHandler (`@RestControllerAdvice`) — 14 handlers

| Exception | HTTP Status | Khi nào |
|---|---|---|
| `BadRequestException` | 400 | Vi phạm business rule |
| `ResourceNotFoundException` | 404 | Entity không tồn tại |
| `InsufficientStockException` | 409 | Không đủ stock |
| `InvalidStatusTransitionException` | 409 | Chuyển trạng thái order không hợp lệ |
| `CategoryHasProductsException` | 409 | Xoá category còn sản phẩm |
| `DuplicateResourceException` | 409 | Trùng email/phone |
| `ObjectOptimisticLockingFailureException` | 409 | Race condition (@Version) |
| **`MaxUploadSizeExceededException`** | **400** | **★ MỚI — File vượt giới hạn upload** |
| `MethodArgumentNotValidException` | 400 | Validation lỗi → `Map<field, message>` |
| `DataIntegrityViolationException` | 409 | DB unique constraint |
| `BadCredentialsException` | 401 | Sai email/password |
| `DisabledException` | 403 | Account bị disable |
| `AccessDeniedException` | 403 | Thiếu quyền (role) |
| `InvalidRefreshTokenException` | 401 | Refresh token không hợp lệ |
| `Exception` (catch-all) | 500 | Lỗi không dự kiến |

---

## 16. Common — API Response Format

### ApiResponse\<T\>

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

### PageResponse\<T\> — ★ MỚI

```java
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PageResponse<T> of(Page<T> page) { ... }
}
```

> **Lưu ý:** `PageResponse` đã được tạo nhưng **chưa được dùng** trong controllers — controllers vẫn return `Page<T>` trực tiếp. Nên migrate sang `PageResponse` để response format thống nhất.

---

## 17. Business Flows (Luồng nghiệp vụ)

### 17.1. Order + Payment Flow — Không đổi

### 17.2. File Upload Flow — ★ MỚI

```
1. Client gửi multipart/form-data
2. Controller nhận file → gọi Service
3. Service gọi StorageService.uploadFile()
4. FileValidationUtil validate (null, size, magic bytes)
5. Thumbnailator resize (800x800, 0.85 quality)
6. MinioClient upload lên bucket
7. Trả về URL → set vào entity (imageUrl/avatarUrl) → save DB
```

### 17.3. Disable Account Flow — Không đổi

---

## 18. Coding Conventions & Patterns

### 18.1. Quy ước đặt tên — Không đổi

| Loại | Quy ước | Ví dụ |
|---|---|---|
| Package | lowercase, underscore | `mini_ecommerce` |
| Class | PascalCase | `StorageServiceImpl` |
| Method | camelCase | `uploadProductImage` |
| Enum constant | UPPER_CASE | `PENDING`, `SUCCESS` |
| URL path | kebab-case | `/vnpay-return`, `/me/avatar` |
| DB column | snake_case | `image_url`, `avatar_url` |

### 18.2. Patterns áp dụng

| Pattern | Áp dụng ở đâu |
|---|---|
| **Layered Architecture** | Controller → Service → Repository → Entity |
| **Builder** | Tất cả Entities + Response DTOs (Lombok `@Builder`) |
| **Record** | Request DTOs — immutable |
| **Service Interface + Impl** | Mọi Service đều có interface + impl |
| **Repository Projection** | `ProductRepository` — interface-based projections |
| **Optimistic Locking** | `@Version` trên `ProductEntity` + `OrderEntity` |
| **Scheduled Task** | `@Scheduled(fixedRate)` — auto-expire payments |
| **IDOR Prevention** | Order + Payment verify `order.user.id == tokenUserId` |
| **Multi-stage Docker Build** | ★ MỚI — Tách build & runtime |
| **Magic Bytes Validation** | ★ MỚI — Check file header thay vì tin Content-Type |
| **Object Storage (S3-compatible)** | ★ MỚI — MinIO cho file upload |

---

## 19. Trạng thái phát triển & TODO

### 19.1. Đã hoàn thành ✅

- [x] Layered Architecture chuẩn
- [x] CRUD đầy đủ: Category, Product, Order, User
- [x] JWT Authentication + Refresh Token (stateless)
- [x] Role-based Authorization (USER/ADMIN)
- [x] VNPay Payment Integration (sandbox)
- [x] Optimistic Locking (Product + Order)
- [x] Scheduled Job (auto-expire payments)
- [x] Flyway Database Migrations (7 migrations)
- [x] Swagger/OpenAPI documentation
- [x] Docker + Docker Compose
- [x] GitHub Actions CI
- [x] MinIO file upload + image resize
- [x] File validation (magic bytes)
- [x] Global Exception Handling (14 handlers)
- [x] Analytics APIs (top products, revenue by category, monthly revenue)
- [x] CORS configuration
- [x] PageResponse wrapper (created)

### 19.2. Chưa làm / Cần cải thiện ⚠️

- [ ] **Unit Tests** — Chưa có bất kỳ test nào
- [ ] **PageResponse chưa được sử dụng** — Controllers vẫn return `Page<T>`
- [ ] **BasePageRequest chưa được sử dụng** — Controllers vẫn nhận params riêng lẻ
- [ ] **TestUploadController** — Nên xoá hoặc protect khi deploy production
- [ ] **CORS** — Hardcode `localhost:8085`, cần config cho FE domain thực
- [ ] **.env.example thiếu** MinIO và VNPay vars
- [ ] **Cart (Giỏ hàng)** — Chưa có
- [ ] **Email notifications** — Config mail sẵn nhưng chưa dùng
- [ ] **Search nâng cao** — Chưa có filter theo price range, category
- [ ] **Pagination cho Order** — Hiện trả List, chưa có Page
- [ ] **Audit trail / Logging** — Chưa có structured logging

---

## 20. Góp ý tối ưu & Kiến thức cần bổ sung

### 20.1. 🔴 Ưu tiên CAO — Cần sửa trước khi lên FE

| # | Vấn đề | Giải pháp | Kiến thức cần học |
|---|---|---|---|
| 1 | **CORS chỉ allow localhost:8085** | Thêm FE domain (vd: `http://localhost:3000`) vào `allowedOrigins` hoặc dùng config từ `.env` | Spring CORS |
| 2 | **PageResponse đã tạo nhưng chưa dùng** | Migrate tất cả controller trả `Page<T>` sang `PageResponse<T>` → FE nhận format thống nhất | — |
| 3 | **BasePageRequest chưa dùng** | Refactor controllers dùng `BasePageRequest` thay vì 4 `@RequestParam` riêng lẻ | — |
| 4 | **GET /products/{id} yêu cầu Auth** | Nên public để guest xem chi tiết sản phẩm — hiện `PUBLIC_GET_URLS` đã có nhưng controller không có `@SecurityRequirements({})` | — |
| 5 | **TestUploadController public** | Thêm `@PreAuthorize("hasRole('ADMIN')")` hoặc xoá trước production | — |

### 20.2. 🟡 Ưu tiên TRUNG BÌNH — Nâng chất lượng code

| # | Vấn đề | Giải pháp | Kiến thức cần học |
|---|---|---|---|
| 6 | **Chưa có Unit Test** | Viết test cho Service layer (JUnit 5 + Mockito) | JUnit 5, Mockito, `@WebMvcTest`, `@DataJpaTest` |
| 7 | **Manual mapping DTO ↔ Entity** | Dùng MapStruct để auto-generate mapper | MapStruct |
| 8 | **Không có validation trùng phone khi updateProfile** | Check phone unique trước khi save | — |
| 9 | **Order cancelled nhưng stock chưa hoàn (qua scheduled job)** | Khi `expiredPayment()` cancel order → cần restore stock cho từng item | — |
| 10 | **`decreaseStock` throws `IllegalArgumentException`** | Đổi sang `BadRequestException` cho consistent | — |
| 11 | **`CategoryServiceImpl.create` throws `ResourceNotFoundException` khi trùng tên** | Nên throw `DuplicateResourceException` | — |
| 12 | **Retry cho Optimistic Locking** | Thêm `@Retryable` (Spring Retry) khi gặp `ObjectOptimisticLockingFailureException` | Spring Retry |

### 20.3. 🟢 Ưu tiên THẤP — Nâng cấp sau

| # | Vấn đề | Giải pháp | Kiến thức cần học |
|---|---|---|---|
| 13 | **Chưa có Cart** | Thiết kế `CartEntity` + `CartItemEntity` hoặc dùng Redis session | Redis, Cart design |
| 14 | **Chưa có Email notification** | Dùng `JavaMailSender` đã config → gửi mail khi register, order confirmed | Spring Mail, Thymeleaf template |
| 15 | **Enums inline trong Entity** | Tách ra package `enums/` để reuse và clean code | — |
| 16 | **Chưa có Caching** | Thêm Redis cache cho product list, category list | Spring Cache, Redis |
| 17 | **Chưa có Rate Limiting** | Thêm Bucket4j hoặc Resilience4j để chống spam API | Rate Limiting |
| 18 | **Logging chưa structured** | Dùng MDC + JSON formatter cho log → dễ parse khi deploy | SLF4J MDC, Logback JSON |
| 19 | **Chưa có API Versioning strategy** | Hiện dùng `/api/v1/` — cần plan khi có breaking changes | API Versioning |
| 20 | **Chưa có Soft Delete cho Order** | Hiện chỉ có CANCELLED status, không có deleted_at | Soft Delete pattern |
| 21 | **uploadProductImage/uploadUserImage chưa xoá ảnh cũ** | Khi upload ảnh mới, ảnh cũ trên MinIO vẫn tồn tại → cần delete | MinIO `removeObject()` |
| 22 | **`BasePageRequest.toPageable()` dùng `sortBy` thay vì `direction`** | Bug: `"desc".equalsIgnoreCase(sortBy)` → phải là `direction` | — |

### 20.4. 📚 Kiến thức kỹ năng nên học thêm (theo thứ tự ưu tiên)

| # | Chủ đề | Lý do | Tài liệu gợi ý |
|---|---|---|---|
| 1 | **JUnit 5 + Mockito** | Bắt buộc cho portfolio — nhà tuyển dụng sẽ hỏi | Baeldung JUnit 5 series |
| 2 | **MapStruct** | Giảm boilerplate mapping code đáng kể | mapstruct.org |
| 3 | **Spring Cache + Redis** | Performance optimization phổ biến nhất | Baeldung Spring Cache |
| 4 | **Docker Compose cho dev stack đầy đủ** | Thêm MinIO, Redis vào compose | Docker docs |
| 5 | **Spring Retry** | Xử lý transient failures (optimistic lock, network) | Spring Retry guide |
| 6 | **API Documentation nâng cao** | `@Schema`, `@ApiResponse` annotations cho Swagger đẹp hơn | Springdoc docs |
| 7 | **Integration Testing** | `@SpringBootTest` + Testcontainers (PostgreSQL) | Testcontainers.org |
| 8 | **Monitoring** | Spring Actuator + Prometheus + Grafana | Baeldung Actuator |

---

> **🎯 Kết luận:** Project Mini Ecommerce đã có nền tảng backend vững chắc với **35 API endpoints**, **7 bảng database**, **7 Flyway migrations**, JWT auth đầy đủ, VNPay payment, MinIO file upload, Docker containerization, và CI pipeline. Giai đoạn tiếp theo nên tập trung vào: **(1)** Fix CORS cho FE, **(2)** Thống nhất response format (PageResponse), **(3)** Viết Unit Tests, và **(4)** Bắt đầu lên FE.
