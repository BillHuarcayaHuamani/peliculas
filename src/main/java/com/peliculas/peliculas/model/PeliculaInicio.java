package com.peliculas.peliculas.model;

public class PeliculaInicio {
    private String nombre;
    private String enlace;
    private String imagen;

    public PeliculaInicio(String nombre, String enlace, String imagen) {
        this.nombre = nombre;
        this.enlace = enlace;
        this.imagen = imagen;
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
