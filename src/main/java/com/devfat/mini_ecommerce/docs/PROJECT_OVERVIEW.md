# 📖 PROJECT OVERVIEW V6 — Mini Ecommerce (Current State Edition)

> **Mục đích tài liệu:** Đây là tài liệu **CHÍNH XÁC NHẤT** phản ánh trạng thái codebase thực tế tính đến ngày cập nhật. Bất kỳ AI Agent hay Developer nào khi bắt đầu hoặc tiếp tục công việc trên project này đều phải đọc file này **ĐẦU TIÊN**.
>
> **Cập nhật lần cuối:** 2026-07-26
>
> **Supersedes:** PROJECT_OVERVIEW_V2.md, PROJECT_OVERVIEW_V3.md, PROJECT_OVERVIEW_V4.md, PROJECT_OVERVIEW_V5.md (các bản cũ đã bị xoá)

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
| **Plan giai đoạn** | Đang chuẩn bị **Production Deployment** lên Render + hoàn thiện CI/CD Pipeline |

### 1.1. Điểm thay đổi & bổ sung mới nhất (V5 → V6, tính đến 2026-07-26)

| Module / Tính năng | Trạng thái V5 (2026-07-24) | Trạng thái V6 Hiện tại (2026-07-26) |
|---|---|---|
| **Email Architecture — Strategy Pattern** | ⚠️ `EmailSenderUtil` — class helper duy nhất cho cả dev & prod | ✅ **REFACTORED** — Áp dụng **Strategy Pattern** với `MailTransport` interface + `SmtpMailTransport` (`@Profile("dev")` — JavaMailSender/SMTP) + `BrevoMailTransport` (`@Profile("prod")` — Brevo REST API). `EmailSenderUtil` đã bị **XOÁ**. |
| **Flyway Migrations — Hợp nhất** | 8 file migration riêng lẻ (V1→V8) | ✅ **CONSOLIDATED** — Gộp V1→V7 thành 1 file `V1__init_mini_shop.sql` (schema hợp nhất). V8 cũ → `V2__seed_seafood_categories_and_products.sql`. Thêm `FlywayConfig` bean (`repair()` trước `migrate()`). |
| **CORS Configuration** | ❌ Hardcoded `localhost:8085` | ✅ **ĐÃ XỬ LÝ** — Externalized qua `@Value("${app.cors.allowed-origins}")`, đọc từ `.env`. |
| **PMD Static Analysis** | ❌ Chưa có | ✅ **MỚI** — `maven-pmd-plugin` v3.26.0 + `pmd-ruleset.xml` custom (bắt unused vars/fields/methods, empty catch blocks). Tích hợp vào CI Pipeline. |
| **Docker Compose** | 2 services (db, app) | ✅ **MỞ RỘNG** — 4 services: `db` (postgres:16), `app`, `redis` (redis:latest), `minio` (quay.io/minio/minio). |
| **CI Pipeline** | Build + test cơ bản | ✅ **NÂNG CẤP** — 3 steps: PMD Quality Gate → Run Tests → Build Compile Check. Job đổi tên `quality-and-test`. |
| **Production Deployment** | ❌ Chưa chuẩn bị | ✅ **MỚI** — `.env.production` (Render PostgreSQL, Render Redis, Cloudflare R2, Brevo API), `deploy_render_plan.md`, `application-prod.yaml` tối ưu. |
| **Dockerfile** | `maven:3.9-eclipse-temurin-21` + `wget` healthcheck | ✅ **TỐI ƯU** — Đổi base `eclipse-temurin:21-jdk-alpine` (nhẹ hơn), bỏ `wget`, thêm `chmod +x mvnw`. |
| **application.yaml** | Config cố định, mail inline | ✅ **TÁI CẤU TRÚC** — Dynamic port `${PORT:8085}`, Redis password support, Brevo config, mail chuyển sang profile `dev`. Swagger sorter alpha. |
| **Profile configs** | 3 profiles (dev/test/prod) | ✅ **THAY ĐỔI** — Chỉ còn 2 profiles: `dev` (SMTP mail, debug logging) và `prod` (tắt SQL log). `application-test.yaml` đã **XOÁ**. |
| **Security — Public Endpoints** | `GET /products/{id}` cần Authenticated | ✅ **THAY ĐỔI** — `GET /products/{id}` giờ là **Public** (thêm vào `PUBLIC_GET_URLS`). |
| **New Docs** | 4 files docs | ✅ **MỚI** — Thêm `deploy_render_plan.md` (kế hoạch deploy Render) + `docker-commands-cheatsheet.md` (tham khảo Docker CLI). Tổng **6 files docs**. |

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

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| **Java** | 21 | Ngôn ngữ chính |
| **Spring Boot** | 3.5.16 | Framework core |
| **Maven** | (wrapper mvnw) | Build tool |
| **Docker** | Multi-stage | Containerization |
| **PostgreSQL** | 16 (Docker image) | Database chính |
| **Redis** | latest (Docker image) | In-memory Data Store cho Caching |

### 2.2. Dependencies (pom.xml) — Đầy đủ

| Dependency | Phiên bản | Mục đích | Scope |
|---|---|---|---|
| `spring-boot-starter-web` | (managed) | REST API, Tomcat embedded | compile |
| `spring-boot-starter-data-jpa` | (managed) | ORM (Hibernate), JpaRepository | compile |
| `spring-boot-starter-validation` | (managed) | Bean Validation (`@NotNull`, `@Size`...) | compile |
| `spring-boot-starter-security` | (managed) | Spring Security framework | compile |
| `spring-boot-starter-mail` | (managed) | Gửi Email (JavaMailSender) — dùng ở profile `dev` | compile |
| `spring-boot-starter-cache` | (managed) | Spring Cache Abstraction | compile |
| `spring-boot-starter-data-redis` | (managed) | Spring Data Redis Connector & Serializers | compile |
| `spring-boot-devtools` | (managed) | Hot reload khi dev | runtime, optional |
| `postgresql` | (managed) | PostgreSQL JDBC driver | runtime |
| `flyway-core` | (managed) | Database migration versioning | compile |
| `flyway-database-postgresql` | (managed) | Flyway PostgreSQL adapter | compile |
| `springdoc-openapi-starter-webmvc-ui` | **2.8.17** | Swagger UI + OpenAPI 3 docs | compile |
| `jjwt-api` | **0.12.6** | JWT API | compile |
| `jjwt-impl` | **0.12.6** | JWT implementation | runtime |
| `jjwt-jackson` | **0.12.6** | JWT JSON serialization | runtime |
| `lombok` | (managed) | Giảm boilerplate | compile, optional |
| `spring-dotenv` | **4.0.0** | Tự động load file `.env` vào Spring properties | compile |
| `minio` | **8.5.17** | MinIO Java SDK (S3-compatible object storage) | compile |
| `thumbnailator` | **0.4.20** | Resize/compress ảnh trước khi upload | compile |
| `spring-boot-starter-test` | (managed) | Unit/Integration testing | test |

