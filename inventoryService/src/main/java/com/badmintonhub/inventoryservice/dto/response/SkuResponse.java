// dto/response/SkuResponse.java
package com.badmintonhub.inventoryservice.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkuResponse {
    private Long id;
    private String skuCode;
    private Long productId;
    private String name;
    private String optionJson;
    private String barcode;
    private Integer weightGram;
    private Integer widthMm;
    private Integer heightMm;
    private Integer lengthMm;
    private Boolean isActive;
}
