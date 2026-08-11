package com.devfat.mini_ecommerce.user.internal;

import com.devfat.mini_ecommerce.shared.exception.DuplicateResourceException;
import com.devfat.mini_ecommerce.shared.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.user.RbacAdminService;
import com.devfat.mini_ecommerce.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacAdminServiceImpl implements RbacAdminService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserPermissionCacheService userPermissionCacheService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponseDto createRole(CreateRoleRequestDto request) {
        String roleName = request.name().trim().toUpperCase();
        if (roleRepository.existsByName(roleName)) {
            throw new DuplicateResourceException("Role '" + roleName + "' đã tồn tại trong hệ thống");
        }

        RoleEntity role = RoleEntity.builder()
                .name(roleName)
                .description(request.description())
                .permissions(new HashSet<>())
                .build();

        RoleEntity savedRole = roleRepository.save(role);
        return mapToRoleResponseDto(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponseDto(p.getId(), p.getCode(), p.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponseDto updateRolePermissions(Long roleId, UpdateRolePermissionsRequestDto request) {
        RoleEntity role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Role với ID: " + roleId));

        List<PermissionEntity> permissions = permissionRepository.findAllById(request.permissionIds());
        role.setPermissions(new HashSet<>(permissions));

        RoleEntity updatedRole = roleRepository.save(role);

        // Kích hoạt Evict toàn bộ user-permissions cache trong Redis
        userPermissionCacheService.evictAllUserPermissions();

        return mapToRoleResponseDto(updatedRole);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserRoles(Long userId, UpdateUserRolesRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + userId));

        List<RoleEntity> roles = roleRepository.findAllById(request.roleIds());
        user.setRoles(new HashSet<>(roles));

        UserEntity updatedUser = userRepository.save(user);

        // Kích hoạt Evict cache của duy nhất userId này trong Redis
        userPermissionCacheService.evictUserPermissions(userId);

        return userMapper.toResponseDto(updatedUser);
    }

    private RoleResponseDto mapToRoleResponseDto(RoleEntity role) {
        Set<PermissionResponseDto> permissionDtos = role.getPermissions() != null
                ? role.getPermissions().stream()
                .map(p -> new PermissionResponseDto(p.getId(), p.getCode(), p.getDescription()))
                .collect(Collectors.toSet())
                : Set.of();

        return new RoleResponseDto(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissionDtos
        );
    }
}
