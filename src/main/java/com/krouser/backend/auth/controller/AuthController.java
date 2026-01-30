package com.krouser.backend.auth.controller;

import com.krouser.backend.auth.dto.LoginRequest;
import com.krouser.backend.auth.dto.LoginResponse;
import com.krouser.backend.auth.dto.RegisterRequest;
import com.krouser.backend.auth.dto.RegisterResponse;
import com.krouser.backend.auth.service.AuthService;
import com.krouser.backend.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", response, httpRequest.getRequestURI()));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", response,
                        httpRequest.getRequestURI()));
    }
}
