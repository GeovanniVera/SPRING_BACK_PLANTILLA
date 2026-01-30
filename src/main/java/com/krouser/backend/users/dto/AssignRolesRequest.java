package com.krouser.backend.users.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssignRolesRequest {
    @NotNull(message = "Roles list is required")
    private List<String> roles;

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
