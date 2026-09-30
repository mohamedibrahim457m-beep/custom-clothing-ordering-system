package com.customclothing.system.service;

import com.customclothing.system.dto.*;
import com.customclothing.system.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponseDTO placeOrder(PlaceOrderDTO placeOrderDTO);

    OrderResponseDTO getOrderById(Long id);

    OrderResponseDTO getOrderByTrackingNumber(String trackingNumber);

    List<OrderResponseDTO> getCustomerOrderHistory(Long customerId);

    List<OrderResponseDTO> getAllOrders();

    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);

    OrderResponseDTO assignOrderToTailor(Long orderId, AssignTailorDTO assignTailorDTO);

    List<OrderResponseDTO> getAssignedOrdersForTailor(Long tailorId);

    OrderResponseDTO updateOrderStatusByTailor(Long orderId, Long tailorId, OrderStatusUpdateDTO statusUpdateDTO);

    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatusUpdateDTO statusUpdateDTO);

    OrderResponseDTO cancelOrder(Long orderId);
}