> **Lưu ý:** Dự án **KHÔNG** dùng VNPay SDK bên thứ 3. VNPay được tích hợp trực tiếp bằng thuật toán HMAC-SHA512 (`javax.crypto.Mac`).

### 2.3. Build Plugins (3 Plugins)

| Plugin | Phiên bản | Vai trò |
|---|---|---|
| `spring-boot-maven-plugin` | (managed) | Build JAR, exclude Lombok |
| `maven-compiler-plugin` | (managed) | Config annotation processor paths cho Lombok ở cả compile + testCompile |
| **`maven-pmd-plugin`** | **3.26.0** | **★ MỚI** — Static Analysis, sử dụng custom `pmd-ruleset.xml` |

### 2.4. PMD Ruleset (`pmd-ruleset.xml`) — ★ MỚI

| Rule | Mô tả |
|---|---|
| `UnusedLocalVariable` | Bắt biến local không sử dụng |
| `UnusedPrivateField` | Bắt field private không sử dụng |
| `UnusedPrivateMethod` | Bắt method private không sử dụng |
| `UnusedFormalParameter` | Bắt tham số method không sử dụng |
| `EmptyCatchBlock` | Bắt catch block rỗng (không xử lý exception) |

> **Triết lý:** Chỉ bắt code chết & lỗi nguy hiểm — KHÔNG ép coding style cá nhân.

---

## 3. DevOps & Containerization

### 3.1. Dockerfile (Multi-stage build)

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder    ← Đổi sang JDK Alpine (nhẹ hơn maven base)
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw                               ← Đảm bảo quyền execute cho mvnw
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

> **Thay đổi so với V5:** Đổi base image từ `maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jdk-alpine` (nhẹ hơn). Bỏ `apk add wget` vì không dùng healthcheck trong Dockerfile nữa. Thêm `chmod +x mvnw`.

### 3.2. docker-compose.yml (4 Services)

| Service | Image | Port | Healthcheck / Depends |
|---|---|---|---|
| **db** | `postgres:16` | `5432:5432` | `pg_isready -U ${DB_USERNAME}` mỗi 5s |
| **redis** | `redis:latest` | `6379:6379` | — (★ MỚI) |
| **minio** | `quay.io/minio/minio` | `9000:9000`, `9001:9001` | — (★ MỚI) |
| **app** | Build từ Dockerfile | `8085:8085` | depends_on: db (service_healthy), redis & minio (service_started) |

**Đặc điểm quan trọng:**
- `app` depends_on `db` với `condition: service_healthy` — chờ DB ready rồi mới start app
- `app` depends_on `redis` và `minio` với `condition: service_started`
- Env từ `.env` + override cho container hostnames
- 2 Volumes persist data: `pgdata` (PostgreSQL) + `miniodata` (MinIO)
- MinIO chạy với `server /data --console-address ":9001"` — Console UI trên port 9001

### 3.3. GitHub Actions CI (`ci.yml`) — ★ NÂNG CẤP

```yaml
name: CI Pipeline
on: push/PR to main
jobs:
  quality-and-test:              ← Đổi tên job
    - Checkout code
    - Setup JDK 21 (Temurin) + Maven cache
    - Grant execute permission for mvnw      ← MỚI
    - Code Quality Gate (PMD): ./mvnw pmd:check    ← ★ MỚI — Fail fast trước test
    - Run tests: ./mvnw clean test
    - Build (compile check): ./mvnw clean package -DskipTests
```

> **Thay đổi so với V5:** Thêm step PMD Quality Gate chạy TRƯỚC test (fail fast). Tách riêng test và build thành 2 steps.

### 3.4. .dockerignore & .gitignore

| File | Ignore |
|---|---|
| `.dockerignore` | `target/`, `.git/`, `.idea/`, `*.iml`, `.env`, `.mvn/wrapper/maven-wrapper.jar` |
| `.gitignore` | Maven build, IDE files, `.env`, `application.yaml` (chứa secrets) |

### 3.5. Environment Files (3 files)

| File | Mô tả | Git tracked? |
|---|---|---|
| `.env` | Chứa secrets thực cho **Development** (DB, JWT, Mail SMTP, MinIO local, Redis, VNPay) | ❌ KHÔNG commit |
| `.env.production` | **★ MỚI** — Chứa secrets cho **Production** (Render PostgreSQL, Render Redis, Cloudflare R2, Brevo API, Render VNPay URLs) | ❌ KHÔNG commit |
| `.env.example` | Template cho developer mới — có hướng dẫn local vs production cho từng nhóm config | ✅ Commit |

