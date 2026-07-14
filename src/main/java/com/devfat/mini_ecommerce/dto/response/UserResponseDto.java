package com.devfat.mini_ecommerce.dto.response;
import com.devfat.mini_ecommerce.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserEntity.Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
