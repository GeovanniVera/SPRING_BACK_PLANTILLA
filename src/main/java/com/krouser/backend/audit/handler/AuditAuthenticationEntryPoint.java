package com.krouser.backend.audit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouser.backend.audit.entity.AuditEvent;
import com.krouser.backend.audit.service.AuditService;
import com.krouser.backend.audit.util.AuditDetailsBuilder;
import com.krouser.backend.shared.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuditAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuditAuthenticationEntryPoint.class);

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditAuthenticationEntryPoint(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        logger.error("Unauthorized error: {}", authException.getMessage());

        auditService.audit(
                "AUTH_FAIL_ENTRY_POINT",
                "AUTH",
                AuditEvent.AuditOutcome.FAIL,
                null,
                "anonymous",
                null,
                null,
                new AuditDetailsBuilder()
                        .add("message", authException.getMessage())
                        .build());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                null,
                request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
