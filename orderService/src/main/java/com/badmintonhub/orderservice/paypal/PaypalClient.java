package com.badmintonhub.orderservice.paypal;

import com.badmintonhub.orderservice.dto.model.PaypalAuthDTO;
import com.badmintonhub.orderservice.dto.request.PaypalOrderCreateRequest;
import com.badmintonhub.orderservice.dto.response.PaypalOrderCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaypalClient {
    private final WebClient webClient;
    private final PaypalProperties paypalProperties;

    public PaypalOrderCreateResponse createOrder(
            String referenceId,
            BigDecimal amount,
            String currency,
            PaypalOrderCreateRequest.ExperienceContext exp
    ) {
        String value = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        PaypalOrderCreateRequest req = PaypalOrderCreateRequest.builder()
                .intent("CAPTURE")
                .purchaseUnits(List.of(
                        PaypalOrderCreateRequest.PurchaseUnit.builder()
                                .referenceId(referenceId)
                                .amount(PaypalOrderCreateRequest.Amount.builder()
                                        .currencyCode(currency)   // -> JSON: currency_code
                                        .value(value)             // -> JSON: value
                                        .build())
                                .build()
                ))
                .paymentSource(
                        PaypalOrderCreateRequest.PaymentSource.builder()
                                .paypal(
                                        PaypalOrderCreateRequest.Paypal.builder()
                                                .experienceContext(
                                                        PaypalOrderCreateRequest.ExperienceContext.builder()
                                                                .returnUrl(exp.getReturnUrl())
                                                                .cancelUrl(exp.getCancelUrl())
                                                                .brandName("BadmintonHub")
                                                                .userAction("PAY_NOW")
                                                                .shippingPreference("NO_SHIPPING")
                                                                .locale("en-US")
                                                                .paymentMethodPreference("IMMEDIATE_PAYMENT_REQUIRED")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();


        return webClient.post()
                .uri(paypalProperties.getBaseUrl() + "/v2/checkout/orders")
                .headers(header -> header.setBearerAuth(getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(PaypalOrderCreateResponse.class)
                .block();
    }

    // Trả về DTO thay vì String
    public PaypalOrderCreateResponse captureOrder(String paypalOrderId) {
        return webClient.post()
                .uri(paypalProperties.getBaseUrl() + "/v2/checkout/orders/{id}/capture", paypalOrderId)
                .headers(header -> header.setBearerAuth(getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Collections.emptyMap())
                .retrieve()
                .bodyToMono(PaypalOrderCreateResponse.class)
                .block();
    }

    private String getAccessToken(){
        PaypalAuthDTO result = webClient.post()
                .uri(paypalProperties.getBaseUrl() + "/v1/oauth2/token")
                .headers(headers
                        -> headers.setBasicAuth(paypalProperties.getClientId(), paypalProperties.getSecret()))
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(PaypalAuthDTO.class)
                .block();
        log.info("<<<<Log: " + result.toString());
        if (result.access_token == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Can't get access token from paypal client_id/secret");
        return result.access_token;
    }

}
