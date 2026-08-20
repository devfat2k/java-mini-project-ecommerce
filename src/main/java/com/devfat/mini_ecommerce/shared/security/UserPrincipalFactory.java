package com.devfat.mini_ecommerce.shared.security;

import com.devfat.mini_ecommerce.user.internal.PermissionEntity;
import com.devfat.mini_ecommerce.user.internal.RoleEntity;
import com.devfat.mini_ecommerce.user.internal.UserEntity;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class UserPrincipalFactory {

    public static UserPrincipal fromEntity(UserEntity user) {
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        if (user.getRoles() != null) {
            for (RoleEntity role : user.getRoles()) {
                if (role.getName() != null) {
                    roles.add(role.getName());
                }
                if (role.getPermissions() != null) {
                    for (PermissionEntity permission : role.getPermissions()) {
                        if (permission.getCode() != null) {
                            permissions.add(permission.getCode());
                        }
                    }
                }
            }
        }

        return UserPrincipal.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(roles)
                .permissions(permissions)
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
