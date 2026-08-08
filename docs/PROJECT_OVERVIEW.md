# 📖 PROJECT OVERVIEW V7 — Mini Ecommerce (Current State & FE Integration Edition)

> **Mục đích tài liệu:** Đây là tài liệu **CHÍNH XÁC NHẤT** phản ánh trạng thái codebase thực tế tính đến ngày cập nhật. Bất kỳ AI Agent hay Developer nào (bao gồm Backend, Frontend/Full-stack) khi bắt đầu hoặc tiếp tục công việc trên project này đều phải đọc file này **ĐẦU TIÊN**.
>
> **Cập nhật lần cuối:** 2026-07-28
>
> **Supersedes:** PROJECT_OVERVIEW_V2.md, PROJECT_OVERVIEW_V3.md, PROJECT_OVERVIEW_V4.md, PROJECT_OVERVIEW_V5.md, PROJECT_OVERVIEW_V6.md (các bản cũ đã bị xoá/thay thế)

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
20. [Góp ý tối ưu & Bảng theo dõi cải tiến](#20-góp-ý-tối-ưu--bảng-theo-dõi-cải-tiến)
21. [Tổng hợp Kiến thức & Kỹ thuật chức năng cốt lõi](#21-tổng-hợp-kiến-thức--kỹ-thuật-chức-năng-cốt-lõi)
22. [Hướng dẫn & Lưu ý Dành Cho Frontend (FE / Full-stack & AI Reader)](#22-hướng-dẫn--lưu-ý-dành-cho-frontend-fe--full-stack--ai-reader)

---

## 1. Tổng quan dự án

| Thuộc tính                  | Giá trị                                                                                                                                       |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| **Tên dự án**               | Mini Ecommerce — Bán Hải Sản Tươi Sống & Chế Biến (Seafood)                                                                                   |
| **Group ID**                | `com.devfat`                                                                                                                                  |
| **Artifact ID**             | `mini-ecommerce`                                                                                                                              |
| **Version**                 | `0.0.1-SNAPSHOT`                                                                                                                              |
| **Main class**              | `MiniEcommerceApplication`                                                                                                                    |
| **Base package**            | `com.devfat.mini_ecommerce`                                                                                                                   |
| **Miền nghiệp vụ (Domain)** | **E-commerce Bán Hải Sản (Seafood)** — Tươi sống, phile, chế biến sẵn & gia vị                                                                |
| **Mục tiêu**                | Side project cá nhân — rèn luyện Java Backend, áp dụng best practices thực tế, chuẩn bị kết nối Frontend (React/Next.js/Vue) và làm portfolio |
| **Kiến trúc**               | Layered Architecture (Controller → Service → Repository → Entity)                                                                             |
| **API Style**               | RESTful JSON API, Stateless (JWT, không session)                                                                                              |
| **Trạng thái hiện tại**     | Hoàn thiện hệ thống **Xác thực OTP Email**, chuẩn bị **Frontend Integration** & **Production Deployment** lên Render                          |

### 1.1. Điểm thay đổi & bổ sung mới nhất (V6 → V7, tính đến 2026-07-28)

| Module / Tính năng                                            | Trạng thái V6 (2026-07-26)                       | Trạng thái V7 Hiện tại (2026-07-28)                                                                                                                                                                                                                                                           |
| ------------------------------------------------------------- | ------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Xác thực Tài Khoản qua OTP Email (Account Verification)**   | ❌ Chưa có — Đăng ký là tài khoản kích hoạt ngay | ✅ **MỚI** — Luồng Đăng ký tạo user với `email_verified = false`, `is_active = false`. Gửi mã OTP 6 chữ số qua email. Client cần gọi `/api/v1/auth/verify-otp` để kích hoạt tài khoản (`is_active = true`, `email_verified = true`) và nhận ngay JWT tokens (`accessToken` + `refreshToken`). |
| **Quên mật khẩu & Đặt lại (Forgot & Reset Password via OTP)** | ❌ Chưa có                                       | ✅ **MỚI** — Endpoints `/forgot-password`, `/verify-otp`, `/resend-otp`. Khi xác thực OTP quên mật khẩu thành công, hệ thống trả về `actionToken` để client thực hiện đổi mật khẩu an toàn.                                                                                                   |
| **Bảng & Schema OTP (`otp_verifications`)**                   | ❌ 2 migrations (V1, V2)                         | ✅ **MỚI** — Thêm `V3__add_otp_verifications_table.sql`: Tạo cột `email_verified` trong `users` và bảng `otp_verifications` (FK `user_id`, `otp_hash`, `purpose`, `expires_at`, `attempts`, `consumed`). Index `idx_otp_user_purpose`.                                                        |
| **OTP Cleanup Scheduler**                                     | ❌ Chưa có                                       | ✅ **MỚI** — `OtpCleanupScheduler` với `@Scheduled(cron = "0 0 * * * *")` tự động dọn dẹp các OTP đã dùng hoặc hết hạn hàng giờ.                                                                                                                                                              |
| **HTML Email Templates & Helper**                             | Plain text email                                 | ✅ **NÂNG CẤP** — `EmailTemplateHelper` + HTML Email Template (`SendEmailTemplate.html`) thiết kế chuẩn UI thương hiệu Hải Sản (màu xanh tươi mát, nút bấm rõ ràng, mã OTP nổi bật).                                                                                                          |
| **Tách biệt Enums (`com.devfat.mini_ecommerce.enums`)**       | Enums nằm inline trong Entity                    | ✅ **REFACTORED** — Tách riêng các Enums thành file độc lập trong package `enums`: `Role`, `OrderStatus`, `OtpPurpose`.                                                                                                                                                                       |
| **Exception Handling cho Auth/OTP**                           | 15 Exception Handlers                            | ✅ **TĂNG LÊN 22 HANDLERS** — Thêm 7 custom OTP exceptions (`OtpInvalidException`, `OtpExpiredException`, `OtpAttemptsExceededException`, `ResendCooldownException`, `OtpNotFoundException`, `AccountNotVerifiedException`, `InvalidActionTokenException`).                                   |
| **Tổng số Endpoints REST API**                                | 35 Endpoints                                     | ✅ **39 ENDPOINTS** — Thêm 4 endpoints chuyên biệt cho xác thực OTP & Quên mật khẩu (`verify-otp`, `forgot-password`, `resend-otp`).                                                                                                                                                          |
| **Tài liệu Hướng dẫn Frontend**                               | ❌ Chưa có                                       | ✅ **MỚI (MỤC 22)** — Thêm toàn bộ phần hướng dẫn chi tiết về API Response, Pagination, Interceptor 401, Auth Flow Specs dành cho Frontend / Full-stack / AI Developers.                                                                                                                      |

---

### 1.2. Miền nghiệp vụ & Mô hình Sản phẩm Hải Sản (Seafood E-commerce Domain Model)

Dự án **Mini Ecommerce** được định hình là một hệ thống **Thương mại Điện tử chuyên doanh Hải Sản Tươi Sống & Chế Biến (Seafood E-commerce System)**. Hệ thống phục vụ đa dạng phân khúc khách hàng từ hộ gia đình, dân văn phòng đến các buổi tiệc nhậu/BBQ cuối tuần với 9 nhóm danh mục cốt lõi (theo migration `V2__seed_seafood_categories_and_products.sql`):

1. 🐟 **Cá biển tươi & Phile:** Cá thu tươi 1kg, Cá bớp phile không xương, Cá hồi Nauy phile tươi cut, Cá chẽm tươi nguyên con.
2. 🦐 **Tôm tươi sống & Cao cấp:** Tôm sú biển size lớn (20-30 con/kg), Tôm thẻ chân trắng, Tôm càng xanh loại 1, Tôm hùm baby Alaska nhập khẩu.
3. 🦑 **Mực & Bạch tuộc tươi:** Mực ống loại 1 thân dày, Mực lá thịt giòn ngọt, Mực trứng non, Bạch tuộc tươi làm sạch sẵn.
4. 🦀 **Cua & Ghẹ chắc thịt:** Cua biển gạch son Cà Mau, Ghẹ xanh Phú Quốc tự nhiên, Cua thịt chắc, Ghẹ ba chấm size lớn.
5. 🐚 **Ốc & Nghêu Sò đầm phá:** Ốc hương tươi giòn thơm, Nghêu trắng, Sò huyết đầm phá thịt đỏ, Cồi sò điệp Nhật nhập khẩu.
6. 🍱 **Set hải sản văn phòng tiện lợi:** Set hải sản 1 người ăn hấp sẵn, Set cơm văn phòng hải sản, Set salad eat-clean, Set lẩu hải sản mini 1-2 người.
7. 🍻 **Set hải sản nhậu & Tiệc gia đình:** Set nhậu 4-6 người, Set nướng hải sản BBQ kèm sốt, Set hải sản hấp sả bia, Combo 8 món tiệc cuối tuần.
8. 🦑 **Hải sản khô & Một nắng:** Tôm khô loại 1 màu đỏ tự nhiên, Mực khô nguyên con phơi nắng, Cá cơm khô, Cá thu một nắng.
9. 🧂 **Nước mắm & Gia vị chấm chuyên dụng:** Nước mắm nhĩ Phú Quốc 40 độ đạm, Nước mắm cá cơm Phan Thiết, Muối tiêu chanh chấm hải sản hấp, Sốt me chua ngọt chấm nướng.

---

## 2. Tech Stack & Dependencies

### 2.1. Core

| Công nghệ       | Phiên bản             | Vai trò                                                                                          |
| --------------- | --------------------- | ------------------------------------------------------------------------------------------------ |
| **Java**        | 21                    | Ngôn ngữ chính                                                                                   |
| **Spring Boot** | 3.5.16                | Framework core (`@SpringBootApplication`, `@EnableScheduling`, `@EnableAsync`, `@EnableCaching`) |
| **Maven**       | (wrapper mvnw)        | Build tool                                                                                       |
| **Docker**      | Multi-stage           | Containerization                                                                                 |
| **PostgreSQL**  | 16 (Docker image)     | Database chính                                                                                   |
| **Redis**       | latest (Docker image) | In-memory Data Store cho Caching & Performance                                                   |

### 2.2. Dependencies (pom.xml) — Đầy đủ

| Dependency                            | Phiên bản  | Mục đích                                                           | Scope             |
| ------------------------------------- | ---------- | ------------------------------------------------------------------ | ----------------- |
| `spring-boot-starter-web`             | (managed)  | REST API, Tomcat embedded                                          | compile           |
| `spring-boot-starter-data-jpa`        | (managed)  | ORM (Hibernate), JpaRepository, Projections                        | compile           |
| `spring-boot-starter-validation`      | (managed)  | Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`...)    | compile           |
| `spring-boot-starter-security`        | (managed)  | Spring Security framework                                          | compile           |
| `spring-boot-starter-mail`            | (managed)  | Gửi Email (JavaMailSender) — profile `dev`                         | compile           |
| `spring-boot-starter-cache`           | (managed)  | Spring Cache Abstraction                                           | compile           |
| `spring-boot-starter-data-redis`      | (managed)  | Spring Data Redis Connector & Serializers                          | compile           |
| `spring-boot-devtools`                | (managed)  | Hot reload khi dev                                                 | runtime, optional |
| `postgresql`                          | (managed)  | PostgreSQL JDBC driver                                             | runtime           |
| `flyway-core`                         | (managed)  | Database migration versioning                                      | compile           |
| `flyway-database-postgresql`          | (managed)  | Flyway PostgreSQL adapter                                          | compile           |
| `springdoc-openapi-starter-webmvc-ui` | **2.8.17** | Swagger UI + OpenAPI 3 docs (`/swagger-ui.html`)                   | compile           |
| `jjwt-api`                            | **0.12.6** | JWT API                                                            | compile           |
| `jjwt-impl`                           | **0.12.6** | JWT implementation                                                 | runtime           |
| `jjwt-jackson`                        | **0.12.6** | JWT JSON serialization                                             | runtime           |
| `lombok`                              | (managed)  | Giảm boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`) | compile, optional |
| `spring-dotenv`                       | **4.0.0**  | Tự động load file `.env` vào Spring properties                     | compile           |
| `minio`                               | **8.5.17** | MinIO Java SDK (S3-compatible object storage)                      | compile           |
| `thumbnailator`                       | **0.4.20** | Resize/compress ảnh trước khi upload                               | compile           |
| `spring-boot-starter-test`            | (managed)  | Unit/Integration testing                                           | test              |

> **Lưu ý:** Dự án **KHÔNG** dùng VNPay SDK bên thứ 3. VNPay được tích hợp trực tiếp bằng thuật toán HMAC-SHA512 (`javax.crypto.Mac`).

### 2.3. Build Plugins (3 Plugins)

| Plugin                     | Phiên bản  | Vai trò                                                                 |
| -------------------------- | ---------- | ----------------------------------------------------------------------- |
| `spring-boot-maven-plugin` | (managed)  | Build executable JAR, exclude Lombok                                    |
| `maven-compiler-plugin`    | (managed)  | Config annotation processor paths cho Lombok ở cả compile + testCompile |
| `maven-pmd-plugin`         | **3.26.0** | Static Analysis, sử dụng custom `pmd-ruleset.xml`                       |

---

## 3. DevOps & Containerization

### 3.1. Dockerfile (Multi-stage build)

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3.2. docker-compose.yml (4 Services)

| Service   | Image                 | Port                     | Healthcheck / Depends                                             |
| --------- | --------------------- | ------------------------ | ----------------------------------------------------------------- |
| **db**    | `postgres:16`         | `5432:5432`              | `pg_isready -U ${DB_USERNAME}` mỗi 5s                             |
| **redis** | `redis:latest`        | `6379:6379`              | —                                                                 |
| **minio** | `quay.io/minio/minio` | `9000:9000`, `9001:9001` | —                                                                 |
| **app**   | Build từ Dockerfile   | `8085:8085`              | depends_on: db (service_healthy), redis & minio (service_started) |

---

## 4. Cấu hình (Configuration)

### 4.1. application.yaml (file chính — Shared cả dev & prod)

| Nhóm cấu hình  | Key                                           | Giá trị / Mô tả                                 |
| -------------- | --------------------------------------------- | ----------------------------------------------- |
| **Profile**    | `spring.profiles.active`                      | `${SPRING_PROFILES_ACTIVE:dev}`                 |
| **Multipart**  | `spring.servlet.multipart.max-file-size`      | `5MB`                                           |
|                | `spring.servlet.multipart.max-request-size`   | `5MB`                                           |
| **Redis**      | `spring.data.redis.host`                      | `${REDIS_HOST:localhost}`                       |
|                | `spring.data.redis.port`                      | `${REDIS_PORT:6379}`                            |
|                | `spring.data.redis.password`                  | `${REDIS_PASSWORD:}`                            |
| **Database**   | `spring.datasource.url`                       | `${DB_URL}`                                     |
|                | `spring.datasource.driver-class-name`         | `org.postgresql.Driver`                         |
|                | `spring.datasource.hikari.maximum-pool-size`  | `10`                                            |
|                | `spring.datasource.hikari.connection-timeout` | `30000`                                         |
| **JPA**        | `spring.jpa.database`                         | `postgresql`                                    |
|                | `spring.jpa.open-in-view`                     | `false`                                         |
|                | `spring.jpa.hibernate.ddl-auto`               | `validate` — Flyway là nguồn sự thật duy nhất   |
| **Flyway**     | `spring.flyway.enabled`                       | `true`                                          |
|                | `spring.flyway.locations`                     | `classpath:db/migration`                        |
|                | `spring.flyway.baseline-on-migrate`           | `true`                                          |
|                | `spring.flyway.create-schemas`                | `false`                                         |
| **Server**     | `server.port`                                 | `${PORT:8085}` — Dynamic cho Render             |
| **CORS**       | `app.cors.allowed-origins`                    | `${CORS_ALLOWED_ORIGINS:http://localhost:8085}` |
| **JWT**        | `app.jwt.secret-key`                          | `${JWT_SECRET_KEY}`                             |
|                | `app.jwt.expiration`                          | `3600000` (1 giờ, ms)                           |
|                | `app.jwt.refresh-expiration-days`             | `7` (7 ngày)                                    |
| **Upload**     | `app.upload.max-image-size`                   | `5MB`                                           |
| **MinIO**      | `app.minio.endpoint`                          | `${MINIO_ENDPOINT:http://localhost:9000}`       |
|                | `app.minio.access-key`                        | `${MIN_IO_ACCESS_KEY}`                          |
|                | `app.minio.secret-key`                        | `${MIN_IO_SECRET_KEY}`                          |
|                | `app.minio.bucket`                            | `${MIN_IO_BUCKET}`                              |
| **Brevo Mail** | `app.mail.brevo.api-key`                      | `${BREVO_API_KEY:}`                             |
|                | `app.mail.brevo.sender-email`                 | `${BREVO_SENDER_EMAIL:}`                        |
| **VNPay**      | `vnpay.pay-url`                               | `${VNPAY_PAY_URL:sandbox URL}`                  |
|                | `vnpay.tmn-code`                              | `${VNPAY_TMN_CODE}`                             |
|                | `vnpay.secret-key`                            | `${VNPAY_SECRET_KEY}`                           |
|                | `vnpay.return-url`                            | `${VNPAY_RETURN_URL}`                           |
|                | `vnpay.ipn-url`                               | `${VNPAY_IPN_URL}`                              |
| **Swagger**    | `springdoc.api-docs.path`                     | `/v1/api-docs`                                  |
|                | `springdoc.swagger-ui.path`                   | `/swagger-ui.html`                              |
|                | `springdoc.swagger-ui.tags-sorter`            | `alpha`                                         |
|                | `springdoc.swagger-ui.operations-sorter`      | `alpha`                                         |

---

## 5. Cấu trúc thư mục chi tiết

```
mini-ecommerce/
├── pom.xml                                        # Maven build config (+ PMD plugin 3.26.0)
├── pmd-ruleset.xml                                # Custom PMD static analysis rules
├── mvnw / mvnw.cmd                                # Maven wrapper
├── Dockerfile                                     # Multi-stage build (JDK-alpine → JRE-alpine)
├── docker-compose.yml                             # 4 services: db + app + redis + minio
├── .env                                           # ⚠️ KHÔNG commit — secrets Development
├── .env.production                                # ⚠️ KHÔNG commit — secrets Production (Render)
├── .env.example                                   # Template cho developer mới (local & production)
├── .github/
│   └── workflows/
│       └── ci.yml                                 # PMD → Test → Build CI Pipeline
├── src/
│   ├── main/
│   │   ├── java/com/devfat/mini_ecommerce/
│   │   │   ├── MiniEcommerceApplication.java      # Entry point (@SpringBootApplication @EnableScheduling)
│   │   │   │
│   │   │   ├── common/                            # Shared response wrappers (2 files)
│   │   │   │   ├── ApiResponse.java               # Generic API response <T> + success/error factories
│   │   │   │   └── PageResponse.java              # Standardized pagination response
│   │   │   │
│   │   │   ├── config/                            # Spring Configuration beans (9 files)
│   │   │   │   ├── SecurityConfig.java            # Filter chain, CORS externalized, BCrypt, AuthManager
│   │   │   │   ├── OpenApiConfig.java             # Swagger UI + Bearer Auth scheme
│   │   │   │   ├── VNPayConfig.java               # @ConfigurationProperties(prefix="vnpay")
│   │   │   │   ├── VNPayUtil.java                 # HMAC-SHA512 checksum & query string generator
│   │   │   │   ├── MinioConfig.java               # MinioClient bean
│   │   │   │   ├── AsyncConfig.java               # @EnableAsync + emailTaskExecutor ThreadPool
│   │   │   │   ├── RedisCacheConfig.java          # @EnableCaching + RedisCacheManager custom TTLs
│   │   │   │   ├── WebConfig.java                 # CustomPageableArgumentResolver registration
│   │   │   │   └── FlywayConfig.java              # FlywayMigrationStrategy: repair() → migrate()
│   │   │   │
│   │   │   ├── controller/                        # REST Controllers (8 controllers — 39 Endpoints)
│   │   │   │   ├── AuthController.java            # /api/v1/auth/** (8 endpoints) — Auth & OTP Flow
│   │   │   │   ├── CategoryController.java        # /api/v1/categories/** (5 endpoints) — Trả PageResponse
│   │   │   │   ├── ProductController.java         # /api/v1/products/** (11 endpoints) — Trả PageResponse
│   │   │   │   ├── OrderController.java           # /api/v1/orders/** (4 endpoints)
│   │   │   │   ├── UserController.java            # /api/v1/users/** (6 endpoints) — Trả PageResponse
│   │   │   │   ├── PaymentController.java         # /api/v1/payments/** (3 endpoints)
│   │   │   │   ├── PingController.java            # /api/v1/health (1 endpoint)
│   │   │   │   └── TestUploadController.java      # /api/v1/test/upload (1 endpoint test MinIO)
│   │   │   │
│   │   │   ├── docs/                              # Tài liệu dự án (7 files)
│   │   │   │   ├── PROJECT_OVERVIEW.md            # ★ TÀI LIỆU NÀY (V7)
│   │   │   │   ├── plan_tong_hop.md               # Kế hoạch tổng hợp các phase
│   │   │   │   ├── plan_uu_tien_hoc_tap.md        # Roadmap học tập ưu tiên
│   │   │   │   ├── auth_security_jwt_plan.md      # Plan JWT chi tiết
│   │   │   │   ├── account_verification_otp_plan.md # Plan OTP Verification chi tiết
│   │   │   │   ├── deploy_render_plan.md          # Kế hoạch deploy lên Render
│   │   │   │   └── docker-commands-cheatsheet.md  # Tham khảo Docker CLI
│   │   │   │
│   │   │   ├── shared/                            # 🛠️ Hạ tầng dùng chung (Open Module)
│   │   │   │   ├── PingController.java            # /api/v1/health
│   │   │   │   ├── package-info.java              # @ApplicationModule(type = OPEN)
│   │   │   │   ├── base/                          # BaseEntity, ApiResponse, PageResponse, BasePageRequest
│   │   │   │   ├── config/                        # SecurityConfig, FlywayConfig, WebConfig, AsyncConfig...
│   │   │   │   ├── exception/                     # BusinessException (Base), GlobalExceptionHandler, BadRequestException...
│   │   │   │   ├── security/                      # JwtProvider, JwtFilter, UserPrincipal (Security DTO)
│   │   │   │   └── util/                          # CustomPageableArgumentResolver
│   │   │   │
│   │   │   ├── auth/                              # 🔑 Module Authentication & OTP
│   │   │   │   ├── AuthController.java, AuthService.java, OtpService.java, OtpPurpose.java
│   │   │   │   ├── dto/                           # LoginRequestDto, RegisterRequestDto, AuthResponseDto...
│   │   │   │   ├── exception/                     # InvalidOtpException, OtpExpiredException, OtpNotFoundException...
│   │   │   │   └── internal/                      # AuthServiceImpl, OtpServiceImpl, RefreshTokenEntity, RefreshTokenRepository, RefreshTokenGenerator...
│   │   │   │
│   │   │   ├── category/                          # 🗂️ Module Category
│   │   │   │   ├── CategoryController.java, CategoryService.java, CategoryResponseDto...
│   │   │   │   ├── exception/                     # CategoryHasProductsException
│   │   │   │   └── internal/                      # CategoryServiceImpl, CategoryEntity, CategoryRepository, CategoryMapper
│   │   │   │
│   │   │   ├── product/                           # 📦 Module Product
│   │   │   │   ├── ProductController.java, ProductService.java, ProductResponseDto...
│   │   │   │   ├── exception/                     # InsufficientStockException, OptimisticLockException
│   │   │   │   └── internal/                      # ProductServiceImpl, ProductEntity, ProductRepository, ProductMapper
│   │   │   │
│   │   │   ├── order/                             # 🛒 Module Order
│   │   │   │   ├── OrderController.java, OrderService.java, OrderStatus, OrderResponseDto...
│   │   │   │   ├── exception/                     # InvalidStatusTransitionException
│   │   │   │   └── internal/                      # OrderServiceImpl, OrderEntity, OrderItemEntity, OrderRepository...
│   │   │   │
│   │   │   ├── payment/                           # 💳 Module Payment (VNPAY)
│   │   │   │   ├── PaymentController.java, PaymentService.java, PaymentStatus, PaymentMethod, PaymentProvider
│   │   │   │   └── internal/                      # PaymentServiceImpl, PaymentEntity, PaymentRepository, VNPayConfig...
│   │   │   │
│   │   │   ├── user/                              # 👤 Module User & Profile
│   │   │   │   ├── UserController.java, UserService.java, Role, UserResponseDto...
│   │   │   │   └── internal/                      # UserServiceImpl, UserEntity, CustomUserDetailsService...
│   │   │   │
│   │   │   ├── storage/                           # 💾 Module Storage (MinIO)
│   │   │   │   ├── StorageService.java
│   │   │   │   └── internal/                      # StorageServiceImpl, MinioConfig, FileValidationUtil, TestUploadController
│   │   │   │
│   │   │   └── notification/                      # 📧 Module Notification (Brevo / SMTP)
│   │   │       ├── EmailService.java, EmailRequestDto
│   │   │       └── internal/                      # EmailServiceImpl, MailTransport, BrevoMailTransport...
│   │   │
│   │   └── resources/
│   │       ├── application.yaml                   # Shared config
│   │       ├── application-dev.yaml               # Dev profile: SMTP mail, debug log
│   │       ├── application-prod.yaml              # Prod profile: Brevo mail, SQL log OFF
│   │       ├── db/migration/                      # 2 Flyway Migrations tối ưu
│   │       │   ├── V1__init_mini_shop.sql         # Core Schema hợp nhất (8 bảng: users, categories, products, orders, order_items, refresh_tokens, payments, otp_verifications)
│   │       │   └── V2__seed_seafood_categories_and_products.sql # 9 categories & 36 hải sản seed data
│   │       └── templates/
│   │           └── email/
│   │               └── SendEmailTemplate.html    # HTML Email Template gửi mã OTP & Đơn hàng
```

---

## 6. Database Schema & Migrations

### 6.1. Lịch sử Flyway Migrations (2 migrations tối ưu)

| File                                           | Nội dung                                                                                                                                                                                                                                                         |
| ---------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `V1__init_mini_shop.sql`                       | **Core Schema hợp nhất** — Tạo 8 bảng core (`users`, `categories`, `products`, `orders`, `order_items`, `refresh_tokens`, `payments`, `otp_verifications`) + full `created_at`/`updated_at` + index. Tất cả PK/FK là `BIGINT`, có `version` cho Optimistic Lock. |
| `V2__seed_seafood_categories_and_products.sql` | **Data Seed Hải Sản** — Seed 9 danh mục hải sản & 36 sản phẩm thực tế kèm giá, stock, mô tả chi tiết và đầy đủ timestamp.                                                                                                                                        |

### 6.2. Schema tổng quan (8 bảng)

```
users ───────────┬───────── orders ──── order_items ──── products ──── categories
                 ├───────── refresh_tokens
                 ├───────── otp_verifications (★ MỚI)
                 └───────── payments ──── orders
```

#### Chi tiết bảng `otp_verifications` (★ MỚI)

```sql
CREATE TABLE otp_verifications (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id),
    otp_hash     VARCHAR(255) NOT NULL,
    purpose      VARCHAR(150) NOT NULL CHECK (purpose IN ('REGISTER_VERIFICATION', 'RESET_PASSWORD', 'CHANGE_PASSWORD_CONFIRMATION')),
    expires_at   TIMESTAMP NOT NULL,
    attempts     INTEGER NOT NULL DEFAULT 0,
    consumed     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_user_purpose ON otp_verifications(user_id, purpose);
```

---

## 7. Entity Layer — Chi tiết từng Entity

### 7.1. UserEntity (`users`) — ★ CẬP NHẬT

| Field                     | Type            | JPA Annotation                                         | Ghi chú                                                         |
| ------------------------- | --------------- | ------------------------------------------------------ | --------------------------------------------------------------- |
| `id`                      | `Long`          | `@Id @GeneratedValue(IDENTITY)`                        | PK                                                              |
| `fullName`                | `String`        | `@Column(name="full_name", length=100)`                |                                                                 |
| `email`                   | `String`        | `@Column(unique=true, length=150)`                     | Username đăng nhập                                              |
| `avatarUrl`               | `String`        | `@Column(name="avatar_url", length=500)`               | URL avatar                                                      |
| `phoneNumber`             | `String`        | `@Column(name="phone_number", unique=true, length=15)` |                                                                 |
| `password`                | `String`        | `@Column(nullable=false)`                              | BCrypt hash                                                     |
| `role`                    | `Role` enum     | `@Enumerated(STRING)`                                  | Enum package: `USER`, `ADMIN`                                   |
| `isActive`                | `boolean`       | `@Column(name="is_active")`                            | Trạng thái hoạt động account (mặc định `false` khi mới đăng ký) |
| **`emailVerified`**       | `boolean`       | `@Column(name="email_verified")`                       | **★ MỚI** — Trạng thái đã xác thực email qua OTP                |
| `createdAt` / `updatedAt` | `LocalDateTime` | `@CreationTimestamp` / `@UpdateTimestamp`              |                                                                 |

### 7.2. OtpVerificationEntity (`otp_verifications`) — ★ MỚI

| Field       | Type              | JPA Annotation / Ghi chú                                                                          |
| ----------- | ----------------- | ------------------------------------------------------------------------------------------------- |
| `id`        | `Long`            | `@Id @GeneratedValue(IDENTITY)`                                                                   |
| `user`      | `UserEntity`      | `@ManyToOne(LAZY)`, FK: `user_id`                                                                 |
| `otpHash`   | `String`          | `@Column(name="otp_hash", nullable=false)` — BCrypt Hash mã OTP 6 chữ số                          |
| `purpose`   | `OtpPurpose` enum | `@Enumerated(STRING)` — `REGISTER_VERIFICATION`, `RESET_PASSWORD`, `CHANGE_PASSWORD_CONFIRMATION` |
| `expiresAt` | `LocalDateTime`   | Thới gian hết hạn (mặc định 5 phút)                                                               |
| `attempts`  | `int`             | Số lần nhập sai (max 5 lần)                                                                       |
| `consumed`  | `boolean`         | Mã đã được sử dụng hay chưa                                                                       |
| `createdAt` | `LocalDateTime`   | Thời điểm tạo OTP                                                                                 |

---

## 8. Repository Layer — Data Access

### 8.5. OtpVerificationRepository (★ MỚI)

| Method                                                                            | Mô tả                                                               |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------------- |
| `findTopByUserIdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(Long, OtpPurpose)` | Tìm bản ghi OTP mới nhất chưa sử dụng của User theo mục đích        |
| `deleteByExpiresAtBeforeOrConsumedTrue(LocalDateTime)`                            | Xoá tất cả OTP đã hết hạn hoặc đã được sử dụng (dùng bởi Scheduler) |

---

## 9. DTO Layer — Request & Response

### 9.1. Request DTOs Mới Cho Auth & OTP Flow (★ MỚI)

- **`VerifyOtpRequestDto` (Record)**: `@NotBlank @Email String email`, `@NotBlank @Pattern(regexp = "\\d{6}") String otpCode`, `@NotNull OtpPurpose purpose`.
- **`ForgotPasswordRequestDto` (Record)**: `@NotBlank @Email String email`.
- **`ResendOtpRequestDto` (Record)**: `@NotBlank @Email String email`, `@NotNull OtpPurpose purpose`.

### 9.2. Response DTOs Mới Cho Auth & OTP Flow (★ MỚI)

- **`VerifyOtpResponseDto` (Record)**:
  - `String accessToken` (trả về khi purpose = `REGISTER_VERIFICATION`)
  - `String refreshToken` (trả về khi purpose = `REGISTER_VERIFICATION`)
  - `String actionToken` (trả về khi purpose = `RESET_PASSWORD` hoặc `CHANGE_PASSWORD_CONFIRMATION`)
- **`ResendOtpResponseDto` (Record)**: `String message` ("Gửi lại mã OTP thành công").

---

## 10. Service Layer — Business Logic

### 10.6. OtpService / OtpServiceImpl — ★ MỚI

- **`generateAndSendOtp(user, purpose)`**:
  - Kiểm tra resend cooldown (60 giây kể từ OTP gần nhất). Throws `ResendCooldownException` nếu chưa đủ 60s.
  - Tạo ngẫu nhiên 6 chữ số (`SecureRandom`), hash bằng BCrypt trước khi lưu DB.
  - Đặt thới gian hết hạn (5 phút).
  - Gọi bất đồng bộ `EmailService` gửi email HTML có mã OTP cho user.
- **`verifyOtp(email, otpCode, purpose)`**:
  - Tìm OTP mới nhất theo `user_id` và `purpose`. Throws `OtpNotFoundException` nếu không tìm thấy.
  - Kiểm tra hết hạn. Throws `OtpExpiredException` nếu đã hết 5 phút.
  - Kiểm tra `attempts >= 5`. Throws `OtpAttemptsExceededException` nếu nhập sai quá 5 lần.
  - Khớp mã OTP qua `passwordEncoder.matches()`. Nếu sai → tăng `attempts++`, throws `OtpInvalidException`.
  - Đánh dấu `consumed = true`.
  - Nếu `purpose == REGISTER_VERIFICATION` → Kích hoạt user (`is_active = true`, `email_verified = true`), sinh cặp JWT Access/Refresh Token và trả về trong `VerifyOtpResponseDto`.
  - Nếu `purpose == RESET_PASSWORD` → Sinh `actionToken` ngẫu nhiên (UUID hashed/stored) và trả về cho FE.

---

## 11. Controller Layer — API Endpoints (39 Endpoints)

### 11.4. AuthController (`/api/v1/auth`) — 8 Endpoints (★ CẬP NHẬT)

| HTTP | Path               | Auth   | Request Body             | Response Wrapper / Data                                                  |
| ---- | ------------------ | ------ | ------------------------ | ------------------------------------------------------------------------ |
| POST | `/register`        | Public | RegisterRequestDto       | `ApiResponse<UserResponseDto>` (tài khoản chưa active, OTP được gửi)     |
| POST | `/verify-otp`      | Public | VerifyOtpRequestDto      | `ApiResponse<VerifyOtpResponseDto>` (Trả JWT Tokens hoặc ActionToken)    |
| POST | `/resend-otp`      | Public | ResendOtpRequestDto      | `ApiResponse<ResendOtpResponseDto>` (Gửi lại OTP có cooldown 60s)        |
| POST | `/forgot-password` | Public | ForgotPasswordRequestDto | `ApiResponse<String>` (Gửi OTP quên mật khẩu)                            |
| POST | `/login`           | Public | LoginRequestDto          | `ApiResponse<AuthResponseDto>` (Bắt buộc tài khoản đã active & verified) |
| POST | `/refresh-token`   | Public | RefreshTokenRequestDto   | `ApiResponse<RefreshTokenResponseDto>`                                   |
| POST | `/logout`          | Public | RefreshTokenRequestDto   | `ApiResponse<Void>`                                                      |

---

## 12. Security & Authentication (JWT)

- **Register State:** Khi đăng ký, User có `is_active = false` và `email_verified = false`.
- **Login Requirement:** `AuthServiceImpl.login()` kiểm tra `user.isActive() && user.isEmailVerified()`. Nếu chưa verified → ném `AccountNotVerifiedException` (401 Unauthorized).
- **Public Auth Endpoints:** Tất cả 8 endpoints dưới `/api/v1/auth/**` đều là **Public**.

---

## 15. Exception Handling

### Danh sách 22 Exception Handlers trong `GlobalExceptionHandler`

| Exception Class                           | HTTP Status               | Thông điệp / Mô tả                                                           |
| ----------------------------------------- | ------------------------- | ---------------------------------------------------------------------------- |
| **`AccountNotVerifiedException`**         | **401 Unauthorized**      | **★ MỚI** — "Tài khoản chưa được xác thực email. Vui lòng xác thực OTP."     |
| **`OtpInvalidException`**                 | **400 Bad Request**       | **★ MỚI** — "Mã OTP không chính xác. Số lần thử còn lại: N"                  |
| **`OtpExpiredException`**                 | **400 Bad Request**       | **★ MỚI** — "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã."                |
| **`OtpAttemptsExceededException`**        | **409 Conflict**          | **★ MỚI** — "Bạn đã nhập sai mã OTP quá 5 lần. Vui lòng lấy mã mới."         |
| **`ResendCooldownException`**             | **409 Conflict**          | **★ MỚI** — "Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới."             |
| **`OtpNotFoundException`**                | **404 Not Found**         | **★ MỚI** — "Không tìm thấy yêu cầu xác thực OTP phù hợp."                   |
| **`InvalidActionTokenException`**         | **400 Bad Request**       | **★ MỚI** — "Mã xác nhận thao tác (Action Token) không hợp lệ hoặc đã dùng." |
| `BadRequestException`                     | 400 Bad Request           | Yêu cầu không hợp lệ                                                         |
| `ResourceNotFoundException`               | 404 Not Found             | Không tìm thấy tài nguyên                                                    |
| `InsufficientStockException`              | 409 Conflict              | Số lượng hàng trong kho không đủ                                             |
| `InvalidStatusTransitionException`        | 409 Conflict              | Chuyển trạng thái đơn hàng không hợp lệ                                      |
| `CategoryHasProductsException`            | 409 Conflict              | Không thể xoá danh mục đang chứa sản phẩm                                    |
| `DuplicateResourceException`              | 409 Conflict              | Email hoặc số điện thoại đã tồn tại                                          |
| `ObjectOptimisticLockingFailureException` | 409 Conflict              | Dữ liệu đã bị thay đổi bởi giao dịch khác (Optimistic Lock)                  |
| `DataIntegrityViolationException`         | 409 Conflict              | Lỗi vi phạm ràng buộc dữ liệu Database                                       |
| `MaxUploadSizeExceededException`          | 400 Bad Request           | Dung lượng file upload vượt quá 5MB                                          |
| `MethodArgumentNotValidException`         | 400 Bad Request           | Lỗi Validation dữ liệu đầu vào                                               |
| `BadCredentialsException`                 | 401 Unauthorized          | Email hoặc mật khẩu không chính xác                                          |
| `InvalidRefreshTokenException`            | 401 Unauthorized          | Refresh token không hợp lệ hoặc đã hết hạn                                   |
| `DisabledException`                       | 403 Forbidden             | Tài khoản đã bị khoá bởi Admin                                               |
| `AccessDeniedException`                   | 403 Forbidden             | Không có quyền truy cập API                                                  |
| `Exception`                               | 500 Internal Server Error | Lỗi hệ thống chưa xác định                                                   |

---

## 16. Common — API Response Format

### Format chuẩn `ApiResponse<T>`

```json
{
  "code": 200,
  "message": "Thao tác thành công!",
  "data": { ... },
  "errors": null,
  "timestamp": "2026-07-28T00:45:00.123"
}
```

---

## 17. Business Flows (Luồng nghiệp vụ)

### 17.5. Account Registration & OTP Verification Flow (Luồng Đăng ký & Xác thực Tài khoản) — ★ MỚI

```
1. Client (FE) ──► POST /api/v1/auth/register (fullName, email, phoneNumber, password)
2. Service ──────► Lưu UserEntity (is_active = false, email_verified = false)
3. Service ──────► OtpService.generateAndSendOtp(user, REGISTER_VERIFICATION)
4. OtpService ───► Sinh 6 chữ số ➔ Hash BCrypt ➔ Save OtpVerificationEntity (expires_at = NOW + 5m)
5. OtpService ───► Async Email (HTML Template với mã 6 chữ số)
6. Backend ──────► Trả UserResponseDto (HTTP 201 Created)
7. Client (FE) ──► Chuyển sang màn hình nhập OTP 6 chữ số
8. Client (FE) ──► POST /api/v1/auth/verify-otp (email, otpCode, purpose = REGISTER_VERIFICATION)
9. Service ──────► Check OTP valid ➔ Set user (is_active = true, email_verified = true)
10. Service ─────► Sinh cặp JWT Tokens (accessToken, refreshToken)
11. Backend ─────► Trả VerifyOtpResponseDto chứa accessToken + refreshToken (HTTP 200 OK)
12. Client (FE) ──► Đăng nhập thành công tự động, lưu Token vào LocalStorage/Cookie ➔ Vào App!
```

### 17.6. Forgot Password & Reset Flow (Luồng Quên Mật Khẩu & Đặt Lại) — ★ MỚI

```
1. Client (FE) ──► POST /api/v1/auth/forgot-password (email)
2. Service ──────► OtpService.generateAndSendOtp(user, RESET_PASSWORD) ➔ Send Email OTP
3. Client (FE) ──► Màn hình nhập OTP Quên mật khẩu
4. Client (FE) ──► POST /api/v1/auth/verify-otp (email, otpCode, purpose = RESET_PASSWORD)
5. Service ──────► Check OTP valid ➔ Sinh `actionToken`
6. Backend ──────► Trả VerifyOtpResponseDto chứa `actionToken`
7. Client (FE) ──► Chuyển màn hình Đặt mật khẩu mới (truyền actionToken + newPassword)
8. Client (FE) ──► Đổi mật khẩu thành công ➔ Chuyển về Đăng nhập
```

---

## 18. Coding Conventions & Patterns

| Pattern                        | Áp dụng ở đâu                                                                        |
| ------------------------------ | ------------------------------------------------------------------------------------ |
| **Layered Architecture**       | Controller → Service → Repository → Entity                                           |
| **Multi-step Auth & OTP Flow** | ★ **MỚI V7** — OTP Secure Generation, Hash Storage, Max Attempt Guard, Cooldown Lock |
| **Strategy Pattern**           | `MailTransport` interface + `SmtpMailTransport` (dev) / `BrevoMailTransport` (prod)  |
| **Distributed Caching**        | Redis + Spring `@Cacheable` / `@CacheEvict`                                          |
| **Async Task Isolation**       | `@Async("emailTaskExecutor")` tách biệt gửi mail HTML                                |
| **Scheduled Maintenance**      | `@Scheduled` — `OtpCleanupScheduler` & `PaymentExpiredScheduler`                     |
| **Optimistic Locking**         | `@Version` trên `ProductEntity` + `OrderEntity`                                      |
| **Magic Bytes Validation**     | `FileValidationUtil`                                                                 |
| **Static Analysis**            | PMD Static Analysis (`maven-pmd-plugin` 3.26.0)                                      |

---

## 19. Trạng thái phát triển & TODO

### 19.1. Đã hoàn thành ✅

- [x] Layered Architecture chuẩn
- [x] CRUD đầy đủ: Category, Product, Order, User
- [x] JWT Authentication + Refresh Token (stateless)
- [x] **Xác thực tài khoản qua Email OTP** (★ MỚI V7)
- [x] **Luồng Quên & Đặt lại Mật khẩu via OTP** (★ MỚI V7)
- [x] **OTP Cleanup Scheduler** (★ MỚI V7)
- [x] **HTML Email Template Branding Hải Sản** (★ MỚI V7)
- [x] Role-based Authorization (USER/ADMIN)
- [x] VNPay Payment Integration (sandbox) + Auto Expire Scheduled Job
- [x] Optimistic Locking (`@Version` trên Product & Order)
- [x] Flyway Database Migrations (3 migrations — V1 schema, V2 seed, V3 OTP table)
- [x] Docker Multi-stage + Docker Compose (4 services: db, app, redis, minio)
- [x] GitHub Actions CI (PMD Quality Gate + Test + Build)
- [x] MinIO file upload + magic bytes validation + Thumbnailator image resize
- [x] Redis Distributed Caching (`@Cacheable`, `@CacheEvict`)
- [x] Async Email Processing (`@Async`, MailTransport Strategy Pattern)
- [x] CORS Externalized (đọc từ `.env`)
- [x] Production Config (`.env.production`, `deploy_render_plan.md`)

### 19.2. Chưa làm / Cần cải thiện ⚠️

- [ ] **Unit Tests** — Viết JUnit 5 + Mockito cho Service Layer & Controller Layer
- [ ] **Frontend UI** — Xây dựng giao diện Web (React / Next.js / Vue) tích hợp với REST API
- [ ] **Cart (Giỏ hàng)** — Module quản lý giỏ hàng lưu trữ Database / Redis
- [ ] **Search & Filter nâng cao** — Lọc sản phẩm theo khoảng giá (`minPrice`, `maxPrice`)
- [ ] **Production Deployment** — Triển khai chính thức lên Render + Cloudflare R2 + Brevo

---

## 20. Góp ý tối ưu & Bảng theo dõi cải tiến

| #   | Vấn đề ban đầu                         | Trạng thái           | Giải pháp đã áp dụng                                                                            |
| --- | -------------------------------------- | -------------------- | ----------------------------------------------------------------------------------------------- |
| 1   | **Chưa có xác thực email khi đăng ký** | ✅ **ĐÃ XỬ LÝ (V7)** | Triển khai OTP 6 chữ số gửi qua HTML Email, bảo mật hash BCrypt, 5 phút hết hạn, max 5 lần thử. |
| 2   | **Chưa có luồng Quên mật khẩu**        | ✅ **ĐÃ XỬ LÝ (V7)** | Bổ sung OTP Reset Password flow + trả `actionToken` an toàn.                                    |
| 3   | **OTP hết hạn chiếm dụng DB**          | ✅ **ĐÃ XỬ LÝ (V7)** | `OtpCleanupScheduler` dọn dẹp các OTP hết hạn/consumed hàng giờ.                                |
| 4   | **Enums nằm rải rác trong Entity**     | ✅ **ĐÃ XỬ LÝ (V7)** | Tách riêng package `enums` độc lập (`Role`, `OrderStatus`, `OtpPurpose`).                       |
| 5   | **Email bị chậm HTTP Request**         | ✅ **ĐÃ XỬ LÝ**      | Async Email với ThreadPoolTaskExecutor + Strategy Pattern.                                      |
| 6   | **CORS Hardcoded**                     | ✅ **ĐÃ XỬ LÝ**      | CORS whitelist đọc động từ `.env`.                                                              |
| 7   | **Thiếu Unit Test**                    | 🟡 **CẦN LÀM**       | Chuẩn bị viết test coverage cho Service & Controller layers.                                    |

---

## 21. Tổng hợp Kiến thức & Kỹ thuật chức năng cốt lõi

### 21.9. Multi-step OTP Verification & Hashed Security (★ MỚI V7)

- **Non-reversible Hash:** Mã OTP 6 chữ số không bao giờ lưu dưới dạng plain text trong DB. Hệ thống nén BCrypt Hash mã OTP trước khi lưu.
- **Brute-force Prevention:** Giới hạn tối đa 5 lần nhập sai (`attempts >= 5`). Quá 5 lần, bản ghi bị khoá và bắt buộc yêu cầu OTP mới.
- **Resend Cooldown Guard:** Áp dụng khoảng chờ 60s giữa các lần bấm "Gửi lại OTP" tránh spam email và lãng phí tài nguyên mail gateway.
- **Action Token Pattern:** Sau khi verify OTP cho tác vụ nhạy cảm (như Quên Mật Khẩu), Backend trả về `actionToken` có thời hạn ngắn để làm bằng chứng cho bước đổi mật khẩu tiếp theo.

---

## 22. Hướng dẫn & Lưu ý Dành Cho Frontend (FE / Full-stack & AI Reader)

> **Phần này dành riêng cho Developer Frontend (React, Vue, Next.js...) hoặc các AI subagents hỗ trợ viết code FE để tích hợp chính xác và mượt mà với Backend.**

### 22.1. Server Connection & Swagger Reference

- **Base URL (Local):** `http://localhost:8085`
- **Swagger UI (Interactive API Docs):** `http://localhost:8085/swagger-ui.html`
- **OpenAPI JSON Spec:** `http://localhost:8085/v1/api-docs`

---

### 22.2. Quy Chuẩn Phản Hồi API (API Response Conventions)

Tất cả các API đều phản hồi dưới định dạng JSON đồng nhất theo class `ApiResponse<T>`:

```typescript
interface ApiResponse<T> {
  code: number; // HTTP Status Code (200, 201, 400, 401, 403, 404, 409, 500)
  message: string; // Thông điệp tiếng Anh/Việt mô tả kết quả
  data: T | null; // Dữ liệu trả về (Object, Array, PageResponse, hoặc null)
  errors: any | null; // Chi tiết lỗi (nếu có, ví dụ map lỗi validation)
  timestamp: string; // ISO Timestamp
}
```

---

### 22.3. Quy Chuẩn Phân Trang (Pagination Standards)

Các API danh sách (`GET /products`, `GET /categories`, `GET /users`) nhận query parameters phân trang:

- `page`: Trang cần lấy (**0-indexed**, mặc định `0` = trang 1)
- `size`: Số lượng items/trang (mặc định `10`)
- `sort`: Field cần sắp xếp (ví dụ: `price`, `createdAt`, `name`)
- `direction`: Hướng sắp xếp (`asc` hoặc `desc`)

**Dữ liệu trả về nằm trong `data` dưới dạng `PageResponse<T>`:**

```typescript
interface PageResponse<T> {
  content: T[]; // Mảng các item của trang hiện tại
  page: number; // Số trang hiện tại (0-indexed)
  size: number; // Kích thước trang
  totalElements: number; // Tổng số item trên toàn hệ thống
  totalPages: number; // Tổng số trang
  last: boolean; // Có phải trang cuối cùng không
}
```

---

### 22.4. Luồng Xác Thực Auth & Token Storage (Frontend Auth Specs)

#### 1. Đăng ký & Xác thực OTP (Registration Flow)

```
[Bước 1] FE gọi POST /api/v1/auth/register
         Body: { fullName, email, phoneNumber, password }
         -> Trả về HTTP 201 + UserResponseDto.
         -> FE hiển thị Modal/Màn hình "Nhập mã OTP 6 chữ số đã gửi về email".

[Bước 2] FE gọi POST /api/v1/auth/verify-otp
         Body: { email, otpCode: "123456", purpose: "REGISTER_VERIFICATION" }
         -> Trả về HTTP 200 + VerifyOtpResponseDto: { accessToken, refreshToken, actionToken: null }
         -> FE lưu accessToken & refreshToken vào LocalStorage / Secure Cookie.
         -> FE set header Authorization cho các request sau & Tự động chuyển thẳng vào Dashboard/Trang chủ!
```

#### 2. Đăng nhập (Login Flow)

```
FE gọi POST /api/v1/auth/login
Body: { email, password }
-> Nếu thành công: Trả về AuthResponseDto: { accessToken, refreshToken, tokenType: "Bearer", expiresIn: 3600000 }
-> Nếu lỗi 401 "AccountNotVerifiedException":
   FE bắt mã lỗi 401 ➔ Thông báo user: "Tài khoản chưa được kích thực email" ➔ Chuyển user sang màn hình nhập OTP ➔ Có nút "Resend OTP" gọi POST /api/v1/auth/resend-otp { email, purpose: "REGISTER_VERIFICATION" }.
```

#### 3. Tự động Refresh Token khi 401 Unauthorized (Axios Interceptor Pattern)

- Access Token có thời hạn **1 giờ**.
- Refresh Token có thời hạn **7 ngày**.
- Khi bất kỳ API nào trả về `401 Unauthorized` (do Token hết hạn):
  1. Axios Interceptor tạm hoãn các request bị lỗi.
  2. Gửi request `POST /api/v1/auth/refresh-token` với Body: `{ refreshToken }`.
  3. Nhận `accessToken` mới ➔ Cập nhật LocalStorage/State.
  4. Thử lại request ban đầu với Header `Authorization: Bearer <new_access_token>`.
  5. Nếu Refresh Token cũng hết hạn (401) ➔ Xoá LocalStorage & Chuyển hướng người dùng về `/login`.

---

### 22.5. Bảng Phân Quyền & Access Rules Cho Frontend

| API Group              | Endpoints                                                                                                                                                                                             | Mức độ Phân quyền (Auth Requirement)                                |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------- |
| **Public Catalog**     | `GET /api/v1/products`, `GET /api/v1/products/{id}`, `GET /api/v1/categories`, `GET /api/v1/categories/{id}`                                                                                          | **Public** — Không cần token (dùng cho khách vãng lai xem sản phẩm) |
| **Auth Operations**    | `POST /api/v1/auth/**` (register, verify-otp, resend-otp, forgot-password, login, refresh-token, logout)                                                                                              | **Public** — Không cần token                                        |
| **Payment Return/IPN** | `GET /api/v1/payments/vnpay-return`, `GET /api/v1/payments/vnpay-ipn`                                                                                                                                 | **Public** — VNPay callback                                         |
| **User Profile**       | `GET /api/v1/users/me`, `PATCH /api/v1/users/me/update`, `PATCH /api/v1/users/password`, `POST /api/v1/users/me/avatar`                                                                               | **Authenticated** — Cần Header `Authorization: Bearer <token>`      |
| **Orders & Payment**   | `POST /api/v1/orders`, `GET /api/v1/orders/user/{userId}`, `GET /api/v1/orders/{id}`, `POST /api/v1/payments/create`                                                                                  | **Authenticated** — User chỉ xem/tạo đơn của chính mình             |
| **Admin Portal**       | `POST/PUT/DELETE` Products & Categories, `GET /api/v1/products/top-buy`, `GET /api/v1/products/revenue-*`, `GET /api/v1/users`, `PATCH /api/v1/users/{id}/status`, `PATCH /api/v1/orders/{id}/status` | **ADMIN Role Only** — Cần Token có Role `ADMIN`                     |

---

### 22.6. Hướng dẫn Upload Media (Hình ảnh Sản phẩm & Avatar User)

- **Header Content-Type:** `multipart/form-data`
- **Field Name:** `file`
- **Dung lượng tối đa:** `5MB`
- **Định dạng cho phép:** `image/jpeg`, `image/png`, `image/webp`, `image/gif`
- **Endpoints Upload:**
  - POST `/api/v1/users/me/avatar`: Upload avatar cá nhân (Cần Bearer Token)
  - POST `/api/v1/products/{id}/image`: Upload ảnh sản phẩm (Cần Admin Bearer Token)

---

> **🎯 KẾT LUẬN CHO FE & AI AGENTS:**
> Hệ thống Backend **Mini Ecommerce Seafood** hiện đã hoàn chỉnh 100% về cơ chế **Xác thực OTP Email**, **Phân quyền Security**, **Quản lý Sản phẩm/Đơn hàng/Thanh toán VNPay**, **Upload ảnh**, và **Swagger Documentation**. Frontend có thể bắt đầu tích hợp trực tiếp dựa theo tài liệu hướng dẫn trên.
