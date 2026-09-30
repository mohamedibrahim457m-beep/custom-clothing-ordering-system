package com.customclothing.system.service;

import com.customclothing.system.dto.OrderItemResponseDTO;
import com.customclothing.system.dto.OrderResponseDTO;
import com.customclothing.system.dto.UserResponseDTO;
import com.customclothing.system.entity.User;
import com.customclothing.system.enums.OrderStatus;
import com.customclothing.system.enums.Role;
import com.customclothing.system.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // By default, set test credentials
        ReflectionTestUtils.setField(emailService, "senderEmail", "test.store@gmail.com");
        ReflectionTestUtils.setField(emailService, "senderPassword", "app-password-test");
        ReflectionTestUtils.setField(emailService, "fromName", "Custom Clothing System");
    }

    @Test
    @DisplayName("isEmailConfigured returns true when credentials are set")
    void testIsEmailConfigured_True() {
        assertTrue(emailService.isEmailConfigured());
    }

    @Test
    @DisplayName("isEmailConfigured returns false when sender email is blank or missing")
    void testIsEmailConfigured_FalseWhenCredentialsMissing() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "");
        assertFalse(emailService.isEmailConfigured());

        ReflectionTestUtils.setField(emailService, "senderEmail", "test@gmail.com");
        ReflectionTestUtils.setField(emailService, "senderPassword", "");
        assertFalse(emailService.isEmailConfigured());
    }

    @Test
    @DisplayName("sendCustomerWelcomeEmail successfully dispatches email when configured")
    void testSendCustomerWelcomeEmail_Success() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        User user = new User("Alice Johnson", "alice@example.com", "secret123", "9876543210", "123 High St", Role.CUSTOMER);
        user.setId(10L);

        assertDoesNotThrow(() -> emailService.sendCustomerWelcomeEmail(user));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendCustomerWelcomeEmail handles SMTP errors gracefully without throwing")
    void testSendCustomerWelcomeEmail_HandlesExceptionGracefully() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Gmail SMTP connection failed")).when(mailSender).send(any(MimeMessage.class));

        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setEmail("alice@example.com");
        userDto.setFullName("Alice Johnson");
        userDto.setRole(Role.CUSTOMER);

        assertDoesNotThrow(() -> emailService.sendCustomerWelcomeEmail(userDto));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendCustomerWelcomeEmail skips sending when credentials are not configured")
    void testSendCustomerWelcomeEmail_SkippedWhenCredentialsMissing() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "");
        ReflectionTestUtils.setField(emailService, "senderPassword", "");

        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setEmail("alice@example.com");
        userDto.setFullName("Alice Johnson");

        assertDoesNotThrow(() -> emailService.sendCustomerWelcomeEmail(userDto));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendOrderConfirmationEmail successfully sends email with order and item details")
    void testSendOrderConfirmationEmail_Success() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        OrderResponseDTO orderDto = new OrderResponseDTO();
        orderDto.setId(101L);
        orderDto.setOrderTrackingNumber("CCO-2026-A1B2C3D4");
        orderDto.setCustomerName("David Miller");
        orderDto.setCustomerEmail("david@example.com");
        orderDto.setStatus(OrderStatus.PENDING);
        orderDto.setTotalAmount(15999.0);
        orderDto.setShippingAddress("45 Bespoke Avenue, London");
        orderDto.setContactPhone("+44 7911 123456");
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setExpectedDeliveryDate(LocalDateTime.now().plusDays(10));

        OrderItemResponseDTO item = new OrderItemResponseDTO();
        item.setProductName("Classic Bespoke Charcoal Suit");
        item.setQuantity(1);
        item.setPrice(15999.0);
        item.setSelectedSize("40R");
        item.setSelectedColor("Charcoal Grey");
        item.setSelectedFabric("Super 120s Wool");

        List<OrderItemResponseDTO> items = new ArrayList<>();
        items.add(item);
        orderDto.setItems(items);

        assertDoesNotThrow(() -> emailService.sendOrderConfirmationEmail(orderDto));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendOrderConfirmationEmail catches SMTP failure without propagating exception")
    void testSendOrderConfirmationEmail_HandlesExceptionGracefully() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Gmail authentication failed")).when(mailSender).send(any(MimeMessage.class));

        OrderResponseDTO orderDto = new OrderResponseDTO();
        orderDto.setOrderTrackingNumber("CCO-2026-ERR");
        orderDto.setCustomerName("David Miller");
        orderDto.setCustomerEmail("david@example.com");

        assertDoesNotThrow(() -> emailService.sendOrderConfirmationEmail(orderDto));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendOrderConfirmationEmail skips dispatch when credentials not configured")
    void testSendOrderConfirmationEmail_SkippedWhenCredentialsMissing() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "");
        ReflectionTestUtils.setField(emailService, "senderPassword", "");

        OrderResponseDTO orderDto = new OrderResponseDTO();
        orderDto.setCustomerEmail("david@example.com");
        orderDto.setOrderTrackingNumber("CCO-2026-X1");

        assertDoesNotThrow(() -> emailService.sendOrderConfirmationEmail(orderDto));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
