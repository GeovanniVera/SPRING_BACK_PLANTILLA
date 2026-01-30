package com.krouser.backend.rbac.service;

import com.krouser.backend.audit.entity.AuditEvent;
import com.krouser.backend.audit.service.AuditService;
import com.krouser.backend.audit.util.AuditDetailsBuilder;
import com.krouser.backend.rbac.dto.CreateRoleRequest;
import com.krouser.backend.rbac.dto.RoleDto;
import com.krouser.backend.rbac.dto.AssignPrivilegesRequest;
import com.krouser.backend.rbac.entity.Privilege;
import com.krouser.backend.rbac.entity.Role;
import com.krouser.backend.rbac.repository.PrivilegeRepository;
import com.krouser.backend.rbac.repository.RoleRepository;
import com.krouser.backend.shared.exception.DuplicateResourceException;
import com.krouser.backend.shared.exception.ResourceInUseException;
import com.krouser.backend.shared.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final AuditService auditService;

    public RoleService(RoleRepository roleRepository, PrivilegeRepository privilegeRepository,
            AuditService auditService) {
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.auditService = auditService;
    }

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public RoleDto getRole(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", name));
        return mapToDto(role);
    }

    @Transactional
    public RoleDto createRole(CreateRoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Role", request.getName());
        }

        Role role = new Role();
        role.setName(request.getName().toUpperCase());
        role.setDescription(request.getDescription());
        role.setActive(true);

        Set<Privilege> privileges = new HashSet<>();
        if (request.getPrivileges() != null) {
            for (String pName : request.getPrivileges()) {
                privileges.add(privilegeRepository.findByName(pName)
                        .orElseThrow(() -> new ResourceNotFoundException("Privilege", pName)));
            }
        }
        role.setPrivileges(privileges);

        Role saved = roleRepository.save(role);

        auditService.audit("ROLE_CREATED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                null, getCurrentUsername(),
                "Role", saved.getName(),
                new AuditDetailsBuilder()
                        .add("name", saved.getName())
                        .add("privileges", request.getPrivileges() != null ? request.getPrivileges().toString() : "[]")
                        .build());

        return mapToDto(saved);
    }

    @Transactional
    public RoleDto updateRole(String name, CreateRoleRequest request) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", name));

        role.setDescription(request.getDescription());
        // Name usually immutable or handle carefully. Assuming only desc update for now
        // or other fields if expanded.

        Role saved = roleRepository.save(role);

        auditService.audit("ROLE_UPDATED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                null, getCurrentUsername(),
                "Role", saved.getName(),
                new AuditDetailsBuilder().add("desc", saved.getDescription()).build());

        return mapToDto(saved);
    }

    @Transactional
    public void deleteRole(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", name));

        // Check if role is in use
        if (roleRepository.isRoleAssignedToUsers(role.getId())) {
            throw new ResourceInUseException("Role", name, "users");
        }

        role.setActive(false);
        roleRepository.save(role);

        auditService.audit("ROLE_DISABLED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                null, getCurrentUsername(),
                "Role", role.getName(),
                new AuditDetailsBuilder().add("active", false).build());
    }

    @Transactional
    public void assignPrivileges(String roleName, AssignPrivilegesRequest request) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleName));

        Set<Privilege> newPrivileges = new HashSet<>();
        if (request.getPrivileges() != null) {
            for (String pName : request.getPrivileges()) {
                newPrivileges.add(privilegeRepository.findByName(pName)
                        .orElseThrow(() -> new ResourceNotFoundException("Privilege", pName)));
            }
        }

        role.setPrivileges(newPrivileges);
        roleRepository.save(role);

        auditService.audit("ROLE_PRIV_REPLACED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                null, getCurrentUsername(),
                "Role", role.getName(),
                new AuditDetailsBuilder().add("privileges", request.getPrivileges().toString()).build());
    }

    private RoleDto mapToDto(Role role) {
        Set<String> privs = role.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet());
        return new RoleDto(role.getName(), role.getDescription(), role.isActive(), privs);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
