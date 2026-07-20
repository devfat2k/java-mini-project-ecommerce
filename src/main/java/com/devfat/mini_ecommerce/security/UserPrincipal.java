package com.devfat.mini_ecommerce.security;

import com.devfat.mini_ecommerce.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UserEntity userEntity;

    public Long getUserId() {
        return userEntity.getId();
    }

    public String getRole() {
        return userEntity.getRole().name();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    /**
     * mặc định sẽ có ROLE_ nên khi tạo db không chỉ cần ADMIN/USER/GUEST là được
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + userEntity.getRole().name()
                )
        );
    }

    /**
     * Trả trạng thái theo isActive từ DB -> mapping Sang UserEntity
     */
    @Override
    public boolean isEnabled() {
        return userEntity.isActive();
    }


    /**
     * Tạm thời trả true cho 3 method này là Tài khoản đã hết hạn, Khoá tài khoản, Chứng chỉ tài khoản hết hạn
     * Sẽ mở rộng sau nên tạm thời mặc định sẽ true, Hiện tại hệ thống chỉ cần isActive có hoạt động không là đủ
     */
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
