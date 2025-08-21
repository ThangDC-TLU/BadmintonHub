package com.badmintonhub.cartservice.service;

import com.badmintonhub.cartservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.cartservice.dto.request.CartItemRequest;
import com.badmintonhub.cartservice.service.base.BaseRedisService;

import java.util.List;

public interface CartRedisService{
    void addProductToCart(String userId, CartItemRequest item);

    void updateProductQuantity(String userId, CartItemRequest item);

    void removeProductFromCart(String userId, CartItemRequest item);

    void clearCart(String userId);

    List<ProductItemBriefDTO> getProductFromCart(String userId);
}
