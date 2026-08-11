# 🎯 PLAN KỸ THUẬT TỔNG HỢP V4 — MINI E-COMMERCE ARCHITECTURE

> **Triết lý:** Technical vững → Business dễ thở.
> **Mục tiêu:** Chuẩn hóa toàn bộ kiến thức kỹ thuật theo **phiên bản thực tế trong `pom.xml` (Java 21, Spring Boot 3.5.x, JJWT 0.12.6, Jakarta EE 10, SpringDoc 2.8.17)**.
> **Sắp xếp:** Chia theo **Tầng độ khó kỹ thuật (từ Dễ/Thấp → Trung Bình → Nâng Cao/Phức Tạp)**. Không chia theo ngày/tuần/giờ.
> **Tài liệu tham khảo uy tín:** Official Spring Security 6 & Spring Boot 3 Docs, Vlad Mihalcea's Hibernate Best Practices, Docker Compose Specification v2 & Jenkins Declarative Pipeline Guides.

---

## 📌 BẢNG ĐỐI CHIẾU THÔNG SỐ CÔNG NGHỆ THỰC TẾ TRONG `POM.XML`

| Thành phần | Phiên bản trong `pom.xml` | Chuẩn kỹ thuật & Best Practice thực chiến |
| :--- | :--- | :--- |
| **Java Version** | `Java 21 (LTS)` | Tận dụng **Java Records** cho DTOs bất biến (`public record ProductResponse(...) {}`), Pattern Matching cho `switch`/`instanceof`, và khả năng bật **Virtual Threads** (`spring.threads.virtual.enabled=true`). |
| **Spring Boot Parent** | `Spring Boot 3.5.x` | Chuẩn **Jakarta EE 10** (`jakarta.persistence.*`, `jakarta.validation.*`, `jakarta.servlet.*`). Tuyệt đối không dùng package `javax.*` đã lỗi thời. |
| **JWT Library** | `JJWT 0.12.6` | Sử dụng cú pháp jjwt 0.12.x mới nhất: `Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)` thay cho cú pháp `setSigningKey()` cũ đã deprecated. |
| **OpenAPI / Swagger** | `SpringDoc 2.8.17` | Phân nhóm Swagger bằng `GroupedOpenApi` bean cho 3 nhóm API (`Public`, `User`, `Admin`). |
| **Database & Migration**| `PostgreSQL + Flyway` | Migration qua `flyway-database-postgresql`, không sửa file migration đã release. |
| **Code Quality** | `PMD Plugin 3.26.0` | Tích hợp static code analysis với file quy tắc custom `pmd-ruleset.xml`. |

---

## 📊 BẢN ĐỒ TỔNG THỂ 4 TẦNG ĐỘ KHÓ KỸ THUẬT

| Tầng (Level) | Chủ đề & Khối Kiến Thức (Technical Knowledge Block) | Mức độ phức tạp | Mục đích chính |
| :--- | :--- | :--- | :--- |
| **LEVEL 1** | **Quick Wins (Task dễ - Tác động ngay)**<br>1. Externalize Cron Config<br>2. Restructure Docs Folder<br>3. Group Controllers (Admin/User/Public)<br>4. BaseEntity & JPA Auditing | 🟢 **Dễ (Low)** | Làm sạch code, chuẩn hóa cấu trúc cơ bản, hoàn thiện nền tảng security. |
| **LEVEL 2** | **Core Data & Domain Architecture**<br>5. JPA/Hibernate Deep Dive & Association Mapping (Vlad Mihalcea)<br>6. Modularization by Domain (Feature-based structure)<br>7. Dynamic RBAC under DB | 🟡 **Trung bình (Medium)** | Tối ưu hóa truy vấn DB, loại bỏ N+1, chống race condition, sẵn sàng tách Microservice. |
| **LEVEL 3** | **Advanced Infrastructure & Security**<br>8. Redis Token Device/IP Binding & Session Management<br>9. Rate Limiting (Bucket4j + Redis)<br>10. CI/CD Multi-Env (Jenkins + Docker Compose v2 for Dev/Stg/Prod) | 🟠 **Khá/Cao (High)** | Chống brute-force, bảo vệ API, theo dõi phiên đăng nhập đa thiết bị, tự động hóa CI/CD. |
| **LEVEL 4** | **Complex Distributed Systems**<br>11. Maven Multi-Module Project (`core`, `api`, `web`, `scheduler`)<br>12. Single Sign-On (SSO) OAuth2/OIDC<br>13. Kafka & Event-Driven Architecture | 🔴 **Phức tạp (Advanced)** | Scale kiến trúc phân tán, tách instance deploy độc lập, giao tiếp event-driven bất đồng bộ. |

