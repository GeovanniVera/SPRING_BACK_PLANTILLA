package com.krouser.backend.auth.dto;

import java.util.Set;
import java.util.UUID;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private UUID idPublic;
    private String username;
    private String alias;
    private String tag;
    private Set<String> roles;

    private String refreshToken;

    public LoginResponse(String token, String refreshToken, UUID idPublic, String username, String alias, String tag,
            Set<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.idPublic = idPublic;
        this.username = username;
        this.alias = alias;
        this.tag = tag;
        this.roles = roles;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
