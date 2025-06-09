package com.peliculas.peliculas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peliculas.peliculas.model.Favoritos;

@Repository
public interface FavoritosRepository extends JpaRepository<Favoritos, Long> {
    Optional<Favoritos> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    List<Favoritos> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);
}
