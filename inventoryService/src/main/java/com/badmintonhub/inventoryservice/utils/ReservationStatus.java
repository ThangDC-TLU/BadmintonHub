package com.badmintonhub.inventoryservice.utils;

public enum ReservationStatus {
    ACTIVE,     // đang giữ chỗ
    RELEASED,   // đã nhả (huỷ đơn/hết hạn)
    CONVERTED,  // chuyển sang allocated
    EXPIRED     // cron hết hạn
}
