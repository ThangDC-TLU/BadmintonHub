package com.badmintonhub.cartservice.controller;

import com.badmintonhub.cartservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.cartservice.dto.request.CartItemRequest;
import com.badmintonhub.cartservice.service.CartRedisService;
import com.badmintonhub.cartservice.utils.CustomHeaders;
import com.badmintonhub.cartservice.utils.anotation.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartRedisService cartRedisService;

    public CartController(CartRedisService cartRedisService) {
        this.cartRedisService = cartRedisService;
    }

    @GetMapping
    @ApiMessage("Get all product")
    public ResponseEntity<List<ProductItemBriefDTO>> getProductFromCart(
            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId
    ){
        return ResponseEntity.ok(this.cartRedisService.getProductFromCart(userId));
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

    @PutMapping
    @ApiMessage("Update cart item successfully!")
    public ResponseEntity<Void> updateProductQuantity(
            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId,
            @RequestBody CartItemRequest item
    ) {
        this.cartRedisService.updateProductQuantity(userId, item);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping
    @ApiMessage("Remove item from cart successfully!")
    public ResponseEntity<Void> removeProductFromCart(
            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId,
            @RequestBody CartItemRequest item
    ) {
        this.cartRedisService.removeProductFromCart(userId, item);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/clear")
    @ApiMessage("Clear cart successfully!")
    public ResponseEntity<Void> clearCart(
            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId
    ) {
        this.cartRedisService.clearCart(userId);
        return ResponseEntity.ok().build();
    }


}
