# 🏗️ KIỂM TRA & TỐI ƯU CẤU TRÚC DỰ ÁN — SPRING BOOT BEST PRACTICES

> **Mục đích**: Đánh giá toàn bộ cấu trúc routing, phân chia Admin/User, tính nhất quán và tạo ticket refactor cụ thể  
> **Thời điểm**: 2026-08-11  
> **Phạm vi**: 9 modules hiện có + định hướng Microservice-ready  
> **Kết luận**: Cấu trúc package theo domain ✅ TỐT — Routing & phân quyền ⚠️ CẦN TỐI ƯU

---

## 🎯 ĐÁNH GIÁ TỔNG THỂ (Điểm số)

| Tiêu chí | Hiện tại | Điểm | Nhận xét |
|---|---|---|---|
| **Cấu trúc package theo domain** | `auth/`, `product/`, `order/`... | ✅ 9/10 | Chuẩn Spring Modulith, sẵn sàng tách service |
| **Quy ước internal layer** | `internal/` ẩn Entity, Repo, ServiceImpl | ✅ 9/10 | Đúng encapsulation principle |
| **Phân tách Public / Admin URL** | Trộn lẫn trong cùng controller | ❌ 3/10 | Vấn đề lớn nhất cần sửa |
| **Nhất quán URL pattern** | Chỉ RBAC có `/admin/` prefix | ❌ 4/10 | 4/5 module admin không có prefix |
| **Bảo mật theo lớp** | Phụ thuộc `@PreAuthorize` đơn lẻ | ⚠️ 5/10 | Wildcard `/**` gây lỗ hổng tiềm ẩn |
| **DTOs & Mappers** | MapStruct + Java Records | ✅ 9/10 | Đúng chuẩn, clean |
| **Exception handling** | Custom exceptions theo module | ✅ 8/10 | Tốt |
| **Caching pattern** | `@Cacheable` + Redis | ✅ 8/10 | Chuẩn |
| **Microservice readiness** | Package-by-feature hiện tại | ⚠️ 6/10 | Sẵn sàng về structure, chưa sẵn sàng về routing |

**Tổng điểm: 61/90 → 68% — Tốt về core, cần cải thiện về routing & security layer**

---

## 📊 HIỆN TRẠNG — BẢN ĐỒ ROUTING TOÀN DỰ ÁN

### Package Structure (✅ Tốt — Chuẩn Spring Modulith)

```
mini_ecommerce/
├── auth/           ✅ AuthController, AuthService, internal/{Entity,Repo,Impl}
├── category/       ✅ CategoryController, CategoryService, dto/, internal/
├── notification/   ✅ EmailService, internal/
├── order/          ✅ OrderController, OrderService, dto/, exception/, internal/
├── payment/        ✅ PaymentController, PaymentService, dto/, internal/
├── product/        ✅ ProductController, ProductService, dto/, exception/, internal/
├── storage/        ✅ StorageService, internal/
├── user/           ✅ UserController, RbacAdminController, address/, dto/, internal/
└── shared/         ✅ base/, config/, exception/, ratelimit/, security/, util/
```

> ✅ **Đây là điểm mạnh lớn nhất** — Package-by-Feature/Domain đã được áp dụng đúng. Mỗi module là 1 bounded context độc lập. Khi tách Microservice, chỉ cần "cắt" theo các thư mục này.

### API Routing Map (⚠️ Cần tối ưu)

