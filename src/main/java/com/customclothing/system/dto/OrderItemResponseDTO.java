package com.customclothing.system.dto;

import com.customclothing.system.entity.OrderItem;

public class OrderItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private String selectedSize;
    private String selectedColor;
    private String selectedFabric;
    private MeasurementDTO measurement;
    private String customizationInstructions;

    public OrderItemResponseDTO() {
    }

    public OrderItemResponseDTO(OrderItem item) {
        this.id = item.getId();
        if (item.getProduct() != null) {
            this.productId = item.getProduct().getId();
            this.productName = item.getProduct().getName();
        }
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.subtotal = item.getPrice() != null && item.getQuantity() != null
                ? item.getPrice() * item.getQuantity()
                : 0.0;
        this.selectedSize = item.getSelectedSize();
        this.selectedColor = item.getSelectedColor();
        this.selectedFabric = item.getSelectedFabric();
        if (item.getMeasurement() != null) {
            this.measurement = new MeasurementDTO(item.getMeasurement());
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public MeasurementDTO getMeasurement() {
        return measurement;
    }

    public void setMeasurement(MeasurementDTO measurement) {
        this.measurement = measurement;
    }

    public String getCustomizationInstructions() {
        return customizationInstructions;
    }

    public void setCustomizationInstructions(String customizationInstructions) {
        this.customizationInstructions = customizationInstructions;
    }
}
