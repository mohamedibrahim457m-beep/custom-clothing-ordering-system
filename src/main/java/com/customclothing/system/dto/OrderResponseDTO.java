package com.customclothing.system.dto;

import com.customclothing.system.entity.Order;
import com.customclothing.system.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseDTO {

    private Long id;
    private String orderTrackingNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long assignedTailorId;
    private String assignedTailorName;
    private OrderStatus status;
    private Double totalAmount;
    private String shippingAddress;
    private String contactPhone;
    private String customerNotes;
    private String tailorNotes;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private List<OrderItemResponseDTO> items = new ArrayList<>();

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.orderTrackingNumber = order.getOrderTrackingNumber();
        if (order.getCustomer() != null) {
            this.customerId = order.getCustomer().getId();
            this.customerName = order.getCustomer().getFullName();
            this.customerEmail = order.getCustomer().getEmail();
        }
        if (order.getAssignedTailor() != null) {
            this.assignedTailorId = order.getAssignedTailor().getId();
            this.assignedTailorName = order.getAssignedTailor().getFullName();
        }
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.shippingAddress = order.getShippingAddress();
        this.contactPhone = order.getContactPhone();
        this.customerNotes = order.getCustomerNotes();
        this.tailorNotes = order.getTailorNotes();
        this.orderDate = order.getOrderDate();
        this.expectedDeliveryDate = order.getExpectedDeliveryDate();
        if (order.getOrderItems() != null) {
            this.items = order.getOrderItems().stream()
                    .map(OrderItemResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderTrackingNumber() {
        return orderTrackingNumber;
    }

    public void setOrderTrackingNumber(String orderTrackingNumber) {
        this.orderTrackingNumber = orderTrackingNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Long getAssignedTailorId() {
        return assignedTailorId;
    }

    public void setAssignedTailorId(Long assignedTailorId) {
        this.assignedTailorId = assignedTailorId;
    }

    public String getAssignedTailorName() {
        return assignedTailorName;
    }

    public void setAssignedTailorName(String assignedTailorName) {
        this.assignedTailorName = assignedTailorName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public String getTailorNotes() {
        return tailorNotes;
    }

    public void setTailorNotes(String tailorNotes) {
        this.tailorNotes = tailorNotes;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public List<OrderItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponseDTO> items) {
        this.items = items;
    }
}