**Cấu trúc `.env.example`:**
- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` — Local Docker hoặc Render PostgreSQL
- `CORS_ALLOWED_ORIGINS` — ★ MỚI — CORS whitelist
- `REDIS_HOST` / `REDIS_PORT` — Local Docker hoặc Render Key Value
- `MINIO_ENDPOINT` / `MIN_IO_*` — Local MinIO hoặc Cloudflare R2
- `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` — Gmail SMTP (dev)
- `BREVO_API_KEY` — ★ MỚI — Brevo Transactional Email API (prod)
- `JWT_SECRET_KEY`
- `VNPAY_*` — VNPay Sandbox config

---

## 4. Cấu hình (Configuration)

### 4.1. application.yaml (file chính — Shared cả dev & prod)

| Nhóm cấu hình | Key | Giá trị / Mô tả |
|---|---|---|
| **Profile** | `spring.profiles.active` | `${SPRING_PROFILES_ACTIVE:dev}` — ★ Dynamic từ env |
| **Multipart** | `spring.servlet.multipart.max-file-size` | `5MB` |
| | `spring.servlet.multipart.max-request-size` | `5MB` |
| **Redis** | `spring.data.redis.host` | `${REDIS_HOST:localhost}` |
| | `spring.data.redis.port` | `${REDIS_PORT:6379}` |
| | `spring.data.redis.password` | `${REDIS_PASSWORD:}` — ★ MỚI: hỗ trợ Redis có password |
| **Database** | `spring.datasource.url` | `${DB_URL}` |
| | `spring.datasource.driver-class-name` | `org.postgresql.Driver` |
| | `spring.datasource.hikari.maximum-pool-size` | `10` |
| | `spring.datasource.hikari.connection-timeout` | `30000` |
| **JPA** | `spring.jpa.database` | `postgresql` |
| | `spring.jpa.open-in-view` | `false` |
| | `spring.jpa.hibernate.ddl-auto` | `validate` — Flyway là nguồn sự thật duy nhất |
| | `spring.jpa.properties.hibernate.format_sql` | `true` |
| **Flyway** | `spring.flyway.enabled` | `true` |
| | `spring.flyway.locations` | `classpath:db/migration` |
| | `spring.flyway.baseline-on-migrate` | `true` |
| | `spring.flyway.create-schemas` | `false` — ★ MỚI |
| **Server** | `server.port` | `${PORT:8085}` — ★ Dynamic cho Render |
| **CORS** | `app.cors.allowed-origins` | `${CORS_ALLOWED_ORIGINS:http://localhost:8085}` — ★ MỚI |
| **JWT** | `app.jwt.secret-key` | `${JWT_SECRET_KEY}` |
| | `app.jwt.expiration` | `3600000` (1 giờ, ms) |
| | `app.jwt.refresh-expiration-days` | `7` (7 ngày) |
| **Upload** | `app.upload.max-image-size` | `5MB` |
| **MinIO** | `app.minio.endpoint` | `${MINIO_ENDPOINT:http://localhost:9000}` |
| | `app.minio.access-key` | `${MIN_IO_ACCESS_KEY}` |
| | `app.minio.secret-key` | `${MIN_IO_SECRET_KEY}` |
| | `app.minio.bucket` | `${MIN_IO_BUCKET}` |
| **Brevo Mail** | `app.mail.brevo.api-key` | `${BREVO_API_KEY:}` — ★ MỚI |
| | `app.mail.brevo.sender-email` | `${BREVO_SENDER_EMAIL:}` — ★ MỚI |
| **VNPay** | `vnpay.pay-url` | `${VNPAY_PAY_URL:sandbox URL}` |
| | `vnpay.tmn-code` | `${VNPAY_TMN_CODE}` |
| | `vnpay.secret-key` | `${VNPAY_SECRET_KEY}` |
| | `vnpay.return-url` | `${VNPAY_RETURN_URL}` |
| | `vnpay.ipn-url` | `${VNPAY_IPN_URL}` |
| **Swagger** | `springdoc.api-docs.path` | `/v1/api-docs` |
| | `springdoc.swagger-ui.path` | `/swagger-ui.html` |
| | `springdoc.swagger-ui.tags-sorter` | `alpha` — ★ MỚI |
| | `springdoc.swagger-ui.operations-sorter` | `alpha` — ★ MỚI |

### 4.2. Profile-specific files (2 Profiles — ★ THAY ĐỔI)

| File | Nội dung | Ghi chú |
|---|---|---|
| `application-dev.yaml` | `show-sql: true`, SMTP Mail config (Gmail), Flyway `repair-on-migrate`, Logging DEBUG | Mail config chỉ ở dev |
| `application-prod.yaml` | `show-sql: false`, Logging INFO/WARN only | Tối giản cho production |
| ~~`application-test.yaml`~~ | — | **ĐÃ XOÁ** |

**application-dev.yaml chi tiết:**
- `spring.jpa.show-sql: true`
- `spring.flyway.repair-on-migrate: true`
- `spring.mail.*` — SMTP Gmail config (host, port, username, password, starttls)
- `logging.level`: `org.springframework.web: DEBUG`, `org.hibernate.SQL: DEBUG`, `org.hibernate.type: TRACE`

**application-prod.yaml chi tiết:**
- `spring.jpa.show-sql: false`
- `logging.level`: `root: INFO`, `org.hibernate.SQL: WARN`

### 4.3. Configuration Beans (8 Beans — ★ TĂNG TỪ 7)

| Bean Class | Annotation | Vai trò |
|---|---|---|
| `SecurityConfig` | `@Configuration @EnableWebSecurity @EnableMethodSecurity` | Filter chain, CORS (externalized), BCrypt, AuthenticationManager |
| `OpenApiConfig` | `@Configuration` | Swagger/OpenAPI + Bearer Auth scheme |
| `VNPayConfig` | `@Configuration @ConfigurationProperties(prefix="vnpay")` | VNPay settings + helper methods |
| `MinioConfig` | `@Configuration` | Tạo `MinioClient` bean từ endpoint/access-key/secret-key |
| `AsyncConfig` | `@Configuration @EnableAsync` | Cấu hình `ThreadPoolTaskExecutor` (`emailTaskExecutor` core=3, max=10, queue=50) |
| `RedisCacheConfig` | `@Configuration @EnableCaching` | Khởi tạo `RedisCacheManager` với TTL riêng (products/categories: 30m, analytics: 5m) |
| `WebConfig` | `@Configuration` | Đăng ký `CustomPageableArgumentResolver` tự động parse tham số phân trang |
| **`FlywayConfig`** | **`@Configuration`** | **★ MỚI** — `FlywayMigrationStrategy` gọi `repair()` trước `migrate()` để đồng bộ checksum sau khi hợp nhất migrations |

---

## 5. Cấu trúc thư mục chi tiết

```
mini-ecommerce/
├── pom.xml                                        # Maven build config (+ PMD plugin 3.26.0)
├── pmd-ruleset.xml                                # ★ MỚI — Custom PMD rules (unused code, empty catch)
├── mvnw / mvnw.cmd                                # Maven wrapper
├── Dockerfile                                     # Multi-stage build (JDK-alpine → JRE-alpine)
├── docker-compose.yml                             # 4 services: db + app + redis + minio
├── .env                                           # ⚠️ KHÔNG commit — secrets Development
├── .env.production                                # ★ MỚI — ⚠️ KHÔNG commit — secrets Production (Render)
├── .env.example                                   # Template cho developer mới (local & production)
├── .dockerignore                                  # Ignore target, .git, .idea, .env
├── .gitignore                                     # Ignore .env, application.yaml, IDE
├── .gitattributes                                 # Git line-ending config
├── README.md                                      # Project readme
├── HELP.md                                        # Spring Boot generated help
├── .github/
│   └── workflows/
│       └── ci.yml                                 # ★ NÂNG CẤP — PMD → Test → Build
├── .mvn/                                          # Maven wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/devfat/mini_ecommerce/
│   │   │   ├── MiniEcommerceApplication.java      # Entry point (@SpringBootApplication @EnableScheduling)
│   │   │   │
│   │   │   ├── common/                            # Shared response wrappers (2 files)
│   │   │   │   ├── ApiResponse.java               # Generic API response <T> + success/error factories
│   │   │   │   └── PageResponse.java              # Standardized pagination response
│   │   │   │
│   │   │   ├── config/                            # Spring Configuration beans (9 files — ★ TĂNG TỪ 8)
│   │   │   │   ├── SecurityConfig.java            # Security filter chain, CORS externalized, BCrypt, AuthManager
│   │   │   │   ├── OpenApiConfig.java             # Swagger/OpenAPI + Bearer Auth scheme
│   │   │   │   ├── VNPayConfig.java               # @ConfigurationProperties(prefix="vnpay")
│   │   │   │   ├── VNPayUtil.java                 # HMAC-SHA512, buildQueryAndHash, verifySignature
│   │   │   │   ├── MinioConfig.java               # MinioClient bean
│   │   │   │   ├── AsyncConfig.java               # @EnableAsync + emailTaskExecutor ThreadPool
│   │   │   │   ├── RedisCacheConfig.java          # @EnableCaching + RedisCacheManager custom TTLs
│   │   │   │   ├── WebConfig.java                 # Register CustomPageableArgumentResolver
│   │   │   │   └── FlywayConfig.java              # ★ MỚI — FlywayMigrationStrategy: repair() → migrate()
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
│   │   │   ├── docs/                              # Tài liệu dự án (6 files — ★ TĂNG TỪ 4)
│   │   │   │   ├── PROJECT_OVERVIEW.md            # ★ TÀI LIỆU NÀY (V6)
│   │   │   │   ├── plan_tong_hop.md               # Kế hoạch tổng hợp các phase
│   │   │   │   ├── plan_uu_tien_hoc_tap.md        # Roadmap học tập ưu tiên
│   │   │   │   ├── auth_security_jwt_plan.md      # Plan JWT chi tiết
│   │   │   │   ├── deploy_render_plan.md          # ★ MỚI — Kế hoạch deploy lên Render
│   │   │   │   └── docker-commands-cheatsheet.md  # ★ MỚI — Tham khảo Docker CLI
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
│   │   │   │   │   └── EmailRequestDto.java       # Request DTO cho gửi Email
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
│   │   │   │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice (15 handlers)
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
│   │   │   │   └── EmailService.java / EmailServiceImpl.java — @Async Email via MailTransport
│   │   │   │
│   │   │   └── util/                              # Utility classes (5 files — ★ THAY ĐỔI TỪ 3)
│   │   │       ├── FileValidationUtil.java        # Magic bytes validation
│   │   │       ├── MailTransport.java             # ★ MỚI: Interface (Strategy Pattern) cho email transport
│   │   │       ├── SmtpMailTransport.java          # ★ MỚI: @Profile("dev") — JavaMailSender/SMTP
│   │   │       ├── BrevoMailTransport.java         # ★ MỚI: @Profile("prod") — Brevo REST API
│   │   │       └── CustomPageableArgumentResolver.java # Pageable Argument Resolver
│   │   │
│   │   └── resources/
│   │       ├── application.yaml                   # Config chính (gitignored)
│   │       ├── application-dev.yaml               # Dev: SMTP mail, debug logging
│   │       ├── application-prod.yaml              # Prod: tắt SQL log, logging INFO
│   │       ├── db/migration/                      # ★ HỢP NHẤT — Chỉ còn 2 files
│   │       │   ├── V1__init_mini_shop.sql         # Schema hợp nhất (gộp V1→V7 cũ)
│   │       │   └── V2__seed_seafood_categories_and_products.sql # Data seed 9 categories & 36 hải sản
│   │       ├── templates/
│   │       │   └── email/
│   │       │       └── SendEmailTemplate.html    # HTML Email Template
│   │       └── static/                            # (RỖNG)
│   │
│   └── test/java/                                 # (Chưa có test code)
```

---

## 6. Database Schema & Migrations

### 6.1. Lịch sử Flyway Migrations (2 migrations — ★ HỢP NHẤT TỪ 8)

| File | Nội dung |
|---|---|
| `V1__init_mini_shop.sql` | **★ HỢP NHẤT** — Gộp toàn bộ V1→V7 cũ thành 1 file duy nhất: Tạo 7 bảng core (`users`, `categories`, `products`, `orders`, `order_items`, `refresh_tokens`, `payments`) + 9 indexes. Tất cả `id`/FK đều là `BIGINT`, có `version` cho Optimistic Lock (products, orders), có `image_url`/`avatar_url`. |
| `V2__seed_seafood_categories_and_products.sql` | **★ ĐỔI TÊN** (V8 cũ) — Seed dữ liệu 9 danh mục hải sản & 36 sản phẩm mẫu kèm mô tả, giá thực tế, stock và version. Dùng `ON CONFLICT DO NOTHING` cho categories và `WHERE NOT EXISTS` cho products (tránh duplicate khi restart). |

> **Lý do hợp nhất:** Database production trên Render là fresh — không có migration history cũ. Hợp nhất giúp khởi tạo nhanh, sạch và dễ bảo trì. `FlywayConfig` bean thực hiện `repair()` trước `migrate()` để đồng bộ checksum.

### 6.2. Schema tổng quan (7 bảng)

```
users ─────────┐
               ├── orders ──── order_items ──── products ──── categories
               └── refresh_tokens
                    payments ──── orders
```

| Bảng | PK | Quan hệ |
|---|---|---|
| `users` | BIGSERIAL | 1:N → orders, 1:N → refresh_tokens |
| `categories` | BIGSERIAL | 1:N → products |
| `products` | BIGSERIAL | N:1 → categories, 1:N → order_items |
| `orders` | BIGSERIAL | N:1 → users, 1:N → order_items, 1:N → payments |
| `order_items` | BIGSERIAL | N:1 → orders, N:1 → products |
| `refresh_tokens` | BIGSERIAL | N:1 → users |
| `payments` | BIGSERIAL | N:1 → orders |

---

## 7. Entity Layer — Chi tiết từng Entity

> **Quy ước chung:** Tất cả entities dùng `@Data @NoArgsConstructor @AllArgsConstructor @Builder @DynamicUpdate @DynamicInsert`. Timestamps dùng `@CreationTimestamp` và `@UpdateTimestamp`.

### 7.1. UserEntity (`users`)

| Field | Type | JPA Annotation | Ghi chú |
|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | PK |
| `fullName` | `String` | `@Column(name="full_name", length=100)` | |
| `email` | `String` | `@Column(unique=true, length=150)` | Username đăng nhập |
| `avatarUrl` | `String` | `@Column(name="avatar_url", length=500)` | URL avatar (MinIO) |
| `phoneNumber` | `String` | `@Column(name="phone_number", unique=true, length=15)` | |
| `password` | `String` | `@Column(nullable=false)` | BCrypt hash |
| `role` | `UserEnum` enum | `@Enumerated(STRING)` | Inline enum: `USER`, `ADMIN` |
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
| `note` | `String` | `@Column(columnDefinition="TEXT")` |
| `items` | `List<OrderItemEntity>` | `@OneToMany(cascade=ALL, orphanRemoval=true)` |
| `createdAt` / `updatedAt` | `LocalDateTime` | |
| `version` | `Integer` | `@Version` |

### 7.5. PaymentEntity (`payments`)

| Field | Type | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `amount` | `BigDecimal` | precision=12, scale=2 |
| `paymentProvider` | `PaymentProvider` enum | `VNPAY, MOMO, ZALOPAY, ACB, VCB` |
| `paymentStatus` | `PaymentStatus` enum | `PENDING, SUCCESS, FAILED, EXPIRED` |
| `paymentMethod` | `PaymentMethod` enum | `CASH, BANK, WALLET` |
| `providerTransactionId` | `String` | UNIQUE, max 100 |
| `order` | `OrderEntity` | `@ManyToOne(LAZY)` |
| `paidAt` | `LocalDateTime` | |
| `createdAt` / `updatedAt` | `LocalDateTime` | |

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
| `findByStockGreaterThan(int)` | Lấy sản phẩm còn hàng |
| `findByCategoryIdAndStockGreaterThan(Long, int)` | Lấy sản phẩm theo category còn hàng |
| `findAvailableWithCategory()` | `JOIN FETCH p.category WHERE p.stock > 0` |
| `findByMinPriceWithCategory(BigDecimal)` | `JOIN FETCH p.category WHERE p.price >= :minPrice` |
| `findByStockGreaterThan(int, Pageable)` | Sản phẩm còn hàng có phân trang |
| `findByNameContainsIgnoreCase(String, Pageable)` | **JOIN FETCH + isActive=true + LIKE search** — có countQuery riêng |

**Projections:** `TopProductView`, `CategoryRevenueView`, `MonthlyRevenueView`.

### 8.4. OrderRepository

| Method | Mô tả |
|---|---|
| `findAllByUserId(Long)` | Lấy orders theo userId |
| `findByUserId(Long)` | Lấy order đầu tiên theo userId |
| `findAllByStatus(OrderStatus)` | Lấy orders theo status |
| `findAllByUserIdAndStatus(Long, OrderStatus)` | JPQL JOIN FETCH user, lọc userId + status |
| `findByUserIdWithDetails(Long)` | JPQL JOIN FETCH user + LEFT JOIN FETCH items |

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
| **EmailRequestDto** | **Class** | `to, subject, messageBody, attachmentPath` | DTO truyền dữ liệu gửi Email |

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
- **`getProductsWithSearch()`**: `@Cacheable(value = "categories", key = ...)` — Cache kết quả search sản phẩm.
- **`update()`, `softDelete()`, `increaseStock()`, `decreaseStock()`, `uploadProductImage()`**: `@CacheEvict(value = "products", key = "#id")` — Xoá cache sản phẩm khi có dữ liệu thay đổi.
- **Analytics APIs (`getTopProducts`, `getCategoryRevenue`, `getMonthlyRevenue`)**: `@Cacheable(value = "analytics", key = "'top-products'")` v.v. — Cache thống kê với TTL 5 min.

### 10.2. CategoryService / CategoryServiceImpl — Caching

- **`create()`, `update()`, `deleteById()`**: `@CacheEvict(value = "categories", allEntries = true)` — Xoá sạch cache danh mục khi admin thêm/sửa/xoá.

### 10.3. OrderService / OrderServiceImpl — Async Email & Stock Restoration

- **`create()`**: Trừ stock theo số lượng đặt hàng. Khi tạo đơn hàng thành công, kích hoạt gửi mail xác nhận bất đồng bộ `emailService.sendPaymentSuccessEmail(...)`.
- **`changeStatus()`**: Kiểm tra chuyển trạng thái hợp lệ qua `ALLOWED_ORDERS` map. **Đặc biệt:** Khi đơn hàng bị huỷ (`CANCELLED`), hệ thống tự động hoàn lại stock tương ứng vào kho cho từng sản phẩm.
- **`findByUserIdWithDetails()` & `findById()`**: Kiểm tra ownership — USER chỉ xem đơn hàng của mình, ADMIN xem tất cả.

### 10.4. PaymentService / PaymentServiceImpl — Async Email IPN & Auto Expire

- **`handleVnPayIpn()`**: Kiểm tra chữ ký HMAC-SHA512. Khi VNPay báo `00` (Thành công), chuyển Order sang `CONFIRMED` và gọi bất đồng bộ `emailService.sendPaymentSuccessEmail(...)`.
- **`expiredPayment()`**: `@Scheduled(fixedRate = 120000)` — Mỗi 2 phút quét Payment PENDING > 15 phút → chuyển EXPIRED, Order → CANCELLED.

### 10.5. EmailService / EmailServiceImpl — ★ REFACTORED (Strategy Pattern)

- **`sendTextEmail(EmailRequestDto)`**: `@Async("emailTaskExecutor")` — Gửi mail plain text qua `MailTransport`.
- **`sendHtmlEmail(EmailRequestDto)`**: `@Async("emailTaskExecutor")` — Gửi mail HTML qua `MailTransport`.
- **`sendAttachmentEmail(EmailRequestDto)`**: `@Async("emailTaskExecutor")` — Stub (throw `UnsupportedOperationException`).
- **`sendOrderConfirmation(toEmail, orderId)` & `sendPaymentSuccessEmail(toEmail, orderId)`**: `@Async("emailTaskExecutor")` — Các helper method chuyên biệt phục vụ luồng nghiệp vụ đơn hàng & thanh toán.

> **Kiến trúc mới:** `EmailServiceImpl` inject `MailTransport` interface. Spring tự động chọn implementation theo active profile:
> - **Dev:** `SmtpMailTransport` (`@Profile("dev")`) → dùng `JavaMailSender` gửi qua SMTP Gmail.
> - **Prod:** `BrevoMailTransport` (`@Profile("prod")`) → dùng Brevo REST API (`https://api.brevo.com/v3/smtp/email`).

---

## 11. Controller Layer — API Endpoints

> **Thay đổi nổi bật V6:** `GET /products/{id}` chuyển từ **Authenticated** sang **Public** (thêm vào `PUBLIC_GET_URLS` trong SecurityConfig).

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
| GET | `/{id}` | **Public** (★ ĐỔI từ Authenticated) | Path: id | `ApiResponse<ProductResponseDto>` |
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

### 11.4. AuthController (`/api/v1/auth`) — 4 endpoints

| HTTP | Path | Auth | Response |
|---|---|---|---|
| POST | `/register` | Public | `ApiResponse<UserResponseDto>` |
| POST | `/login` | Public | `ApiResponse<AuthResponseDto>` |
| POST | `/refresh-token` | Public | `ApiResponse<RefreshTokenResponseDto>` |
| POST | `/logout` | Public | `ApiResponse<Void>` |

### 11.5. OrderController (`/api/v1/orders`) — 4 endpoints

| HTTP | Path | Auth | Response |
|---|---|---|---|
| GET | `/user/{userId}` | Authenticated (ownership check) | `ApiResponse<List<OrderResponseDto>>` |
| GET | `/{id}` | Authenticated (ownership check) | `ApiResponse<OrderResponseDto>` |
| POST | `/` | Authenticated | `ApiResponse<OrderResponseDto>` |
| PATCH | `/{id}/status` | ADMIN | `ApiResponse<OrderResponseDto>` |

### 11.6. PaymentController (`/api/v1/payments`) — 3 endpoints

| HTTP | Path | Auth | Response |
|---|---|---|---|
| POST | `/create` | Authenticated | `ApiResponse<CreatePaymentResponseDto>` |
| GET | `/vnpay-return` | Public | Redirect/HTML |
| GET | `/vnpay-ipn` | Public | VNPay IPN response |

---

## 12. Security & Authentication (JWT)

- **Access Token:** JWT Expiration 1h (3600000ms).
- **Refresh Token:** Opaque Random 64 bytes → SHA-256 Hash stored in DB, Expiration 7 days.
- **Security Chain:** `JwtAuthenticationFilter` intercepts, validates token, sets `SecurityContext`.
- **Public Endpoints:** `/auth/**`, `/health`, `/payments/vnpay-*`, Swagger URLs.
- **Public GET:** `/products`, `/products/{id}` (★ MỚI), `/categories`, `/categories/{id}`.
- **CORS:** ★ **Externalized** qua `@Value("${app.cors.allowed-origins}")` — đọc từ `.env`, hỗ trợ multiple origins.

---

## 13. File Upload & Storage (MinIO)

- **Magic Bytes Validation:** `FileValidationUtil` kiểm tra trực tiếp byte header (JPEG, PNG, GIF, WebP) loại bỏ nguy cơ mạo danh định dạng file.
- **Image Resizing:** `Thumbnailator` tự động nén & chuẩn hoá ảnh về kích thước `800x800` (quality 0.85).
- **Docker:** MinIO container riêng trong `docker-compose.yml` (port 9000 API, 9001 Console).
- **Production:** Cloudflare R2 (S3-compatible) thay thế MinIO local.

---

## 14. Payment Integration — VNPay

- **Flow:** `POST /orders` (PENDING) ➔ `POST /payments/create` ➔ VNPay Sandbox ➔ `handleVnPayIpn()` ➔ Order CONFIRMED + Async Email sent.
- **Scheduler:** `@Scheduled(fixedRate = 120000)` — Mỗi 2 phút quét payment PENDING > 15 phút → chuyển EXPIRED, Order → CANCELLED.

---

## 15. Exception Handling

- **GlobalExceptionHandler (`@RestControllerAdvice`)**: Xử lý **15** loại exceptions:

| Exception | HTTP Status |
|---|---|
| `BadRequestException` | 400 |
| `ResourceNotFoundException` | 404 |
| `InsufficientStockException` | 409 |
| `InvalidStatusTransitionException` | 409 |
| `CategoryHasProductsException` | 409 |
| `DuplicateResourceException` | 409 |
| `ObjectOptimisticLockingFailureException` | 409 |
| `DataIntegrityViolationException` | 409 |
| `MaxUploadSizeExceededException` | 400 |
| `MethodArgumentNotValidException` | 400 (+ validation error map) |
| `BadCredentialsException` | 401 |
| `InvalidRefreshTokenException` | 401 |
| `DisabledException` | 403 |
| `AccessDeniedException` | 403 |
| `Exception` (catch-all) | 500 |

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

### 17.1. Email Strategy Pattern Flow — ★ MỚI V6

```
                    ┌────────────────────────────┐
                    │     EmailServiceImpl        │
                    │  @Async("emailTaskExecutor")│
                    └──────────┬─────────────────┘
                               │ inject MailTransport
                    ┌──────────┴─────────────────┐
                    │    MailTransport (interface)  │
                    └──────────┬─────────────────┘
                 ┌─────────────┴─────────────────┐
                 │                               │
    ┌────────────┴────────────┐    ┌─────────────┴────────────┐
    │   SmtpMailTransport      │    │   BrevoMailTransport      │
    │   @Profile("dev")        │    │   @Profile("prod")        │
    │   JavaMailSender/SMTP    │    │   Brevo REST API          │
    │   (Gmail SMTP relay)     │    │   (api.brevo.com/v3/smtp) │
    └─────────────────────────┘    └──────────────────────────┘
```

### 17.2. Async Email Notification Flow

```
1. Client gửi request Đặt hàng / Thanh toán thành công (VNPay IPN)
2. Service xử lý xong DB Transaction (Save Order / Update Status)
3. Service gọi EmailService.sendOrderConfirmation(...) / sendPaymentSuccessEmail(...)
4. Spring AOP Proxy bắt lời gọi → Đẩy task sang ThreadPoolTaskExecutor ("emailTaskExecutor")
5. Main Thread trả HTTP Response lập tức cho Client (Không bị block bởi SMTP/API delay)
6. Worker Thread trong ThreadPool gọi MailTransport implementation (SMTP hoặc Brevo tùy profile)
```

### 17.3. Redis Caching Flow

```
1. Client request GET /api/v1/products/{id}
2. Spring Cache Interceptor kiểm tra key "products::{id}" trong Redis:
   - HIT  ➔ Trả kết quả JSON từ Redis lập tức (Bỏ qua DB & Service logic)
   - MISS ➔ Gọi method ProductServiceImpl.findById() truy vấn DB ➔ Lưu kết quả vào Redis ➔ Trả kết quả cho Client
3. Khi Admin gọi PATCH/DELETE /products/{id}:
   - Executing DB update
   - Interceptor thực thi @CacheEvict(value = "products", key = "#id") ➔ Xoá key tương ứng trong Redis
```

### 17.4. Flyway Migration Flow — ★ MỚI V6

```
1. Spring Boot khởi động → FlywayConfig bean (FlywayMigrationStrategy) được kích hoạt
2. flyway.repair() — Đồng bộ checksum trong flyway_schema_history với file hiện tại
   (KHÔNG chạy lại migration, KHÔNG xoá dữ liệu)
3. flyway.migrate() — Chạy các migration chưa apply
4. JPA Hibernate ddl-auto=validate — Kiểm tra Entity mapping khớp với schema thực tế
```

---

## 18. Coding Conventions & Patterns

| Pattern | Áp dụng ở đâu |
|---|---|
| **Layered Architecture** | Controller → Service → Repository → Entity |
| **Strategy Pattern** | ★ **MỚI V6** — `MailTransport` interface + `SmtpMailTransport` (dev) / `BrevoMailTransport` (prod) |
| **Profile-based DI** | ★ **MỚI V6** — `@Profile("dev")` và `@Profile("prod")` tự động chọn bean theo môi trường |
| **Distributed Caching** | Redis + Spring `@Cacheable` / `@CacheEvict` với TTL linh hoạt |
| **Async Task Execution** | `@Async` + `ThreadPoolTaskExecutor` tách biệt xử lý Email khỏi HTTP Worker Thread |
| **Custom Argument Resolver** | `CustomPageableArgumentResolver` tự động bind query params vào Spring `Pageable` |
| **Optimistic Locking** | `@Version` trên `ProductEntity` + `OrderEntity` chống Race Condition |
| **Magic Bytes Validation** | `FileValidationUtil` kiểm tra byte header file upload |
| **Object Storage (S3-compatible)** | MinIO SDK lưu trữ media tách biệt |
| **Static Analysis** | ★ **MỚI V6** — PMD plugin (`maven-pmd-plugin` 3.26.0) tích hợp CI Pipeline |
| **Flyway Repair Strategy** | ★ **MỚI V6** — `FlywayConfig` bean gọi `repair()` trước `migrate()` |

---

## 19. Trạng thái phát triển & TODO

### 19.1. Đã hoàn thành ✅

- [x] Layered Architecture chuẩn
- [x] CRUD đầy đủ: Category, Product, Order, User
- [x] JWT Authentication + Refresh Token (stateless)
- [x] Role-based Authorization (USER/ADMIN)
- [x] VNPay Payment Integration (sandbox) + Auto Expire Scheduled Job
- [x] Optimistic Locking (`@Version` trên Product & Order)
- [x] Flyway Database Migrations (★ hợp nhất thành 2 migrations — schema + seed data)
- [x] Docker Multi-stage + Docker Compose (4 services: db, app, redis, minio)
- [x] GitHub Actions CI (★ PMD Quality Gate + Test + Build)
- [x] MinIO file upload + magic bytes validation + Thumbnailator image resize
- [x] **Redis Distributed Caching** (`@Cacheable`, `@CacheEvict` cho Products, Categories, Analytics)
- [x] **Async Email Processing** (`@Async`, `ThreadPoolTaskExecutor`, MailTransport Strategy Pattern)
- [x] **Email Strategy Pattern** (★ MỚI — SmtpMailTransport dev / BrevoMailTransport prod)
- [x] **Chuẩn hoá Pagination** (`PageResponse<T>` & `CustomPageableArgumentResolver`)
- [x] **Hoàn Stock khi Cancel Order** (Tự động cộng bù kho khi huỷ đơn)
- [x] **CORS Externalized** (★ MỚI — đọc từ `.env` qua `@Value`)
- [x] **PMD Static Analysis** (★ MỚI — custom ruleset + CI integration)
- [x] **FlywayConfig** (★ MỚI — repair before migrate strategy)
- [x] **Production Config** (★ MỚI — `.env.production`, `deploy_render_plan.md`)

### 19.2. Chưa làm / Cần cải thiện ⚠️

- [ ] **Unit Tests** — Viết JUnit 5 + Mockito cho Service Layer
- [ ] **Cart (Giỏ hàng)** — Module quản lý giỏ hàng lưu trữ Database / Redis
- [ ] **Search nâng cao** — Filter sản phẩm theo khoảng giá (`minPrice`, `maxPrice`) và danh mục
- [ ] **TestUploadController** — Ẩn hoặc phân quyền ADMIN trước khi deploy Production
- [ ] **sendAttachmentEmail** — Triển khai gửi email có đính kèm file (hiện đang throw UnsupportedOperationException)
- [ ] **Frontend UI** — Xây dựng giao diện người dùng

---

## 20. Góp ý tối ưu & Bảng theo dõi cải tiến

| # | Vấn đề ban đầu | Trạng thái | Giải pháp đã áp dụng |
|---|---|---|---|
| 1 | **PageResponse chưa được sử dụng** | ✅ **ĐÃ XỬ LÝ** | Áp dụng `PageResponse.of(Page<T>)` cho tất cả GET Controllers có phân trang. |
| 2 | **Pagination parameters rời rạc** | ✅ **ĐÃ XỬ LÝ** | Xây dựng `CustomPageableArgumentResolver` & `WebConfig` parse tự động. |
| 3 | **Order cancelled chưa hoàn stock** | ✅ **ĐÃ XỬ LÝ** | Thêm logic tự động hoàn stock sản phẩm trong `OrderServiceImpl.changeStatus()`. |
| 4 | **Email thông báo bị chậm HTTP Request** | ✅ **ĐÃ XỬ LÝ** | Tách luồng gửi mail sang `@Async` với `emailTaskExecutor` Thread Pool riêng. |
| 5 | **Truy vấn DB lặp lại nhiều lần** | ✅ **ĐÃ XỬ LÝ** | Redis Caching cho Product detail, Category list và Analytics reports. |
| 6 | **CORS Hardcoded** | ✅ **ĐÃ XỬ LÝ (V6)** | ★ Chuyển CORS config sang đọc từ `.env` qua `@Value("${app.cors.allowed-origins}")`. |
| 7 | **Email chỉ có 1 transport** | ✅ **ĐÃ XỬ LÝ (V6)** | ★ Áp dụng Strategy Pattern: `MailTransport` interface + 2 implementations theo `@Profile`. |
| 8 | **Flyway 8 migrations rời rạc** | ✅ **ĐÃ XỬ LÝ (V6)** | ★ Hợp nhất thành 2 files + `FlywayConfig` bean auto repair. |
| 9 | **Không có Static Analysis** | ✅ **ĐÃ XỬ LÝ (V6)** | ★ PMD plugin + custom ruleset + CI Quality Gate. |
| 10 | **Thiếu Unit Test** | 🟡 **CẦN LÀM** | Chuẩn bị viết test coverage cho Service & Controller layers bằng JUnit 5 + Mockito. |

---

## 21. Tổng hợp Kiến thức & Kỹ thuật chức năng cốt lõi

### 21.1. DevOps, Containerization & CI/CD
- **Multi-stage Docker Build:** Tách giai đoạn `builder` (`eclipse-temurin:21-jdk-alpine`) và `runtime` (`eclipse-temurin:21-jre-alpine`) giảm dung lượng image, tối ưu layer cache.
- **Docker Compose 4 Services:** Orchestrate `db` (PostgreSQL 16), `app` (Spring Boot), `redis` (cache), `minio` (object storage) với healthcheck và dependency ordering.
- **GitHub Actions CI Pipeline:** 3-step pipeline: **PMD Quality Gate** (fail fast) → **Unit Tests** → **Build Compile Check**.
- **PMD Static Analysis:** Custom ruleset bắt code chết (unused variables/fields/methods) và lỗi nguy hiểm (empty catch blocks).

### 21.2. Strategy Pattern — Profile-based Email Transport (★ MỚI V6)
- **Interface Segregation:** `MailTransport` interface định nghĩa 2 method `sendTextEmail()` và `sendHtmlEmail()`.
- **Profile-based DI:** Spring tự động inject implementation phù hợp dựa trên `@Profile`:
  - `dev` → `SmtpMailTransport` (JavaMailSender, Gmail SMTP relay)
  - `prod` → `BrevoMailTransport` (Brevo REST API, không cần SMTP server)
- **Lợi ích:** Không cần `if/else` chọn transport, code sạch, dễ thêm provider mới (VD: SendGrid, AWS SES).

### 21.3. Flyway Migration Consolidation (★ MỚI V6)
- **Hợp nhất Migrations:** 8 files incremental (V1→V8) → 2 files (V1 schema + V2 seed data).
- **FlywayConfig Bean:** `FlywayMigrationStrategy` gọi `repair()` trước `migrate()` để xử lý checksum mismatch khi file migration thay đổi nội dung.
- **An toàn:** `repair()` chỉ cập nhật metadata trong `flyway_schema_history` — KHÔNG chạy lại migration hay xoá dữ liệu.

### 21.4. Redis Distributed Caching Strategy
- **Cache Abstraction:** Sử dụng `@Cacheable` và `@CacheEvict` decouple logic ứng dụng khỏi cache provider.
- **Granular TTLs:** `RedisCacheManager` định cấu hình thời gian sống khác nhau cho từng cache region (Products: 30m, Categories: 30m, Analytics: 5m).
- **Cache Invalidation:** Xoá sạch hoặc xoá theo key chính xác khi có thao tác Mutation.

### 21.5. Async Task Processing & Thread Pool Isolation
- **Non-blocking Execution:** Áp dụng `@Async` giúp giải phóng worker thread đảm nhận request HTTP ngay lập tức.
- **ThreadPool Task Executor:** Cấu hình `emailTaskExecutor` tùy chỉnh (`corePoolSize=3`, `maxPoolSize=10`, `queueCapacity=50`, `threadNamePrefix="EmailAsync-"`).

### 21.6. Object Storage & File Security (MinIO)
- **Magic Bytes Validation:** `FileValidationUtil` kiểm tra trực tiếp byte header (JPEG, PNG, GIF, WebP) loại bỏ nguy cơ fake extension.
- **Image Optimization:** Sử dụng `Thumbnailator` nén & resize về kích thước `800x800` (quality 0.85).
- **Production Migration:** Cloudflare R2 (S3-compatible) thay thế MinIO local — chỉ cần đổi endpoint/credentials trong `.env.production`.

### 21.7. Security & Stateless Authentication
- **Dual-Token Strategy:** Access Token JWT (1h) + Opaque Refresh Token (7 ngày, SHA-256 hashed in DB).
- **Custom Security EntryPoints:** Phản hồi chuẩn format JSON cho 401 Unauthorized và 403 Access Denied.
- **CORS Externalized:** ★ **MỚI V6** — Domain whitelist đọc từ `.env`, không hardcode trong source code.

### 21.8. Production Deployment Preparation (★ MỚI V6)
- **Render Platform:** PostgreSQL + Redis (Key Value) managed services.
- **Cloudflare R2:** S3-compatible object storage thay thế MinIO.
- **Brevo API:** Transactional email API thay thế Gmail SMTP (no rate limit, production-grade).
- **Dynamic Port:** `server.port = ${PORT:8085}` — Render cung cấp port qua biến môi trường.

---

> **🎯 Kết luận:** Dự án Mini Ecommerce đã được nâng cấp toàn diện với **35 API Endpoints**, **2 Flyway Migrations (hợp nhất)**, **Redis Distributed Caching**, **Async Email Processing (Strategy Pattern)**, **MinIO Storage**, **VNPay Sandbox Integration**, **PMD Static Analysis**, **CI Pipeline 3-step**, **CORS Externalized**, và **Production Deployment Preparation (Render + Cloudflare R2 + Brevo)**. Các bước tiếp theo sẽ tập trung vào **Unit Testing (JUnit 5 + Mockito)** và xây dựng **Frontend UI**.
