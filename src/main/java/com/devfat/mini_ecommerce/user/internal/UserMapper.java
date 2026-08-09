package com.devfat.mini_ecommerce.user.internal;

import com.devfat.mini_ecommerce.auth.dto.RegisterRequestDto;
import com.devfat.mini_ecommerce.user.dto.UpdateProfileRequestDto;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;










import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 1. Entity -> Response DTO
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "roles", target = "roles")
    UserResponseDto toResponseDto(UserEntity userEntity);

    default java.util.Set<String> mapRoles(java.util.Set<RoleEntity> roles) {
        if (roles == null) return java.util.Collections.emptySet();
        return roles.stream().map(RoleEntity::getName).collect(java.util.stream.Collectors.toSet());
    }

    // 2. List<Entity> -> List<Response DTO>
    List<UserResponseDto> toResponseDtoList(List<UserEntity> userEntities);

    // 3. Register DTO -> Entity (Đăng ký tài khoản)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true) // Set mặc định ROLE_USER trong Service
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    UserEntity toEntity(RegisterRequestDto registerDto);

    // 4. Update Profile DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // Không cho sửa Email
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDto(UpdateProfileRequestDto updateDto, @MappingTarget UserEntity userEntity);
}
