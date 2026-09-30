package com.customclothing.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddToCartDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private String selectedSize;
    private String selectedColor;
    private String selectedFabric;
    private Long measurementId;
    private String customizationInstructions;

    public AddToCartDTO() {
    }

    public AddToCartDTO(Long productId, Integer quantity, String selectedSize,
                          String selectedColor, String selectedFabric,
                          Long measurementId, String customizationInstructions) {
        this.productId = productId;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
        this.selectedColor = selectedColor;
        this.selectedFabric = selectedFabric;
        this.measurementId = measurementId;
        this.customizationInstructions = customizationInstructions;
    }

    // Getters and Setters

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public String getCustomizationInstructions() {
        return customizationInstructions;
    }

    public void setCustomizationInstructions(String customizationInstructions) {
        this.customizationInstructions = customizationInstructions;
    }
}
