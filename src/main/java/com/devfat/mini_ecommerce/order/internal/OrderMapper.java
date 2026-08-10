package com.devfat.mini_ecommerce.order.internal;

import com.devfat.mini_ecommerce.order.dto.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.order.dto.OrderResponseDto;










import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

// Sử dụng 'uses = OrderItemMapper.class' để tự động map danh sách items bên trong Order!
@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "items", target = "orderItems")
    @Mapping(source = "shippingAddressSnapshot", target = "shippingAddressSnapshot")
    OrderResponseDto toResponseDto(OrderEntity orderEntity);

    List<OrderResponseDto> toResponseDtoList(List<OrderEntity> orderEntities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "version", ignore = true)
    OrderEntity toEntity(CreateOrderRequestDto requestDto);
}
