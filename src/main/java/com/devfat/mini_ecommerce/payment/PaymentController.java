package com.devfat.mini_ecommerce.payment;

import com.devfat.mini_ecommerce.payment.dto.CreatePaymentResponseDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
@Tag(name = "Payment", description = "Payment Processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create VNPay payment", description = "Generate a VNPay payment URL for an order.")
    @PostMapping("/{orderId}/create")
    public ResponseEntity<ApiResponse<CreatePaymentResponseDto>> createPayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long orderId,
            HttpServletRequest request
            ) {
        Long userId = userPrincipal.getUserId();
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        paymentService.createPayment(userId, orderId, request),
                        "Create Payment Successfully!"
                )
        );
    }

    @Operation(summary = "VNPay return callback", description = "Handle user redirect return from VNPay payment gateway.")
    @GetMapping("/vnpay-return")
    public ResponseEntity<Void> vnPayReturn(
            @RequestParam Map<String, String> allParams
            ) {
        String redirectUrl = paymentService.handleVnPayReturn(allParams);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }

    @Operation(summary = "VNPay IPN webhook", description = "Process Instant Payment Notification (IPN) from VNPay.")
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> allParams) {
        try {
            paymentService.handleVnPayIpn(allParams);
            // VNPay yêu cầu response đúng định dạng này để biết webhook đã nhận thành công
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "Unknown error"));
        }
    }
}
