package com.customclothing.system.controller;

import com.customclothing.system.dto.AddToCartDTO;
import com.customclothing.system.dto.ApiResponse;
import com.customclothing.system.dto.CartResponseDTO;
import com.customclothing.system.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(@PathVariable Long customerId) {
        CartResponseDTO cart = cartService.getCartByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addToCart(
            @PathVariable Long customerId,
            @Valid @RequestBody AddToCartDTO addToCartDTO) {
        CartResponseDTO updatedCart = cartService.addToCart(customerId, addToCartDTO);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", updatedCart));
    }

    @DeleteMapping("/{customerId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeCartItem(
            @PathVariable Long customerId,
            @PathVariable Long itemId) {
        CartResponseDTO updatedCart = cartService.removeCartItem(customerId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", updatedCart));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
}