| Zone | URL Pattern | Controller | Trạng thái |
|---|---|---|---|
| **PUBLIC** | `/api/v1/auth/**` | AuthController | ✅ SecurityConfig |
| **PUBLIC** | `/api/v1/products` (GET) | ProductController | ⚠️ Wildcard quá rộng |
| **PUBLIC** | `/api/v1/categories` (GET) | CategoryController | ⚠️ Wildcard quá rộng |
| **PUBLIC** | `/api/v1/payments/vnpay-*` | PaymentController | ✅ SecurityConfig |
| **AUTHENTICATED** | `/api/v1/users/me`, `/api/v1/addresses`, `/api/v1/orders`, `/api/v1/payments` | Mixed controllers | ⚠️ Trộn với admin |
| **ADMIN** | `/api/v1/admin/rbac` | RbacAdminController | ✅ Đúng pattern |
| **ADMIN** | `/api/v1/products` (POST/PATCH/DELETE) | ProductController | ❌ Trộn với public |
| **ADMIN** | `/api/v1/categories` (POST/PUT/DELETE) | CategoryController | ❌ Trộn với public |
| **ADMIN** | `/api/v1/users` (GET, PATCH /{id}/status) | UserController | ❌ Trộn với user |
| **ADMIN** | `/api/v1/orders` (GET all, PATCH status) | OrderController | ❌ Trộn với user |

---

## 🔴 VẤN ĐỀ PHÁT HIỆN

### ISSUE-01 — 🔴 CRITICAL: Lỗ hổng bảo mật — Wildcard PUBLIC_GET_URLS

```java
// SecurityConfig.java — HIỆN TẠI
private static final String[] PUBLIC_GET_URLS = {
    "/api/v1/products/**",   // ← Wildcard! Expose toàn bộ sub-paths
    "/api/v1/categories/**"
};
```

**Hệ quả**: 3 endpoints analytics kinh doanh nhạy cảm bị expose công khai nếu ai đó xóa `@PreAuthorize`:
```
GET /api/v1/products/top-buy              ← Doanh thu top sản phẩm
GET /api/v1/products/revenue-by-category  ← Doanh thu theo category
GET /api/v1/products/revenue-in-month     ← Doanh thu theo tháng
```

### ISSUE-02 — 🔴 HIGH: Admin và User trộn lẫn trong cùng Controller + URL

4 modules vi phạm: `ProductController`, `CategoryController`, `UserController`, `OrderController`.  
Hệ quả thực tế: không thể dùng SecurityConfig `/api/v1/admin/**` rule; Swagger lộn xộn; FE team khó phân biệt.

### ISSUE-03 — 🟠 HIGH: Chỉ RbacAdminController có `/admin/` prefix

Không nhất quán: `RbacAdminController` dùng `/api/v1/admin/rbac` nhưng 4 admin controllers khác không có.

### ISSUE-04 — 🟠 MEDIUM: `GET /orders/user/{userId}` lộ userId trong URL (IDOR risk)

Bất kỳ authenticated user nào biết userId người khác đều có thể thử gọi endpoint này.

### ISSUE-05 — 🟡 MEDIUM: `cancelOrder()` nhận `UpdateOrderStatusRequestDto` — không trực quan

API consumer phải tự biết điền `status: CANCELLED` trong body, không tự nhiên.

### ISSUE-06 — 🟡 BUG: `UserController.updateProfile()` gọi service 2 lần (double write)

```java
userService.updateProfile(userId, requestDto);           // ← Gọi 1 — bỏ kết quả
return ApiResponse.success(
    userService.updateProfile(userId, requestDto), "OK"  // ← Gọi 2 — double DB write
);
```

---

## ✅ PHƯƠNG ÁN TỐI ƯU — CHUẨN SPRING BOOT BEST PRACTICE + MICROSERVICE-READY

### Nguyên Tắc Thiết Kế Sau Refactor

1. **Defense in Depth**: Bảo mật theo 2 lớp — SecurityConfig (URL level) + `@PreAuthorize` (method level)
2. **URL-driven Authorization**: Chỉ nhìn URL là biết cần quyền gì — không cần đọc code
3. **Single Responsibility**: Mỗi Controller chỉ phục vụ 1 nhóm user (Public / Authenticated / Admin)
4. **Microservice boundary**: Package boundary = Service boundary — cắt là được

### Cấu Trúc URL Mục Tiêu

