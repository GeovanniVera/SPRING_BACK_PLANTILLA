package com.krouser.backend.email.config;

import com.krouser.backend.email.service.EmailService;
import com.krouser.backend.email.service.impl.MockEmailService;
import com.krouser.backend.email.service.impl.SmtpEmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class EmailConfig {

    @Bean
    @ConditionalOnProperty(name = "app.email.enabled", havingValue = "true")
    public EmailService smtpEmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        return new SmtpEmailService(javaMailSender, templateEngine);
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.enabled", havingValue = "false", matchIfMissing = true)
    public EmailService mockEmailService() {
        return new MockEmailService();
    }
}
