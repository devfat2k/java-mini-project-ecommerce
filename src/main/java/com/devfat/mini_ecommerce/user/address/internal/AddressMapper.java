package com.devfat.mini_ecommerce.user.address.internal;

import com.devfat.mini_ecommerce.user.address.dto.AddressResponseDto;
import com.devfat.mini_ecommerce.user.address.dto.CreateAddressRequestDto;
import com.devfat.mini_ecommerce.user.address.dto.UpdateAddressRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    // 1. Entity -> Response DTO
    AddressResponseDto toResponseDto(UserAddressEntity entity);

    // 2. List<Entity> -> List<Response DTO>
    List<AddressResponseDto> toResponseDtoList(List<UserAddressEntity> entities);

    // 3. Create Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaultAddress", ignore = true)
    UserAddressEntity toEntity(CreateAddressRequestDto requestDto);

    // 4. Update Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaultAddress", ignore = true)
    void updateEntityFromDto(UpdateAddressRequestDto updateDto, @MappingTarget UserAddressEntity entity);
}
