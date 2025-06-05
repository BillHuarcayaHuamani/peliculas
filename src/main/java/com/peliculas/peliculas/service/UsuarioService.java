/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peliculas.peliculas.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.peliculas.peliculas.model.Usuario;
import com.peliculas.peliculas.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/fotos_perfil/";

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de subida: " + e.getMessage());
        }
    }

    public Usuario registrar(Usuario usuario, MultipartFile fotoPerfil) throws IOException {
        // Encripta la contraseña antes de guardarla
        String hashedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(hashedPassword);

        // Maneja la subida de la foto
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + fotoPerfil.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(fotoPerfil.getInputStream(), filePath);
            usuario.setFoto("/uploads/fotos_perfil/" + fileName);
        } else {
            usuario.setFoto("/assets/img/PersonaLogin.png");
        }

        // Establece la fecha de registro
        if (usuario.getFechaDeRegistro() == null) {
            usuario.setFechaDeRegistro(LocalDateTime.now());
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Construye y retorna un objeto UserDetails de Spring Security
        return new User(usuario.getEmail(), usuario.getPassword(), Collections.emptyList());
    }

    public Usuario autenticar(String email, String rawPassword) {
        try {
            UserDetails userDetails = loadUserByUsername(email);
            if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
                return usuarioRepository.findByEmail(email).orElse(null);
            }
        } catch (UsernameNotFoundException e) {
            return null;
        }
        return null;
    }
}