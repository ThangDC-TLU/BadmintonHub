package com.badmintonhub.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaypalOrderCreateResponse {
    private String id;      // PayPal order id
    private String status;  // CREATED/PAYER_ACTION_REQUIRED/...
    private List<Link> links;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        private String href;
        private String rel;     // "approve" | "payer-action"
        private String method;  // "GET"/"POST"
    }
}
