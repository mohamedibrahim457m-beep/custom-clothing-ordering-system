package com.customclothing.system.service.impl;

import com.customclothing.system.dto.*;
import com.customclothing.system.entity.*;
import com.customclothing.system.enums.OrderStatus;
import com.customclothing.system.enums.Role;
import com.customclothing.system.exception.BadRequestException;
import com.customclothing.system.exception.ResourceNotFoundException;
import com.customclothing.system.repository.OrderRepository;
import com.customclothing.system.service.CartService;
import com.customclothing.system.service.EmailService;
import com.customclothing.system.service.OrderService;
import com.customclothing.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserService userService;
    private final EmailService emailService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartService cartService,
                            UserService userService,
                            EmailService emailService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    public OrderResponseDTO placeOrder(PlaceOrderDTO dto) {
        User customer = userService.getUserEntity(dto.getCustomerId());
        Cart cart = cartService.getOrCreateCartEntity(customer.getId());

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty shopping cart");
        }

        // Calculate total amount
        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        // Unique tracking number: CCO-YYYY-UUID
        String trackingNumber = "CCO-" + LocalDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order(
                trackingNumber,
                customer,
                totalAmount,
                dto.getShippingAddress(),
                dto.getContactPhone(),
                dto.getCustomerNotes()
        );
        order.setStatus(OrderStatus.PENDING);
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(10)); // Estimated 10 days for custom tailoring

        // Transfer cart items to order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    order,
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice(),
                    cartItem.getSelectedSize(),
                    cartItem.getSelectedColor(),
                    cartItem.getSelectedFabric(),
                    cartItem.getMeasurement(),
                    cartItem.getCustomizationInstructions()
            );
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart after placing order
        cartService.clearCart(customer.getId());

        OrderResponseDTO responseDTO = new OrderResponseDTO(savedOrder);

        // Send order confirmation email to customer
        try {
            emailService.sendOrderConfirmationEmail(responseDTO);
        } catch (Exception ex) {
            log.error("Failed to trigger order confirmation email for order #{}: {}",
                    responseDTO.getOrderTrackingNumber(), ex.getMessage());
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return new OrderResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByTrackingNumber(String trackingNumber) {
        Order order = orderRepository.findByOrderTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "trackingNumber", trackingNumber));
        return new OrderResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getCustomerOrderHistory(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO assignOrderToTailor(Long orderId, AssignTailorDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User tailor = userService.getUserEntity(dto.getTailorId());
        if (tailor.getRole() != Role.TAILOR) {
            throw new BadRequestException("User with ID " + dto.getTailorId() + " is not a TAILOR");
        }

        order.setAssignedTailor(tailor);
        order.setStatus(OrderStatus.ASSIGNED);
        Order savedOrder = orderRepository.save(order);
        return new OrderResponseDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAssignedOrdersForTailor(Long tailorId) {
        return orderRepository.findByAssignedTailorIdOrderByOrderDateDesc(tailorId).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrderStatusByTailor(Long orderId, Long tailorId, OrderStatusUpdateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getAssignedTailor() == null || !order.getAssignedTailor().getId().equals(tailorId)) {
            throw new BadRequestException("Order is not assigned to this tailor");
        }

        order.setStatus(dto.getStatus());
        if (dto.getTailorNotes() != null && !dto.getTailorNotes().isBlank()) {
            order.setTailorNotes(dto.getTailorNotes());
        }

        Order savedOrder = orderRepository.save(order);
        return new OrderResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatusUpdateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(dto.getStatus());
        if (dto.getTailorNotes() != null && !dto.getTailorNotes().isBlank()) {
            order.setTailorNotes(dto.getTailorNotes());
        }

        Order savedOrder = orderRepository.save(order);
        return new OrderResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel an order that has already been delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return new OrderResponseDTO(savedOrder);
    }
}
