package com.devfat.mini_ecommerce.user.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

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
    private Set<String> roles;
    private boolean active;
    private LocalDateTime createdAt;
    private Boolean emailVerified;
}
