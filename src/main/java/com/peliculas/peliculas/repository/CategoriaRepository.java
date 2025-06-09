/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.peliculas.peliculas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peliculas.peliculas.model.Categoria;

/**
 *
 * @author INTEL
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
