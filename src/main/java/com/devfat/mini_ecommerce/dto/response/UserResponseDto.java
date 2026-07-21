package com.devfat.mini_ecommerce.dto.response;
import com.devfat.mini_ecommerce.entity.UserEntity;
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
    private UserEntity.Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
