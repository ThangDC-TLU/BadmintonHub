package com.badmintonhub.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class InventorySetLevelRequest {
    @NotNull @Min(0)
    private Integer quantity;
    private String reason;
}