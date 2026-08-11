package com.devfat.mini_ecommerce.shared.security;

import com.devfat.mini_ecommerce.user.internal.PermissionEntity;
import com.devfat.mini_ecommerce.user.internal.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final boolean active;
    private final boolean emailVerified;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                String roleAuth = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                authorities.add(new SimpleGrantedAuthority(roleAuth));
            }
        }
        if (permissions != null) {
            for (String p : permissions) {
                authorities.add(new SimpleGrantedAuthority(p));
            }
        }
        return authorities;
    }

    public String getRole() {
        if (roles != null && !roles.isEmpty()) {
            return roles.contains("ADMIN") ? "ADMIN" : roles.iterator().next();
        }
        return "USER";
    }

    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
}
