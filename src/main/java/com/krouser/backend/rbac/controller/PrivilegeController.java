package com.krouser.backend.rbac.controller;

import com.krouser.backend.rbac.dto.CreatePrivilegeRequest;
import com.krouser.backend.rbac.dto.PrivilegeDto;
import com.krouser.backend.rbac.service.PrivilegeService;
import com.krouser.backend.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rbac/privileges")
public class PrivilegeController {

    private final PrivilegeService privilegeService;

    public PrivilegeController(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_PRIV_READ')")
    public ResponseEntity<ApiResponse<List<PrivilegeDto>>> getAllPrivileges() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Privileges retrieved successfully",
                privilegeService.getAllPrivileges(),
                "/api/rbac/privileges"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_PRIV_CREATE')")
    public ResponseEntity<ApiResponse<PrivilegeDto>> createPrivilege(@RequestBody CreatePrivilegeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Privilege created successfully",
                        privilegeService.createPrivilege(request),
                        "/api/rbac/privileges"));
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_PRIV_READ')")
    public ResponseEntity<ApiResponse<PrivilegeDto>> getPrivilege(@PathVariable String name) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Privilege retrieved successfully",
                privilegeService.getPrivilege(name),
                "/api/rbac/privileges/" + name));
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_PRIV_UPDATE')")
    public ResponseEntity<ApiResponse<PrivilegeDto>> updatePrivilege(@PathVariable String name,
            @RequestBody CreatePrivilegeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Privilege updated successfully",
                privilegeService.updatePrivilege(name, request),
                "/api/rbac/privileges/" + name));
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('RBAC_PRIV_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deletePrivilege(@PathVariable String name) {
        privilegeService.deletePrivilege(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
