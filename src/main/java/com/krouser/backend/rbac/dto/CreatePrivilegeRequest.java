package com.krouser.backend.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePrivilegeRequest {
    @NotBlank(message = "Privilege name is required")
    @Size(min = 2, max = 80, message = "Privilege name must be between 2 and 80 characters")
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
