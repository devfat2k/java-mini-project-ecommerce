package com.devfat.mini_ecommerce.mapper;

import com.devfat.mini_ecommerce.dto.request.CreateCategoryRequestDto;
import com.devfat.mini_ecommerce.dto.response.CategoryResponseDto;
import com.devfat.mini_ecommerce.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // Entity -> Response DTO
    @Mapping(source = "name", target = "categoryName")
    CategoryResponseDto toResponseDto(CategoryEntity categoryEntity);

    // List<Entity> -> List<Response DTO>
    List<CategoryResponseDto> toResponseDtoList(List<CategoryEntity> categoryEntities);

    // Request DTO -> Entity (Tạo mới)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    CategoryEntity toEntity(CreateCategoryRequestDto requestDto);

    // Update DTO -> Entity có sẵn (Cập nhật)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntityFromDto(CreateCategoryRequestDto requestDto, @MappingTarget CategoryEntity categoryEntity);
}
