// src/main/java/com/peliculas/peliculas/repository/PeliculaRepository.java
package com.peliculas.peliculas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importar Query
import org.springframework.stereotype.Repository;

import com.peliculas.peliculas.model.Pelicula;

@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {
    List<Pelicula> findByGenero(String genero);

    // Nuevo método para búsqueda por título, director, actores o género
    // Se usa LOWER() para hacer la búsqueda insensible a mayúsculas/minúsculas
    // Se usa %:searchTerm% para buscar coincidencias parciales en cualquier parte del campo
    @Query("SELECT p FROM Pelicula p WHERE " +
           "LOWER(p.titulo) LIKE %:searchTerm% OR " +
           "LOWER(p.director) LIKE %:searchTerm% OR " +
           "LOWER(p.actoresPrincipales) LIKE %:searchTerm% OR " +
           "LOWER(p.genero) LIKE %:searchTerm%")
    List<Pelicula> searchByKeyword(String searchTerm);
}
