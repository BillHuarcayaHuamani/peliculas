package com.peliculas.peliculas.model;

public class PeliculaConFavoritoInfo extends Pelicula {
    private String usuarioFavoritoNombre;

    public PeliculaConFavoritoInfo(Pelicula pelicula, String usuarioFavoritoNombre) {
        this.setId(pelicula.getId());
        this.setTitulo(pelicula.getTitulo());
        this.setDescripcion(pelicula.getDescripcion());
        this.setPortada(pelicula.getPortada());
        this.setVideo(pelicula.getVideo());
        this.setGenero(pelicula.getGenero());
        this.setDirector(pelicula.getDirector());
        this.setActoresPrincipales(pelicula.getActoresPrincipales());
        this.setAnio(pelicula.getAnio());
        this.setPersonalId(pelicula.getPersonalId());
        
        this.usuarioFavoritoNombre = usuarioFavoritoNombre;
    }

    public PeliculaConFavoritoInfo() {
    }

    public String getUsuarioFavoritoNombre() {
        return usuarioFavoritoNombre;
    }

    public void setUsuarioFavoritoNombre(String usuarioFavoritoNombre) {
        this.usuarioFavoritoNombre = usuarioFavoritoNombre;
    }
}
