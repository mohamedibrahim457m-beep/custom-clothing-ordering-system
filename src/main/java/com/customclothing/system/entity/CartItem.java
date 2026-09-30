package com.customclothing.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Individual clothing item inside a Customer's Cart with chosen customizations.
 */
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Double unitPrice;

    // Selected customization attributes
    @Column(name = "selected_size", length = 50)
    private String selectedSize;

    @Column(name = "selected_color", length = 50)
    private String selectedColor;

    @Column(name = "selected_fabric", length = 100)
    private String selectedFabric;

    // Optional link to customer's saved custom measurement profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id")
    private Measurement measurement;

    // Customer instructions (e.g. "Double vents, monogram initials 'MK' on cuff")
    @Column(name = "customization_instructions", columnDefinition = "TEXT")
    private String customizationInstructions;

    public CartItem() {
    }

    public CartItem(Cart cart, Product product, Integer quantity, Double unitPrice,
                    String selectedSize, String selectedColor, String selectedFabric,
                    Measurement measurement, String customizationInstructions) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.selectedSize = selectedSize;
        this.selectedColor = selectedColor;
        this.selectedFabric = selectedFabric;
        this.measurement = measurement;
        this.customizationInstructions = customizationInstructions;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public Measurement getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }

    public String getCustomizationInstructions() {
        return customizationInstructions;
    }

    public void setCustomizationInstructions(String customizationInstructions) {
        this.customizationInstructions = customizationInstructions;
    }
}
