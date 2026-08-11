package com.devfat.mini_ecommerce.user;

import com.devfat.mini_ecommerce.user.dto.*;

import java.util.List;

public interface RbacAdminService {
    List<RoleResponseDto> getAllRoles();

    RoleResponseDto createRole(CreateRoleRequestDto request);

    List<PermissionResponseDto> getAllPermissions();

    RoleResponseDto updateRolePermissions(Long roleId, UpdateRolePermissionsRequestDto request);

    UserResponseDto updateUserRoles(Long userId, UpdateUserRolesRequestDto request);
}
