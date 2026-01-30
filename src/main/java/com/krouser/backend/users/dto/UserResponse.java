package com.krouser.backend.users.dto;

import com.krouser.backend.users.entity.User;
import com.krouser.backend.rbac.entity.Role;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserResponse {
    private UUID idPublic;
    private String username;
    private String alias;
    private String tag;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private List<String> roles;

    public UserResponse() {
    }

    public UserResponse(User user) {
        this.idPublic = user.getIdPublic();
        this.username = user.getUsername();
        this.alias = user.getAlias();
        this.tag = user.getTag();
        this.nombre = user.getNombre();
        this.segundoNombre = user.getSegundoNombre();
        this.apellidoPaterno = user.getApellidoPaterno();
        this.apellidoMaterno = user.getApellidoMaterno();
        this.roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