```
/api/v1/
│
├── 🌐 PUBLIC ZONE (không cần auth)
│   ├── auth/**              ← Đăng nhập, đăng ký, OTP, refresh token
│   ├── home                 ← Trang chủ (HOME-011) [MỚI]
│   ├── products             ← List sản phẩm + search
│   ├── products/{id}        ← Xem chi tiết
│   ├── products/{id}/reviews ← Reviews của product [MỚI - HOME-015]
│   ├── categories           ← List categories
│   └── categories/{id}      ← Xem chi tiết category
│
├── 🔐 AUTHENTICATED ZONE (cần JWT, mọi role)
│   ├── users/me             ← Xem profile bản thân
│   ├── users/me/update      ← Cập nhật profile
│   ├── users/me/avatar      ← Upload avatar
│   ├── users/change-password ← Đổi mật khẩu
│   ├── addresses/**         ← Quản lý địa chỉ giao hàng
│   ├── orders               ← Tạo đơn
│   ├── orders/my-orders     ← Đơn của tôi
│   ├── orders/{id}          ← Xem đơn (có ownership check)
│   ├── orders/{id}/cancel   ← Hủy đơn
│   ├── payments/**          ← Thanh toán
│   └── reviews              ← Viết đánh giá [MỚI - HOME-015]
│
└── 🛡️ ADMIN ZONE (cần role ADMIN — SecurityConfig tự block toàn bộ)
    ├── admin/products/**    ← CRUD + stock + analytics + home config
    ├── admin/categories/**  ← CRUD + image + home config
    ├── admin/orders/**      ← Xem tất cả + cập nhật status
    ├── admin/users/**       ← List + khóa/mở tài khoản
    ├── admin/reviews/**     ← Curate, toggle visibility
    ├── admin/hero-banners/** ← CRUD Hero Banner [MỚI]
    ├── admin/daily-arrivals/** ← CRUD Daily Arrival [MỚI]
    ├── admin/rbac/**        ← Roles & Permissions (đã chuẩn ✅)
    └── admin/dashboard/**   ← Stats tổng hợp [Tương lai]
```

---

## 📋 TICKETS REFACTOR CHI TIẾT

---

### ⚡ [REFACTOR-001] Fix SecurityConfig — Thu hẹp PUBLIC_GET_URLS

| Field | Value |
|---|---|
| **ID** | `REFACTOR-001` |
| **Type** | 🔒 Security Fix |
| **Priority** | 🔴 CRITICAL — Làm NGAY trước mọi việc khác |
| **Estimate** | **20 phút** |
| **File** | `SecurityConfig.java` |

#### Mô tả
Wildcard `/**` đang expose toàn bộ sub-path của `/api/v1/products` và `/api/v1/categories` ra public, bao gồm các analytics endpoints nhạy cảm.

#### Các bước thực hiện

**Bước 1** — Mở `SecurityConfig.java`, sửa `PUBLIC_GET_URLS`:
```java
// TRƯỚC:
private static final String[] PUBLIC_GET_URLS = {
    "/api/v1/products/**",
    "/api/v1/categories/**"
};

// SAU:
private static final String[] PUBLIC_GET_URLS = {
    "/api/v1/products",
    "/api/v1/categories",
    "/api/v1/home"
};
```

**Bước 2** — Thêm riêng pattern cho `GET /products/{id}` và `GET /categories/{id}`:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(PUBLIC_AUTH_URLS).permitAll()
    .requestMatchers(SWAGGER_URLS).permitAll()
    .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
    // Thêm cho path variable:
    .requestMatchers(HttpMethod.GET, "/api/v1/products/{id}").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/categories/{id}").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/products/{id}/reviews").permitAll()
    // Admin zone
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

**Bước 3** — Test:
- [ ] `GET /api/v1/products/top-buy` không có token → phải 401/403
- [ ] `GET /api/v1/products` không có token → vẫn 200 OK
- [ ] `GET /api/v1/products/123` không có token → vẫn 200 OK

#### Definition of Done
- ✅ Analytics endpoints không còn accessible khi không có token
- ✅ Public product/category list vẫn hoạt động
- ✅ App build và start không lỗi

---

### 🐛 [REFACTOR-002] Fix Double Service Call trong UserController

