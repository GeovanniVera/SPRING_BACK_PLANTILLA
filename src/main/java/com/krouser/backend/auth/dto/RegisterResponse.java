package com.krouser.backend.auth.dto;

import java.util.Set;
import java.util.UUID;

public class RegisterResponse {

    private UUID idPublic;
    private String username;
    private String alias;
    private String tag;
    private String message;
    private Set<String> roles;

    public RegisterResponse() {
    }

    public RegisterResponse(UUID idPublic, String username, String alias, String tag, String message,
            Set<String> roles) {
        this.idPublic = idPublic;
        this.username = username;
        this.alias = alias;
        this.tag = tag;
        this.message = message;
        this.roles = roles;
    }

    public UUID getIdPublic() {
        return idPublic;
    }

    public void setIdPublic(UUID idPublic) {
        this.idPublic = idPublic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
