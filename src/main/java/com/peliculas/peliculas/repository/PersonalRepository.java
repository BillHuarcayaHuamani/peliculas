package com.peliculas.peliculas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peliculas.peliculas.model.Personal;

@Repository
public interface PersonalRepository extends JpaRepository<Personal, Long> {
    Optional<Personal> findByEmail(String email);
    boolean existsByEmail(String email); 
}
