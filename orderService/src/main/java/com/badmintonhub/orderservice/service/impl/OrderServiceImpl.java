package com.badmintonhub.orderservice.service.impl;

import com.badmintonhub.orderservice.dto.message.RestResponse;
import com.badmintonhub.orderservice.dto.model.AddressDTO;
import com.badmintonhub.orderservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.orderservice.dto.request.CreateOrderRequest;
import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.repository.OrderRepository;
import com.badmintonhub.orderservice.service.OrderService;
import com.badmintonhub.orderservice.utils.constant.CustomHeaders;
import jakarta.transaction.Transactional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public OrderServiceImpl(OrderRepository orderRepository, WebClient webClient) {
        this.orderRepository = orderRepository;
        this.webClient = webClient;
    }

    @Override
    @Transactional //RollBack khi có lỗi
    public AddressDTO createOrder(long userId, CreateOrderRequest createOrderRequest) {
        RestResponse<AddressDTO> resp = webClient.get()
                .uri("http://localhost:9000/api/v1/address/{id}", createOrderRequest.getAddressId())
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class).map(body ->
                                new ResponseStatusException(r.statusCode(), "Address HTTP error: " + body)))
                .bodyToMono(new ParameterizedTypeReference<RestResponse<AddressDTO>>() {})
                .block();

        // Kiểm tra wrapper & lấy data
        if (resp == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "User service no response");
        }
        if (resp.getStatusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Address business error: " + resp.getMessage());
        }
        AddressDTO addr = resp.getData();
        if (addr == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
        }

        // selectedOptionIds có thể là null/empty => lấy toàn bộ giỏ
        List<Long> selected = createOrderRequest.getSelectedOptionIds();

        RestResponse<List<ProductItemBriefDTO>> resProductItem = webClient.get()
                .uri("http://localhost:8082/api/v1/carts")
                .header(CustomHeaders.X_AUTH_USER_ID, String.valueOf(userId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class).map(body ->
                                new ResponseStatusException(r.statusCode(), "Cart HTTP error: " + body)))
                .bodyToMono(new ParameterizedTypeReference<RestResponse<List<ProductItemBriefDTO>>>() {})
                .block();


        List<ProductItemBriefDTO> productItemBriefDTO = resProductItem.getData();

        return addr;
    }
}

