package com.krouser.backend.rbac.service;

import com.krouser.backend.audit.entity.AuditEvent;
import com.krouser.backend.audit.service.AuditService;
import com.krouser.backend.audit.util.AuditDetailsBuilder;
import com.krouser.backend.rbac.dto.CreatePrivilegeRequest;
import com.krouser.backend.rbac.dto.PrivilegeDto;
import com.krouser.backend.rbac.entity.Privilege;
import com.krouser.backend.rbac.repository.PrivilegeRepository;
import com.krouser.backend.shared.exception.DuplicateResourceException;
import com.krouser.backend.shared.exception.ResourceInUseException;
import com.krouser.backend.shared.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrivilegeService {

    private final PrivilegeRepository privilegeRepository;
    private final AuditService auditService;

    public PrivilegeService(PrivilegeRepository privilegeRepository, AuditService auditService) {
        this.privilegeRepository = privilegeRepository;
        this.auditService = auditService;
    }

    public List<PrivilegeDto> getAllPrivileges() {
        return privilegeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PrivilegeDto getPrivilege(String name) {
        Privilege privilege = privilegeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege", name));
        return mapToDto(privilege);
    }

    @Transactional
    public PrivilegeDto createPrivilege(CreatePrivilegeRequest request) {
        if (privilegeRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Privilege", request.getName());
        }

        Privilege privilege = new Privilege();
        privilege.setName(request.getName().toUpperCase());
        privilege.setDescription(request.getDescription());
        privilege.setActive(true);

        Privilege saved = privilegeRepository.save(privilege);

        auditService.audit("PRIV_CREATED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                null, getCurrentUsername(),
                "Privilege", saved.getName(),
                new AuditDetailsBuilder()
                        .add("name", saved.getName())
                        .add("active", saved.isActive())
                        .build());

        return mapToDto(saved);
    }

    @Transactional
    public PrivilegeDto updatePrivilege(String name, CreatePrivilegeRequest request) {
        Privilege privilege = privilegeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege", name));

        privilege.setDescription(request.getDescription());
        // Assume active state isn't changing here directly or use dedicated method?
        // For simplicity update generic fields. Name typically shouldn't change for
        // consistency.

        Privilege saved = privilegeRepository.save(privilege);

        auditService.audit("PRIV_UPDATED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                null, getCurrentUsername(),
                "Privilege", saved.getName(),
                new AuditDetailsBuilder()
                        .add("description", saved.getDescription())
                        .build());

        return mapToDto(saved);
    }

    @Transactional
    public void deletePrivilege(String name) {
        Privilege privilege = privilegeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege", name));

        // Check if privilege is in use
        if (privilegeRepository.isPrivilegeAssignedToRoles(privilege.getId())) {
            throw new ResourceInUseException("Privilege", name, "roles");
        }

        // Logic delete (deactivate) if in use check is hard
        // But requested is deactivate/block
        privilege.setActive(false);
        privilegeRepository.save(privilege);

        auditService.audit("PRIV_DISABLED", "RBAC", AuditEvent.AuditOutcome.SUCCESS,
                null, getCurrentUsername(),
                "Privilege", privilege.getName(),
                new AuditDetailsBuilder()
                        .add("active", false)
                        .build());
    }

    private PrivilegeDto mapToDto(Privilege privilege) {
        return new PrivilegeDto(privilege.getName(), privilege.getDescription(), privilege.isActive());
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
