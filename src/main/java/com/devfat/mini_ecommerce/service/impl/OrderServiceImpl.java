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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.devfat.mini_ecommerce.entity.OrderEntity.OrderStatus.*;
import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;



    private static final Map<OrderEntity.OrderStatus, Set<OrderEntity.OrderStatus>> ALLOWED_ORDERS = Map.of(
            PENDING,   Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(SHIPPED, CANCELLED),
            SHIPPED,   Set.of(DONE)
            // DONE, CANCELLED không có entry -> get() trả null -> exception -> đúng vì đây là trạng thái kết thúc
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


    /**
     * 1. Lấy userId hiện tại từ @AuthenticationPrincipal (giống trên)
     * 2. Lấy role hiện tại từ userPrincipal (dùng lại getAuthorities() đã có, hoặc thêm getRole() nếu UserPrincipal đã có sẵn từ C3)
     * 3. Nếu role là ADMIN -> cho phép xem bất kỳ userId nào trong path (không cần so sánh)
     * 4. Nếu role là USER -> so sánh userId trong path với userId hiện tại:
     *      - Khớp -> cho qua
     *      - Không khớp -> throw AccessDeniedException (dùng đúng class Spring Security,
     *        để rơi vào đúng handler đã có sẵn từ D2 -> tự động trả 403 đúng format)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findByUserIdWithDetails(Long userIdPath, Long userIdInToken, String userRole) throws AccessDeniedException {
        if(userIdPath == null && userIdInToken == null) {
            throw new ResourceNotFoundException("User id is required!");
        }
        if(userRepository.findById(Objects.requireNonNull(userIdPath)).isEmpty() || userRepository.findById(userIdInToken).isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
        }

        if(userRole.equalsIgnoreCase("USER") && !(userIdInToken.equals(userIdPath))) {
            throw new AccessDeniedException("Access denied!. User ID is invalid or does not belong to you!");
        }

        return orderRepository.findByUserIdWithDetails(userIdPath)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto findById(Long orderId, Long userIdInToken, String userRole) throws AccessDeniedException {
        if(orderId == null) {
            throw new ResourceNotFoundException("Order id is null");
        }

        if(userIdInToken == null) {
            throw new AccessDeniedException("User ID is invalid or does not belong to you!");
        }

//        nếu KHÔNG phải ADMIN thì phải đúng userId -> cùng đơn hàng user đang có mới xem được -> Bắt trường hợp xem orderId của người khác
//        if(!(userRole.equalsIgnoreCase("ADMIN"))) {
//            orderRepository.findAllByUserId(userIdInToken).orElseThrow(() -> new AccessDeniedException("Access denied! Order ID is invalid or does not belong to you!"));
//        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found!"));

        if (userRole.equalsIgnoreCase("USER") && !order.getUser().getId().equals(userIdInToken)) {
            throw new AccessDeniedException("Access denied. This order does not belong to you!");
        }

        return toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDto create(Long userId, CreateOrderRequestDto createOrderRequestDto) {
        // b1. Tìm User theo user id
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
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

    // TODO (Optimize later): Xử lý Race Condition khi có nhiều request cùng update stock
    // Keywords để nâng cấp sau:
    // 1. JPA Optimistic Locking (Thêm annotation @Version vào entity Product)
    // 2. JPA Pessimistic Locking (Dùng @Lock(LockModeType.PESSIMISTIC_WRITE) ở Repository)
    // 3. Native DB Update (Tối ưu nhất: Viết @Modifying @Query("UPDATE Product p SET p.stock = p.stock + :qty WHERE p.id = :id"))
    @Override
    @Transactional
    public OrderResponseDto changeStatus(Long id, UpdateOrderStatusRequestDto updateOrderStatusRequestDto) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found!"));

        Set<OrderEntity.OrderStatus> allowedNext = ALLOWED_ORDERS.get(order.getStatus());
        if (allowedNext == null || !allowedNext.contains(updateOrderStatusRequestDto.orderStatus())) {
            throw new InvalidStatusTransitionException("Do not change from " + order.getStatus() + " to " + updateOrderStatusRequestDto.orderStatus());
        }

        if(updateOrderStatusRequestDto.orderStatus().equals(CANCELLED)) {
            order.getItems().forEach(item -> {
                int quantityInOrderCancel =  item.getQuantity();
                item.getProduct().setStock(item.getProduct().getStock() + quantityInOrderCancel);
            });
        }

        order.setStatus(updateOrderStatusRequestDto.orderStatus());
        return toOrderResponse(orderRepository.save(order));
    }
}
