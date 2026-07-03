package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderItemResponseDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import com.devfat.mini_ecommerce.entity.OrderEntity;
import com.devfat.mini_ecommerce.repository.OrderRepository;
import com.devfat.mini_ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private OrderResponseDto toOrderResponse(OrderEntity orderEntity) {
        return OrderResponseDto.builder()
                .id(orderEntity.getId())
                .status(orderEntity.getStatus())
                .totalAmount(orderEntity.getTotalAmount())
                .createdAt(orderEntity.getCreatedAt())
                .orderItems(
                        orderEntity.getItems().stream()
                                .map(item -> OrderItemResponseDto.builder()
                                        .productName(item.getProduct().getName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .build()
                                ).toList()
                ).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findByUserIdWithDetails(Long userId) {
        if(userId == null) {
            throw new IllegalArgumentException("userId is null");
        }
        return orderRepository.findByUserIdWithDetails(userId)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto findById(Long orderId) {
        if(orderId == null) {
            throw new IllegalArgumentException("orderId is null");
        }
        return  toOrderResponse(Objects.requireNonNull(orderRepository.findById(orderId).orElse(null)));
    }

    @Override
    public OrderResponseDto create(CreateOrderRequestDto createOrderRequestDto) {
        return null;
    }

}
