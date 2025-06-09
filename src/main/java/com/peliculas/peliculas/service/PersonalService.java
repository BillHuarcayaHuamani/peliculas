package com.peliculas.peliculas.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.peliculas.peliculas.model.Personal;
import com.peliculas.peliculas.model.Usuario;
import com.peliculas.peliculas.repository.PersonalRepository;
import com.peliculas.peliculas.repository.UsuarioRepository;

@Service
public class PersonalService {

    private final PersonalRepository personalRepository;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PersonalService(PersonalRepository personalRepository, UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.personalRepository = personalRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Personal registrarPersonal(Personal personal) {
        if (usuarioRepository.findByEmail(personal.getEmail()).isPresent() || personalRepository.findByEmail(personal.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        String hashedPassword = passwordEncoder.encode(personal.getPassword());
        personal.setPassword(hashedPassword); 

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombres(personal.getNombres());
        nuevoUsuario.setApellidos(personal.getApellidos());
        nuevoUsuario.setEmail(personal.getEmail());
        nuevoUsuario.setPassword(hashedPassword); 
        nuevoUsuario.setFoto("/assets/img/PersonaLogin.png"); 
        nuevoUsuario.setFechaDeRegistro(LocalDateTime.now()); 
        usuarioRepository.save(nuevoUsuario);

        return personalRepository.save(personal);
    }

    public Optional<Personal> findByEmail(String email) {
        return personalRepository.findByEmail(email);
    }
}
