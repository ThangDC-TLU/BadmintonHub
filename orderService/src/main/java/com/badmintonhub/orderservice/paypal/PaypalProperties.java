package com.badmintonhub.orderservice.paypal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "paypal")
public class PaypalProperties {
    private String baseUrl;
    private String clientId;
    private String secret;
}
