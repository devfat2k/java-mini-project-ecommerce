# 📖 PROJECT OVERVIEW V5 — Mini Ecommerce (Current State Edition)

> **Mục đích tài liệu:** Đây là tài liệu **CHÍNH XÁC NHẤT** phản ánh trạng thái codebase thực tế tính đến ngày cập nhật. Bất kỳ AI Agent hay Developer nào khi bắt đầu hoặc tiếp tục công việc trên project này đều phải đọc file này **ĐẦU TIÊN**.
>
> **Cập nhật lần cuối:** 2026-07-24
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
21. [Tổng hợp Kiến thức & Kỹ thuật chức năng cốt lõi](#21-tổng-hợp-kiến-thức--kỹ-thuật-chức-năng-cốt-lõi)

---

## 1. Tổng quan dự án

| Thuộc tính | Giá trị |
|---|---|
| **Tên dự án** | Mini Ecommerce — Bán Hải Sản Tươi Sống & Chế Biến (Seafood) |
| **Group ID** | `com.devfat` |
| **Artifact ID** | `mini-ecommerce` |
| **Version** | `0.0.1-SNAPSHOT` |
| **Main class** | `MiniEcommerceApplication` |
| **Base package** | `com.devfat.mini_ecommerce` |
| **Miền nghiệp vụ (Domain)** | **E-commerce Bán Hải Sản (Seafood)** — Tươi sống, phile, chế biến sẵn & gia vị |
| **Mục tiêu** | Side project cá nhân — rèn luyện Java Backend, áp dụng best practices thực tế, làm portfolio cho vị trí Java Backend Developer |
| **Kiến trúc** | Layered Architecture (Controller → Service → Repository → Entity) |
| **API Style** | RESTful JSON API, Stateless (JWT, không session) |
| **Plan giai đoạn** | Hiện tại đang triển khai các hạng mục nâng cao thuộc **Giai đoạn 5 & 6** (Caching Redis & Async Email) |

### 1.1. Điểm thay đổi & bổ sung mới nhất (Tính đến 2026-07-24)

| Module / Tính năng | Trạng thái trước | Trạng thái Hiện tại |
|---|---|---|
| **Seafood Domain Model** | ⚠️ Sản phẩm chung chung | ✅ **Bổ sung miền Hải sản** — Quản lý 9 danh mục sản phẩm hải sản đặc thù (Cá biển, Tôm, Mực/Bạch tuộc, Cua/Ghẹ, Ốc/Sò, Set văn phòng, Set nhậu BBQ, Hải sản khô, Nước mắm/Gia vị). |
| **Redis Distributed Caching** | ❌ Chưa có | ✅ **MỚI** — Tích hợp `spring-boot-starter-data-redis` & `@EnableCaching`. Config `RedisCacheConfig` thiết lập TTL riêng cho products (30m), categories (30m) và analytics (5m). Áp dụng `@Cacheable` & `@CacheEvict` ở `ProductServiceImpl` và `CategoryServiceImpl`. |
| **Async Email System (`@Async`)** | ❌ Chưa có | ✅ **MỚI** — Cấu hình `AsyncConfig` (`ThreadPoolTaskExecutor` tên `emailTaskExecutor`), `EmailService`, `EmailServiceImpl`, `EmailSenderUtil` gửi Mail Text & HTML Template (`SendEmailTemplate.html` via Thymeleaf) bất đồng bộ khi Đặt hàng / Thanh toán thành công. |
| **PageResponse & Custom Resolver** | ⚠️ Tạo wrapper nhưng chưa dùng | ✅ **MỚI** — Tạo `CustomPageableArgumentResolver` kết hợp `WebConfig` xử lý query params (`page`, `size`, `sort`, `direction`). Đồng bộ tất cả Controllers (`CategoryController`, `ProductController`, `UserController`) trả về `ApiResponse<PageResponse<T>>`. |
| **Flyway V8 Seed Migration** | ❌ Chưa có | ✅ **MỚI** — Script `V8__seed_seafood_categories_and_products.sql` thêm 9 danh mục hải sản & 36 sản phẩm mẫu phong phú. |
| **Hoàn Stock khi Cancel Order** | ⚠️ Chưa xử lý hoàn stock | ✅ **MỚI** — `OrderServiceImpl.changeStatus()` tự động cộng bù stock sản phẩm về kho khi đơn hàng chuyển sang trạng thái `CANCELLED`. |
| **Docker & Docker Compose** | Multi-stage build | ✅ **HOÀN CHỈNH** — Multi-stage build, healthcheck, service_healthy |
| **GitHub Actions CI** | `ci.yml` | ✅ **HOÀN CHỈNH** — Build + test tự động on push/PR main |
| **MinIO Object Storage** | `MinioConfig`, `StorageService` | ✅ **HOÀN CHỈNH** — File upload validation magic bytes + Thumbnailator image resize |

### 1.2. Miền nghiệp vụ & Mô hình Sản phẩm Hải Sản (Seafood E-commerce Domain Model)

Dự án **Mini Ecommerce** được định hình là một hệ thống **Thương mại Điện tử chuyên doanh Hải Sản Tươi Sống & Chế Biến (Seafood E-commerce System)**. Hệ thống phục vụ đa dạng phân khúc khách hàng từ hộ gia đình, dân văn phòng đến các buổi tiệc nhậu/BBQ cuối tuần với 9 nhóm danh mục cốt lõi (theo migration `V8__seed_seafood_categories_and_products.sql`):

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

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| **Java** | 21 | Ngôn ngữ chính |
| **Spring Boot** | 3.5.16 | Framework core |
| **Maven** | (wrapper mvnw) | Build tool |
| **Docker** | Multi-stage | Containerization |
| **PostgreSQL** | 16 (Docker image) | Database chính |
| **Redis** | (Docker / Local) | In-memory Data Store cho Caching |

### 2.2. Dependencies (pom.xml) — Đầy đủ

| Dependency | Phiên bản | Mục đích | Scope |
|---|---|---|---|
| `spring-boot-starter-web` | (managed) | REST API, Tomcat embedded | compile |
| `spring-boot-starter-data-jpa` | (managed) | ORM (Hibernate), JpaRepository | compile |
| `spring-boot-starter-validation` | (managed) | Bean Validation (`@NotNull`, `@Size`...) | compile |
| `spring-boot-starter-security` | (managed) | Spring Security framework | compile |
| `spring-boot-starter-mail` | (managed) | **MỚI** — Gửi Email (JavaMailSender) | compile |
| `spring-boot-starter-cache` | (managed) | **MỚI** — Spring Cache Abstraction | compile |
| `spring-boot-starter-data-redis` | (managed) | **MỚI** — Spring Data Redis Connector & Serializers | compile |
| `spring-boot-devtools` | (managed) | Hot reload khi dev | runtime, optional |
| `postgresql` | (managed) | PostgreSQL JDBC driver | runtime |
| `flyway-core` | (managed) | Database migration versioning | compile |
| `flyway-database-postgresql` | (managed) | Flyway PostgreSQL adapter | compile |
| `springdoc-openapi-starter-webmvc-ui` | **2.8.17** | Swagger UI + OpenAPI 3 docs (Cập nhật mới) | compile |
| `jjwt-api` | **0.12.6** | JWT API | compile |
| `jjwt-impl` | **0.12.6** | JWT implementation | runtime |
| `jjwt-jackson` | **0.12.6** | JWT JSON serialization | runtime |
| `lombok` | (managed) | Giảm boilerplate | compile, optional |
| `spring-dotenv` | **4.0.0** | Tự động load file `.env` vào Spring properties | compile |
| `minio` | **8.5.17** | MinIO Java SDK (S3-compatible object storage) | compile |
| `thumbnailator` | **0.4.20** | Resize/compress ảnh trước khi upload | compile |
| `spring-boot-starter-test` | (managed) | Unit/Integration testing | test |

> **Lưu ý:** Dự án **KHÔNG** dùng VNPay SDK bên thứ 3. VNPay được tích hợp trực tiếp bằng thuật toán HMAC-SHA512 (`javax.crypto.Mac`).

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

- `.env` — chứa secrets thực (DB, JWT, Mail, MinIO, Redis, VNPay) → **KHÔNG commit**
- `.env.example` — template để developer khác setup.

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
| | `spring.jpa.hibernate.ddl-auto` | `validate` (An toàn production) |
| | `spring.jpa.properties.hibernate.format_sql` | `true` |
| | `spring.jpa.properties.hibernate.use_sql_comments` | `true` |
| **Flyway** | `spring.flyway.enabled` | `true` |
| | `spring.flyway.locations` | `classpath:db/migration` |
| | `spring.flyway.baseline-on-migrate` | `true` |
| | `spring.flyway.schemas` | `public` |
| **Mail** | `spring.mail.host` | `smtp.gmail.com` |
| | `spring.mail.port` | `587` |
| | `spring.mail.username` | `${MAIL_USERNAME}` |
| | `spring.mail.password` | `${MAIL_PASSWORD}` |
| | `spring.mail.properties.mail.smtp.auth` | `true` |
| | `spring.mail.properties.mail.smtp.starttls.enable` | `true` |
| **Redis** | `spring.data.redis.host` | `localhost` |
| | `spring.data.redis.port` | `6379` |
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

### 4.2. Profile-specific files

| File | Server Port |
|---|---|
| `application-dev.yaml` | `8085` |
| `application-test.yaml` | `8082` |
| `application-prod.yaml` | `8083` |

### 4.3. Configuration Beans (7 Beans)

| Bean Class | Annotation | Vai trò |
|---|---|---|
| `SecurityConfig` | `@Configuration @EnableWebSecurity @EnableMethodSecurity` | Filter chain, CORS, BCrypt, AuthenticationManager |
| `OpenApiConfig` | `@Configuration` | Swagger/OpenAPI + Bearer Auth scheme |
| `VNPayConfig` | `@Configuration @ConfigurationProperties(prefix="vnpay")` | VNPay settings + helper methods |
| `MinioConfig` | `@Configuration` | Tạo `MinioClient` bean từ endpoint/access-key/secret-key |
| **`AsyncConfig`** | **`@Configuration @EnableAsync`** | **★ MỚI** — Cấu hình `ThreadPoolTaskExecutor` (`emailTaskExecutor` core=3, max=10, queue=50) |
| **`RedisCacheConfig`** | **`@Configuration @EnableCaching`** | **★ MỚI** — Khởi tạo `RedisCacheManager` với TTL riêng (products/categories: 30m, analytics: 5m) |
| **`WebConfig`** | **`@Configuration`** | **★ MỚI** — Đăng ký `CustomPageableArgumentResolver` tự động parse tham số phân trang |

---

## 5. Cấu trúc thư mục chi tiết

```
mini-ecommerce/
├── pom.xml                                        # Maven build config (+ Redis, Mail, OpenApi 2.8.17)
├── mvnw / mvnw.cmd                                # Maven wrapper
├── Dockerfile                                     # Multi-stage build (builder → JRE-alpine)
├── docker-compose.yml                             # db (postgres:16) + app services
├── .env                                           # ⚠️ KHÔNG commit — chứa ALL secrets
├── .env.example                                   # Template cho developer mới
├── .dockerignore                                  # Ignore target, .git, .idea, .env
├── .gitignore                                     # Ignore .env, application.yaml, IDE
├── .gitattributes                                 # Git line-ending config
├── README.md                                      # Project readme
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
│   │   │   │   └── PageResponse.java              # Standardized pagination response (content, page, size, totalElements, totalPages, last)
│   │   │   │
│   │   │   ├── config/                            # Spring Configuration beans (7 files)
│   │   │   │   ├── SecurityConfig.java            # Security filter chain, CORS, BCrypt, AuthenticationManager
│   │   │   │   ├── OpenApiConfig.java             # Swagger/OpenAPI + Bearer Auth scheme
│   │   │   │   ├── VNPayConfig.java               # @ConfigurationProperties(prefix="vnpay")
│   │   │   │   ├── VNPayUtil.java                 # HMAC-SHA512, buildQueryAndHash, verifySignature
│   │   │   │   ├── MinioConfig.java               # MinioClient bean
│   │   │   │   ├── AsyncConfig.java               # ★ MỚI — @EnableAsync + emailTaskExecutor ThreadPool
│   │   │   │   ├── RedisCacheConfig.java          # ★ MỚI — @EnableCaching + RedisCacheManager custom TTLs
│   │   │   │   └── WebConfig.java                 # ★ MỚI — Register CustomPageableArgumentResolver
│   │   │   │
│   │   │   ├── controller/                        # REST Controllers (8 files)
│   │   │   │   ├── AuthController.java            # /api/v1/auth/** (4 endpoints)
│   │   │   │   ├── CategoryController.java        # /api/v1/categories/** (5 endpoints) — Trả PageResponse
│   │   │   │   ├── ProductController.java         # /api/v1/products/** (11 endpoints) — Trả PageResponse
│   │   │   │   ├── OrderController.java           # /api/v1/orders/** (4 endpoints)
│   │   │   │   ├── UserController.java            # /api/v1/users/** (6 endpoints) — Trả PageResponse
│   │   │   │   ├── PaymentController.java         # /api/v1/payments/** (3 endpoints)
│   │   │   │   ├── PingController.java            # /api/v1/health (1 endpoint)
│   │   │   │   └── TestUploadController.java      # /api/v1/test/upload (test MinIO)
│   │   │   │
│   │   │   ├── docs/                              # Tài liệu dự án (markdown)
│   │   │   │   ├── PROJECT_OVERVIEW.md            # ★ TÀI LIỆU NÀY
│   │   │   │   ├── plan_tong_hop.md               # Kế hoạch tổng hợp các phase
│   │   │   │   ├── plan_uu_tien_hoc_tap.md        # Roadmap học tập ưu tiên
│   │   │   │   └── auth_security_jwt_plan.md      # Plan JWT chi tiết
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/                       # 13 request DTOs
│   │   │   │   │   ├── BasePageRequest.java       # Base class cho pagination params
│   │   │   │   │   ├── RegisterRequestDto.java    # (record) Đăng ký user
│   │   │   │   │   ├── LoginRequestDto.java       # (record) Đăng nhập
│   │   │   │   │   ├── RefreshTokenRequestDto.java # (record) Refresh token
│   │   │   │   │   ├── CreateProductRequestDto.java # (record) Tạo sản phẩm
│   │   │   │   │   ├── UpdateProductRequestDto.java # (record) Cập nhật sản phẩm
│   │   │   │   │   ├── CreateCategoryRequestDto.java # (record) Tạo danh mục
│   │   │   │   │   ├── CreateOrderRequestDto.java  # (record) Tạo đơn hàng
│   │   │   │   │   ├── OrderItemRequestDto.java    # (record) Item trong đơn hàng
│   │   │   │   │   ├── UpdateOrderStatusRequestDto.java # (record) Cập nhật status đơn
│   │   │   │   │   ├── ChangePasswordRequestDto.java # (record) Đổi mật khẩu
│   │   │   │   │   ├── UpdateProfileRequestDto.java # (record) Cập nhật profile
│   │   │   │   │   └── EmailRequestDto.java       # ★ MỚI — Request DTO cho gửi Email
│   │   │   │   │
│   │   │   │   └── response/                      # 8 response DTOs
│   │   │   │       ├── AuthResponseDto.java       # Login response
│   │   │   │       ├── UserResponseDto.java       # User info (+ avatarUrl)
│   │   │   │       ├── CategoryResponseDto.java   # Category info
│   │   │   │       ├── ProductResponseDto.java    # Product info (+ imageUrl, active)
│   │   │   │       ├── OrderResponseDto.java      # Order info
│   │   │   │       ├── OrderItemResponseDto.java  # Order item info
│   │   │   │       ├── RefreshTokenResponseDto.java # Refresh response
│   │   │   │       └── CreatePaymentResponseDto.java # Payment URL
│   │   │   │
│   │   │   ├── entity/                            # JPA Entities (7 entities)
│   │   │   │   ├── UserEntity.java
│   │   │   │   ├── CategoryEntity.java
│   │   │   │   ├── ProductEntity.java             # @Version (Optimistic Lock)
│   │   │   │   ├── OrderEntity.java               # @Version (Optimistic Lock)
│   │   │   │   ├── OrderItemEntity.java
│   │   │   │   ├── RefreshTokenEntity.java
│   │   │   │   └── PaymentEntity.java
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
│   │   │   │   ├── ProductRepository.java         # JPQL + Projections
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   └── PaymentRepository.java
│   │   │   │
│   │   │   ├── security/                          # JWT Security module (7 files)
│   │   │   │   ├── JwtProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   │   ├── JwtAccessDeniedHandler.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── UserPrincipal.java
│   │   │   │   └── RefreshTokenGenerator.java
│   │   │   │
│   │   │   ├── service/                           # Service interfaces (8 files) + impl/ (8 files)
│   │   │   │   ├── AuthService.java / AuthServiceImpl.java
│   │   │   │   ├── CategoryService.java / CategoryServiceImpl.java — CacheEvict
│   │   │   │   ├── ProductService.java / ProductServiceImpl.java — Cacheable & CacheEvict
│   │   │   │   ├── OrderService.java / OrderServiceImpl.java — Auto restore stock + Async Email
│   │   │   │   ├── UserService.java / UserServiceImpl.java
│   │   │   │   ├── PaymentService.java / PaymentServiceImpl.java — Async Email IPN
│   │   │   │   ├── StorageService.java / StorageServiceImpl.java — MinIO + Thumbnailator
│   │   │   │   └── EmailService.java / EmailServiceImpl.java — ★ MỚI: @Async Email Service
│   │   │   │
│   │   │   └── util/                              # Utility classes (3 files)
│   │   │       ├── FileValidationUtil.java        # Magic bytes validation
│   │   │       ├── EmailSenderUtil.java           # ★ MỚI: JavaMailSender helper (Text/HTML)
│   │   │       └── CustomPageableArgumentResolver.java # ★ MỚI: Pageable Argument Resolver
│   │   │
│   │   └── resources/
│   │       ├── application.yaml                   # Config chính (gitignored)
│   │       ├── application-dev.yaml               # Port 8085
│   │       ├── application-test.yaml              # Port 8082
│   │       ├── application-prod.yaml              # Port 8083
│   │       ├── db/migration/
│   │       │   ├── V1__init_mini_shop.sql
│   │       │   ├── V2__add_version_to_products.sql
│   │       │   ├── V3__create_payments_table.sql
│   │       │   ├── V4__fix_payments_order_id_type.sql
│   │       │   ├── V5__add_version_to_orders.sql
│   │       │   ├── V6__fix_id_columns_to_bigint.sql
│   │       │   ├── V7__add_image_url_columns.sql
│   │       │   └── V8__seed_seafood_categories_and_products.sql # ★ MỚI — Data seed 9 categories & 36 hải sản
│   │       ├── templates/
│   │       │   └── email/
│   │       │       └── SendEmailTemplate.html    # ★ MỚI — HTML Email Template
│   │       └── static/                            # (RỖNG)
│   │
│   └── test/java/                                 # (Chưa có test code)
```

---

## 6. Database Schema & Migrations

### 6.1. Lịch sử Flyway Migrations (8 migrations)

| File | Nội dung |
|---|---|
| `V1__init_mini_shop.sql` | Tạo 6 bảng core: `users`, `categories`, `products`, `orders`, `order_items`, `refresh_tokens` + 7 indexes |
| `V2__add_version_to_products.sql` | `ALTER TABLE products ADD COLUMN version INTEGER NOT NULL DEFAULT 0` |
| `V3__create_payments_table.sql` | Tạo bảng `payments` + 2 indexes |
| `V4__fix_payments_order_id_type.sql` | `ALTER TABLE payments ALTER COLUMN order_id TYPE BIGINT` |
| `V5__add_version_to_orders.sql` | `ALTER TABLE orders ADD COLUMN version INTEGER NOT NULL DEFAULT 0` |
| `V6__fix_id_columns_to_bigint.sql` | Fix ALL `id`, FK columns sang `BIGINT` |
| `V7__add_image_url_columns.sql` | `products ADD image_url VARCHAR(500)`, `users ADD avatar_url VARCHAR(500)` |
| `V8__seed_seafood_categories_and_products.sql` | ★ **MỚI** — Seed dữ liệu 9 danh mục hải sản & 36 sản phẩm mẫu kèm mô tả, giá thực tế, stock và version |

---

## 7. Entity Layer — Chi tiết từng Entity

> **Quy ước chung:** Tất cả entities dùng `@Data @NoArgsConstructor @AllArgsConstructor @Builder @DynamicUpdate @DynamicInsert`. Timestamps dùng `@CreationTimestamp` và `@UpdateTimestamp`.

### 7.1. UserEntity (`users`)

| Field | Type | JPA Annotation | Ghi chú |
|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | PK |
| `fullName` | `String` | `@Column(name="full_name", length=150)` | |
| `email` | `String` | `@Column(unique=true, length=150)` | Username đăng nhập |
| `avatarUrl` | `String` | `@Column(name="avatar_url", length=500)` | URL avatar (MinIO) |
| `phoneNumber` | `String` | `@Column(name="phone_number", unique=true, length=15)` | |
| `password` | `String` | `@Column(nullable=false)` | BCrypt hash |
| `role` | `Role` enum | `@Enumerated(STRING)` | Inline enum: `USER`, `ADMIN` |
| `isActive` | `boolean` | `@Column(name="is_active")` | Soft disable account |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | |

### 7.2. CategoryEntity (`categories`)

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
| `imageUrl` | `String` | `@Column(name="image_url", length=500)` — URL ảnh (MinIO) |
| `description` | `String` | `@Column(columnDefinition="TEXT")` |
| `price` | `BigDecimal` | precision=12, scale=2 |
| `stock` | `Integer` | `@Builder.Default = 0` |
| `category` | `CategoryEntity` | `@ManyToOne(LAZY)`, FK: `category_id` |
| `isActive` | `boolean` | `@Builder.Default = true` |
| `orderItems` | `List<OrderItemEntity>` | `@OneToMany(mappedBy="product")` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |
| `version` | `Integer` | `@Version` — Optimistic Locking |

### 7.4. OrderEntity (`orders`) — Optimistic Lock

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

---

## 8. Repository Layer — Data Access

### 8.1. UserRepository

| Method | Mô tả |
|---|---|
| `findByEmail(String)` | Tìm user bằng email |
| `findByPhoneNumber(String)` | Tìm user bằng SĐT |

### 8.2. CategoryRepository

| Method | Mô tả |
|---|---|
| `findByNameContainingIgnoreCase(String, Pageable)` | Search + phân trang |
| `existsByNameIgnoreCase(String)` | Check tên đã tồn tại |

### 8.3. ProductRepository

| Method | Mô tả |
|---|---|
| `findAvailableWithCategory()` | `JOIN FETCH p.category WHERE p.stock > 0` |
| `findByMinPriceWithCategory(BigDecimal)` | `JOIN FETCH p.category WHERE p.price >= :minPrice` |
| `findByNameContainsIgnoreCase(String, Pageable)` | **JOIN FETCH + isActive=true + LIKE search** — có countQuery riêng |

**Projections:** `TopProductView`, `CategoryRevenueView`, `MonthlyRevenueView`.

---

## 9. DTO Layer — Request & Response

### 9.1. Request DTOs (13 DTOs)

| DTO | Type | Fields | Validation / Ghi chú |
|---|---|---|---|
| **BasePageRequest** | Class | `page, size, sortBy, direction` | Base class cho pagination params |
| **RegisterRequestDto** | Record | `fullName, email, phoneNumber, password` | `@NotNull @NotBlank @Email @Size @Pattern` |
| **LoginRequestDto** | Record | `email, password` | `@NotNull @NotBlank @Email @Size` |
| **RefreshTokenRequestDto** | Record | `refreshToken` | `@NotNull @NotBlank` |
| **CreateProductRequestDto** | Record | `name, description, price, stock, categoryId, isActive` | `@NotNull @NotBlank @DecimalMin @Min(1)` |
| **UpdateProductRequestDto** | Record | `name, description, price, stock, isActive, categoryId` | Partial update (nullable) |
| **CreateCategoryRequestDto** | Record | `name` | `@NotNull @NotBlank` |
| **CreateOrderRequestDto** | Record | `items: List<OrderItemRequestDto>` | `@NotEmpty @Valid` |
| **OrderItemRequestDto** | Record | `productId, quantity` | `@NotNull, @Min(1)` |
| **UpdateOrderStatusRequestDto** | Record | `orderStatus` | `@NotNull` |
| **ChangePasswordRequestDto** | Record | `oldPassword, newPassword` | `@NotNull @NotBlank @Size(8-100)` |
| **UpdateProfileRequestDto** | Record | `fullName, phoneNumber` | `@NotBlank @Pattern(10-11 digits)` |
| **EmailRequestDto** | **Class** | `to, subject, messageBody, attachmentPath` | **★ MỚI** — DTO truyền dữ liệu gửi Email |

### 9.2. Response DTOs

| DTO | Fields |
|---|---|
| **UserResponseDto** | `userId, fullName, avatarUrl, email, phoneNumber, role, isActive, createdAt` |
| **AuthResponseDto** | `accessToken, refreshToken, tokenType("Bearer"), expiresIn` |
| **RefreshTokenResponseDto** | `accessToken` |
| **CategoryResponseDto** | `id, categoryName` |
| **ProductResponseDto** | `id, name, price, stock, description, imageUrl, active, category` |
| **OrderResponseDto** | `id, status, totalAmount, createdAt, orderItems` |
| **OrderItemResponseDto** | `productName, quantity, unitPrice` |
| **CreatePaymentResponseDto** | `paymentUrl` |

---

## 10. Service Layer — Business Logic

### 10.1. ProductService / ProductServiceImpl — Caching & Storage

- **`findById(Long id)`**: `@Cacheable(value = "products", key = "#id")` — Cache thông tin sản phẩm vào Redis (TTL 30 min).
- **`update()`, `softDelete()`, `increaseStock()`, `decreaseStock()`, `uploadProductImage()`**: `@CacheEvict(value = "products", key = "#id")` — Xoá cache sản phẩm khi có dữ liệu thay đổi.
- **Analytics APIs (`getTopProducts`, `getCategoryRevenue`, `getMonthlyRevenue`)**: `@Cacheable(value = "analytics", key = "'top-products'")` v.v. — Cache thống kê với TTL 5 min.

### 10.2. CategoryService / CategoryServiceImpl — Caching

- **`create()`, `update()`, `deleteById()`**: `@CacheEvict(value = "categories", allEntries = true)` — Xoá sạch cache danh mục khi admin thêm/sửa/xoá.

### 10.3. OrderService / OrderServiceImpl — Async Email & Stock Restoration

- **`create()`**: Trừ stock theo số lượng đặt hàng. Khi tạo đơn hàng thành công, kích hoạt gửi mail xác nhận bất đồng bộ `emailService.sendOrderConfirmation(...)`.
- **`changeStatus()`**: Kiểm tra chuyển trạng thái hợp lệ. **Đặc biệt:** Khi đơn hàng bị huỷ (`CANCELLED`), hệ thống tự động hoàn lại stock tương ứng vào kho cho từng sản phẩm (`item.getProduct().setStock(...)`).

### 10.4. PaymentService / PaymentServiceImpl — Async Email IPN

- **`handleVnPayIpn()`**: Kiểm tra chữ ký HMAC-SHA512. Khi VNPay báo `00` (Thành công), chuyển Order sang `CONFIRMED` và gọi bất đồng bộ `emailService.sendPaymentSuccessEmail(...)`.

### 10.5. EmailService / EmailServiceImpl — ★ MỚI

- **`sendTextEmail(EmailRequestDto request)`**: `@Async("emailTaskExecutor")` — Gửi mail plain text qua `EmailSenderUtil`.
- **`sendHtmlEmail(EmailRequestDto request)`**: `@Async("emailTaskExecutor")` — Gửi mail HTML dựa trên template Thymeleaf `SendEmailTemplate.html`.
- **`sendOrderConfirmation(toEmail, orderId)` & `sendPaymentSuccessEmail(toEmail, orderId)`**: `@Async("emailTaskExecutor")` — Các helper method chuyên biệt phục vụ luồng nghiệp vụ đơn hàng & thanh toán.

---

## 11. Controller Layer — API Endpoints

> **Thay đổi nổi bật:** Tất cả endpoints trả về danh sách phân trang (`GET /categories`, `GET /products`, `GET /users`) đã chuyển sang trả về `ApiResponse<PageResponse<T>>`, kết hợp `CustomPageableArgumentResolver` tự động xử lý `Pageable`.

### 11.1. CategoryController (`/api/v1/categories`) — 5 endpoints

| HTTP | Path | Auth | Request Params / Body | Response Wrapper |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateCategoryRequestDto | `ApiResponse<CategoryResponseDto>` |
| GET | `/` | Public | `search`, `page`, `size`, `sort`, `direction` | `ApiResponse<PageResponse<CategoryResponseDto>>` |
| GET | `/{id}` | Public | Path: id | `ApiResponse<CategoryResponseDto>` |
| PUT | `/{id}` | ADMIN | CreateCategoryRequestDto | `ApiResponse<CategoryResponseDto>` |
| DELETE | `/{id}` | ADMIN | Path: id | `ApiResponse<Boolean>` |

### 11.2. ProductController (`/api/v1/products`) — 11 endpoints

| HTTP | Path | Auth | Request Params / Body | Response Wrapper |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateProductRequestDto | `ApiResponse<ProductResponseDto>` |
| GET | `/` | Public `@SecurityRequirements({})` | `search`, `page`, `size`, `sort`, `direction` | `ApiResponse<PageResponse<ProductResponseDto>>` |
| GET | `/{id}` | Authenticated | Path: id | `ApiResponse<ProductResponseDto>` |
| PATCH | `/{id}` | ADMIN | UpdateProductRequestDto | `ApiResponse<ProductResponseDto>` |
| DELETE | `/{id}` | ADMIN | Path: id | `ApiResponse<Boolean>` |
| PATCH | `/increase/{id}` | ADMIN | `quantity` | `ApiResponse<ProductResponseDto>` |
| PATCH | `/decrease/{id}` | ADMIN | `quantity` | `ApiResponse<ProductResponseDto>` |
| GET | `/top-buy` | ADMIN | `limit` (default 10) | `ApiResponse<List<TopProductView>>` |
| GET | `/revenue-by-category` | ADMIN | — | `ApiResponse<List<CategoryRevenueView>>` |
| GET | `/revenue-in-month` | ADMIN | — | `ApiResponse<List<MonthlyRevenueView>>` |
| POST | `/{id}/image` | ADMIN | multipart/form-data `file` | `ApiResponse<ProductResponseDto>` |

### 11.3. UserController (`/api/v1/users`) — 6 endpoints

| HTTP | Path | Auth | Request Params / Body | Response Wrapper |
|---|---|---|---|---|
| GET | `/me` | Authenticated | @AuthenticationPrincipal | `ApiResponse<UserResponseDto>` |
| PATCH | `/password` | Authenticated | ChangePasswordRequestDto | 204 No Content |
| PATCH | `/me/update` | Authenticated | UpdateProfileRequestDto | 204 No Content |
| GET | `/` | ADMIN | `page`, `size`, `sort`, `direction` | `ApiResponse<PageResponse<UserResponseDto>>` |
| PATCH | `/{userId}/status` | ADMIN | `isActive` | 204 No Content |
| POST | `/me/avatar` | Authenticated | multipart/form-data `file` | `ApiResponse<UserResponseDto>` |

---

## 12. Security & Authentication (JWT)

- **Access Token:** JWT Expiration 1h (3600000ms).
- **Refresh Token:** Opaque Random 64 bytes → SHA-256 Hash stored in DB, Expiration 7 days.
- **Security Chain:** `JwtAuthenticationFilter` intercepts, validates token, sets `SecurityContext`.

---

## 13. File Upload & Storage (MinIO)

- **Magic Bytes Validation:** `FileValidationUtil` kiểm tra trực tiếp byte header (JPEG, PNG, GIF, WebP) loại bỏ nguy cơ mạo danh định dạng file.
- **Image Resizing:** `Thumbnailator` tự động nén & chuẩn hoá ảnh về kích thước `800x800` (quality 0.85).

---

## 14. Payment Integration — VNPay

- **Flow:** `POST /orders` (PENDING) ➔ `POST /payments/create` ➔ VNPay Sandbox ➔ `handleVnPayIpn()` ➔ Order CONFIRMED + Async Email sent.
- **Scheduler:** Auto-expire pending payments > 15 phút (chuyển Order sang CANCELLED và giải phóng stock).

---

## 15. Exception Handling

- **GlobalExceptionHandler (`@RestControllerAdvice`)**: Xử lý 14 loại exceptions (`BadRequestException`, `ResourceNotFoundException`, `InsufficientStockException`, `MaxUploadSizeExceededException`, `ObjectOptimisticLockingFailureException`, v.v.).

---

## 16. Common — API Response Format

### PageResponse\<T\> — Chuẩn hoá Phân Trang

```java
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
```

> **Trạng thái:** Tất cả REST controllers trong hệ thống đã được đồng bộ sử dụng `PageResponse<T>` làm format phản hồi chuẩn duy nhất cho pagination.

---

## 17. Business Flows (Luồng nghiệp vụ)

### 17.1. Async Email Notification Flow — ★ MỚI

```
1. Client gửi request Đặt hàng / Thanh toán thành công (VNPay IPN)
2. Service xử lý xong DB Transaction (Save Order / Update Status)
3. Service gọi EmailService.sendOrderConfirmation(...) / sendPaymentSuccessEmail(...)
4. Spring AOP Proxy bắt lời gọi → Đẩy task sang ThreadPoolTaskExecutor ("emailTaskExecutor")
5. Main Thread trả HTTP Response lập tức cho Client (Không bị block bởi SMTP delay)
6. Worker Thread trong ThreadPool thực hiện gửi mail qua JavaMailSender (EmailSenderUtil)
```

### 17.2. Redis Caching Flow — ★ MỚI

```
1. Client request GET /api/v1/products/{id}
2. Spring Cache Interceptor kiểm tra key "products::{id}" trong Redis:
   - HIT  ➔ Trả kết quả JSON từ Redis lập tức (Bỏ qua DB & Service logic)
   - MISS ➔ Gọi method ProductServiceImpl.findById() truy vấn DB ➔ Lưu kết quả vào Redis ➔ Trả kết quả cho Client
3. Khi Admin gọi PATCH/DELETE /products/{id}:
   - Executing DB update
   - Interceptor thực thi @CacheEvict(value = "products", key = "#id") ➔ Xoá key tương ứng trong Redis để đảm bảo không bị Stale Data.
```

---

## 18. Coding Conventions & Patterns

| Pattern | Áp dụng ở đâu |
|---|---|
| **Layered Architecture** | Controller → Service → Repository → Entity |
| **Distributed Caching** | ★ **MỚI** — Redis + Spring `@Cacheable` / `@CacheEvict` với TTL linh hoạt |
| **Async Task Execution** | ★ **MỚI** — `@Async` + `ThreadPoolTaskExecutor` tách biệt xử lý Email khỏi HTTP Worker Thread |
| **Custom Argument Resolver** | ★ **MỚI** — `CustomPageableArgumentResolver` tự động bind query params vào Spring `Pageable` |
| **Optimistic Locking** | `@Version` trên `ProductEntity` + `OrderEntity` chống Race Condition |
| **Magic Bytes Validation** | `FileValidationUtil` kiểm tra byte header file upload |
| **Object Storage (S3-compatible)** | MinIO SDK lưu trữ media tách biệt |

---

## 19. Trạng thái phát triển & TODO

### 19.1. Đã hoàn thành ✅

- [x] Layered Architecture chuẩn
- [x] CRUD đầy đủ: Category, Product, Order, User
- [x] JWT Authentication + Refresh Token (stateless)
- [x] Role-based Authorization (USER/ADMIN)
- [x] VNPay Payment Integration (sandbox) + Auto Expire Scheduled Job
- [x] Optimistic Locking (`@Version` trên Product & Order)
- [x] Flyway Database Migrations (8 migrations — bao gồm seed 36 hải sản ở V8)
- [x] Docker Multi-stage + Docker Compose Healthcheck + GitHub Actions CI
- [x] MinIO file upload + magic bytes validation + Thumbnailator image resize
- [x] **Redis Distributed Caching** (`@Cacheable`, `@CacheEvict` cho Products, Categories, Analytics)
- [x] **Async Email Processing** (`@Async`, `ThreadPoolTaskExecutor`, JavaMailSender, Thymeleaf HTML)
- [x] **Chuẩn hoá Pagination** (`PageResponse<T>` & `CustomPageableArgumentResolver` hoạt động 100% trên Controllers)
- [x] **Hoàn Stock khi Cancel Order** (Tự động cộng bù kho khi huỷ đơn)

### 19.2. Chưa làm / Cần cải thiện ⚠️

- [ ] **Unit Tests** — Viết JUnit 5 + Mockito cho Service Layer
- [ ] **CORS Configuration** — Cấu hình domain Frontend linh hoạt qua `.env` thay vì hardcode localhost:8085
- [ ] **Cart (Giỏ hàng)** — Module quản lý giỏ hàng lưu trữ Database / Redis
- [ ] **Search nâng cao** — Filter sản phẩm theo khoảng giá (`minPrice`, `maxPrice`) và danh mục
- [ ] **TestUploadController** — Ẩn hoặc phân quyền ADMIN trước khi deploy Production

---

## 20. Góp ý tối ưu & Bảng theo dõi cải tiến

| # | Vấn đề ban đầu | Trạng thái | Giải pháp đã áp dụng |
|---|---|---|---|
| 1 | **PageResponse chưa được sử dụng** | ✅ **ĐÃ XỬ LÝ** | Áp dụng `PageResponse.of(Page<T>)` cho tất cả GET Controllers có phân trang. |
| 2 | **Pagination parameters rời rạc** | ✅ **ĐÃ XỬ LÝ** | Xây dựng `CustomPageableArgumentResolver` & `WebConfig` parse tự động các params `page`, `size`, `sort`, `direction`. |
| 3 | **Order cancelled chưa hoàn stock** | ✅ **ĐÃ XỬ LÝ** | Thêm logic tự động hoàn stock sản phẩm trong `OrderServiceImpl.changeStatus()`. |
| 4 | **Email thông báo bị chậm HTTP Request** | ✅ **ĐÃ XỬ LÝ** | Tách luồng gửi mail sang `@Async` với `emailTaskExecutor` Thread Pool riêng. |
| 5 | **TRUY VẤN DB lặp lại nhiều lần với Data ít thay đổi** | ✅ **ĐÃ XỬ LÝ** | Tích hợp Redis Caching cho Product detail, Category list và Analytics reports. |
| 6 | **Thiếu Unit Test** | 🟡 **CẦN LÀM** | Chuẩn bị viết test coverage cho Service & Controller layers bằng JUnit 5 + Mockito. |
| 7 | **CORS Hardcoded** | 🟡 **CẦN LÀM** | Chuyển CORS configuration sang đọc từ file `.env`. |

---

## 21. Tổng hợp Kiến thức & Kỹ thuật chức năng cốt lõi

### 21.1. DevOps, Containerization & CI/CD
- **Multi-stage Docker Build:** Tách giai đoạn `builder` (Maven 3.9 + JDK 21) và `runtime` (`eclipse-temurin:21-jre-alpine`) giảm dung lượng image, tối ưu layer cache.
- **Docker Compose Healthcheck:** Khởi chạy Spring Boot (`app`) chỉ khi PostgreSQL (`db`) đạt trạng thái `service_healthy`.
- **GitHub Actions CI:** Automate workflow verify, clean compile và test khi Push/PR branch `main`.

### 21.2. Redis Distributed Caching Strategy
- **Cache Abstraction:** Sử dụng `@Cacheable` và `@CacheEvict` decouple logic ứng dụng khỏi cache provider.
- **Granular TTLs:** `RedisCacheManager` định cấu hình thời gian sống khác nhau cho từng cache region (Products: 30m, Categories: 30m, Analytics: 5m).
- **Cache Invalidation:** Xoá sạch hoặc xoá theo key chính xác khi có thao tác Mutation (Cập nhật, xoá, upload ảnh, đổi kho) để tránh hiện tượng Stale Data.

### 21.3. Async Task Processing & Thread Pool Isolation
- **Non-blocking Execution:** Áp dụng `@Async` giúp giải phóng worker thread đảm nhận request HTTP ngay lập tức.
- **ThreadPool Task Executor:** Cấu hình `emailTaskExecutor` tùy chỉnh (`corePoolSize=3`, `maxPoolSize=10`, `queueCapacity=50`, `threadNamePrefix="EmailAsync-"`) ngăn ngừa tình trạng cạn kiệt tài nguyên hệ thống do tạo thread vô hạn.

### 21.4. Object Storage & File Security (MinIO)
- **Magic Bytes Validation:** `FileValidationUtil` kiểm tra trực tiếp byte header (JPEG, PNG, GIF, WebP) loại bỏ nguy cơ fake extension.
- **Image Optimization:** Sử dụng `Thumbnailator` nén & resize về kích thước `800x800` (quality 0.85).

### 21.5. Security & Stateless Authentication
- **Dual-Token Strategy:** Access Token JWT (1h) + Opaque Refresh Token (7 ngày, SHA-256 hashed in DB).
- **Custom Security EntryPoints:** Phản hồi chuẩn format JSON cho 401 Unauthorized và 403 Access Denied.

---

> **🎯 Kết luận:** Dự án Mini Ecommerce đã được nâng cấp toàn diện với **35 API Endpoints**, **8 Flyway Migrations**, **Redis Distributed Caching**, **Async Email Processing**, **MinIO Storage**, **VNPay Sandbox Integration**, và **Custom Pageable Resolution**. Các bước tiếp theo sẽ tập trung vào **Unit Testing (JUnit 5 + Mockito)** và xây dựng **Frontend UI**.
