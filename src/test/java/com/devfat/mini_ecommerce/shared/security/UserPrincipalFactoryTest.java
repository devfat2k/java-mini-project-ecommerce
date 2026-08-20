package com.devfat.mini_ecommerce.shared.security;

import com.devfat.mini_ecommerce.user.internal.PermissionEntity;
import com.devfat.mini_ecommerce.user.internal.RoleEntity;
import com.devfat.mini_ecommerce.user.internal.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalFactoryTest {

    @Test
    @DisplayName("Should correctly map UserEntity with roles and permissions to UserPrincipal")
    void shouldMapUserEntityToUserPrincipal() {
        PermissionEntity perm1 = PermissionEntity.builder()
                .code("PRODUCT_CREATE")
                .description("Create Product")
                .build();

        PermissionEntity perm2 = PermissionEntity.builder()
                .code("PRODUCT_UPDATE")
                .description("Update Product")
                .build();

        RoleEntity roleAdmin = RoleEntity.builder()
                .name("ADMIN")
                .permissions(Set.of(perm1, perm2))
                .build();

        UserEntity user = UserEntity.builder()
                .id(100L)
                .email("admin@test.com")
                .password("hashed_secret")
                .active(true)
                .emailVerified(true)
                .roles(Set.of(roleAdmin))
                .build();

        UserPrincipal principal = UserPrincipalFactory.fromEntity(user);

        assertThat(principal).isNotNull();
        assertThat(principal.getUserId()).isEqualTo(100L);
        assertThat(principal.getEmail()).isEqualTo("admin@test.com");
        assertThat(principal.getPassword()).isEqualTo("hashed_secret");
        assertThat(principal.isActive()).isTrue();
        assertThat(principal.isEmailVerified()).isTrue();
        assertThat(principal.getRoles()).containsExactly("ADMIN");
        assertThat(principal.getPermissions()).containsExactlyInAnyOrder("PRODUCT_CREATE", "PRODUCT_UPDATE");
    }

    @Test
    @DisplayName("Should handle null roles safely")
    void shouldHandleNullRoles() {
        UserEntity user = UserEntity.builder()
                .id(101L)
                .email("user@test.com")
                .password("hashed_secret")
                .active(true)
                .emailVerified(false)
                .roles(null)
                .build();

        UserPrincipal principal = UserPrincipalFactory.fromEntity(user);

        assertThat(principal).isNotNull();
        assertThat(principal.getUserId()).isEqualTo(101L);
        assertThat(principal.getRoles()).isEmpty();
        assertThat(principal.getPermissions()).isEmpty();
    }
}
