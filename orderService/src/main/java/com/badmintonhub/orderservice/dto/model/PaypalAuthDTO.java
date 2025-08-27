package com.badmintonhub.orderservice.dto.model;

import lombok.Data;

@Data
public class PaypalAuthDTO {
    public String scope;
    public String access_token;
    public String token_type;
    public String app_id;
    public int expires_in;
    public String nonce;
}
