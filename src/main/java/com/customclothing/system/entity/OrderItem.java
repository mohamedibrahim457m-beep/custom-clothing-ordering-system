package com.customclothing.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Line item of an order detailing product, quantity, customizations, and measurements.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Price is required")
    @Column(nullable = false)
    private Double price;

    @Column(name = "selected_size", length = 50)
    private String selectedSize;

    @Column(name = "selected_color", length = 50)
    private String selectedColor;

    @Column(name = "selected_fabric", length = 100)
    private String selectedFabric;

    // Measurement reference for the tailor to cut & stitch accurately
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "measurement_id")
    private Measurement measurement;

    @Column(name = "customization_instructions", columnDefinition = "TEXT")
    private String customizationInstructions;

    public OrderItem() {
    }

    public OrderItem(Order order, Product product, Integer quantity, Double price,
                     String selectedSize, String selectedColor, String selectedFabric,
                     Measurement measurement, String customizationInstructions) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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