| Field | Value |
|---|---|
| **ID** | `REFACTOR-002` |
| **Type** | 🐛 Bug Fix |
| **Priority** | 🟡 TRIVIAL — Làm ngay, 5 phút |
| **Estimate** | **5 phút** |
| **File** | `UserController.java` |

#### Mô tả
`updateProfile()` đang gọi service 2 lần — lần đầu bỏ kết quả, lần hai lấy kết quả. Gây ra 2 DB write transactions cho cùng 1 request.

#### Các bước thực hiện

```java
// TRƯỚC (bug):
@PatchMapping("/me/update")
public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(...) {
    Long userId = userPrincipal.getUserId();
    userService.updateProfile(userId, requestDto);  // ← Bỏ kết quả — BUG
    return ResponseEntity.ok().body(
        ApiResponse.success(
            userService.updateProfile(userId, requestDto),  // ← Gọi lần 2
            "Update User Successfully!"
        )
    );
}

// SAU (fix):
@PatchMapping("/me/update")
public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(...) {
    Long userId = userPrincipal.getUserId();
    UserResponseDto result = userService.updateProfile(userId, requestDto); // ← 1 lần duy nhất
    return ResponseEntity.ok().body(ApiResponse.success(result, "Update User Successfully!"));
}
```

#### Definition of Done
- ✅ Chỉ 1 DB write transaction cho 1 request
- ✅ Kết quả trả về giống hệt trước

---

### 🔧 [REFACTOR-003] Tách AdminProductController ra khỏi ProductController

| Field | Value |
|---|---|
| **ID** | `REFACTOR-003` |
| **Type** | 🔧 Refactor |
| **Priority** | 🟠 HIGH |
| **Estimate** | **2 giờ** |
| **Files** | `ProductController.java` [MODIFY], `AdminProductController.java` [NEW] |

#### Mô tả
Tách các admin-only endpoints ra controller riêng tại `/api/v1/admin/products`. `ProductController` chỉ giữ lại public read-only endpoints.

#### Các bước thực hiện

**Bước 1** — Tạo `AdminProductController.java` trong package `product/`:
```java
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Product", description = "Admin: Quản lý sản phẩm")
// Không cần @PreAuthorize ở class level vì SecurityConfig đã block /admin/** rồi
// Nhưng vẫn nên thêm để document rõ intent và double protection:
@PreAuthorize("hasRole('ADMIN') or hasAuthority('product:create') or hasAuthority('product:update') or hasAuthority('product:delete')")
public class AdminProductController {
    private final ProductService productService;
}
```

**Bước 2** — Di chuyển các endpoints sau từ `ProductController` sang `AdminProductController`:
- [ ] `POST /` → `POST /api/v1/admin/products` (Tạo sản phẩm)
- [ ] `PATCH /{id}` → `PATCH /api/v1/admin/products/{id}` (Sửa sản phẩm)
- [ ] `DELETE /{id}` → `DELETE /api/v1/admin/products/{id}` (Xóa sản phẩm)
- [ ] `PATCH /increase/{id}` → `PATCH /api/v1/admin/products/{id}/stock/increase`
- [ ] `PATCH /decrease/{id}` → `PATCH /api/v1/admin/products/{id}/stock/decrease`
- [ ] `POST /{id}/image` → `POST /api/v1/admin/products/{id}/image`
- [ ] `GET /top-buy` → `GET /api/v1/admin/products/analytics/top-buy`
- [ ] `GET /revenue-by-category` → `GET /api/v1/admin/products/analytics/revenue-by-category`
- [ ] `GET /revenue-in-month` → `GET /api/v1/admin/products/analytics/revenue-in-month`

**Bước 3** — Thêm từ HOME-012 vào AdminProductController ngay luôn:
- [ ] `PATCH /api/v1/admin/products/{id}/featured` — toggle isFeatured
- [ ] `PATCH /api/v1/admin/products/{id}/combo-config` — cập nhật combo settings

**Bước 4** — `ProductController` chỉ giữ lại:
```java
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "Xem sản phẩm")
public class ProductController {
    @GetMapping          // Public list + search
    @GetMapping("/{id}") // Public chi tiết
    @GetMapping("/{id}/reviews") // Public reviews (sẽ thêm ở HOME-015)
}
```

