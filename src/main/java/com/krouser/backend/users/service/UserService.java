package com.krouser.backend.users.service;

import com.krouser.backend.rbac.entity.Role;
import com.krouser.backend.rbac.repository.RoleRepository;
import com.krouser.backend.common.exception.BusinessException;
import com.krouser.backend.common.exception.UserAlreadyExistsException;
import com.krouser.backend.common.exception.UserNotFoundException;
import com.krouser.backend.shared.exception.ResourceNotFoundException;
import com.krouser.backend.shared.util.TagGenerator;
import com.krouser.backend.users.dto.CreateUserRequest;
import com.krouser.backend.users.dto.UserResponse;
import com.krouser.backend.users.entity.User;
import com.krouser.backend.users.repository.UserRepository;
import com.krouser.backend.audit.entity.AuditEvent;
import com.krouser.backend.audit.service.AuditService;
import com.krouser.backend.audit.util.AuditDetailsBuilder;
import com.krouser.backend.users.dto.UpdateUserRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    private final TagGenerator tagGenerator;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            TagGenerator tagGenerator, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tagGenerator = tagGenerator;
        this.auditService = auditService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("User already exists: " + request.getUsername());
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", roleName));
                roles.add(role);
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);
        user.setRoles(roles);
        user.setStatus(com.krouser.backend.users.entity.UserStatus.ACTIVE);

        // Profile
        user.setAlias(request.getAlias());
        user.setNombre(request.getNombre());
        user.setSegundoNombre(request.getSegundoNombre());
        user.setApellidoPaterno(request.getApellidoPaterno());
        user.setApellidoMaterno(request.getApellidoMaterno());

        // Generate ID Public
        user.setIdPublic(UUID.randomUUID());

        boolean saved = false;
        int attempts = 0;
        User savedUser = null;

        while (!saved && attempts < 3) {
            String tag = tagGenerator.generateTagWithAlternateSuffix(user.getAlias(), user.getIdPublic(), attempts);
            user.setTag(tag);
            try {
                savedUser = userRepository.save(user);
                saved = true;
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                if (userRepository.existsByTag(tag)) {
                    attempts++;
                } else {
                    throw e;
                }
            }
        }

        if (!saved || savedUser == null)
            throw new BusinessException("Could not generate unique tag");

        auditService.audit("USER_CREATED_ADMIN", "USER", AuditEvent.AuditOutcome.SUCCESS,
                getCurrentUsername(), getCurrentUsername(),
                "User", savedUser.getIdPublic().toString(),
                new AuditDetailsBuilder()
                        .add("username", savedUser.getUsername())
                        .add("roles", request.getRoles() != null ? request.getRoles().toString() : "[]")
                        .build());

        return new UserResponse(savedUser);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("username", username));
        return new UserResponse(user);
    }

    public UserResponse getUserByIdPublic(UUID idPublic) {
        User user = userRepository.findByIdPublic(idPublic)
                .orElseThrow(() -> new UserNotFoundException("ID", idPublic.toString()));
        return new UserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID idPublic, UpdateUserRequest request) {
        User user = userRepository.findByIdPublic(idPublic)
                .orElseThrow(() -> new UserNotFoundException("ID", idPublic.toString()));

        if (request.getAlias() != null && !request.getAlias().equals(user.getAlias())) {
            // Update alias and tag logic if needed. For now just update alias, tag remains
            // unique per bootstrap logic?
            // If tag logic depends on alias, we might need to regenerate tag.
            // Simplest for now: update alias, keep tag (or throw if specific rule).
            user.setAlias(request.getAlias());
        }
        if (request.getNombre() != null)
            user.setNombre(request.getNombre());
        if (request.getSegundoNombre() != null)
            user.setSegundoNombre(request.getSegundoNombre());
        if (request.getApellidoPaterno() != null)
            user.setApellidoPaterno(request.getApellidoPaterno());
        if (request.getApellidoMaterno() != null)
            user.setApellidoMaterno(request.getApellidoMaterno());

        User saved = userRepository.save(user);

        auditService.audit("USER_UPDATED", "USER", AuditEvent.AuditOutcome.SUCCESS,
                getCurrentUsername(), getCurrentUsername(),
                "User", saved.getIdPublic().toString(), null);

        return new UserResponse(saved);
    }

    @Transactional
    public void changeUserStatus(UUID idPublic, boolean enabled) {
        User user = userRepository.findByIdPublic(idPublic)
                .orElseThrow(() -> new UserNotFoundException("ID", idPublic.toString()));
        user.setStatus(enabled ? com.krouser.backend.users.entity.UserStatus.ACTIVE
                : com.krouser.backend.users.entity.UserStatus.BLOCKED);
        userRepository.save(user);

        auditService.audit(enabled ? "USER_UNBLOCKED" : "USER_BLOCKED", "USER", AuditEvent.AuditOutcome.SUCCESS,
                getCurrentUsername(), getCurrentUsername(),
                "User", user.getIdPublic().toString(), null);
    }

    @Transactional
    public void assignRolesToUser(UUID idPublic, List<String> roleNames) {
        User user = userRepository.findByIdPublic(idPublic)
                .orElseThrow(() -> new UserNotFoundException("ID", idPublic.toString()));

        Set<Role> roles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String name : roleNames) {
                roles.add(roleRepository.findByName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", name)));
            }
        }
        user.setRoles(roles);
        userRepository.save(user);

        auditService.audit("USER_ROLE_REPLACED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                getCurrentUsername(), getCurrentUsername(),
                "User", user.getIdPublic().toString(),
                new AuditDetailsBuilder().add("roles", roleNames != null ? roleNames.toString() : "[]").build());
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
