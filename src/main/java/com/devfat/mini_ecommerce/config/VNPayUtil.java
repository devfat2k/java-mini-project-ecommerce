package com.devfat.mini_ecommerce.config;


import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPayUtil {

    // Sinh chữ ký HMAC-SHA512 — copy nguyên lý từ Config.hmacSHA512() gốc
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo chữ ký VNPay", e);
        }
    }

    // Lấy IP thật của client — copy từ Config.getIpAddress() gốc
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    // Build cả query string (cho URL) VÀ hashData (để ký) trong 1 lần duyệt
    // Copy đúng logic vòng lặp trong ajaxServlet.java — chú ý URLEncoder ở CẢ 2 bên
    public static Map<String, String> buildQueryAndHash(Map<String, String> params, String secretKey) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);   // BẮT BUỘC sort trước khi ký

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(secretKey, hashData.toString());

        Map<String, String> result = new HashMap<>();
        result.put("queryUrl", query.toString());
        result.put("secureHash", secureHash);
        return result;
    }

    public static boolean verifySignature(Map<String, String> params, String secretKey) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return false; // Nếu request không có chữ ký, chắc chắn là không hợp lệ
        }

        Map<String, String> cleanParams = new HashMap<>(params);
        cleanParams.remove("vnp_SecureHash");
        cleanParams.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(cleanParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();


        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = cleanParams.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Nếu chưa phải tham số cuối cùng thì thêm dấu &
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String mySecureHash = hmacSHA512(secretKey, hashData.toString());

        return mySecureHash.equals(vnp_SecureHash);
    }
}