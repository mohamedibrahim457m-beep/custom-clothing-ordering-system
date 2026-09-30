package com.customclothing.system.controller;

import com.customclothing.system.dto.ApiResponse;
import com.customclothing.system.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @Value("${spring.mail.username:}")
    private String configuredSender;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailStatus() {
        boolean configured = emailService.isEmailConfigured();
        Map<String, Object> data = Map.of(
                "configured", configured,
                "senderEmail", (configuredSender != null && !configuredSender.isBlank()) ? configuredSender : "Not set",
                "smtpHost", "smtp.gmail.com",
                "smtpPort", 587
        );
        return ResponseEntity.ok(ApiResponse.success("Email service configuration status", data));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendTestEmail(@RequestParam String toEmail) {
        if (!emailService.isEmailConfigured()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Gmail credentials are not configured. Please set MAIL_USERNAME and MAIL_PASSWORD environment variables."));
        }

        String subject = "Test Email from Custom Clothing System";
        String html = "<!DOCTYPE html><html><body style='font-family:sans-serif;color:#1e293b;padding:20px;'>"
                + "<div style='max-width:500px;border:1px solid #e2e8f0;border-radius:8px;padding:24px;'>"
                + "<h2 style='color:#4f46e5;margin-top:0;'>✂️ Custom Clothing System</h2>"
                + "<p>This is a test email confirming that your Gmail SMTP configuration is working successfully!</p>"
                + "<p style='font-size:12px;color:#64748b;'>Custom Clothing Ordering System</p>"
                + "</div></body></html>";
        String text = "Custom Clothing System: This is a test email confirming that your Gmail SMTP configuration is working successfully.";

        boolean sent = emailService.sendEmail(toEmail, subject, html, text);
        if (sent) {
            return ResponseEntity.ok(ApiResponse.success("Test email sent successfully to " + toEmail,
                    Map.of("sent", true, "recipient", toEmail)));
        } else {
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "Failed to send test email to " + toEmail + ". Please check your Gmail App Password and network connection."));
        }
    }
}
