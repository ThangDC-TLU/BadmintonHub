package com.badmintonhub.orderservice.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

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
