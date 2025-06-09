package com.peliculas.peliculas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint; 

@Entity
@Table(name = "favoritos", uniqueConstraints = {@UniqueConstraint(columnNames = {"usuario_id", "pelicula_id"})})
public class Favoritos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "pelicula_id", nullable = false)
    private Long peliculaId;

    public Favoritos() {
    }

    public Favoritos(Long usuarioId, Long peliculaId) {
        this.usuarioId = usuarioId;
        this.peliculaId = peliculaId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getPeliculaId() {
        return peliculaId;
    }

    public void setPeliculaId(Long peliculaId) {
        this.peliculaId = peliculaId;
    }
}
