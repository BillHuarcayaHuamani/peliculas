// src/main/java/com/peliculas/peliculas/repository/ProgresoVisualizacionRepository.java
package com.peliculas.peliculas.repository;

import java.util.List; // Importar List
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peliculas.peliculas.model.ProgresoVisualizacion;

@Repository
public interface ProgresoVisualizacionRepository extends JpaRepository<ProgresoVisualizacion, Long> {

    Optional<ProgresoVisualizacion> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    // ¡IMPORTANTE: Añadir este nuevo método para obtener todas las películas vistas por un usuario!
    List<ProgresoVisualizacion> findByUsuarioId(Long usuarioId);
}