---

## 🛠️ CHI TIẾT TỪNG TẦNG KIẾN THỨC KỸ THUẬT

---

### 🟢 LEVEL 1: QUICK WINS (TASK DỄ & CẤP THIẾT - ĐỘ KHÓ THẤP)

#### 1. Externalize Cron Config vào `.yml` / `.properties`
* **Vấn đề cũ:** Hardcode biểu thức cron trong annotation `@Scheduled(cron = "0 0 * * * *")`.
* **Chuẩn hóa kỹ thuật:**
  * Đưa cấu hình cron vào `application.yml`:
    ```yaml
    app:
      scheduler:
        otp-cleanup:
          cron: "${OTP_CLEANUP_CRON:0 0 * * * *}"
        payment-expired:
          cron: "${PAYMENT_EXPIRED_CRON:0 */2 * * * *}"
    ```
  * Sử dụng trong Java:
    ```java
    @Scheduled(cron = "${app.scheduler.otp-cleanup.cron}")
    public void cleanupExpiredOtp() { ... }
    ```
* **Lợi ích:** Cho phép thay đổi chuỗi cron động qua biến môi trường Docker/Jenkins mà không cần recompile source code.

#### 2. Chuẩn hóa cấu trúc thư mục Documentation (`docs/`)
* **Vấn đề cũ:** Folder `docs/` nằm trong package code hoặc lộn xộn trong source tree.
* **Chuẩn hóa kỹ thuật:**
  * Đưa toàn bộ thư mục `docs/` ra ngoài gốc của repository (root directory):
    ```text
    mini-ecommerce/
    ├── docs/                       <-- Nằm ngoài src, chứa tài liệu kiến trúc & API
    │   ├── architecture.md
    │   ├── plan_ky_thuat_tong_hop_v4.md
    │   └── refactor_plan.md
    ├── src/                        <-- Chỉ chứa duy nhất source code ứng dụng
    │   ├── main/
    │   └── test/
    ├── pom.xml
    └── docker-compose.yml
    ```

#### 3. Mô-đun hóa Cấu trúc Thư mục (Spring Modulith Convention & Package-by-Feature)
* **Vấn đề cũ:** Gom chung tất cả Controller, Service, Repository, Entity vào các thư mục kỹ thuật phẳng (`controller/`, `service/`, `entity/`, `repository/`, `dto/`).
* **Chuẩn hóa kỹ thuật (Spring Modulith Convention):**
  * Tách toàn bộ ứng dụng thành các **Application Modules** theo miền nghiệp vụ (`auth/`, `category/`, `product/`, `order/`, `payment/`, `user/`, `storage/`, `notification/`).
  * Áp dụng quy chuẩn đóng gói nghiêm ngặt:
    * **Root Package của Module** (VD: `com.devfat.mini_ecommerce.product`): Đóng vai trò là **Public API** (chứa Controller, Service Interface, Public DTOs, `package-info.java`).
    * **Package `internal/`** (VD: `com.devfat.mini_ecommerce.product.internal`): Giấu kín phần implementation (`ServiceImpl`, `Repository`, `Entity`, `Mapper`).
    * **Package `exception/`** (VD: `com.devfat.mini_ecommerce.product.exception`): Chứa các Exception nghiệp vụ riêng của từng domain.
    * **Package `shared/`**: Chứa hạ tầng dùng chung dạng **Open Module** (`@ApplicationModule(type = OPEN)`). Chứa `base/`, `config/`, `exception/`, `security/`, `util/` và `PingController.java`.
  * **Định hướng Phụ thuộc 100% Sạch (Clean Dependency Direction):**
    1. **`BusinessException` (Shared Exception Base):** `shared/exception/BusinessException.java` đóng vai trò là class cha của mọi Exception. `GlobalExceptionHandler` chỉ phụ thuộc `BusinessException` (0 import về Domain).
    2. **Khắc phục Circular Dependency (`shared` ↔ `user`):** `CustomUserDetailsService` đưa vào `user/internal/`. `UserPrincipal` trong `shared/security/` là **Security DTO thuần túy** (không phụ thuộc `UserEntity`). `JwtAuthenticationFilter` sử dụng `UserDetailsService` abstraction từ Spring Security.
    3. **Domain Config về đúng Module:** `VNPayConfig` ở `payment/internal/`, `MinioConfig` ở `storage/internal/`.
  * **Cấu hình Spring Security 6:**
    ```java
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/categories/**").permitAll()
                .requestMatchers("/api/v1/auth/**", "/api/v1/payments/vnpay-return", "/api/v1/payments/vnpay-ipn").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
    ```



