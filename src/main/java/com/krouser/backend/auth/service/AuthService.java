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

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
            JwtService jwtService, UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, TagGenerator tagGenerator, AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tagGenerator = tagGenerator;
        this.auditService = auditService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String jwtToken = jwtService.generateToken(userDetails);

            // Get the full user entity to access roles and profile information
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Extract role names
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            auditService.audit("AUTH_LOGIN_SUCCESS", "AUTH", AuditEvent.AuditOutcome.SUCCESS,
                    user.getIdPublic().toString(), request.getUsername(), "User", null, null);

            return new LoginResponse(
                    jwtToken,
                    user.getIdPublic(),
                    user.getUsername(),
                    user.getAlias(),
                    user.getTag(),
                    roleNames);
        } catch (Exception e) {
            auditService.audit("AUTH_LOGIN_FAIL", "AUTH", AuditEvent.AuditOutcome.FAIL,
                    null, request.getUsername(), "User", null,
                    new AuditDetailsBuilder().add("reason", e.getMessage()).build());
            throw e;
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(userRole));
        user.setEnabled(true);

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
                        .build());

        return new RegisterResponse(
                savedUser.getIdPublic(),
                savedUser.getUsername(),
                savedUser.getAlias(),
                savedUser.getTag(),
                "User registered successfully",
                savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }
}
