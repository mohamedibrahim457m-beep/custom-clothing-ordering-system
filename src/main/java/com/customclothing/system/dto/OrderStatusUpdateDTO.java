package com.customclothing.system.dto;

import com.customclothing.system.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateDTO {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    private String tailorNotes;

    public OrderStatusUpdateDTO() {
    }

    public OrderStatusUpdateDTO(OrderStatus status, String tailorNotes) {
        this.status = status;
        this.tailorNotes = tailorNotes;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getTailorNotes() {
        return tailorNotes;
    }

    public void setTailorNotes(String tailorNotes) {
        this.tailorNotes = tailorNotes;
    }
}
