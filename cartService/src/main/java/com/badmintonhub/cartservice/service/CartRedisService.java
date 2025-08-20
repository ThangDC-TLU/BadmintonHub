package com.badmintonhub.cartservice.service;

import com.badmintonhub.cartservice.dto.request.CartItemRequest;
import com.badmintonhub.cartservice.service.base.BaseRedisService;

public interface CartRedisService{
    void addProductToCart(String userId, CartItemRequest item);
}
