# 🛡️ CẨM NANG TỰ THỰC THI (MASTER SELF-IMPLEMENTATION PLAN)
## TASK 7: DYNAMIC RBAC UNDER DB & REDIS AUTHORIZATION CACHING

> **Mục tiêu:** Tài liệu này được thiết kế theo dạng **Cẩm nang từng bước (Step-by-Step Workbook)**, giúp bạn tự tay viết từng dòng code, hiểu sâu bản chất kỹ thuật của kiến trúc **Dynamic Role-Based Access Control (RBAC)** 4 bảng trong CSDL PostgreSQL, tích hợp với **Spring Security 6** và bộ nhớ đệm **Redis Authorization Cache**.
>
> **Nguyên tắc:** AI đóng vai trò **Người hướng dẫn (Mentor)** giải thích khái niệm, luồng chạy, quy chuẩn code. Bạn đóng vai trò **Kỹ sư thực thi (Developer)** tự gõ code và làm chủ 100% codebase.

---

## 🗺️ BẢN ĐỒ TỔNG THỂ VÀ LUỒNG NGHỆP VỤ (ARCHITECTURE FLOW)

### 1. Mô Hình CSDL 4 Bảng (2 Tầng Phân Quyền)
```text
users (id, email, password, ...)
  │  
user_roles (user_id, role_id)                <-- Bảng trung gian N-N (Composite PK)
  │  
roles (id, name, description)                <-- 'ADMIN', 'USER', 'STAFF' (KHÔNG lưu 'ROLE_' prefix)
  │  
role_permissions (role_id, permission_id)    <-- Bảng trung gian N-N (Composite PK)
  │  
permissions (id, code, description)          <-- 'product:create', 'product:update', 'order:manage'
```

### 2. Luồng Xử Lý Request Runtime (Stateless JWT + Redis Cache)
```text
[Client gửi Request kèm JWT Access Token] 
              │
              ▼
[JwtAuthenticationFilter đọc Token, trích xuất userId]
              │
              ▼
[Tra cứu qua UserPermissionCacheService (Redis Cache)]
    ├── NẾU CÓ CACHE  ──> Lấy danh sách Authorities từ Redis (phản hồi ~1ms, KHÔNG đụng Postgres)
    └── NẾU MẤT CACHE ──> Gọi CustomUserDetailsService query DB (JOIN 4 bảng 1 lần SQL) ──> Ghi vào Redis
              │
              ▼
[Nạp gộp Authorities (Roles + Permissions) vào SecurityContextHolder]
              │
              ▼
[Controller kiểm tra @PreAuthorize("hasAuthority('product:create')")]
    ├── NẾU CÓ QUYỀN  ──> Cho phép thực thi Controller (200 OK / 201 Created)
    └── NẾU KHÔNG CÓ ──> Trả về 403 Forbidden
```

---

## 🧭 CÁC GIAI ĐOẠN THỰC THI (6 GIAI ĐOẠN CHUYÊN SÂU)

---

