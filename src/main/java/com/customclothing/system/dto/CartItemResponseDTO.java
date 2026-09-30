package com.customclothing.system.dto;

import com.customclothing.system.entity.CartItem;

public class CartItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
    private String selectedSize;
    private String selectedColor;
    private String selectedFabric;
    private Long measurementId;
    private String measurementProfileName;
    private String customizationInstructions;

    public CartItemResponseDTO() {
    }

    public CartItemResponseDTO(CartItem item) {
        this.id = item.getId();
        if (item.getProduct() != null) {
            this.productId = item.getProduct().getId();
            this.productName = item.getProduct().getName();
        }
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.subtotal = item.getUnitPrice() != null && item.getQuantity() != null
                ? item.getUnitPrice() * item.getQuantity()
                : 0.0;
        this.selectedSize = item.getSelectedSize();
        this.selectedColor = item.getSelectedColor();
        this.selectedFabric = item.getSelectedFabric();
        if (item.getMeasurement() != null) {
            this.measurementId = item.getMeasurement().getId();
            this.measurementProfileName = item.getMeasurement().getProfileName();
        }
        this.customizationInstructions = item.getCustomizationInstructions();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(String selectedColor) {
        this.selectedColor = selectedColor;
    }

    public String getSelectedFabric() {
        return selectedFabric;
    }

    public void setSelectedFabric(String selectedFabric) {
        this.selectedFabric = selectedFabric;
    }

    public Long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(Long measurementId) {
        this.measurementId = measurementId;
    }

    public String getMeasurementProfileName() {
        return measurementProfileName;
    }

    public void setMeasurementProfileName(String measurementProfileName) {
        this.measurementProfileName = measurementProfileName;
    }

    public String getCustomizationInstructions() {
        return customizationInstructions;
    }

    public void setCustomizationInstructions(String customizationInstructions) {
        this.customizationInstructions = customizationInstructions;
    }
}
