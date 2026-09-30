package com.customclothing.system.service;

import com.customclothing.system.dto.OrderResponseDTO;
import com.customclothing.system.dto.UserResponseDTO;
import com.customclothing.system.entity.Order;
import com.customclothing.system.entity.User;

/**
 * Service for sending transactional emails (Registration Welcome, Order Confirmation).
 */
public interface EmailService {

    /**
     * Sends a welcome email when a new customer registers.
     *
     * @param user The registered user entity
     */
    void sendCustomerWelcomeEmail(User user);

    /**
     * Sends a welcome email using user response DTO.
     *
     * @param userDto The registered user response DTO
     */
    void sendCustomerWelcomeEmail(UserResponseDTO userDto);

    /**
     * Sends an order confirmation email when a customer places an order.
     *
     * @param order The placed order entity
     */
    void sendOrderConfirmationEmail(Order order);

    /**
     * Sends an order confirmation email using order response DTO.
     *
     * @param orderDto The placed order response DTO containing all line items
     */
    void sendOrderConfirmationEmail(OrderResponseDTO orderDto);

    /**
     * Sends a generic email with both plain text and HTML fallback.
     *
     * @param toEmail The recipient's email address
     * @param subject The email subject line
     * @param htmlContent The formatted HTML content
     * @param plainTextContent The fallback plain text content
     * @return true if email was dispatched or queued, false if skipped or failed
     */
    boolean sendEmail(String toEmail, String subject, String htmlContent, String plainTextContent);

    /**
     * Checks if Gmail SMTP credentials are configured via environment variables.
     *
     * @return true if both username and password are set and non-empty
     */
    boolean isEmailConfigured();
}
