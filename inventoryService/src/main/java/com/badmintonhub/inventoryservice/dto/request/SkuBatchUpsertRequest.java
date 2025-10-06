// dto/request/SkuBatchUpsertRequest.java
package com.badmintonhub.inventoryservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter; import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SkuBatchUpsertRequest {
    @NotEmpty @Valid
    private List<SkuUpsertRequest> items;
}