#### 4. BaseEntity & JPA Auditing (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`)
* **Vấn đề cũ:** Mỗi Entity tự định nghĩa field timestamp riêng, thiếu thông tin vết người tạo/sửa.
* **Chuẩn hóa kỹ thuật (Jakarta EE 10 - `jakarta.persistence.*`):**
  * Tạo `@MappedSuperclass BaseEntity`:
    ```java
    import jakarta.persistence.*;
    import org.springframework.data.annotation.CreatedBy;
    import org.springframework.data.annotation.CreatedDate;
    import org.springframework.data.annotation.LastModifiedBy;
    import org.springframework.data.annotation.LastModifiedDate;
    import org.springframework.data.jpa.domain.support.AuditingEntityListener;

    @Getter
    @Setter
    @MappedSuperclass
    @EntityListeners(AuditingEntityListener.class)
    public abstract class BaseEntity {
        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate
        @Column(name = "updated_at", insertable = false)
        private LocalDateTime updatedAt;

        @CreatedBy
        @Column(name = "created_by", updatable = false)
        private String createdBy;

        @LastModifiedBy
        @Column(name = "updated_by", insertable = false)
        private String updatedBy;
    }
    ```
  * Cấu hình `AuditorAware<String>` lấy User từ `SecurityContextHolder`:
    ```java
    @Component
    public class SecurityAuditorAware implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return Optional.of("SYSTEM");
            }
            return Optional.ofNullable(auth.getName());
        }
    }
    ```
  * Kích hoạt qua `@EnableJpaAuditing` ở Configuration Class.

---

### 🟡 LEVEL 2: CORE DATA & DOMAIN ARCHITECTURE (ĐỘ KHÓ TRUNG BÌNH)

#### 5. Hibernate/JPA Deep Dive & Entity Mapping Best Practices (Theo Vlad Mihalcea)
* **Quy tắc 1: Tránh Lombok `@Data` & `@EqualsAndHashCode` trên Entity**
  * `@Data` tạo `equals()` và `hashCode()` dựa trên tất cả các thuộc tính. Khi Entity ở trạng thái Transient (id=null) chuyển sang Managed (id có giá trị), hashCode bị thay đổi -> Hỏng cấu trúc `Set` và `Map`.
  * **Best practice chuẩn Vlad Mihalcea:**
    ```java
    @Getter @Setter
    @Entity
    public class ProductEntity extends BaseEntity {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProductEntity that)) return false;
            return id != null && id.equals(that.getId());
        }

        @Override
        public int hashCode() {
            return getClass().hashCode(); // Constant hashCode chuẩn Hibernate
        }
    }
    ```
* **Quy tắc 2: Quan hệ `@ManyToOne` là Owner & LUÔN LUÔN để `FetchType.LAZY`**
  * Mặc định `@ManyToOne` là `EAGER` -> Gây ra N+1 query thảm họa. Phải khai báo `fetch = FetchType.LAZY` tường minh.
