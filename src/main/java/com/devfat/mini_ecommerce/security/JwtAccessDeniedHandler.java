package com.devfat.mini_ecommerce.security;

import com.devfat.mini_ecommerce.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ====================================================================================
 * JWT ACCESS DENIED HANDLER (TRÌNH XỬ LÝ TỪ CHỐI TRUY CẬP)
 * ====================================================================================
 *
 * 1. VAI TRÒ CHÍNH (WHAT):
 *    - Đóng vai trò xử lý các trường hợp người dùng ĐÃ ĐƯỢC XÁC THỰC (đã đăng nhập hợp lệ)
 *      nhưng cố tình truy cập vào các API yêu cầu quyền hạn cao hơn (ví dụ: USER vào ADMIN).
 *
 * 2. LÝ DO XÂY DỰNG TÙY BIẾN (WHY):
 *    - Tránh việc Spring trả về trang lỗi HTML 403 mặc định.
 *    - Đảm bảo dữ liệu trả về cho Client luôn luôn là cấu trúc JSON đồng nhất với lỗi 401.
 *
 * 3. ĐIỂM KHÁC BIỆT VỚI ENTRY POINT (HOW):
 *    - Class này trả về mã trạng thái HTTP 403 Forbidden thay vì 401 Unauthorized.
 * ====================================================================================
 */
@Component
@AllArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // BƯỚC 1: Thiết lập mã trạng thái HTTP là 403 Forbidden (Bị cấm truy cập)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // BƯỚC 2: Định hình định dạng phản hồi trả về là JSON UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // BƯỚC 3: Cấu trúc JSON lỗi đồng bộ với JwtAuthenticationEntryPoint
//        String jsonResponse = String.format(
//                "{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"Access Denite ! %s\"}",
//                accessDeniedException.getMessage()
//        );
        ApiResponse<?> apiResponse = ApiResponse.error("Access Denied! " + accessDeniedException.getMessage());
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        // BƯỚC 4: Ghi trực tiếp JSON phản hồi xuống luồng kết nối mạng
        response.getWriter().write(jsonResponse);
    }
}