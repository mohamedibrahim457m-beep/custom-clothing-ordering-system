package com.customclothing.system.controller;

import com.customclothing.system.dto.*;
import com.customclothing.system.enums.OrderStatus;
import com.customclothing.system.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ==========================================
    // Customer Endpoints
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(@Valid @RequestBody PlaceOrderDTO placeOrderDTO) {
        OrderResponseDTO order = orderService.placeOrder(placeOrderDTO);
        return new ResponseEntity<>(ApiResponse.success("Order placed successfully", order), HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getCustomerOrderHistory(@PathVariable Long customerId) {
        List<OrderResponseDTO> orders = orderService.getCustomerOrderHistory(customerId);
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved successfully", orders));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> trackOrder(@PathVariable String trackingNumber) {
        OrderResponseDTO order = orderService.getOrderByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Order tracking status retrieved successfully", order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved successfully", order));
    }

    // ==========================================
    // Admin Endpoints
    // ==========================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status) {
        List<OrderResponseDTO> orders = status != null
                ? orderService.getOrdersByStatus(status)
                : orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @PutMapping("/{id}/assign-tailor")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> assignOrderToTailor(
            @PathVariable Long id,
            @Valid @RequestBody AssignTailorDTO assignTailorDTO) {
        OrderResponseDTO order = orderService.assignOrderToTailor(id, assignTailorDTO);
        return ResponseEntity.ok(ApiResponse.success("Order assigned to tailor successfully", order));
    }

    // ==========================================
    // Tailor Endpoints
    // ==========================================

    @GetMapping("/tailor/{tailorId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getAssignedOrdersForTailor(@PathVariable Long tailorId) {
        List<OrderResponseDTO> orders = orderService.getAssignedOrdersForTailor(tailorId);
        return ResponseEntity.ok(ApiResponse.success("Assigned orders retrieved successfully", orders));
    }

    @PutMapping("/{id}/tailor/{tailorId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable Long tailorId,
            @Valid @RequestBody OrderStatusUpdateDTO statusUpdateDTO) {
        OrderResponseDTO order = orderService.updateOrderStatusByTailor(id, tailorId, statusUpdateDTO);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatusByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDTO statusUpdateDTO) {
        OrderResponseDTO order = orderService.updateOrderStatus(id, statusUpdateDTO);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancelOrder(@PathVariable Long id) {
        OrderResponseDTO order = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
