package com.badmintonhub.inventoryservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLevelDTO {
    /** Mã SKU (business key) */
    private String skuCode;

    /** Số lượng vật lý trong kho */
    private int onHand;

    /** Đã giữ chỗ cho đơn (chưa trừ onHand) */
    private int reserved;

    /** Đã cam kết sau thanh toán (chờ xuất) */
    private int allocated;

    /** Khả dụng = onHand - reserved - allocated (được tính trong service) */
    private int available;
}
