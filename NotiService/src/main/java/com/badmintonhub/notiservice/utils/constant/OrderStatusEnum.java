package com.badmintonhub.notiservice.utils.constant;

public enum OrderStatusEnum {
    CREATED,      // vừa tạo đơn
    PROCESSING,   // đang xử lý/đóng gói
    SHIPPED,      // đã bàn giao vận chuyển
    DELIVERED,    // giao thành công
    CANCELLED     // đã hủy
}
