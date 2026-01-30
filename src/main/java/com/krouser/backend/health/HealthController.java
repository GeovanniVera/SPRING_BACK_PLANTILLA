package com.krouser.backend.health;

import com.krouser.backend.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth(HttpServletRequest request) {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "time", LocalDateTime.now());
        return ResponseEntity.ok(new ApiResponse<>(200, "System is OK", status, request.getRequestURI()));
    }
}
