package com.badmintonhub.orderservice.service;

import com.badmintonhub.orderservice.dto.message.RestResponse;
import com.badmintonhub.orderservice.dto.model.AddressDTO;
import com.badmintonhub.orderservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.orderservice.dto.request.PaypalOrderCreateRequest;
import com.badmintonhub.orderservice.dto.response.PaypalOrderCreateResponse;
import com.badmintonhub.orderservice.exception.IdInvalidException;
import com.badmintonhub.orderservice.paypal.PaypalClient;
import com.badmintonhub.orderservice.utils.constant.CustomHeaders;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class OrderExternalService {
    private final WebClient.Builder webClientBuilder;
    private final PaypalClient paypalClient;

    public OrderExternalService(WebClient.Builder webClientBuilder, PaypalClient paypalClient){
        this.webClientBuilder = webClientBuilder;
        this.paypalClient = paypalClient;
    }

    @CircuitBreaker(name = "orderToAuth", fallbackMethod = "fallbackAddress")
    @Retry(name = "orderToAuth")
    public RestResponse<AddressDTO> getAddressById(long id) throws IdInvalidException {
        return webClientBuilder
                .build()
                .get()
                .uri("http://AUTH-SERVICE/api/v1/address/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "AUTH-SERVICE HTTP %s: %s".formatted(r.statusCode(), body)))))
                .bodyToMono(new ParameterizedTypeReference<RestResponse<AddressDTO>>() {})
                .timeout(Duration.ofSeconds(2))
                .block();
    }

    @CircuitBreaker(name = "orderToCart", fallbackMethod = "fallbackCart")
    @Retry(name = "orderToCart")
    public RestResponse<List<ProductItemBriefDTO>> getCart(long userId) throws IdInvalidException {
        return webClientBuilder
                .build()
                .get()
                .uri("http://CART-SERVICE/api/v1/carts")
                .header(CustomHeaders.X_AUTH_USER_ID, String.valueOf(userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "CART-SERVICE HTTP %s: %s".formatted(r.statusCode(), body)))))
                .bodyToMono(new ParameterizedTypeReference<RestResponse<List<ProductItemBriefDTO>>>() {})
                .block();
    }

    // ===== PAYPAL =====
    @CircuitBreaker(name = "orderToPaypal", fallbackMethod = "fallbackPaypal")
    @Retry(name = "orderToPaypal")
    @Bulkhead(name = "orderToPaypal")
    public RestResponse<PaypalOrderCreateResponse> createPaypalOrder(
            String orderCode, BigDecimal amount, String currency,
            PaypalOrderCreateRequest.ExperienceContext exp
    ) {
        PaypalOrderCreateResponse p = paypalClient.createOrder(orderCode, amount, currency, exp);
        // call thành công
        RestResponse<PaypalOrderCreateResponse> r = new RestResponse<>();
        r.setStatusCode(HttpStatus.CREATED.value());
        r.setError(null);
        r.setMessage("Paypal Order Created");
        r.setData(p);

        return r;
    }


    // Fallback methods
    private RestResponse<AddressDTO> fallbackAddress(long addressId, Throwable t) { // dùng long
        log.error("Address service failed for addressId {}: {}", addressId, t.getMessage());

        RestResponse<AddressDTO> resp = new RestResponse<>();   // dùng no-args
        resp.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
        resp.setError("Service Unavailable");                   // nếu RestResponse có field 'error'
        resp.setMessage("Address service unavailable");
        resp.setData(null);
        return resp;
    }

    private RestResponse<List<ProductItemBriefDTO>> fallbackCart(long userId, Throwable t) {
        log.error("Cart service failed for userId {}: {}", userId, t.getMessage());

        RestResponse<List<ProductItemBriefDTO>> resp = new RestResponse<>();
        resp.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
        resp.setError("Service Unavailable");
        resp.setMessage("Cart service unavailable");
        resp.setData(Collections.emptyList());
        return resp;
    }

    // Fallback PHẢI cùng tham số + thêm Throwable, trả cùng kiểu
    private RestResponse<PaypalOrderCreateResponse> fallbackPaypal(
            String orderCode, BigDecimal amount, String currency,
            PaypalOrderCreateRequest.ExperienceContext exp, Throwable t
    ) {
        log.error("Paypal service failed for orderCode {}: {}", orderCode, t.getMessage());
        RestResponse<PaypalOrderCreateResponse> resp = new RestResponse<>();
        resp.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
        resp.setError("Service Unavailable");
        resp.setMessage("Paypal payment failed, fallback to COD or retry later");
        resp.setData(null);
        return resp;
    }
}
