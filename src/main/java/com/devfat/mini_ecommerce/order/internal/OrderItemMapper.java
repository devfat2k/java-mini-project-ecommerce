package com.devfat.mini_ecommerce.order.internal;

import com.devfat.mini_ecommerce.order.dto.OrderItemRequestDto;
import com.devfat.mini_ecommerce.order.dto.OrderItemResponseDto;










import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "product.name", target = "productName")
    OrderItemResponseDto toResponseDto(OrderItemEntity orderItemEntity);

    List<OrderItemResponseDto> toResponseDtoList(List<OrderItemEntity> orderItemEntities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    OrderItemEntity toEntity(OrderItemRequestDto requestDto);
}
