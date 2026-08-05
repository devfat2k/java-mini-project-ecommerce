package com.devfat.mini_ecommerce.user.dto;

import com.devfat.mini_ecommerce.user.Role;









import lombok.*;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {
    private Long userId;
    private String fullName;
    private String avatarUrl;
    private String email;
    private String phoneNumber;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
