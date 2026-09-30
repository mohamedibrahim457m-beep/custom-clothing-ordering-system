package com.customclothing.system.dto;

import com.customclothing.system.entity.Cart;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CartResponseDTO {

    private Long id;
    private Long customerId;
    private List<CartItemResponseDTO> items = new ArrayList<>();
    private Double grandTotal;
    private LocalDateTime updatedAt;

    public CartResponseDTO() {
    }

    public CartResponseDTO(Cart cart) {
        this.id = cart.getId();
        if (cart.getCustomer() != null) {
            this.customerId = cart.getCustomer().getId();
        }
        if (cart.getItems() != null) {
            this.items = cart.getItems().stream()
                    .map(CartItemResponseDTO::new)
                    .collect(Collectors.toList());
            this.grandTotal = this.items.stream()
                    .mapToDouble(CartItemResponseDTO::getSubtotal)
                    .sum();
        } else {
            this.grandTotal = 0.0;
        }
        this.updatedAt = cart.getUpdatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<CartItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponseDTO> items) {
        this.items = items;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
