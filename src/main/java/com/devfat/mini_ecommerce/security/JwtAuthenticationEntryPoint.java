package com.devfat.mini_ecommerce.security;

import com.devfat.mini_ecommerce.base.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ====================================================================================
 * JWT AUTHENTICATION ENTRY POINT (ĐIỂM ĐÓN NGOẠI LỆ XÁC THỰC)
 * ====================================================================================
 *
 * 1. VAI TRÒ CHÍNH (WHAT):
 *    - Đây là thành phần chịu trách nhiệm "đón" và xử lý các yêu cầu (requests) chưa được
 *      xác thực khi cố gắng truy cập vào các tài nguyên/API yêu cầu quyền hạn bảo mật.
 *    - Phù hợp hoàn hảo cho hệ thống REST API "Stateless" (sử dụng JWT thay vì Session).
 *
 * 2. LÝ DO XÂY DỰNG TÙY BIẾN (WHY):
 *    - Mặc định, Spring Security Web sẽ điều hướng (redirect) người dùng về trang hiển thị
 *      form đăng nhập (Login Page) khi xác thực thất bại.
 *    - Tuy nhiên, với REST API, Client (React, Angular, Mobile App) cần nhận về một phản hồi
 *      dữ liệu JSON kèm mã trạng thái HTTP chuẩn hóa (như 401 Unauthorized) để tự xử lý
 *      logic hiển thị trên giao diện của họ. Thành phần này giải quyết bài toán đó.
 *
 * 3. LUỒNG HOẠT ĐỘNG TRONG CHUỖI BỘ LỌC (HOW):
 *    - Khi Client gửi yêu cầu không hợp lệ (không có JWT hoặc token hết hạn/sai chữ ký):
 *      -> Các bộ lọc xác thực tùy biến hoặc bộ lọc phân quyền phía sau sẽ ném ra
 *         ngoại lệ "AuthenticationException".
 *      -> Ngoại lệ này lập tức bị bắt lại bởi bộ lọc dịch ngoại lệ "ExceptionTranslationFilter"
 *         (bộ lọc đứng chiến lược ngay trước bộ lọc phân quyền cuối cùng).
 *      -> ExceptionTranslationFilter dọn dẹp ngữ cảnh cũ trong SecurityContextHolder
 *         và kích hoạt phương thức "commence()" của class này để cấu trúc phản hồi lỗi.
 * ====================================================================================
 */
@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    /**
     * Phương thức commence() được Spring Security tự động triệu gọi khi phát hiện hành vi
     * truy cập trái phép từ người dùng chưa được xác định danh tính.
     *
     * @param request       Đối tượng HttpServletRequest chứa toàn bộ thông tin yêu cầu gửi đến.
     * @param response      Đối tượng HttpServletResponse dùng để cấu trúc phản hồi gửi về Client.
     * @param authException Chi tiết ngoại lệ xác thực cụ thể bị bắt giữ (chứa thông điệp lỗi hệ thống).
     */

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // BƯỚC 1: Thiết lập mã trạng thái HTTP (HTTP Status Code) là 401 Unauthorized.
        // Đây là mã chuẩn hóa quốc tế để báo cho Client biết: "Bạn cần cung cấp thông tin xác thực hợp lệ để truy cập".
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // BƯỚC 2: Định hình kiểu dữ liệu phản hồi trả về là JSON và thiết lập bộ mã hóa ký tự UTF-8.
        // Điều này đảm bảo phía Client (ví dụ: axios, fetch) phân tích dữ liệu dạng đối tượng JSON chuẩn xác,
        // đồng thời tránh lỗi font chữ khi thông điệp chứa các ký tự tiếng Việt có dấu.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // BƯỚC 3: Tạo chuỗi JSON định dạng lỗi tùy biến.
        // Bạn có thể thiết lập cấu trúc JSON này đồng bộ với định dạng phản hồi lỗi (Error Response) của dự án e-commerce.
        // Trong đó, thông điệp lỗi cụ thể của hệ thống sẽ được tiêm động thông qua "authException.getMessage()".
//        {"success": false, "message": "...", "data": null, "timestamp": "..."}
//        String jsonResponse = String.format(
//                "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
//                authException.getMessage()
//        );
        ApiResponse<?> apiResponse = ApiResponse.error("Unauthorized: " + authException.getMessage());

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        // BƯỚC 4: Xuất dữ liệu JSON trực tiếp vào luồng ghi phản hồi (Response Writer).
        // Thao tác này ghi dữ liệu xuống mạng để phản hồi ngay lập tức cho Client, chặn đứng yêu cầu tại đây
        // và không cho phép yêu cầu HTTP tiếp tục đi sâu vào các Controller nghiệp vụ phía sau.
        response.getWriter().write(jsonResponse);
    }
}