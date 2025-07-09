package com.peliculas.peliculas.repository;

import java.util.List; 
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peliculas.peliculas.model.ProgresoVisualizacion;

@Repository
public interface ProgresoVisualizacionRepository extends JpaRepository<ProgresoVisualizacion, Long> {

    Optional<ProgresoVisualizacion> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    List<ProgresoVisualizacion> findByUsuarioId(Long usuarioId);
}
