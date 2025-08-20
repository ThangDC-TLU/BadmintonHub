package com.badmintonhub.cartservice.controller;

import com.badmintonhub.cartservice.dto.request.CartItemRequest;
import com.badmintonhub.cartservice.service.CartRedisService;
import com.badmintonhub.cartservice.utils.CustomHeaders;
import com.badmintonhub.cartservice.utils.anotation.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartRedisService cartRedisService;

    public CartController(CartRedisService cartRedisService) {
        this.cartRedisService = cartRedisService;
    }

    @PostMapping
    @ApiMessage("Add to cart successfully!")
    public ResponseEntity<Void> addProductToCart(
            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId,
            @RequestBody CartItemRequest item
    ){
        this.cartRedisService.addProductToCart(userId, item);
        return ResponseEntity.ok().build();
    }
}
