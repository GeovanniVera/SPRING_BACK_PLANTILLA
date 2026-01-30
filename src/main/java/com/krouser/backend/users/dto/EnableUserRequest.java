package com.krouser.backend.users.dto;

import jakarta.validation.constraints.NotNull;

public class EnableUserRequest {
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}
