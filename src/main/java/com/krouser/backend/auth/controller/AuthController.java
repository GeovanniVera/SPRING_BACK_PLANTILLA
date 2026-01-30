package com.krouser.backend.auth.controller;

import com.krouser.backend.auth.dto.LoginRequest;
import com.krouser.backend.auth.dto.LoginResponse;
import com.krouser.backend.auth.dto.RegisterRequest;
import com.krouser.backend.auth.dto.RegisterResponse;
import com.krouser.backend.auth.dto.TokenRefreshRequest;
import com.krouser.backend.auth.dto.TokenRefreshResponse;
import com.krouser.backend.auth.service.AuthService;
import com.krouser.backend.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", response, httpRequest.getRequestURI()));
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", response,
                        httpRequest.getRequestURI()));
    }

    @GetMapping("/verify")
    public String verifyAccount(
            @RequestParam String token,
            Model model) {
        try {
            authService.verifyAccount(token);
            model.addAttribute("frontendUrl", frontendUrl);
            return "auth/verify-success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/verify-error";
        }
    }

    @PostMapping("/resend-verification")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email,
            HttpServletRequest httpRequest) {
        authService.resendVerificationToken(email);
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Verification email resent successfully", null, httpRequest.getRequestURI()));
    }

    @PostMapping("/refresh")
    @ResponseBody
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request, HttpServletRequest httpRequest) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity
                .ok(new ApiResponse<>(200, "Token refreshed successfully", response, httpRequest.getRequestURI()));
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        authService.logout(username);
        return ResponseEntity.ok(new ApiResponse<>(200, "Log out successful", null, httpRequest.getRequestURI()));
    }
}
