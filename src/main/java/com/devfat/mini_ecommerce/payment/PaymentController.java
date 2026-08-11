package com.devfat.mini_ecommerce.payment;

import com.devfat.mini_ecommerce.payment.dto.CreatePaymentResponseDto;
import com.devfat.mini_ecommerce.payment.internal.PaymentEntity;
import com.devfat.mini_ecommerce.payment.internal.PaymentRepository;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.exception.ResourceNotFoundException;
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
    private final PaymentRepository paymentRepository;


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
    public ResponseEntity<?> vnPayReturn(
            @RequestParam Map<String, String> allParams
            ) {


        // Bước 1: lấy vnp_TxnRef từ URL — đây là paymentId mình đã tự đặt lúc tạo Payment
        Long paymentId = Long.parseLong(allParams.get("vnp_TxnRef"));

        // Bước 2: dùng paymentId đó, query lại bảng payments trong DB
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Not Found"));

        String feUrl = "http://localhost:3000/";
        String redirectUrl = feUrl + "/payment-result?paymentId=" + payment.getId() + "&status=" + payment.getPaymentStatus() + "&orderId=" + payment.getOrder().getId();
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectUrl).body(payment);
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