**Bước 5** — Xóa `@PreAuthorize` khỏi các methods đã chuyển (không còn cần thiết trong admin controller vì SecurityConfig đã block)

**Bước 6** — Cập nhật Swagger: `OpenApiConfig.java` nếu có GroupedOpenApi

**Bước 7** — Test toàn bộ:
- [ ] `POST /api/v1/admin/products` không có token → 401
- [ ] `POST /api/v1/admin/products` với USER token → 403
- [ ] `POST /api/v1/admin/products` với ADMIN token → 201 Created
- [ ] `GET /api/v1/products` không có token → 200 OK

#### Definition of Done
- ✅ ProductController chỉ còn 2 public GET endpoints
- ✅ AdminProductController có tất cả 12 admin endpoints
- ✅ SecurityConfig `/api/v1/admin/**` tự động bảo vệ mà không cần `@PreAuthorize`
- ✅ Swagger hiện 2 tag riêng: "Product" và "Admin - Product"

---

### 🔧 [REFACTOR-004] Tách AdminCategoryController ra khỏi CategoryController

| Field | Value |
|---|---|
| **ID** | `REFACTOR-004` |
| **Type** | 🔧 Refactor |
| **Priority** | 🟠 HIGH |
| **Estimate** | **1.5 giờ** |
| **Files** | `CategoryController.java` [MODIFY], `AdminCategoryController.java` [NEW] |

#### Mô tả
Tương tự REFACTOR-003. Tách admin CRUD category và home config sang URL `/api/v1/admin/categories`.

#### Các bước thực hiện

**Bước 1** — Tạo `AdminCategoryController.java`:
```java
@RestController
@RequestMapping("/api/v1/admin/categories")
@Tag(name = "Admin - Category", description = "Admin: Quản lý danh mục")
public class AdminCategoryController { }
```

**Bước 2** — Di chuyển từ `CategoryController`:
- [ ] `POST /` → `POST /api/v1/admin/categories`
- [ ] `PUT /{id}` → `PUT /api/v1/admin/categories/{id}`
- [ ] `DELETE /{id}` → `DELETE /api/v1/admin/categories/{id}`

**Bước 3** — Thêm từ HOME-012 và HOME-017 vào ngay:
- [ ] `PATCH /api/v1/admin/categories/{id}/home-config` — home display config
- [ ] `POST /api/v1/admin/categories/{id}/image` — upload ảnh category (MinIO)

**Bước 4** — `CategoryController` chỉ giữ lại:
```java
GET /api/v1/categories        ← Public
GET /api/v1/categories/{id}   ← Public
```

#### Definition of Done
- ✅ CategoryController chỉ còn 2 public GET
- ✅ AdminCategoryController có 5 admin endpoints
- ✅ Không phá vỡ public API category

---

### 🔧 [REFACTOR-005] Tách AdminUserController ra khỏi UserController

| Field | Value |
|---|---|
| **ID** | `REFACTOR-005` |
| **Type** | 🔧 Refactor |
| **Priority** | 🟠 HIGH |
| **Estimate** | **1.5 giờ** |
| **Files** | `UserController.java` [MODIFY], `AdminUserController.java` [NEW] |

#### Mô tả
Tách admin user management ra controller riêng. Đồng thời fix ISSUE-06 (double call) trong quá trình này.

#### Các bước thực hiện

**Bước 1** — Tạo `AdminUserController.java`:
```java
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin - User", description = "Admin: Quản lý tài khoản người dùng")
public class AdminUserController { }
```

**Bước 2** — Di chuyển từ `UserController`:
- [ ] `GET /` → `GET /api/v1/admin/users` — list all users với phân trang
- [ ] `PATCH /{userId}/status` → `PATCH /api/v1/admin/users/{id}/status` — khóa/mở

