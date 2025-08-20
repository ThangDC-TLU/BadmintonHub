package com.badmintonhub.cartservice.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequest {
    private Long productId;        // ID sản phẩm chính
    private Long optionId;         // ID của ProductOption đã chọn (nếu có, có thể null)
    private int quantity;          // số lượng muốn thêm/cập nhật
}
