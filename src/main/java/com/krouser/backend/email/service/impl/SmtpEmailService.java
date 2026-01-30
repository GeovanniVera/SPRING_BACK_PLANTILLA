package com.krouser.backend.email.service.impl;

import com.krouser.backend.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Objects;

/**
 * Real implementation using JavaMailSender and Thymeleaf.
 */
public class SmtpEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public SmtpEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            logger.info("Sending email to [{}] with subject [{}] using template [{}]", to, subject, templateName);

            Context context = new Context();
            context.setVariables(variables);

            // Access templates from src/main/resources/templates/mail/
            String process = templateEngine.process("mail/" + templateName, context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(Objects.requireNonNull(to, "Email 'to' cannot be null"));
            helper.setSubject(Objects.requireNonNull(subject, "Email 'subject' cannot be null"));
            helper.setText(Objects.requireNonNull(process, "Email 'content' cannot be null"), true); // true = isHtml

            mailSender.send(mimeMessage);
            logger.info("Email sent successfully to [{}]", to);

        } catch (MessagingException e) {
            logger.error("Failed to send email to [{}]", to, e);
        }
    }
}