**Bước 3** — `UserController` giữ lại chỉ self-management:
```java
GET    /api/v1/users/me               ← Profile
PATCH  /api/v1/users/me/update        ← Sửa profile (fix double call)
POST   /api/v1/users/me/avatar        ← Upload avatar
POST   /api/v1/users/change-password  ← Đổi mật khẩu
```

**Bước 4** — Fix ISSUE-06 trong `UserController.updateProfile()`:
```java
// Sửa trong updateProfile():
UserResponseDto result = userService.updateProfile(userId, requestDto); // 1 lần
return ResponseEntity.ok().body(ApiResponse.success(result, "Update User Successfully!"));
```

#### Definition of Done
- ✅ UserController chỉ còn 4 self-management endpoints
- ✅ AdminUserController có 2 admin endpoints
- ✅ Double call bug đã được fix

---

### 🔧 [REFACTOR-006] Tách AdminOrderController ra khỏi OrderController

| Field | Value |
|---|---|
| **ID** | `REFACTOR-006` |
| **Type** | 🔧 Refactor |
| **Priority** | 🟠 HIGH |
| **Estimate** | **2 giờ** |
| **Files** | `OrderController.java` [MODIFY], `AdminOrderController.java` [NEW] |

#### Mô tả
Tách admin order management. Đồng thời giải quyết ISSUE-04 (IDOR risk) và ISSUE-05 (cancel endpoint body).

#### Các bước thực hiện

**Bước 1** — Tạo `AdminOrderController.java`:
```java
@RestController
@RequestMapping("/api/v1/admin/orders")
@Tag(name = "Admin - Order", description = "Admin: Quản lý đơn hàng")
public class AdminOrderController { }
```

**Bước 2** — Di chuyển từ `OrderController`:
- [ ] `GET /` → `GET /api/v1/admin/orders` — list tất cả đơn hàng
- [ ] `GET /user/{userId}` → `GET /api/v1/admin/orders?userId={userId}` ← Đổi path variable thành query param để ẩn userId
- [ ] `PATCH /{id}/status` → `PATCH /api/v1/admin/orders/{id}/status`

**Bước 3** — `OrderController` (user) chỉ giữ lại:
```java
POST /api/v1/orders               ← Tạo đơn
GET  /api/v1/orders/my-orders     ← Đơn của tôi (không lộ userId)
GET  /api/v1/orders/{id}          ← Xem 1 đơn (ownership check trong service)
POST /api/v1/orders/{id}/cancel   ← Hủy đơn
```

**Bước 4** — Fix ISSUE-05 — Endpoint cancel không cần body:
```java
// TRƯỚC:
@PostMapping("/{id}/cancel")
public ResponseEntity<?> cancelOrder(@PathVariable Long id,
    @RequestBody UpdateOrderStatusRequestDto dto) // ← Cần FE biết điền CANCELLED

// SAU — tự động set CANCELLED trong service, không cần body:
@PostMapping("/{id}/cancel")
public ResponseEntity<?> cancelOrder(@PathVariable Long id,
    @AuthenticationPrincipal UserPrincipal userPrincipal) {
    orderService.cancelOrder(id, userPrincipal.getUserId());
    // Trong service: tự set status = CANCELLED
}
```

**Bước 5** — Fix ISSUE-04 — Admin lấy đơn theo user dùng query param thay path:
```java
// AdminOrderController:
@GetMapping
public ResponseEntity<?> getAllOrders(
    @RequestParam(required = false) Long userId,  // ← Query param, không lộ trong path
    Pageable pageable
) { ... }
```

#### Definition of Done
- ✅ OrderController chỉ còn 4 user endpoints
- ✅ AdminOrderController có 3 admin endpoints
- ✅ Cancel endpoint không cần body, tự set CANCELLED
- ✅ userId không lộ trong URL path

---

### 🔧 [REFACTOR-007] Cập nhật SecurityConfig — Finalize Public URLs

| Field | Value |
|---|---|
| **ID** | `REFACTOR-007` |
| **Type** | 🔒 Security |
| **Priority** | 🟡 LOW — Làm sau khi REFACTOR-001→006 xong |
| **Estimate** | **0.5 giờ** |
| **File** | `SecurityConfig.java` |

