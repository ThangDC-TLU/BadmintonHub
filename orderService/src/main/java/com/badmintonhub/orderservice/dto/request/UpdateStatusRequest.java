package com.badmintonhub.orderservice.dto.request;

import com.badmintonhub.orderservice.utils.constant.OrderStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateStatusRequest {
    @NotNull
    private OrderStatusEnum nextStatus;
    private String note;
}
