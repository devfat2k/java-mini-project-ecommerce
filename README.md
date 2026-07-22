<div align="center">

# 🛒 Mini E-Commerce Backend API

> **A modern, high-performance, and secure RESTful E-Commerce Backend service built with Java 21 & Spring Boot 3.5.**  
> Designed following **Clean Layered Architecture**, **Production Best Practices**, and **DevOps Standards**.

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![MinIO](https://img.shields.io/badge/MinIO-S3%20Compatible-C72C48?style=for-the-badge&logo=minio)](https://min.io/)
[![CI/CD](https://img.shields.io/badge/GitHub%20Actions-Passing-2088FF?style=for-the-badge&logo=githubactions)](https://github.com/)

[📖 Features](#-tính-năng-nổi-bật--key-features) • [🏛 Architecture](#-kiến-trúc--tech-stack) • [📂 Project Structure](#-cấu-trúc-thư-mục-project-structure) • [🌐 API Docs](#-danh-sách-api-endpoints) • [🚀 Quick Start](#-hướng-dẫn-cài-đặt--chạy-dự-án)

---

</div>

## 📌 Tổng Quan Dự Án (Project Overview)

**Mini E-Commerce Backend** là dự án xây dựng hệ thống quản lý bán hàng thương mại điện tử chuẩn **RESTful JSON API**, thiết kế theo kiến trúc phân lớp (**Layered Architecture**), hoàn toàn **Stateless** và tuân thủ các nguyên tắc thiết kế phần mềm hiện đại.

Dự án được xây dựng nhằm đáp ứng đầy đủ các yêu cầu nghiệp vụ thực tế của một nền tảng E-Commerce: **Xác thực đa tầng (JWT + Refresh Token)**, **Quản lý danh mục & sản phẩm**, **Đặt hàng & State Machine đơn hàng**, **Tích hợp cổng thanh toán VNPay**, **Lưu trữ ảnh trên Object Storage (MinIO)** với kiểm tra bảo mật file **Magic Bytes**, và **Báo cáo thống kê kinh doanh**.

### 🔥 Highlights & Best Practices Được Áp Dụng

- 🔒 **Dual-Token Authentication System:** Access Token ngắn hạn (1h) kết hợp với Opaque Refresh Token dài hạn (7 ngày) được **mã hóa SHA-256** khi lưu trong DB, hỗ trợ Thu hồi token (Revocation) tức thì.
- 💳 **Tích Hợp VNPay Payment Gateway:** Xử lý chữ ký số **HMAC-SHA512**, IPN Webhook callback Idempotency, kết hợp `@Scheduled` background task tự động hủy đơn hết hạn sau 15 phút.
- ☁️ **S3-Compatible Storage (MinIO) & Magic Bytes Validation:** Kiểm tra trực tiếp file header signature (loại bỏ triệt để nguy cơ bypass qua đuôi file giả mạo) và nén/resize ảnh chuẩn 800x800 qua `Thumbnailator`.
- ⚡ **Optimistic Locking (`@Version`):** Ngăn chặn triệt để xung đột Race Condition khi nhiều người dùng cùng mua hàng/trừ kho đồng thời.
- 📊 **JPQL & Interface-based Projections:** Xử lý triệt để lỗi **N+1 Query** bằng `JOIN FETCH`, tối ưu hóa truy vấn báo cáo doanh thu & sản phẩm bán chạy.
- 🐳 **DevOps & Production Ready:** Multi-stage Docker build (giảm dung lượng image JRE-alpine), Docker Compose với Healthcheck (`service_healthy`), Flyway DB migration (7 versions), và GitHub Actions CI pipeline.

---

## 🛠️ Kiến Trúc & Tech Stack (Architecture & Technologies)

```text
                       ┌──────────────────────────────────────────┐
                       │           Client / Frontend App          │
                       └────────────────────┬─────────────────────┘
                                            │ HTTP / RESTful API
                                            ▼
                       ┌──────────────────────────────────────────┐
                       │     Spring Security & JWT Filter         │
                       └────────────────────┬─────────────────────┘
                                            │
                                            ▼
                       ┌──────────────────────────────────────────┐
                       │            Controller Layer              │
                       └────────────────────┬─────────────────────┘
                                            │ DTOs (Records)
                                            ▼
                       ┌──────────────────────────────────────────┐
                       │             Service Layer                │
                       │   (Business Logic, State Machine...)    │
                       └────────┬───────────────────┬─────────────┘
                                │                   │
              MinIO SDK / Stream│                   │ JPA / Hibernate
                                ▼                   ▼
                       ┌─────────────────┐ ┌─────────────────┐
                       │  MinIO Storage  │ │ PostgreSQL 16   │
                       │  (Product/User) │ │ (Flyway Schema) │
                       └─────────────────┘ └─────────────────┘
```

### 🧰 Công Nghệ Sử Dụng

| Nhóm                     | Công nghệ / Thư viện | Phiên bản   | Vai trò & Mục đích                                        |
| ------------------------ | -------------------- | ----------- | --------------------------------------------------------- |
| **Core Framework**       | Java                 | 21 (LTS)    | Ngôn ngữ chính, tận dụng Virtual Threads & Records        |
|                          | Spring Boot          | 3.5.16      | Core framework, Auto-configuration                        |
|                          | Spring Security      | 6.x         | Phân quyền Role-based (USER/ADMIN), Stateless Security    |
|                          | Spring Data JPA      | (Hibernate) | ORM Engine, Dynamic Queries & Projections                 |
| **Database & Migration** | PostgreSQL           | 16          | Hệ quản trị cơ sở dữ liệu quan hệ chính                   |
|                          | Flyway Migration     | 10.x        | Quản lý phiên bản Database Schema (V1 -> V7)              |
| **Security & Auth**      | JJWT                 | 0.12.6      | Khởi tạo, mã hóa và verify JWT Tokens                     |
| **Storage & Image**      | MinIO Java SDK       | 8.5.17      | Object Storage tương thích S3 lưu trữ ảnh sản phẩm/avatar |
|                          | Thumbnailator        | 0.4.20      | Resize và nén hình ảnh tự động trước khi lưu trữ          |
| **Payment Gateway**      | VNPay Integration    | Custom      | Thanh toán trực tuyến qua Chữ ký số HMAC-SHA512           |
| **DevOps & CI/CD**       | Docker & Compose     | Multi-stage | Containerization ứng dụng & database với Healthcheck      |
|                          | GitHub Actions       | Workflows   | Tự động hóa CI/CD pipeline (Build & Test)                 |
| **Documentation**        | Springdoc OpenAPI    | 2.7.0       | Tự động sinh Swagger UI API Documentation                 |

---

## ✨ Tính Năng Nổi Bật (Key Features)

### 🔐 1. Xác Thực & Phân Quyền (Authentication & Authorization)

- Đăng ký, Đăng nhập mã hóa mật khẩu bằng **BCrypt**.
- Cơ chế **Dual Token (Access Token 1h + Refresh Token 7 ngày)**.
- Thu hồi Refresh Token khi Logout hoặc khi Admin khóa tài khoản (Soft Disable).
- Phân quyền theo vai trò (`ROLE_USER`, `ROLE_ADMIN`) linh hoạt với `@PreAuthorize`.

### 🛒 2. Quản Lý Danh Mục & Sản Phẩm (Catalog & Inventory)

- CRUD Danh mục sản phẩm, Tìm kiếm sản phẩm theo tên (Search LIKE + Pagination).
- Tăng/giảm số lượng tồn kho an toàn với **Optimistic Locking (`@Version`)**.
- Soft delete sản phẩm (Ẩn sản phẩm thay vì xóa cứng dữ liệu).
- Upload ảnh sản phẩm lên MinIO Server có kiểm tra Magic Bytes & Resize.

### 📦 3. Đặt Hàng & Quản Lý Luồng Đơn Hàng (Order & State Machine)

- Tạo đơn hàng đa sản phẩm, tự động kiểm tra & trừ số lượng tồn kho.
- Luồng chuyển trạng thái đơn hàng (Order State Machine):
  $$\text{PENDING} \xrightarrow{\text{Thanh toán VNPay thành công}} \text{CONFIRMED} \rightarrow \text{SHIPPED} \rightarrow \text{DONE}$$
- Idempotent Webhook xử lý callback thanh toán an toàn, không bị trùng lặp.

### 💳 4. Tích Hợp Thanh Toán VNPay

- Tạo URL thanh toán Sandbox VNPay mã hóa chữ ký HMAC-SHA512.
- IPN Webhook tự động cập nhật trạng thái đơn hàng ngay khi người dùng thanh toán.
- Cron Job (`@Scheduled`) tự động quét và hủy các đơn hàng thanh toán quá hạn (15 phút).

### 📊 5. Báo Cáo Thống Kê (Business Analytics & Insights)

- Thống kê **Top 10 sản phẩm bán chạy nhất** (dựa trên số lượng đặt).
- Báo cáo **Doanh thu theo danh mục sản phẩm** (Category Revenue).
- Thống kê **Doanh thu theo từng tháng trong năm** (Monthly Revenue).

---

## 📂 Cấu Trúc Thư Mục (Project Structure)

```text
mini-ecommerce/
├── .github/workflows/ci.yml         # GitHub Actions CI Workflow
├── Dockerfile                       # Multi-stage Docker build (Maven Builder -> Alpine JRE)
├── docker-compose.yml               # Service Postgres 16 + App Spring Boot với Healthcheck
├── .env.example                     # Template các biến môi trường
├── pom.xml                          # Maven dependencies & build plugins
└── src/main/java/com/devfat/mini_ecommerce/
    ├── MiniEcommerceApplication.java # Spring Boot Entry Point (@EnableScheduling)
    ├── common/                      # Unified API Response wrappers
    │   ├── ApiResponse.java         # Standard API Response format <T>
    │   └── PageResponse.java        # Standard Pagination Response wrapper
    ├── config/                      # Spring Security, OpenAPI, MinIO, VNPay configs
    ├── controller/                  # 8 REST Controllers (Auth, Category, Product, Order, User, Payment...)
    ├── dto/                         # Request Records & Response DTOs
    ├── entity/                      # JPA Entities (User, Product, Order, Payment, RefreshToken...)
    ├── exception/                   # GlobalExceptionHandler (@RestControllerAdvice 14 handlers)
    ├── repository/                  # Spring Data JPA Repositories & Projections
    ├── security/                    # JWT Provider, Custom UserDetails, Security Filters
    ├── service/                     # Service Interfaces & Implementations
    └── util/                        # FileValidationUtil (Magic Bytes Inspector)
```

---

## 🌐 Danh Sách API Endpoints (API Reference)

Hệ thống cung cấp **35 RESTful Endpoints** được chuẩn hóa:

### 🔑 1. Auth Module (`/api/v1/auth`)

| Method | Endpoint                     | Access | Mô tả                                 |
| ------ | ---------------------------- | ------ | ------------------------------------- |
| `POST` | `/api/v1/auth/register`      | Public | Đăng ký tài khoản người dùng mới      |
| `POST` | `/api/v1/auth/login`         | Public | Đăng nhập & lấy cặp JWT Tokens        |
| `POST` | `/api/v1/auth/refresh-token` | Public | Cấp lại Access Token từ Refresh Token |
| `POST` | `/api/v1/auth/logout`        | Public | Thu hồi Refresh Token (Đăng xuất)     |

### 🛍️ 2. Product Module (`/api/v1/products`)

| Method   | Endpoint                               | Access        | Mô tả                                          |
| -------- | -------------------------------------- | ------------- | ---------------------------------------------- |
| `GET`    | `/api/v1/products`                     | Public        | Xem danh sách sản phẩm (Phân trang & Tìm kiếm) |
| `GET`    | `/api/v1/products/{id}`                | Authenticated | Xem chi tiết sản phẩm theo ID                  |
| `POST`   | `/api/v1/products`                     | ADMIN         | Tạo mới sản phẩm                               |
| `PATCH`  | `/api/v1/products/{id}`                | ADMIN         | Cập nhật thông tin sản phẩm (Partial update)   |
| `DELETE` | `/api/v1/products/{id}`                | ADMIN         | Xóa mềm sản phẩm (`isActive = false`)          |
| `POST`   | `/api/v1/products/{id}/image`          | ADMIN         | Upload & gán ảnh sản phẩm lên MinIO            |
| `PATCH`  | `/api/v1/products/increase/{id}`       | ADMIN         | Tăng số lượng tồn kho sản phẩm                 |
| `PATCH`  | `/api/v1/products/decrease/{id}`       | ADMIN         | Giảm số lượng tồn kho sản phẩm                 |
| `GET`    | `/api/v1/products/top-buy`             | ADMIN         | Báo cáo Top sản phẩm bán chạy nhất             |
| `GET`    | `/api/v1/products/revenue-by-category` | ADMIN         | Báo cáo doanh thu theo danh mục                |
| `GET`    | `/api/v1/products/revenue-in-month`    | ADMIN         | Báo cáo doanh thu theo tháng                   |

### 📦 3. Order & Payment Module (`/api/v1/orders` & `/api/v1/payments`)

| Method  | Endpoint                        | Access        | Mô tả                                       |
| ------- | ------------------------------- | ------------- | ------------------------------------------- |
| `POST`  | `/api/v1/orders`                | Authenticated | Tạo đơn hàng mới                            |
| `GET`   | `/api/v1/orders/{id}`           | Authenticated | Xem thông tin chi tiết đơn hàng             |
| `GET`   | `/api/v1/orders/user/{userId}`  | Authenticated | Danh sách đơn hàng của người dùng           |
| `PATCH` | `/api/v1/orders/{id}/status`    | ADMIN         | Cập nhật trạng thái đơn hàng                |
| `POST`  | `/api/v1/payments/create`       | Authenticated | Khởi tạo giao dịch thanh toán VNPay         |
| `GET`   | `/api/v1/payments/vnpay-return` | Public        | URL nhận kết quả thanh toán từ VNPay        |
| `GET`   | `/api/v1/payments/vnpay-ipn`    | Public        | Instant Payment Notification (IPN Callback) |

### 👤 4. User Module (`/api/v1/users`)

| Method  | Endpoint                        | Access        | Mô tả                                    |
| ------- | ------------------------------- | ------------- | ---------------------------------------- |
| `GET`   | `/api/v1/users/me`              | Authenticated | Lấy thông tin cá nhân người dùng         |
| `PATCH` | `/api/v1/users/me/update`       | Authenticated | Cập nhật Họ tên & Số điện thoại          |
| `PATCH` | `/api/v1/users/password`        | Authenticated | Đổi mật khẩu                             |
| `POST`  | `/api/v1/users/me/avatar`       | Authenticated | Upload Avatar cá nhân lên MinIO          |
| `GET`   | `/api/v1/users`                 | ADMIN         | Danh sách tất cả người dùng (Phân trang) |
| `PATCH` | `/api/v1/users/{userId}/status` | ADMIN         | Khóa / Kích hoạt tài khoản người dùng    |

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án (Quick Start)

### 📋 Yêu Cầu Tiền Đề (Prerequisites)

- **Java Development Kit (JDK):** Version 21 trở lên.
- **Docker & Docker Compose:** Cài đặt sẵn trên máy.
- **Maven:** Phiên bản 3.9+ (hoặc dùng `mvnw` đính kèm).

---

### 1️⃣ Bước 1: Clone Repository & Cấu hình Environment

```bash
git clone https://github.com/huuphat26/java-mini-project-ecommerce.git
cd java-mini-project-ecommerce
```

Tạo file `.env` từ file mẫu `.env.example`:

```bash
cp .env.example .env
```

_(Chỉnh sửa các giá trị `DB_PASSWORD`, `JWT_SECRET_KEY`, `MIN_IO_ACCESS_KEY`, `VNPAY_SECRET_KEY` trong file `.env` nếu cần)._

---

### 2️⃣ Bước 2: Khởi Chạy Bằng Docker Compose (Khuyên dùng 🌟)

Chỉ cần **1 lệnh duy nhất** để khởi chạy toàn bộ hệ thống (Database PostgreSQL 16 + Application Spring Boot):

```bash
docker-compose up -d --build
```

Kiểm tra trạng thái container và Healthcheck:

```bash
docker-compose ps
```

> 🟢 **App Server:** `http://localhost:8085`  
> 🟢 **Healthcheck API:** `http://localhost:8085/api/v1/health`

---

### 3️⃣ Bước 3: Khởi Chạy Local Dev (Nếu chạy thủ công)

Nếu muốn chạy PostgreSQL ngoài Docker hoặc chạy Spring Boot trực tiếp từ IDE (IntelliJ / Eclipse):

1. Khởi chạy PostgreSQL database và MinIO Server.
2. Run ứng dụng bằng Maven:

```bash
./mvnw clean spring-boot:run
```

---

## 📖 Swagger UI & API Documentation

Ứng dụng tích hợp sẵn **Swagger UI (OpenAPI 3)** giúp thử nghiệm API trực tiếp trên giao diện trực quan:

- 🔗 **Swagger UI:** [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
- 🔗 **OpenAPI Spec (JSON):** [http://localhost:8085/v1/api-docs](http://localhost:8085/v1/api-docs)

> **💡 Hướng dẫn Auth trên Swagger:**  
> Sau khi gọi API `/api/v1/auth/login`, copy giá trị `accessToken` ➔ Nhấn nút **Authorize** ở góc trên Swagger UI ➔ Nhập `Bearer <your_token>` ➔ Thực thi các API yêu cầu quyền.

---

## 🛡️ Thiết Kế Bảo Mật & Best Practices (Engineering Highlights)

> [!IMPORTANT]
> **Các giải pháp kỹ thuật nổi bật được triển khai trong ứng dụng:**

1. **Magic Bytes Validation (`FileValidationUtil`):**
   Ngăn chặn lỗ hổng Unrestricted File Upload bằng cách đọc mảng Bytes header của file (`FF D8 FF` cho JPEG, `89 50 4E 47` cho PNG, `RIFF...WEBP` cho WebP). Dù hacker có đổi tên file `.php` hay `.exe` thành `.jpg` thì hệ thống vẫn phát hiện và chặn ngay lập tức.

2. **Optimistic Locking (`@Version`):**
   Trong môi trường E-Commerce có lượng truy cập cao, nhiều khách hàng cùng đòn hàng mua 1 sản phẩm cuối cùng. Thẻ `@Version` của JPA đảm bảo nếu 2 giao dịch đồng thời xảy ra, giao dịch thứ hai sẽ nhận ngoại lệ `ObjectOptimisticLockingFailureException` thay vì ghi đè dữ liệu sai lệch.

3. **HMAC-SHA512 VNPay Checksum & Idempotent Webhook:**
   Mọi request callback từ VNPay đều được kiểm tra chữ ký mã hóa SHA-512. Hệ thống kiểm tra trạng thái đơn hàng trước khi cập nhật (`PENDING` mới được chuyển `CONFIRMED`), chống tấn công Replay Attack hoặc nạp trùng tiền.

4. **Stateless Dual JWT Architecture:**
   Access Token lưu hoàn toàn ở phía client (không tốn dung lượng Server RAM/Session). Refresh Token được băm bằng thuật toán SHA-256 trước khi lưu DB. Khi người dùng bấm Logout, token sẽ ngay lập tức bị vô hiệu hóa trong DB.

---

## 👨‍💻 Tác Giả (Author)

**Hữu Phát(DevFat2K)** — _Java Backend Developer_

- ✉️ **Email:** [EMAIL_ADDRESS]
- 🐙 **GitHub:** [@huuphat26](https://github.com/huuphat26)

---

<div align="center">
  <sub>Built with ❤️ using Java 21 & Spring Boot 3</sub>
</div>
