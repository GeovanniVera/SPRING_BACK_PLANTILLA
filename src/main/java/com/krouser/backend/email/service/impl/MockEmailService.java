package com.krouser.backend.email.service.impl;

import com.krouser.backend.email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Implementation for development/testing when email sending is disabled.
 * Just logs the email content.
 */
public class MockEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        logger.info("================ MOCK EMAIL ================");
        logger.info("TO: {}", to);
        logger.info("SUBJECT: {}", subject);
        logger.info("TEMPLATE: {}", templateName);
        logger.info("VARIABLES: {}", variables);
        logger.info("============================================");
    }
}
