package com.krouser.backend.auth.service;

import com.krouser.backend.auth.dto.LoginRequest;
import com.krouser.backend.auth.dto.LoginResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.krouser.backend.auth.dto.RegisterRequest;
import com.krouser.backend.auth.dto.RegisterResponse;
import com.krouser.backend.rbac.entity.Role;
import com.krouser.backend.rbac.repository.RoleRepository;
import com.krouser.backend.users.entity.User;
import com.krouser.backend.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.krouser.backend.shared.util.TagGenerator;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import com.krouser.backend.audit.service.AuditService;
import com.krouser.backend.audit.entity.AuditEvent;
import com.krouser.backend.audit.util.AuditDetailsBuilder;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagGenerator tagGenerator;
    private final AuditService auditService;
    private final com.krouser.backend.email.service.EmailService emailService;
    private final com.krouser.backend.auth.repository.VerificationTokenRepository tokenRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
            JwtService jwtService, UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, TagGenerator tagGenerator, AuditService auditService,
            com.krouser.backend.email.service.EmailService emailService,
            com.krouser.backend.auth.repository.VerificationTokenRepository tokenRepository,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tagGenerator = tagGenerator;
        this.auditService = auditService;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.refreshTokenService = refreshTokenService;
    }

    /*
     * Account Verification
     */
    public void verifyAccount(String token) {
        com.krouser.backend.auth.entity.VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Token expired");
        }

        User user = verificationToken.getUser();
        user.setStatus(com.krouser.backend.users.entity.UserStatus.ACTIVE);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        auditService.audit("AUTH_VERIFY_SUCCESS", "AUTH", AuditEvent.AuditOutcome.SUCCESS,
                user.getIdPublic().toString(), user.getUsername(), "VerificationToken", null, null);
    }

    public LoginResponse login(LoginRequest request) {
        // 1. Check User Existence & Locking
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == com.krouser.backend.users.entity.UserStatus.BLOCKED) {
            if (user.getLockUntil() != null && user.getLockUntil().isAfter(java.time.LocalDateTime.now())) {
                auditService.audit("AUTH_LOGIN_LOCKED", "AUTH", AuditEvent.AuditOutcome.FAIL,
                        user.getIdPublic().toString(), request.getUsername(), "User", null,
                        new AuditDetailsBuilder().add("lockUntil", user.getLockUntil().toString()).build());
                throw new RuntimeException("Account is locked until " + user.getLockUntil());
            } else {
                // Unlock if time passed
                user.setStatus(com.krouser.backend.users.entity.UserStatus.ACTIVE);
                user.setFailedAttempts(0);
                user.setLockUntil(null);
                userRepository.save(user);
            }
        }

        try {
            // 2. Attempt Authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            // 3. Success -> Reset counters
            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                userRepository.save(user);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String jwtToken = jwtService.generateToken(userDetails);

            // Extract role names
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            auditService.audit("AUTH_LOGIN_SUCCESS", "AUTH", AuditEvent.AuditOutcome.SUCCESS,
                    user.getIdPublic().toString(), request.getUsername(), "User", null, null);

            com.krouser.backend.auth.entity.RefreshToken refreshToken = refreshTokenService
                    .createRefreshToken(user.getId());

            return new LoginResponse(
                    jwtToken,
                    refreshToken.getToken(),
                    user.getIdPublic(),
                    user.getUsername(),
                    user.getAlias(),
                    user.getTag(),
                    roleNames);

        } catch (org.springframework.security.core.AuthenticationException e) {
            // 4. Failure -> Increment counters
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {
                user.setStatus(com.krouser.backend.users.entity.UserStatus.BLOCKED);
                user.setLockUntil(java.time.LocalDateTime.now().plusMinutes(15));
                auditService.audit("ACCOUNT_LOCKED", "AUTH", AuditEvent.AuditOutcome.FAIL,
                        user.getIdPublic().toString(), request.getUsername(), "User", null,
                        new AuditDetailsBuilder().add("reason", "Too many failed attempts").build());
            }

            userRepository.save(user);

            auditService.audit("AUTH_LOGIN_FAIL", "AUTH", AuditEvent.AuditOutcome.FAIL,
                    user.getIdPublic() != null ? user.getIdPublic().toString() : null,
                    request.getUsername(), "User", null,
                    new AuditDetailsBuilder().add("reason", e.getMessage())
                            .add("attempts", String.valueOf(user.getFailedAttempts())).build());

            throw e;
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        validatePassword(request.getPassword());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(userRole));
        user.setRoles(Collections.singleton(userRole));
        user.setRoles(Collections.singleton(userRole));
        user.setStatus(com.krouser.backend.users.entity.UserStatus.PENDING_VERIFICATION);

        // Profile fields
        user.setAlias(request.getAlias());
        user.setNombre(request.getNombre());
        user.setSegundoNombre(request.getSegundoNombre());
        user.setApellidoPaterno(request.getApellidoPaterno());
        user.setApellidoMaterno(request.getApellidoMaterno());

        // Generate ID Public manually
        user.setIdPublic(UUID.randomUUID());

        User savedUser = null;
        int attempts = 0;
        boolean saved = false;

        while (!saved && attempts < 3) {
            // Use getIdPublic for tag generation
            String tag = tagGenerator.generateTagWithAlternateSuffix(user.getAlias(), user.getIdPublic(), attempts);
            user.setTag(tag);
            try {
                savedUser = userRepository.save(user);
                saved = true;
            } catch (DataIntegrityViolationException e) {
                // Determine if it was the tag or username
                // Note: existsByTag might be false if transaction not committed, but usually
                // works for retry logic
                if (userRepository.existsByTag(tag)) {
                    attempts++;
                } else {
                    throw e; // Likely username collision if concurrent register or other constraint
                }
            }
        }

        if (!saved || savedUser == null) {
            throw new RuntimeException("Could not generate unique tag after retries");
        }

        auditService.audit("USER_REGISTER", "AUTH", AuditEvent.AuditOutcome.SUCCESS,
                savedUser.getIdPublic().toString(), savedUser.getUsername(),
                "User", savedUser.getIdPublic().toString(),
                new AuditDetailsBuilder()
                        .add("alias", savedUser.getAlias())
                        .add("tag", savedUser.getTag())
                        .add("alias", savedUser.getAlias())
                        .add("tag", savedUser.getTag())
                        .build());

        sendVerificationEmail(savedUser);

        return new RegisterResponse(
                savedUser.getIdPublic(),
                savedUser.getUsername(),
                savedUser.getAlias(),
                savedUser.getTag(),
                "Usuario registrado exitosamente. Por favor, verifica tu correo electrónica.",
                savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

    private void sendVerificationEmail(User user) {
        com.krouser.backend.auth.entity.VerificationToken token = new com.krouser.backend.auth.entity.VerificationToken(
                user);
        tokenRepository.save(token);

        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token.getToken();

        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("username", user.getAlias());
        variables.put("activationLink", verificationLink);

        emailService.sendEmail(user.getUsername(), "Verifica tu cuenta", "welcome-email", variables);
    }

    public com.krouser.backend.auth.dto.TokenRefreshResponse refreshToken(
            com.krouser.backend.auth.dto.TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    // Rotate Token
                    com.krouser.backend.auth.entity.RefreshToken newToken = refreshTokenService
                            .rotateRefreshToken(token);

                    User user = token.getUser();
                    String jwtToken = jwtService
                            .generateToken(userDetailsService.loadUserByUsername(user.getUsername()));

                    return new com.krouser.backend.auth.dto.TokenRefreshResponse(jwtToken, newToken.getToken());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenService.deleteByUserId(user.getId());
    }

    @org.springframework.transaction.annotation.Transactional
    public void resendVerificationToken(String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != com.krouser.backend.users.entity.UserStatus.PENDING_VERIFICATION) {
            throw new RuntimeException("Account is already verified or blocked.");
        }

        // Delete existing token if any
        tokenRepository.deleteByUser(user);

        // Create new token
        com.krouser.backend.auth.entity.VerificationToken newToken = new com.krouser.backend.auth.entity.VerificationToken(
                user);
        tokenRepository.save(newToken);

        // Send email
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("name", user.getAlias());
        variables.put("verificationLink",
                org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/auth/verify").queryParam("token", newToken.getToken()).toUriString());

        emailService.sendEmail(user.getUsername(), "Resend Verification", "welcome-email", variables);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12) {
            throw new RuntimeException("La contraseña debe tener al menos 12 caracteres.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("La contraseña debe contener al menos una letra mayúscula.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("La contraseña debe contener al menos una letra minúscula.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("La contraseña debe contener al menos un número.");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new RuntimeException("La contraseña debe contener al menos un carácter especial.");
        }
    }
}