#### Mô tả
Sau khi tất cả admin endpoints đã về `/api/v1/admin/**`, cập nhật SecurityConfig để phản ánh cấu trúc mới, thêm Swagger URLs đầy đủ và comment document rõ ràng.

#### Các bước thực hiện

**Bước 1** — Cập nhật SecurityConfig hoàn chỉnh:
```java
// PUBLIC — Không cần auth
private static final String[] PUBLIC_AUTH_URLS = {
    "/api/v1/auth/**",
    "/api/v1/health",
    "/api/v1/payments/vnpay-return",
    "/api/v1/payments/vnpay-ipn"
};

// PUBLIC GET — Các read-only public endpoints
private static final String[] PUBLIC_GET_URLS = {
    "/api/v1/products",
    "/api/v1/categories",
    "/api/v1/home"
};

// PUBLIC GET với path variable — khai báo riêng
private static final String[] PUBLIC_GET_WITH_ID_URLS = {
    "/api/v1/products/{id}",
    "/api/v1/categories/{id}",
    "/api/v1/products/{id}/reviews"
};

// .authorizeHttpRequests(auth -> auth
//     .requestMatchers(PUBLIC_AUTH_URLS).permitAll()
//     .requestMatchers(SWAGGER_URLS).permitAll()
//     .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
//     .requestMatchers(HttpMethod.GET, PUBLIC_GET_WITH_ID_URLS).permitAll()
//     .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")  ← Auto block admin zone
//     .anyRequest().authenticated()
// )
```

**Bước 2** — Thêm Swagger URLs đầy đủ:
```java
private static final String[] SWAGGER_URLS = {
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",   // ← v3 thay vì v1 (SpringDoc 2.x dùng /v3)
    "/v3/api-docs"
};
```

#### Definition of Done
- ✅ SecurityConfig document rõ ràng 3 zone (Public / Authenticated / Admin)
- ✅ Swagger URL đúng phiên bản SpringDoc 2.x

---

## 📋 BẢNG TICKET REFACTOR TỔNG HỢP

| Ticket | Mô tả | Estimate | Priority | Phụ thuộc |
|---|---|---|---|---|
| **REFACTOR-001** | 🔴 Fix SecurityConfig PUBLIC_GET_URLS wildcard | 20 phút | **CRITICAL — Làm ngay** | — |
| **REFACTOR-002** | 🐛 Fix double call `updateProfile()` | 5 phút | **TRIVIAL — Làm ngay** | — |
| REFACTOR-003 | Tách `AdminProductController` (12 endpoints) | 2h | 🟠 HIGH | REFACTOR-001 |
| REFACTOR-004 | Tách `AdminCategoryController` (5 endpoints) | 1.5h | 🟠 HIGH | REFACTOR-001 |
| REFACTOR-005 | Tách `AdminUserController` (2 endpoints) | 1.5h | 🟠 HIGH | REFACTOR-001 |
| REFACTOR-006 | Tách `AdminOrderController` + fix cancel/IDOR | 2h | 🟠 HIGH | REFACTOR-001 |
| REFACTOR-007 | Finalize SecurityConfig sau refactor | 0.5h | 🟡 LOW | REFACTOR-003→006 |

**Tổng**: ~7.75 giờ (~1 ngày)

---

## 🗓️ THỨ TỰ THỰC HIỆN KHUYẾN NGHỊ

```
Ngay hôm nay (25 phút):
  REFACTOR-001 (20 phút) + REFACTOR-002 (5 phút)

Sprint 1 — Trước khi bắt đầu Home Module:
  REFACTOR-003 → REFACTOR-004 → REFACTOR-005 → REFACTOR-006

Sprint 2 — Song song với Home Module:
  REFACTOR-007 (finalize SecurityConfig sau khi /api/v1/home được thêm vào)
```

