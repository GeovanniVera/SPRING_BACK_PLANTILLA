package com.krouser.backend.email.service;

import java.util.Map;

public interface EmailService {
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
