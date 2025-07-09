package com.peliculas.peliculas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "progreso_visualizacion")
public class ProgresoVisualizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "pelicula_id", nullable = false)
    private Long peliculaId;

    @Column(name = "ultima_posicion", nullable = false)
    private Float ultimaPosicion;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    public ProgresoVisualizacion() {
    }

    public ProgresoVisualizacion(Long usuarioId, Long peliculaId, Float ultimaPosicion) {
        this.usuarioId = usuarioId;
        this.peliculaId = peliculaId;
        this.ultimaPosicion = ultimaPosicion;
        this.ultimaActualizacion = LocalDateTime.now();
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

    public Float getUltimaPosicion() {
        return ultimaPosicion;
    }

    public void setUltimaPosicion(Float ultimaPosicion) {
        this.ultimaPosicion = ultimaPosicion;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}