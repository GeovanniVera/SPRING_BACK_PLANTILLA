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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuditAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditAccessDeniedHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";

        // Audit the event
        auditService.audit("ACCESS_DENIED", "ACCESS_CONTROL", AuditEvent.AuditOutcome.FAIL,
                null, username, null, null,
                new AuditDetailsBuilder()
                        .add("message", accessDeniedException.getMessage())
                        .build());

        // Standard response
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.FORBIDDEN.value(),
                "Access Denied: " + accessDeniedException.getMessage(), null, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