### 📍 GIAI ĐOẠN 1: CHUẨN HÓA CSDL TRONG `V1__init_mini_shop.sql`
**File cần sửa:** [V1__init_mini_shop.sql](file:///Users/andy2015bui/Desktop/mini-ecommerce/src/main/resources/db/migration/V1__init_mini_shop.sql)

#### 1. Nhiệm vụ sửa đổi Schema:
* **Bảng `users`**:
  - Xóa hẳn cột `role VARCHAR(20)...` cũ (hoặc comment out).
* **Bảng `roles`**:
  - Định nghĩa cột `name VARCHAR(50) UNIQUE NOT NULL`.
  - Loại bỏ câu lệnh `CHECK (role IN ...)` cũ để cho phép thêm vai trò mới động.
* **Bảng `permissions`**:
  - Sửa tên bảng thành số nhiều `permissions`.
  - Định nghĩa cột `code VARCHAR(100) UNIQUE NOT NULL` (VD: `'product:create'`).
* **Bảng trung gian `user_roles` & `role_permissions`**:
  - Dùng Khóa chính phức hợp `PRIMARY KEY (user_id, role_id)` và `PRIMARY KEY (role_id, permission_id)`.
  - Thêm `ON DELETE CASCADE` cho tất cả các Foreign Key.
  - Đảm bảo xóa bỏ tất cả các dấu phẩy thừa ở cuối dòng trước dấu đóng ngoặc `)`.

#### 2. Thêm Seed Data ở cuối file:
```sql
-- Seed Roles
INSERT INTO roles (id, name, description, created_by) VALUES
    (1, 'ADMIN', 'Quản trị viên toàn quyền hệ thống', 'SYSTEM'),
    (2, 'USER', 'Khách hàng người dùng thông thường', 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- Seed Permissions
INSERT INTO permissions (id, code, description, created_by) VALUES
    (1, 'product:create', 'Quyền tạo sản phẩm mới', 'SYSTEM'),
    (2, 'product:read', 'Quyền xem danh sách & chi tiết sản phẩm', 'SYSTEM'),
    (3, 'product:update', 'Quyền cập nhật thông tin sản phẩm', 'SYSTEM'),
    (4, 'product:delete', 'Quyền xóa/ẩn sản phẩm', 'SYSTEM'),
    (5, 'category:manage', 'Quyền quản lý danh mục sản phẩm', 'SYSTEM'),
    (6, 'order:create', 'Quyền tạo đơn hàng mới', 'SYSTEM'),
    (7, 'order:read', 'Quyền xem đơn hàng của mình', 'SYSTEM'),
    (8, 'order:manage', 'Quyền quản lý & duyệt tất cả đơn hàng', 'SYSTEM'),
    (9, 'user:manage', 'Quyền quản lý người dùng', 'SYSTEM'),
    (10, 'rbac:manage', 'Quyền quản lý vai trò & phân quyền', 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- Map Role-Permissions (ADMIN có 10 quyền, USER có 3 quyền đọc/tạo đơn)
INSERT INTO role_permissions (role_id, permission_id, created_by) VALUES
    (1, 1, 'SYSTEM'), (1, 2, 'SYSTEM'), (1, 3, 'SYSTEM'), (1, 4, 'SYSTEM'), (1, 5, 'SYSTEM'),
    (1, 6, 'SYSTEM'), (1, 7, 'SYSTEM'), (1, 8, 'SYSTEM'), (1, 9, 'SYSTEM'), (1, 10, 'SYSTEM'),
    (2, 2, 'SYSTEM'), (2, 6, 'SYSTEM'), (2, 7, 'SYSTEM')
ON CONFLICT DO NOTHING;
```

---

### 📍 GIAI ĐOẠN 2: THIẾT KẾ ENTITY & REPOSITORY LAYER
**Package làm việc:** `com.devfat.mini_ecommerce.user.internal`

#### 1. Tạo `PermissionEntity.java`
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/user/internal/PermissionEntity.java`
- Khai báo `@Entity`, `@Table(name = "permissions")`, `extends BaseEntity`.
- Thuộc tính: `id` (Long, PK), `code` (String, unique), `description` (String).
- **Quy chuẩn Vlad Mihalcea:** Override `equals()` so sánh `id != null && id.equals(that.getId())` và `hashCode()` return `getClass().hashCode()`. Không dùng Lombok `@Data`.

#### 2. Tạo `RoleEntity.java`
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/user/internal/RoleEntity.java`
- Khai báo `@Entity`, `@Table(name = "roles")`, `extends BaseEntity`.
- Thuộc tính: `id`, `name`, `description`.
- Quan hệ `@ManyToMany(fetch = FetchType.LAZY)` tới `PermissionEntity` thông qua `@JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))`. Khai báo `Set<PermissionEntity> permissions = new HashSet<>()`.
- Override `equals()` & `hashCode()` chuẩn Vlad Mihalcea.

#### 3. Cập nhật `UserEntity.java`
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/user/internal/UserEntity.java`
- Xóa field `private String role;` cũ.
- Khai báo `@ManyToMany(fetch = FetchType.LAZY)` tới `RoleEntity` thông qua `@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))`. Dùng `Set<RoleEntity> roles = new HashSet<>()`.

#### 4. Viết các Repositories:
- **`UserRepository.java`**: Thêm hàm JPQL nạp 4 bảng trong 1 câu SQL duy nhất:
  ```java
  @Query("SELECT DISTINCT u FROM UserEntity u " +
         "LEFT JOIN FETCH u.roles r " +
         "LEFT JOIN FETCH r.permissions " +
         "WHERE u.email = :email")
  Optional<UserEntity> findByEmailWithRolesAndPermissions(@Param("email") String email);
  ```
- **`RoleRepository.java`**: `extends JpaRepository<RoleEntity, Long>`, chứa `findByName`, `existsByName`, `findByIdWithPermissions`.
- **`PermissionRepository.java`**: `extends JpaRepository<PermissionEntity, Long>`, chứa `findByCode`, `existsByCode`.

---

### 📍 GIAI ĐOẠN 3: ĐIỀU CHỈNH SECURITY DTO & SERVICE LAYER

#### 1. Cập nhật `UserPrincipal.java`
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/shared/security/UserPrincipal.java`
- Khai báo 2 thuộc tính tập hợp: `private final Set<String> roles;` và `private final Set<String> permissions;`.
- Viết lại hàm `getAuthorities()`:
  ```java
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
      Set<GrantedAuthority> authorities = new HashSet<>();
      if (roles != null) {
          for (String r : roles) {
              authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
          }
      }
      if (permissions != null) {
          for (String p : permissions) {
              authorities.add(new SimpleGrantedAuthority(p));
          }
      }
      return authorities;
  }
  ```
