package com.krouser.backend.audit.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class AuditRequestFilter implements Filter {

    public static final String REQUEST_ID_KEY = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            String requestId = httpReq.getHeader(REQUEST_ID_KEY);

            // If header not present, generate one
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            // Put it in attribute for downstream access
            request.setAttribute(REQUEST_ID_KEY, requestId);
        }

        chain.doFilter(request, response);
    }
}
