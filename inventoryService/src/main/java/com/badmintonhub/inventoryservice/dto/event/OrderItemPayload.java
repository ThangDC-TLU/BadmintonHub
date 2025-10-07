package com.badmintonhub.inventoryservice.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPayload {
    @NotNull
    private Long skuId;

    @NotNull @Min(1)
    private Integer quantity;
}
