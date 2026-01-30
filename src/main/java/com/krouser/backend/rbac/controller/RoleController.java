package com.krouser.backend.rbac.controller;

import com.krouser.backend.rbac.dto.AssignPrivilegesRequest;
import com.krouser.backend.rbac.dto.CreateRoleRequest;
import com.krouser.backend.rbac.dto.RoleDto;
import com.krouser.backend.rbac.service.RoleService;
import com.krouser.backend.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rbac/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_ROLE_READ')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Roles retrieved successfully", roles, "/api/rbac/roles"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleDto role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Role created successfully", role,
                        "/api/rbac/roles"));
    }

    @GetMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleDto>> getRole(@PathVariable String roleName) {
        RoleDto role = roleService.getRole(roleName);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Role retrieved successfully", role,
                "/api/rbac/roles/" + roleName));
    }

    @PutMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(@PathVariable String roleName,
            @RequestBody CreateRoleRequest request) {
        RoleDto role = roleService.updateRole(roleName, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Role updated successfully", role,
                "/api/rbac/roles/" + roleName));
    }

    @DeleteMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String roleName) {
        roleService.deleteRole(roleName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{roleName}/privileges")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> assignPrivileges(@PathVariable String roleName,
            @RequestBody AssignPrivilegesRequest request) {
        roleService.assignPrivileges(roleName, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Privileges assigned successfully", null,
                "/api/rbac/roles/" + roleName + "/privileges"));
    }
}