- Bổ sung helper methods: `getRole()` (trả về tên Role chính) và `hasRole(String roleName)`.

#### 2. Cập nhật `CustomUserDetailsService.java`
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/user/internal/CustomUserDetailsService.java`
- Đổi câu query thành `userRepository.findByEmailWithRolesAndPermissions(email)`.
- Viết 2 vòng lặp lồng nhau duyệt qua `user.getRoles()` và `role.getPermissions()` để thu thập 2 tập chuỗi: `roleNames` và `permissionCodes`.
- Đóng gói và trả về `UserPrincipal.builder().roles(roleNames).permissions(permissionCodes)...build()`.

---

### 📍 GIAI ĐOẠN 4: THIẾT KẾ REDIS AUTHORIZATION CACHING LAYER

#### 1. Tạo `UserPermissionCacheDto.java`
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/user/dto/UserPermissionCacheDto.java`
- Class/Record `implements Serializable` với `@Serial private static final long serialVersionUID = 1L;`.
- Gồm các thuộc tính: `userId`, `email`, `Set<String> roles`, `Set<String> permissions`.

#### 2. Tạo `UserPermissionCacheService.java`
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/user/internal/UserPermissionCacheService.java`
- Khai báo `@Service`, `@Slf4j`, `@RequiredArgsConstructor`. Inject `CustomUserDetailsService`.
- Viết 3 phương thức:
  1. `getUserPermissions(Long userId, String email)` -> `@Cacheable(value = "user-permissions", key = "#userId")`
  2. `evictUserPermissions(Long userId)` -> `@CacheEvict(value = "user-permissions", key = "#userId")`
  3. `evictAllUserPermissions()` -> `@CacheEvict(value = "user-permissions", allEntries = true)`

---

### 📍 GIAI ĐOẠN 5: TẠO ADMIN MANAGEMENT RESTFUL API & CẬP NHẬT CONTROLLERS

#### 1. Tạo DTOs Quản Lý RBAC (`user/dto`):
- `CreateRoleRequestDto(String name, String description)`
- `UpdateRolePermissionsRequestDto(Set<Long> permissionIds)`
- `UpdateUserRolesRequestDto(Set<Long> roleIds)`
- `RoleResponseDto(Long id, String name, String description, Set<PermissionResponseDto> permissions)`
- `PermissionResponseDto(Long id, String code, String description)`

#### 2. Viết `RbacAdminService.java` & `RbacAdminServiceImpl.java`:
- Interface `RbacAdminService` trong package `user`.
- Implementation trong `user/internal/RbacAdminServiceImpl.java`.
- Thực thi 5 hàm nghiệp vụ: `getAllRoles`, `createRole`, `getAllPermissions`, `updateRolePermissions`, `updateUserRoles`.
- Trong hàm `updateRolePermissions`: Gọi `userPermissionCacheService.evictAllUserPermissions()`.
- Trong hàm `updateUserRoles`: Gọi `userPermissionCacheService.evictUserPermissions(userId)`.

#### 3. Viết `RbacAdminController.java`:
- Thư mục: `src/main/java/com/devfat/mini_ecommerce/user/RbacAdminController.java`
- Phân quyền endpoint: `@PreAuthorize("hasAuthority('rbac:manage') or hasRole('ADMIN')")`.
- Cung cấp 5 RESTful APIs:
  - `GET /api/v1/admin/rbac/roles`
  - `POST /api/v1/admin/rbac/roles`
  - `GET /api/v1/admin/rbac/permissions`
  - `PATCH /api/v1/admin/rbac/roles/{roleId}/permissions`
  - `PATCH /api/v1/admin/rbac/users/{userId}/roles`

#### 4. Cập nhật `@PreAuthorize` ở các Controllers khác:
- Trong `ProductController.java`: Cập nhật `@PreAuthorize("hasAuthority('product:create') or hasRole('ADMIN')")`, `product:update`, `product:delete`.

---

## 🧪 GIAI ĐOẠN 6: KIỂM THỬ VÀ BIÊN DỊCH (VERIFICATION)

Sau khi bạn tự tay hoàn thành các file code theo 5 giai đoạn trên, mở Terminal chạy lệnh sau để kiểm tra:

```bash
./mvnw test-compile
```

* Nếu trả về **`BUILD SUCCESS`**: Mã nguồn của bạn đã hoàn toàn chính xác, biên dịch không lỗi!
* Nếu báo lỗi: Rà soát lại từng vị trí tương ứng theo file hướng dẫn này.

---

Chúc bạn thực thi thành công! Tôi luôn ở đây hỗ trợ giải đáp bất kỳ thắc mắc nào của bạn trong quá trình tự code.
