package com.devfat.mini_ecommerce.user;

import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rbac")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('rbac:manage') or hasRole('ADMIN')")
@Tag(name = "RBAC Admin", description = "Role and Permission Management APIs")
public class RbacAdminController {

    private final RbacAdminService rbacAdminService;

    @Operation(summary = "Get all roles", description = "Retrieve a list of all roles with their assigned permissions.")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> getAllRoles() {
        List<RoleResponseDto> roles = rbacAdminService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles, "Get all roles successfully"));
    }

    @Operation(summary = "Create role", description = "Create a new role.")
    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(
            @Valid @RequestBody CreateRoleRequestDto request) {
        RoleResponseDto role = rbacAdminService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(role, "Create role successfully"));
    }

    @Operation(summary = "Get all permissions", description = "Retrieve a list of all available system permissions.")
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponseDto>>> getAllPermissions() {
        List<PermissionResponseDto> permissions = rbacAdminService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions, "Get all permissions successfully"));
    }

    @Operation(summary = "Update role permissions", description = "Assign or update permissions for a specific role.")
    @PatchMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRolePermissionsRequestDto request) {
        RoleResponseDto role = rbacAdminService.updateRolePermissions(roleId, request);
        return ResponseEntity.ok(ApiResponse.success(role, "Update role permissions successfully"));
    }

    @Operation(summary = "Update user roles", description = "Assign or update roles for a specific user.")
    @PatchMapping("/users/{userId}/roles")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequestDto request) {
        UserResponseDto user = rbacAdminService.updateUserRoles(userId, request);
        return ResponseEntity.ok(ApiResponse.success(user, "Update user roles successfully"));
    }
}