* **Quy tắc 3: Collection Mapping (`List` vs `Set`) & Xử lý `MultipleBagFetchException`**
  * Trong Hibernate 6, `List` đại diện cho Bag (tập hợp không có thứ tự và cho phép trùng).
  * Nếu JOIN FETCH đồng thời 2 `List`, Hibernate ném `MultipleBagFetchException`.
  * **Giải pháp chuẩn:** Không đổi ép sang `Set` một cách mù quáng (tránh Cartesian Product Problem). Hãy dùng **Query Splitting** (Fetch từng tập hợp ở 2 query riêng biệt) hoặc cấu hình Batch Size:
    ```yaml
    spring:
      jpa:
        properties:
          hibernate:
            default_batch_fetch_size: 20
    ```
* **Quy tắc 4: Tối ưu N+1 Query với DTO Projections (Dùng Java 21 Records)**
  * Khai báo Record DTO:
    ```java
    public record ProductSummaryDto(Long id, String name, BigDecimal price, String categoryName) {}
    ```
  * Query JPQL trực tiếp về Java Record:
    ```java
    @Query("SELECT new com.devfat.product.dto.ProductSummaryDto(p.id, p.name, p.price, c.name) " +
           "FROM ProductEntity p JOIN p.category c")
    List<ProductSummaryDto> findAllSummaries();
    ```

#### 6. Module hóa theo Domain Architecture (Feature-Based / DDD Lite)
* **Tư tưởng:** Gom tất cả các lớp thuộc cùng một nghiệp vụ (Domain) vào một thư mục cha. Sau này muốn tách Microservice hoặc Maven Module chỉ cần **Cut - Paste** thư mục đó.
* **Cấu trúc chi tiết từng Domain Module:**
  ```text
  com.devfat.mini_ecommerce/
  ├── vnpay/                           <-- Domain Module VNPay
  │   ├── controller/
  │   ├── service/
  │   ├── repository/
  │   ├── dao/
  │   ├── model/ (Entity)
  │   └── dto/ (Java 21 Records)
  ├── product/                         <-- Domain Module Product
  │   ├── controller/
  │   ├── service/
  │   ├── repository/
  │   ├── dao/
  │   ├── model/
  │   └── dto/
  ├── category/                        <-- Domain Module Category
  │   ├── controller/
  │   ├── service/
  │   ├── repository/
  │   ├── dao/
  │   ├── model/
  │   └── dto/
  ├── redis/                           <-- Infrastructure Module Redis
  │   ├── service/
  │   ├── config/
  │   └── dto/
  └── shared/                          <-- Dung chung (BaseEntity, Exception, Utils)
  ```
* **Nguyên tắc giao tiếp:** Domain Product KHÔNG import trực tiếp Entity của Category. Giao tiếp qua Service Interface hoặc DTO.

#### 7. Dynamic RBAC (Role-Based Access Control) lưu trữ dưới Database
* **Mô hình Database ERD:**
  ```text
  users (id, email, password, ...)
    │
  user_roles (user_id, role_id)
    │
  roles (id, name, description)           <-- 'ADMIN', 'USER', 'MANAGER'
    │
  role_permissions (role_id, permission_id)
    │
  permissions (id, name, description)     <-- 'product:create', 'product:delete', 'order:view'
  ```
* **Tích hợp Spring Security 6:**
  * Trích xuất các `permissions` của User và map thành `SimpleGrantedAuthority`:
    ```java
    Set<GrantedAuthority> authorities = user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .map(permission -> new SimpleGrantedAuthority(permission.getName()))
        .collect(Collectors.toSet());
    ```
  * Phân quyền chi tiết trên Method với `@PreAuthorize`:
    ```java
    @PreAuthorize("hasAuthority('product:create')")
    @PostMapping
    public ResponseEntity<?> createProduct(...) { ... }
    ```

---

### 🟠 LEVEL 3: ADVANCED INFRASTRUCTURE & SECURITY (ĐỘ KHÓ KHÁ / CAO)

#### 8. Redis Session Management & Token Device/IP Binding (JJWT 0.12.6 API Standard)
* **Vấn đề:** JWT là stateless, khó thu hồi token khi bị lộ hoặc quản lý nhiều thiết bị đăng nhập.
* **Tích hợp JJWT 0.12.6:**
  * Sinh Token với Claim `deviceId`:
    ```java
    String accessToken = Jwts.builder()
        .subject(user.getEmail())
        .claim("deviceId", deviceId)
        .issuedAt(new Date())
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
    ```
  * Parse Token chuẩn 0.12.x:
    ```java
    Claims claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    ```
