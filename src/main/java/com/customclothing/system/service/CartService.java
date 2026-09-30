package com.customclothing.system.service;

import com.customclothing.system.dto.AddToCartDTO;
import com.customclothing.system.dto.CartResponseDTO;
import com.customclothing.system.entity.Cart;

public interface CartService {

    CartResponseDTO getCartByCustomerId(Long customerId);

    CartResponseDTO addToCart(Long customerId, AddToCartDTO addToCartDTO);

    CartResponseDTO removeCartItem(Long customerId, Long cartItemId);

    void clearCart(Long customerId);

    Cart getOrCreateCartEntity(Long customerId);
}
