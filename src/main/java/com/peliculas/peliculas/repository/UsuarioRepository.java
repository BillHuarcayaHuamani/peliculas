/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peliculas.peliculas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peliculas.peliculas.model.Usuario; // Para cuando no se encuentre usuarios

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método personalizado para buscar un usuario por su email
    Optional<Usuario> findByEmail(String email);

    // Método personalizado para buscar un usuario por email y password
    Optional<Usuario> findByEmailAndPassword(String email, String password);
}