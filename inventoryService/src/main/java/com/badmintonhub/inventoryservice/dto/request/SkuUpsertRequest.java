// dto/request/SkuUpsertRequest.java
package com.badmintonhub.inventoryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class SkuUpsertRequest {
    @NotBlank
    private String skuCode;

    @NotNull
    private Long productId;

    @NotBlank
    private String name;

    private String optionJson;
    private String barcode;

    private Integer weightGram;
    private Integer widthMm;
    private Integer heightMm;
    private Integer lengthMm;

    @NotNull
    private Boolean isActive;
}
