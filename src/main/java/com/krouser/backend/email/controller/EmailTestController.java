package com.krouser.backend.email.controller;

import com.krouser.backend.email.service.EmailService;
import com.krouser.backend.shared.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    private static final Logger logger = LoggerFactory.getLogger(EmailTestController.class);

    private final EmailService emailService;

    @Value("${app.email.enabled:false}")
    private boolean isEmailEnabled;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<String>> sendVerificationEmail(
            @RequestParam(required = false, defaultValue = "testuser@example.com") String email) {

        String token = UUID.randomUUID().toString();
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;

        logger.info("Attempting to send test email to: {}", email);
        logger.info("Email Service Mode: {}", isEmailEnabled ? "SMTP (Real/MailHog)" : "MOCK (Logs only)");

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", "Test User");
        variables.put("activationLink", verificationLink);

        emailService.sendEmail(email, "Verifica tu cuenta - Test", "welcome-email", variables);

        String message = isEmailEnabled
                ? "Correo enviado a MailHog. Revisa http://localhost:8025"
                : "Correo simulado (MOCK). Revisa los logs de la aplicación.";

        return ResponseEntity.ok(new ApiResponse<>(200, message, token, "/api/test/send-verification"));
    }
}
