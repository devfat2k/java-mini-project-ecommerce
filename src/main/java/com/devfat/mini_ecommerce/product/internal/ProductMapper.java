package com.devfat.mini_ecommerce.product.internal;

import com.devfat.mini_ecommerce.category.internal.CategoryMapper;
import com.devfat.mini_ecommerce.product.dto.CreateProductRequestDto;
import com.devfat.mini_ecommerce.product.dto.ProductResponseDto;
import com.devfat.mini_ecommerce.product.dto.UpdateProductRequestDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {
    // 1. Entity -> Response DTO
    ProductResponseDto toResponseDto(ProductEntity productEntity);
    // 2. List<Entity> -> List<Response DTO>
    List<ProductResponseDto> toResponseDtoList(List<ProductEntity> productEntities);
    // 3. Request DTO -> Entity (Tạo sản phẩm mới)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true) // Category sẽ được set thủ công từ DB trong Service
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "version", ignore = true)
    ProductEntity toEntity(CreateProductRequestDto requestDto);
    // 4. Update DTO -> Entity (Sửa sản phẩm)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(UpdateProductRequestDto requestDto, @MappingTarget ProductEntity productEntity);
}
