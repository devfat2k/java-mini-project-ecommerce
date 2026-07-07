package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateOrderStatusRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderItemResponseDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import com.devfat.mini_ecommerce.entity.OrderEntity;
import com.devfat.mini_ecommerce.entity.OrderItemEntity;
import com.devfat.mini_ecommerce.entity.ProductEntity;
import com.devfat.mini_ecommerce.entity.UserEntity;
import com.devfat.mini_ecommerce.exception.InsufficientStockException;
import com.devfat.mini_ecommerce.exception.InvalidStatusTransitionException;
import com.devfat.mini_ecommerce.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.repository.OrderRepository;
import com.devfat.mini_ecommerce.repository.ProductRepository;
import com.devfat.mini_ecommerce.repository.UserRepository;
import com.devfat.mini_ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.devfat.mini_ecommerce.entity.OrderEntity.OrderStatus.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private static final Map<OrderEntity.OrderStatus, Set<OrderEntity.OrderStatus>> ALLOWED_ORDERS = Map.of(
            PENDING, Set.of(CONFIRMED),
            CONFIRMED, Set.of(SHIPPED),
            SHIPPED, Set.of(DONE)
//            CANCELLED, Set.of(PENDING, CONFIRMED)
    );

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
            throw new ResourceNotFoundException("User id is required!");
        }
        if(userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
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
            throw new ResourceNotFoundException("Order id is null");
        }
        return toOrderResponse(Objects.requireNonNull(orderRepository.findById(orderId).orElse(null)));
    }

    @Override
    @Transactional
    public OrderResponseDto create(CreateOrderRequestDto createOrderRequestDto) {
        // b1. Tìm User theo user id
        UserEntity user = userRepository.findById(createOrderRequestDto.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        // b2. Tạo đơn hàng rỗng - có total amount =0
        OrderEntity order = new OrderEntity();
        order.setUser(Objects.requireNonNull(user));
        order.setStatus(OrderEntity.OrderStatus.PENDING); // Bước này tôi nghĩ rằng vừa đúng vừa sai vì tạo đơn hàng rỗng thì nó sẽ PENDING -> lúc tạo thành công sẽ cặp nhât DONE Hoặc Logic nghiệp vụ khác sau này
        order.setTotalAmount(BigDecimal.ZERO);
        /**
         * Lặp qua từng item trong request.items()
         * Tìm Product theo productId — không thấy → exception
         * Kiểm tra product.getStock() >= quantity — không đủ → exception
         * Trừ stock: product.setStock(stock - quantity)
         * Tạo OrderItem mới (product, quantity, unitPrice = giá hiện tại)
         * Gắn item vào order.getItems().add(item) + set item.setOrder(order)
         */
        createOrderRequestDto.items().forEach(requestItem -> {
           ProductEntity product = productRepository.findById(requestItem.productId()).orElseThrow(() ->  new ResourceNotFoundException("Product not found!"));
            if (product.getStock() < requestItem.quantity()) {
                throw new InsufficientStockException("Not enough product: " + product.getName());
            }
           product.setStock(product.getStock() - requestItem.quantity());
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .product(product)
                    .quantity(requestItem.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.getItems().add(orderItem);
        });
        // Tính total amount
        BigDecimal totalMoney = order.getItems().stream()
                .map(itemPrice -> itemPrice.getUnitPrice().multiply(BigDecimal.valueOf(itemPrice.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalMoney);
        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    @Override
    public OrderResponseDto changeStatus(Long id, UpdateOrderStatusRequestDto updateOrderStatusRequestDto) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found!"));

        Set<OrderEntity.OrderStatus> allowedNext = ALLOWED_ORDERS.get(order.getStatus());
        if (allowedNext == null || !allowedNext.contains(updateOrderStatusRequestDto.orderStatus())) {
            throw new InvalidStatusTransitionException("Do not change from " +  order.getStatus() + " to " + updateOrderStatusRequestDto.orderStatus());
        }
        order.setStatus(updateOrderStatusRequestDto.orderStatus());
        return toOrderResponse(orderRepository.save(order));
    }
}
