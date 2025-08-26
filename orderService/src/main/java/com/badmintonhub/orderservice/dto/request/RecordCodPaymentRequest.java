package com.badmintonhub.orderservice.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class RecordCodPaymentRequest {
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Instant paidAt;
    private String note;
}
