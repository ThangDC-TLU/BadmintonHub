package com.badmintonhub.orderservice.utils.constant;

public enum PaymentStatusEnum {
    UNPAID,    // chưa thanh toán
    PENDING,   // đã tạo phiên thanh toán, đang chờ
    PAID,      // đã thanh toán thành công
    FAILED,    // thanh toán thất bại
    REFUNDED   // đã hoàn tiền
}
