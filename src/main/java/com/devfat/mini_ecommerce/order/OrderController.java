package com.devfat.mini_ecommerce.order;

import com.devfat.mini_ecommerce.order.dto.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.order.dto.OrderResponseDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "User Order Management APIs")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Get orders by user ID and status", description = "Retrieve paginated orders for a specific user and order status.")
    @GetMapping("/me/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getOrderResponse(
            @PathVariable Long userId,
            @RequestParam OrderStatus status,
            Pageable pageable
    )  {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        orderService.getOrderByUserIdAndStatusWithDetails(userId, status, pageable),
                        "Get Order By User ID Successfully!"
                )
        );
    }

    @Operation(summary = "Get order by ID", description = "Retrieve order details by order ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userIdInToken = userPrincipal.getUserId();
        String role = userPrincipal.getRole();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        orderService.findById(orderId, userIdInToken, role),
                        "Get Order Successfully!"
                )
        );
    }

    @Operation(summary = "Create order", description = "Create a new order for the authenticated user.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody() CreateOrderRequestDto createOrderRequest
    ) throws JsonProcessingException {
        Long currentUserId = userPrincipal.getUserId();
        OrderResponseDto orderResponse = orderService.create(currentUserId, createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(orderResponse, "Create Order Successfully!")
        );
    }


    @Operation(summary = "Get current user orders", description = "Retrieve a paginated list of orders for the authenticated user.")
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable
    ){
        Long userId = userPrincipal.getUserId();
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        orderService.getMyOrder(userId, pageable),
                        "Get Your Order Successfully!"
                )
        );
    }

    @Operation(summary = "Cancel order", description = "Cancel an existing order for the authenticated user.")
    @PostMapping("/{id}/cancel-order")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();
        orderService.cancelOrder(id, userId);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        null,
                        "Cancel Order Successfully"
                )
        );
    }
}
