package com.devfat.mini_ecommerce.notification.internal;

/**
 * Phân loại email liên quan đến đơn hàng.
 * Dùng nội bộ trong notification module để build HTML template dùng chung.
 */
public enum OrderEmailType {
    ORDER_PLACED,
    PAYMENT_SUCCESS
}
