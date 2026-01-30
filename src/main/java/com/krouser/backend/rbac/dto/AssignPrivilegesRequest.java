package com.krouser.backend.rbac.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public class AssignPrivilegesRequest {
    @NotNull(message = "Privileges set is required")
    private Set<String> privileges;

    public Set<String> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }
}