* **Giải pháp Redis Hash Structure:**
  * **Redis Key:** `session:{userId}:{deviceFingerprint}`
  * **Data Fields:** `refreshTokenHash`, `ipAddress`, `userAgent`, `deviceName`, `loginAt`, `lastActiveAt`.
* **Quy trình hoạt động:**
  1. Khi Login: Tạo `deviceFingerprint` từ `hash(User-Agent + partial-IP)`. Lưu session info vào Redis Hash với TTL = 7 ngày.
  2. Khi Validate Request: Filter kiểm tra `session:{userId}:{deviceFingerprint}` có tồn tại trong Redis không. Nếu không -> Trả 401 Unauthorized (Token revoked).
  3. Quản lý phiên: 
     * `GET /api/v1/user/sessions`: Xem danh sách thiết bị đang active.
     * `DELETE /api/v1/user/sessions/{deviceId}`: Force Logout thiết bị từ xa (Xóa key trong Redis).

#### 9. Rate Limiting & API Protection với Bucket4j + Redis
* **Bổ sung dependency trong `pom.xml`:**
  ```xml
  <dependency>
      <groupId>com.bucket4j</groupId>
      <artifactId>bucket4j-redis</artifactId>
      <version>8.10.1</version>
  </dependency>
  ```
* **Thuật toán sử dụng:** Token Bucket Algorithm via `bucket4j-redis`.
* **Cấu hình giới hạn theo từng nhóm API:**
  * `POST /api/v1/public/auth/login`: 5 requests / 15 phút / IP (Chống Brute-force).
  * `POST /api/v1/public/auth/verify-otp`: 3 requests / 5 phút / IP.
  * `GET /api/v1/public/products`: 60 requests / 1 phút / IP (Chống Spam/Crawl).
* **HTTP Response khi vượt ngưỡng:** Trả về `429 Too Many Requests` kèm Header `Retry-After: <seconds>`.

#### 10. CI/CD Pipeline với Jenkins & Docker Compose Multi-Environment (`dev`, `stg`, `prod`)
* **Kiến trúc Môi trường (Multi-Env Setup):**
  * Sử dụng chuẩn **Docker Compose v2** (Không dùng thuộc tính `version: '3.8'` đã deprecated).
  * File cấu hình base: `docker-compose.yml`
  * Variable files cho từng môi trường: `.env.dev`, `.env.stg`, `.env.prod`.
* **Cấu hình `docker-compose.yml` chuẩn hiện đại:**
  ```yaml
  services:
    app:
      build:
        context: .
        dockerfile: Dockerfile
      environment:
        - SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}
        - DB_URL=${DATABASE_URL}
        - DB_USER=${DATABASE_USER}
        - DB_PASS=${DATABASE_PASSWORD}
        - REDIS_HOST=${REDIS_HOST}
      ports:
        - "${APP_PORT}:8080"
      restart: always
  ```
* **Cấu hình `Jenkinsfile` (Declarative Pipeline):**
  ```groovy
  pipeline {
      agent any
      parameters {
          choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Deploy Environment')
      }
      environment {
          DOCKER_ENV_FILE = ".env.${params.ENVIRONMENT}"
      }
      stages {
          stage('Checkout & Validate') {
              steps {
                  echo "Building for environment: ${params.ENVIRONMENT}"
              }
          }
          stage('Build Artifact') {
              steps {
                  sh './mvnw clean package -DskipTests'
              }
          }
          stage('Docker Build & Deploy') {
              steps {
                  sh "docker compose --env-file ${DOCKER_ENV_FILE} up -d --build"
              }
          }
      }
  }
  ```

---

### 🔴 LEVEL 4: COMPLEX DISTRIBUTED SYSTEMS (ĐỘ KHÓ PHỨC TẠP)

