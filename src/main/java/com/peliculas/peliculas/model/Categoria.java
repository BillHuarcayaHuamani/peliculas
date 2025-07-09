package com.peliculas.peliculas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String enlace;
    private String imagen;

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEnlace() {
        return enlace;
    }

    public String getImagen() {
        return imagen;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
