package com.customclothing.system.service;

import com.customclothing.system.dto.OrderResponseDTO;
import com.customclothing.system.dto.PlaceOrderDTO;
import com.customclothing.system.entity.Cart;
import com.customclothing.system.entity.CartItem;
import com.customclothing.system.entity.Order;
import com.customclothing.system.entity.Product;
import com.customclothing.system.entity.User;
import com.customclothing.system.enums.Role;
import com.customclothing.system.repository.OrderRepository;
import com.customclothing.system.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceEmailTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("placeOrder sends order confirmation email on successful order placement")
    void testPlaceOrder_TriggersConfirmationEmail() {
        Long customerId = 1L;
        User customer = new User("John Doe", "john@example.com", "pass", "1234567890", "123 Street", Role.CUSTOMER);
        customer.setId(customerId);

        Product product = new Product();
        product.setId(5L);
        product.setName("Bespoke Navy Suit");
        product.setBasePrice(12999.0);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setCustomer(customer);
        cart.setItems(new ArrayList<>());

        CartItem cartItem = new CartItem(cart, product, 1, 12999.0, "42R", "Navy Blue", "Pure Wool", null, "Tailored fit");
        cart.getItems().add(cartItem);

        when(userService.getUserEntity(customerId)).thenReturn(customer);
        when(cartService.getOrCreateCartEntity(customerId)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(200L);
            return o;
        });

        PlaceOrderDTO placeOrderDTO = new PlaceOrderDTO();
        placeOrderDTO.setCustomerId(customerId);
        placeOrderDTO.setShippingAddress("123 Street, City");
        placeOrderDTO.setContactPhone("1234567890");

        OrderResponseDTO result = orderService.placeOrder(placeOrderDTO);

        assertNotNull(result);
        assertNotNull(result.getOrderTrackingNumber());
        assertEquals(12999.0, result.getTotalAmount());

        verify(cartService, times(1)).clearCart(customerId);
        verify(emailService, times(1)).sendOrderConfirmationEmail(any(OrderResponseDTO.class));
    }

    @Test
    @DisplayName("placeOrder succeeds even if emailService throws an unexpected error")
    void testPlaceOrder_SucceedsEvenIfEmailFails() {
        Long customerId = 2L;
        User customer = new User("Jane Doe", "jane@example.com", "pass", "9876543210", "456 Avenue", Role.CUSTOMER);
        customer.setId(customerId);

        Product product = new Product();
        product.setId(6L);
        product.setName("Luxury Silk Shirt");
        product.setBasePrice(3499.0);

        Cart cart = new Cart();
        cart.setId(11L);
        cart.setCustomer(customer);
        cart.setItems(new ArrayList<>());

        CartItem cartItem = new CartItem(cart, product, 2, 3499.0, "39", "Sky Blue", "Egyptian Cotton", null, "Monogram cuffs");
        cart.getItems().add(cartItem);

        when(userService.getUserEntity(customerId)).thenReturn(customer);
        when(cartService.getOrCreateCartEntity(customerId)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(201L);
            return o;
        });

        // Email service throws runtime exception
        doThrow(new RuntimeException("Simulated SMTP Connection Failure"))
                .when(emailService).sendOrderConfirmationEmail(any(OrderResponseDTO.class));

        PlaceOrderDTO placeOrderDTO = new PlaceOrderDTO();
        placeOrderDTO.setCustomerId(customerId);
        placeOrderDTO.setShippingAddress("456 Avenue, City");
        placeOrderDTO.setContactPhone("9876543210");

        // Order placement must not fail
        OrderResponseDTO result = assertDoesNotThrow(() -> orderService.placeOrder(placeOrderDTO));

        assertNotNull(result);
        assertEquals(6998.0, result.getTotalAmount());
        verify(cartService, times(1)).clearCart(customerId);
        verify(emailService, times(1)).sendOrderConfirmationEmail(any(OrderResponseDTO.class));
    }
}