#### 11. Maven Multi-Module Project Architecture
* **Cấu hình cây Module:**
  ```text
  mini-ecommerce-parent/                  <-- Parent POM (packaging = pom)
  ├── pom.xml
  ├── ecommerce-core/                     <-- JAR: Entities, Repositories, Domain Services
  │   └── pom.xml
  ├── ecommerce-api/                      <-- Spring Boot Executable JAR: REST Controllers cho FE/Mobile
  │   └── pom.xml (depends on: core)
  ├── ecommerce-web/                      <-- Spring Boot Executable JAR: Web Admin (Thymeleaf/Admin Controller)
  │   └── pom.xml (depends on: core)
  └── ecommerce-scheduler/                <-- Spring Boot Executable JAR: Background Jobs & Scheduled Tasks
      └── pom.xml (depends on: core)
  ```
* **Phân tích Đánh giá Kỹ thuật:**
  * **Điểm mạnh:** Đóng gói các artifact riêng biệt. Có thể deploy riêng `ecommerce-scheduler` thành 1 instance độc lập để không tốn tài nguyên server API.
  * **Điểm yếu:** Quản lý dependency graph phức tạp hơn, tốn thời gian build từ root POM.

#### 12. Single Sign-On (SSO) với OAuth2 / OpenID Connect
* **Bổ sung dependency trong `pom.xml`:**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-client</artifactId>
  </dependency>
  ```
* **Công nghệ:** Spring Security 6 OAuth2 Client.
* **Luồng tích hợp (Authorization Code Flow with PKCE):**
  1. Frontend bấm "Login with Google" -> Redirect tới Google OAuth2 Server.
  2. User đồng ý cấp quyền -> Google trả Authorization Code về Backend Callback.
  3. Backend đổi Code lấy Google Access Token & Id Token.
  4. Trích xuất thông tin Google Profile -> Tạo/Update `UserEntity` trong DB -> Cấp phát Hệ thống JWT (Access Token + Refresh Token) về cho Client.

#### 13. Kafka & Event-Driven Architecture
* **Bổ sung dependency trong `pom.xml`:**
  ```xml
  <dependency>
      <groupId>org.springframework.kafka</groupId>
      <artifactId>spring-kafka</artifactId>
  </dependency>
  ```
* **Ứng dụng:** Xử lý bất đồng bộ các sự kiện quan trọng trong E-Commerce (Order Created, Payment Completed, Email Trigger).
* **Mô hình kiến trúc:**
  ```text
  [Order Service] ──(Publish Event)──> [Kafka Topic: order-created-events]
                                              │
                                   ┌──────────┴──────────┐
                                   ▼                     ▼
                        [Email Consumer Service]  [Inventory Consumer]
  ```
* **Transactional Outbox Pattern:** Lưu Event vào bảng `outbox_events` trong cùng DB Transaction với Order. Một Relay Process (Debezium hoặc Scheduled Job) đọc bảng Outbox và publish lên Kafka -> Đảm bảo **At-Least-Once Delivery** không bao giờ bị đứt gãy sự kiện.

---

## ❓ OPEN QUESTIONS / THẢO LUẬN THIẾT KẾ

> [!NOTE]
> 1. **Về Java 21 Virtual Threads:** Bạn có muốn bật `spring.threads.virtual.enabled=true` trong `application.yml` để tăng throughput truy vấn I/O không?
> 2. **Về Maven Multi-Module:** Bạn muốn giữ cấu trúc Single Module với Domain Packaging (Level 2.6) trước để phát triển nhanh chóng, hay chuyển đổi sang Maven Multi-Module (Level 4.11) ngay?

---

## 🔍 HƯỚNG DẪN KIỂM THỨC & VERIFICATION

1. **Verify Level 1 & 2:**
   * Chạy thống kê SQL Hibernate (`hibernate.generate_statistics=true`) để kiểm tra N+1 query.
   * Kiểm tra DB table `created_at`, `created_by` được tự động điền bởi JPA Auditing.
2. **Verify Level 3 (Redis & Rate Limit):**
   * Sử dụng Postman / Apache JMeter gửi 10 request liên tiếp trong 1 giây đến `/auth/login` -> Đảm bảo từ request thứ 6 nhận response 429.
   * Kiểm tra Redis CLI với lệnh `HGETALL session:{userId}:{deviceId}` hiển thị đủ metadata.
3. **Verify Level 3 (Jenkins Multi-Env):**
   * Trigger Jenkins job với parameter `ENVIRONMENT=dev` -> Kiểm tra docker container sử dụng đúng cấu hình `.env.dev`.
