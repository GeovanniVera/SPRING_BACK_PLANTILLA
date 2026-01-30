package com.krouser.backend.audit.service;

import com.krouser.backend.audit.entity.AuditEvent;
import com.krouser.backend.audit.repository.AuditEventRepository;
import com.krouser.backend.audit.filter.AuditRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void audit(String action, String category, AuditEvent.AuditOutcome outcome, String details) {
        audit(action, category, outcome, null, null, null, null, details);
    }

    public void audit(String action, String category, AuditEvent.AuditOutcome outcome,
            String actorUserIdPublic, String actorUsername,
            String entityType, String entityIdPublic,
            String details) {
        try {
            AuditEvent event = new AuditEvent();
            event.setEventTimeUtc(LocalDateTime.now(ZoneOffset.UTC));
            event.setAction(truncate(action, 80));
            event.setCategory(truncate(category, 40));
            event.setOutcome(outcome);
            event.setActorUserIdPublic(actorUserIdPublic); // Assume passed or extracted if needed
            event.setActorUsername(truncate(actorUsername, 80));
            event.setEntityType(truncate(entityType, 60));
            event.setEntityIdPublic(entityIdPublic); // UUID string
            event.setDetails(details); // Already built via builder

            // Request Context
            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                event.setRequestId((String) request.getAttribute(AuditRequestFilter.REQUEST_ID_KEY));
                event.setIp(truncate(getClientIp(request), 45));
                event.setHttpMethod(truncate(request.getMethod(), 10));
                event.setPath(truncate(request.getRequestURI(), 200));
                event.setUserAgent(truncate(request.getHeader("User-Agent"), 255));

                // Status code is harder to get here unless passed, usually from Response or
                // Exception Handler
            }

            auditEventRepository.save(event);
        } catch (Exception e) {
            logger.error("Failed to save audit event: {}", e.getMessage(), e);
            // Non-blocking: swallow exception to not break business flow
        }
    }

    public void audit(AuditEvent event) {
        try {
            if (event.getEventTimeUtc() == null) {
                event.setEventTimeUtc(LocalDateTime.now(ZoneOffset.UTC));
            }
            auditEventRepository.save(event);
        } catch (Exception e) {
            logger.error("Failed to save audit event: {}", e.getMessage(), e);
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return (attrs != null) ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String input, int maxLength) {
        if (input == null)
            return null;
        if (input.length() <= maxLength)
            return input;
        return input.substring(0, maxLength);
    }
}
