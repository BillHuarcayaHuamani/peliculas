package com.peliculas.peliculas.model;

import java.time.LocalDateTime;
import java.util.List;

public class UsuarioDisplayDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private String foto;
    private LocalDateTime fechaDeRegistro;
    private List<String> roles; 

    public UsuarioDisplayDTO(Usuario usuario, List<String> roles) {
        this.id = usuario.getId();
        this.nombres = usuario.getNombres();
        this.apellidos = usuario.getApellidos();
        this.email = usuario.getEmail();
        this.foto = usuario.getFoto();
        this.fechaDeRegistro = usuario.getFechaDeRegistro();
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public LocalDateTime getFechaDeRegistro() {
        return fechaDeRegistro;
    }

    public void setFechaDeRegistro(LocalDateTime fechaDeRegistro) {
        this.fechaDeRegistro = fechaDeRegistro;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
