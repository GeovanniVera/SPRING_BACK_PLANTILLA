package com.krouser.backend.rbac.dto;

import java.util.Set;

public class RoleDto {
    private String name;
    private String description;
    private boolean active;
    private Set<String> privileges;

    public RoleDto() {
    }

    public RoleDto(String name, String description, boolean active, Set<String> privileges) {
        this.name = name;
        this.description = description;
        this.active = active;
        this.privileges = privileges;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<String> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }
}
