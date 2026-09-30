package com.customclothing.system.service.impl;

import com.customclothing.system.dto.OrderItemResponseDTO;
import com.customclothing.system.dto.OrderResponseDTO;
import com.customclothing.system.dto.UserResponseDTO;
import com.customclothing.system.entity.Order;
import com.customclothing.system.entity.User;
import com.customclothing.system.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Value("${spring.mail.password:}")
    private String senderPassword;

    @Value("${app.email.from-name:Custom Clothing System}")
    private String fromName;

    @Autowired
    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean isEmailConfigured() {
        return mailSender != null
                && senderEmail != null && !senderEmail.trim().isEmpty()
                && senderPassword != null && !senderPassword.trim().isEmpty();
    }

    @Async
    @Override
    public void sendCustomerWelcomeEmail(User user) {
        if (user == null) {
            log.warn("Cannot send welcome email: User entity is null");
            return;
        }
        sendCustomerWelcomeEmail(new UserResponseDTO(user));
    }

    @Async
    @Override
    public void sendCustomerWelcomeEmail(UserResponseDTO userDto) {
        if (userDto == null || userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            log.warn("Cannot send welcome email: Invalid user or missing email address");
            return;
        }

        try {
            String recipientEmail = userDto.getEmail().trim();
            String customerName = userDto.getFullName() != null && !userDto.getFullName().trim().isEmpty()
                    ? userDto.getFullName().trim()
                    : "Valued Customer";

            String subject = "Welcome to Custom Clothing System, " + customerName + "!";
            String htmlContent = buildWelcomeEmailHtml(customerName, recipientEmail, userDto.getRole() != null ? userDto.getRole().name() : "CUSTOMER");
            String plainTextContent = buildWelcomeEmailText(customerName, recipientEmail);

            boolean sent = sendEmail(recipientEmail, subject, htmlContent, plainTextContent);
            if (sent) {
                log.info("Welcome email successfully sent to customer: {}", recipientEmail);
            }
        } catch (Exception ex) {
            // Guarantee that email failure does not affect the customer registration flow
            log.error("Unexpected error in sendCustomerWelcomeEmail for user '{}': {}",
                    userDto.getEmail(), ex.getMessage(), ex);
        }
    }

    @Async
    @Override
    public void sendOrderConfirmationEmail(Order order) {
        if (order == null) {
            log.warn("Cannot send order confirmation email: Order entity is null");
            return;
        }
        sendOrderConfirmationEmail(new OrderResponseDTO(order));
    }

    @Async
    @Override
    public void sendOrderConfirmationEmail(OrderResponseDTO orderDto) {
        if (orderDto == null) {
            log.warn("Cannot send order confirmation email: OrderResponseDTO is null");
            return;
        }

        String recipientEmail = orderDto.getCustomerEmail();
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            log.warn("Cannot send order confirmation email: Missing customer email for order tracking #{}",
                    orderDto.getOrderTrackingNumber());
            return;
        }

        try {
            String customerName = orderDto.getCustomerName() != null && !orderDto.getCustomerName().trim().isEmpty()
                    ? orderDto.getCustomerName().trim()
                    : "Valued Customer";
            String trackingNumber = orderDto.getOrderTrackingNumber() != null
                    ? orderDto.getOrderTrackingNumber()
                    : "CCO-UNKNOWN";

            String subject = "Order Confirmed! Tracking #: " + trackingNumber + " - Custom Clothing System";
            String htmlContent = buildOrderConfirmationEmailHtml(orderDto, customerName, trackingNumber);
            String plainTextContent = buildOrderConfirmationEmailText(orderDto, customerName, trackingNumber);

            boolean sent = sendEmail(recipientEmail.trim(), subject, htmlContent, plainTextContent);
            if (sent) {
                log.info("Order confirmation email successfully sent for order {} to {}",
                        trackingNumber, recipientEmail);
            }
        } catch (Exception ex) {
            // Guarantee that email failure does not affect the order placement flow
            log.error("Unexpected error in sendOrderConfirmationEmail for order tracking #{}: {}",
                    orderDto.getOrderTrackingNumber(), ex.getMessage(), ex);
        }
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent, String plainTextContent) {
        if (!isEmailConfigured()) {
            log.info("Gmail credentials are not configured (MAIL_USERNAME / MAIL_PASSWORD are empty). " +
                     "Skipping email dispatch for '{}'.", toEmail);
            return false;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            try {
                helper.setFrom(senderEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(senderEmail);
            }

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainTextContent, htmlContent);

            mailSender.send(mimeMessage);
            return true;
        } catch (MessagingException me) {
            log.error("Failed to construct or send email message to '{}': {}", toEmail, me.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("Error sending email via Gmail SMTP to '{}': {}", toEmail, ex.getMessage(), ex);
            return false;
        }
    }

    // ==========================================
    // Email Template Builders (HTML & Plain Text)
    // ==========================================

    private String buildWelcomeEmailHtml(String customerName, String email, String role) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                + "<body style='margin:0;padding:0;background-color:#f1f5f9;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;color:#1e293b;line-height:1.6;'>"
                + "<div style='max-width:600px;margin:24px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1),0 2px 4px -2px rgba(0,0,0,0.1);'>"
                // Header
                + "  <div style='background:linear-gradient(135deg, #1e293b 0%, #0f172a 100%);padding:32px 24px;text-align:center;border-bottom:3px solid #6366f1;'>"
                + "    <h1 style='margin:0;color:#ffffff;font-size:24px;font-weight:700;letter-spacing:0.5px;'>✂️ Custom Clothing System</h1>"
                + "    <p style='margin:6px 0 0 0;color:#94a3b8;font-size:14px;'>Bespoke Craftsmanship & Precision Fit</p>"
                + "  </div>"
                // Body
                + "  <div style='padding:32px 24px;'>"
                + "    <h2 style='margin-top:0;color:#0f172a;font-size:20px;font-weight:600;'>Welcome, " + escapeHtml(customerName) + "!</h2>"
                + "    <p style='color:#475569;font-size:15px;margin-bottom:20px;'>"
                + "      Thank you for joining <strong>Custom Clothing System</strong>. Your account has been registered successfully, and you are all set to experience premium, tailor-made clothing."
                + "    </p>"
                // Account details box
                + "    <div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px 20px;margin:20px 0;'>"
                + "      <div style='font-size:13px;font-weight:600;color:#64748b;text-transform:uppercase;margin-bottom:10px;letter-spacing:0.5px;'>Account Overview</div>"
                + "      <div style='display:flex;margin-bottom:6px;'><strong style='width:120px;color:#334155;font-size:14px;'>Name:</strong> <span style='color:#1e293b;font-size:14px;'>" + escapeHtml(customerName) + "</span></div>"
                + "      <div style='display:flex;margin-bottom:6px;'><strong style='width:120px;color:#334155;font-size:14px;'>Email:</strong> <span style='color:#1e293b;font-size:14px;'>" + escapeHtml(email) + "</span></div>"
                + "      <div style='display:flex;'><strong style='width:120px;color:#334155;font-size:14px;'>Account Role:</strong> <span style='display:inline-block;padding:2px 8px;border-radius:4px;background:#e0e7ff;color:#4338ca;font-weight:600;font-size:12px;'>" + escapeHtml(role) + "</span></div>"
                + "    </div>"
                // Feature highlights
                + "    <h3 style='color:#1e293b;font-size:16px;margin:24px 0 12px 0;'>What you can do next:</h3>"
                + "    <div style='margin-bottom:12px;display:flex;align-items:flex-start;'>"
                + "      <div style='font-size:18px;margin-right:12px;'>👔</div>"
                + "      <div><strong style='color:#1e293b;'>Explore Bespoke Apparel</strong><br><span style='color:#64748b;font-size:13px;'>Browse our catalog of custom suits, formal shirts, chinos, and ethnic couture.</span></div>"
                + "    </div>"
                + "    <div style='margin-bottom:12px;display:flex;align-items:flex-start;'>"
                + "      <div style='font-size:18px;margin-right:12px;'>📏</div>"
                + "      <div><strong style='color:#1e293b;'>Save Your Measurements</strong><br><span style='color:#64748b;font-size:13px;'>Create reusable measurement profiles (chest, waist, inseam, shoulder) for a flawless fit.</span></div>"
                + "    </div>"
                + "    <div style='margin-bottom:12px;display:flex;align-items:flex-start;'>"
                + "      <div style='font-size:18px;margin-right:12px;'>📦</div>"
                + "      <div><strong style='color:#1e293b;'>Live Tailor Tracking</strong><br><span style='color:#64748b;font-size:13px;'>Track every milestone of your order as our master tailors craft your garment.</span></div>"
                + "    </div>"
                + "  </div>"
                // Footer
                + "  <div style='background:#f8fafc;padding:20px 24px;text-align:center;border-top:1px solid #e2e8f0;font-size:12px;color:#94a3b8;'>"
                + "    <p style='margin:0 0 4px 0;'>Custom Clothing Ordering System · High-Quality Personalized Tailoring</p>"
                + "    <p style='margin:0;'>This is an automated notification. Please do not reply directly to this email.</p>"
                + "  </div>"
                + "</div>"
                + "</body></html>";
    }

    private String buildWelcomeEmailText(String customerName, String email) {
        return "Welcome to Custom Clothing System, " + customerName + "!\n\n"
                + "Thank you for creating an account with us. We are thrilled to welcome you.\n\n"
                + "Account Summary:\n"
                + "- Name: " + customerName + "\n"
                + "- Email: " + email + "\n\n"
                + "Next Steps:\n"
                + "1. Browse our custom clothing collection.\n"
                + "2. Save your measurement profiles for a precision tailored fit.\n"
                + "3. Place orders and track their stitching progress in real time.\n\n"
                + "Best regards,\n"
                + "The Custom Clothing System Team\n";
    }

    private String buildOrderConfirmationEmailHtml(OrderResponseDTO orderDto, String customerName, String trackingNumber) {
        StringBuilder itemsTable = new StringBuilder();
        List<OrderItemResponseDTO> items = orderDto.getItems();

        if (items != null && !items.isEmpty()) {
            for (OrderItemResponseDTO item : items) {
                String productName = item.getProductName() != null ? item.getProductName() : "Custom Garment";
                int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                double price = item.getPrice() != null ? item.getPrice() : 0.0;
                double subtotal = item.getSubtotal() != null ? item.getSubtotal() : (price * qty);

                StringBuilder specs = new StringBuilder();
                if (item.getSelectedSize() != null && !item.getSelectedSize().isBlank()) {
                    specs.append("Size: ").append(escapeHtml(item.getSelectedSize())).append(" | ");
                }
                if (item.getSelectedColor() != null && !item.getSelectedColor().isBlank()) {
                    specs.append("Color: ").append(escapeHtml(item.getSelectedColor())).append(" | ");
                }
                if (item.getSelectedFabric() != null && !item.getSelectedFabric().isBlank()) {
                    specs.append("Fabric: ").append(escapeHtml(item.getSelectedFabric())).append(" | ");
                }
                if (item.getMeasurement() != null && item.getMeasurement().getProfileName() != null) {
                    specs.append("Fit: ").append(escapeHtml(item.getMeasurement().getProfileName())).append(" | ");
                }
                if (specs.length() > 3) {
                    specs.setLength(specs.length() - 3); // trim trailing separator
                }

                itemsTable.append("<tr style='border-bottom:1px solid #e2e8f0;'>")
                        .append("<td style='padding:12px 8px;vertical-align:top;'>")
                        .append("<strong style='color:#1e293b;font-size:14px;'>").append(escapeHtml(productName)).append("</strong>");
                if (specs.length() > 0) {
                    itemsTable.append("<br><span style='color:#64748b;font-size:12px;'>").append(specs).append("</span>");
                }
                if (item.getCustomizationInstructions() != null && !item.getCustomizationInstructions().isBlank()) {
                    itemsTable.append("<br><span style='color:#475569;font-size:12px;font-style:italic;'>Note: ")
                            .append(escapeHtml(item.getCustomizationInstructions())).append("</span>");
                }
                itemsTable.append("</td>")
                        .append("<td style='padding:12px 8px;text-align:center;color:#334155;font-size:14px;vertical-align:top;'>").append(qty).append("</td>")
                        .append("<td style='padding:12px 8px;text-align:right;color:#334155;font-size:14px;vertical-align:top;'>₹").append(String.format("%,.2f", price)).append("</td>")
                        .append("<td style='padding:12px 8px;text-align:right;font-weight:600;color:#0f172a;font-size:14px;vertical-align:top;'>₹").append(String.format("%,.2f", subtotal)).append("</td>")
                        .append("</tr>");
            }
        } else {
            itemsTable.append("<tr><td colspan='4' style='padding:16px;text-align:center;color:#64748b;'>No line item details available</td></tr>");
        }

        String orderDateStr = orderDto.getOrderDate() != null
                ? orderDto.getOrderDate().format(DATE_FORMATTER)
                : "Just now";
        String expectedDeliveryStr = orderDto.getExpectedDeliveryDate() != null
                ? orderDto.getExpectedDeliveryDate().format(DATE_FORMATTER)
                : "10 business days";
        String totalAmountStr = orderDto.getTotalAmount() != null
                ? String.format("%,.2f", orderDto.getTotalAmount())
                : "0.00";

        return "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                + "<body style='margin:0;padding:0;background-color:#f1f5f9;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;color:#1e293b;line-height:1.6;'>"
                + "<div style='max-width:640px;margin:24px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.1),0 2px 4px -2px rgba(0,0,0,0.1);'>"
                // Header
                + "  <div style='background:linear-gradient(135deg, #1e293b 0%, #0f172a 100%);padding:30px 24px;text-align:center;border-bottom:3px solid #10b981;'>"
                + "    <h1 style='margin:0;color:#ffffff;font-size:22px;font-weight:700;'>🎉 Order Placed Successfully!</h1>"
                + "    <p style='margin:6px 0 0 0;color:#94a3b8;font-size:13px;'>Custom Clothing System · Order Confirmation</p>"
                + "  </div>"
                // Body
                + "  <div style='padding:28px 24px;'>"
                + "    <p style='font-size:15px;color:#334155;margin-top:0;'>"
                + "      Dear <strong>" + escapeHtml(customerName) + "</strong>,<br>"
                + "      Thank you for your order! We have received your custom clothing order. Our master tailors have been notified and will review your specifications."
                + "    </p>"
                // Order metadata card
                + "    <div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px 20px;margin:20px 0;'>"
                + "      <div style='display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid #e2e8f0;padding-bottom:10px;margin-bottom:10px;'>"
                + "        <div><span style='font-size:12px;color:#64748b;text-transform:uppercase;'>Tracking Number</span><br><strong style='font-size:16px;color:#4f46e5;font-family:monospace;'>" + escapeHtml(trackingNumber) + "</strong></div>"
                + "        <div style='text-align:right;'><span style='font-size:12px;color:#64748b;text-transform:uppercase;'>Status</span><br><span style='display:inline-block;padding:2px 8px;border-radius:4px;background:#fef3c7;color:#92400e;font-weight:600;font-size:12px;'>" + (orderDto.getStatus() != null ? orderDto.getStatus().name() : "PENDING") + "</span></div>"
                + "      </div>"
                + "      <div style='font-size:13px;color:#475569;margin-bottom:4px;'><strong>Order Date:</strong> " + escapeHtml(orderDateStr) + "</div>"
                + "      <div style='font-size:13px;color:#475569;'><strong>Estimated Delivery:</strong> " + escapeHtml(expectedDeliveryStr) + "</div>"
                + "    </div>"
                // Items Table
                + "    <h3 style='color:#0f172a;font-size:16px;margin:24px 0 10px 0;'>Ordered Items</h3>"
                + "    <table style='width:100%;border-collapse:collapse;margin-bottom:20px;'>"
                + "      <thead>"
                + "        <tr style='background:#f8fafc;border-bottom:2px solid #e2e8f0;text-align:left;'>"
                + "          <th style='padding:8px;font-size:12px;color:#64748b;text-transform:uppercase;'>Item</th>"
                + "          <th style='padding:8px;font-size:12px;color:#64748b;text-transform:uppercase;text-align:center;'>Qty</th>"
                + "          <th style='padding:8px;font-size:12px;color:#64748b;text-transform:uppercase;text-align:right;'>Price</th>"
                + "          <th style='padding:8px;font-size:12px;color:#64748b;text-transform:uppercase;text-align:right;'>Total</th>"
                + "        </tr>"
                + "      </thead>"
                + "      <tbody>"
                + itemsTable.toString()
                + "      </tbody>"
                + "      <tfoot>"
                + "        <tr>"
                + "          <td colspan='3' style='padding:12px 8px;text-align:right;font-weight:700;color:#1e293b;font-size:15px;'>Grand Total:</td>"
                + "          <td style='padding:12px 8px;text-align:right;font-weight:700;color:#10b981;font-size:16px;'>₹" + totalAmountStr + "</td>"
                + "        </tr>"
                + "      </tfoot>"
                + "    </table>"
                // Delivery address & notes
                + "    <div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px;margin-top:20px;'>"
                + "      <div style='font-size:13px;font-weight:600;color:#475569;margin-bottom:6px;'>Shipping & Contact Information</div>"
                + "      <div style='font-size:13px;color:#334155;margin-bottom:4px;'><strong>Address:</strong> " + escapeHtml(orderDto.getShippingAddress() != null ? orderDto.getShippingAddress() : "N/A") + "</div>"
                + "      <div style='font-size:13px;color:#334155;margin-bottom:4px;'><strong>Phone:</strong> " + escapeHtml(orderDto.getContactPhone() != null ? orderDto.getContactPhone() : "N/A") + "</div>"
                + (orderDto.getCustomerNotes() != null && !orderDto.getCustomerNotes().isBlank()
                        ? "<div style='font-size:13px;color:#334155;'><strong>Special Instructions:</strong> " + escapeHtml(orderDto.getCustomerNotes()) + "</div>"
                        : "")
                + "    </div>"
                // Live tracking banner
                + "    <div style='background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:14px;margin-top:20px;text-align:center;'>"
                + "      <div style='font-size:14px;color:#1e40af;'>You can track the live status of your order anytime with tracking ID:</div>"
                + "      <div style='font-size:16px;font-weight:700;color:#1d4ed8;margin-top:4px;font-family:monospace;'>" + escapeHtml(trackingNumber) + "</div>"
                + "    </div>"
                + "  </div>"
                // Footer
                + "  <div style='background:#f8fafc;padding:20px 24px;text-align:center;border-top:1px solid #e2e8f0;font-size:12px;color:#94a3b8;'>"
                + "    <p style='margin:0 0 4px 0;'>Custom Clothing Ordering System · High-Quality Personalized Tailoring</p>"
                + "    <p style='margin:0;'>Need assistance with your order? Contact our support desk.</p>"
                + "  </div>"
                + "</div>"
                + "</body></html>";
    }

    private String buildOrderConfirmationEmailText(OrderResponseDTO orderDto, String customerName, String trackingNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Confirmed! Tracking #: ").append(trackingNumber).append("\n\n");
        sb.append("Dear ").append(customerName).append(",\n\n");
        sb.append("Thank you for your order! We have received your order and our master tailors will begin crafting your items shortly.\n\n");
        sb.append("Order Summary:\n");
        sb.append("- Tracking Number: ").append(trackingNumber).append("\n");
        sb.append("- Status: ").append(orderDto.getStatus() != null ? orderDto.getStatus().name() : "PENDING").append("\n");
        if (orderDto.getTotalAmount() != null) {
            sb.append("- Total Amount: ₹").append(String.format("%,.2f", orderDto.getTotalAmount())).append("\n");
        }
        if (orderDto.getShippingAddress() != null) {
            sb.append("- Shipping Address: ").append(orderDto.getShippingAddress()).append("\n");
        }
        if (orderDto.getContactPhone() != null) {
            sb.append("- Contact Phone: ").append(orderDto.getContactPhone()).append("\n");
        }

        if (orderDto.getItems() != null && !orderDto.getItems().isEmpty()) {
            sb.append("\nItems Ordered:\n");
            for (OrderItemResponseDTO item : orderDto.getItems()) {
                sb.append("• ").append(item.getProductName() != null ? item.getProductName() : "Custom Garment")
                  .append(" (Qty: ").append(item.getQuantity() != null ? item.getQuantity() : 1)
                  .append(", Price: ₹").append(item.getPrice() != null ? String.format("%,.2f", item.getPrice()) : "0.00")
                  .append(")\n");
            }
        }

        sb.append("\nYou can track your order status anytime using tracking number: ").append(trackingNumber).append("\n\n");
        sb.append("Best regards,\nThe Custom Clothing System Team\n");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
