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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
@Tag(name = "Payment", description = "Payment Processing APIs")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;


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
    @Transactional(readOnly = true)
    public ResponseEntity<Void> vnPayReturn(
            @RequestParam Map<String, String> allParams
            ) {
        // Bước 1: lấy vnp_TxnRef từ URL — đây là paymentId mình đã tự đặt lúc tạo Payment
        Long paymentId = Long.parseLong(allParams.get("vnp_TxnRef"));

        // Bước 2: dùng paymentId đó, query lại bảng payments trong DB
        // @Transactional(readOnly=true) giữ session mở → truy cập lazy payment.getOrder() an toàn
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Not Found"));

        // Bước 3: Map status theo chuẩn FE — "success" | "failed" (lowercase)
        // vnp_ResponseCode = "00" → giao dịch thành công (VNPay quy định)
        String responseCode = allParams.getOrDefault("vnp_ResponseCode", "");
        String status = "00".equals(responseCode) ? "success" : "failed";

        // Bước 4: Lấy mã giao dịch VNPay (vnp_TransactionNo) — FE hiển thị cho user
        // Chỉ có giá trị khi giao dịch thành công; để trống nếu thất bại
        String vnpTransactionNo = allParams.getOrDefault("vnp_TransactionNo", "");

        // Bước 5: Build redirect URL về FE theo chuẩn FE doc
        // Path: /en/payment-result (next-intl locale prefix)
        String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        StringBuilder redirectUrl = new StringBuilder(baseUrl)
                .append("/en/payment-result")
                .append("?orderId=").append(payment.getOrder().getId())
                .append("&status=").append(status)
                .append("&paymentMethod=").append(payment.getPaymentMethod());

        if (!vnpTransactionNo.isEmpty()) {
            redirectUrl.append("&paymentId=").append(vnpTransactionNo);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl.toString())
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