> 💡 **Lý do làm Refactor TRƯỚC Home Module**: Khi tạo `HomeController`, `HeroBannerController`, `AdminReviewController`... nếu đã có cấu trúc clean sẵn thì sẽ tự nhiên biết đặt chúng đúng chỗ ngay (public vào `/api/v1/`, admin vào `/api/v1/admin/`). Nếu không refactor trước, home module sẽ lại lặp lại vấn đề.

---

## 📐 CẤU TRÚC PACKAGE FILE SAU KHI REFACTOR ĐẦY ĐỦ

```
mini_ecommerce/
│
├── auth/                          ← Không thay đổi
│   ├── AuthController.java        ← /api/v1/auth/**
│   └── internal/...
│
├── category/
│   ├── CategoryController.java    ← /api/v1/categories (GET only — public)
│   ├── AdminCategoryController.java ← /api/v1/admin/categories [NEW]
│   ├── CategoryService.java
│   ├── dto/
│   └── internal/
│
├── product/
│   ├── ProductController.java     ← /api/v1/products (GET only — public)
│   ├── AdminProductController.java ← /api/v1/admin/products [NEW]
│   ├── ProductService.java
│   ├── dto/
│   ├── exception/
│   └── internal/
│
├── order/
│   ├── OrderController.java       ← /api/v1/orders (user only)
│   ├── AdminOrderController.java  ← /api/v1/admin/orders [NEW]
│   ├── OrderService.java
│   ├── dto/
│   ├── exception/
│   └── internal/
│
├── review/                        ← [NEW MODULE — HOME-015]
│   ├── ReviewController.java      ← /api/v1/reviews (authenticated)
│   │                              ← /api/v1/products/{id}/reviews (public GET)
│   ├── AdminReviewController.java ← /api/v1/admin/reviews [NEW]
│   ├── ReviewService.java
│   ├── dto/
│   └── internal/
│
├── herobanner/                    ← [NEW MODULE — HOME-004/005]
│   ├── AdminHeroBannerController.java ← /api/v1/admin/hero-banners
│   ├── HeroBannerService.java
│   ├── dto/
│   └── internal/
│
├── dailyarrival/                  ← [NEW MODULE — HOME-006/007]
│   ├── AdminDailyArrivalController.java ← /api/v1/admin/daily-arrivals
│   ├── DailyArrivalService.java
│   ├── dto/
│   └── internal/
│
├── home/                          ← [NEW MODULE — HOME-010/011]
│   ├── HomeController.java        ← /api/v1/home (PUBLIC)
│   ├── HomeService.java
│   ├── dto/
│   └── internal/
│
├── user/
│   ├── UserController.java        ← /api/v1/users (self-management only)
│   ├── AdminUserController.java   ← /api/v1/admin/users [NEW]
│   ├── RbacAdminController.java   ← /api/v1/admin/rbac (đã chuẩn ✅)
│   ├── address/                   ← Không thay đổi
│   ├── dto/
│   └── internal/
│
├── payment/                       ← Không thay đổi
├── notification/                  ← Không thay đổi
├── storage/                       ← Không thay đổi
└── shared/                        ← Không thay đổi
```

---

## 🚀 LỘ TRÌNH MICROSERVICE-READY (Tương Lai)

Sau khi refactor xong, cấu trúc này đã **sẵn sàng 80%** để tách Microservice:

| Microservice | Package source | Expose API |
|---|---|---|
| `product-service` | `product/`, `category/`, `review/` | `/api/v1/products`, `/api/v1/categories`, `/api/v1/reviews` |
| `order-service` | `order/`, `payment/` | `/api/v1/orders`, `/api/v1/payments` |
| `user-service` | `user/`, `auth/` | `/api/v1/auth`, `/api/v1/users`, `/api/v1/addresses` |
| `home-service` | `home/`, `herobanner/`, `dailyarrival/` | `/api/v1/home`, `/api/v1/admin/hero-banners` |
| `notification-service` | `notification/` | Internal event consumer |
| `storage-service` | `storage/` | Internal file upload |

> Bước tiếp theo để Microservice: Thêm Kafka/Event bus thay cho direct service injection; thêm API Gateway; tách DB schema theo service boundary.
