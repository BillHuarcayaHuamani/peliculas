package com.peliculas.peliculas.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importar Collectors
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.peliculas.peliculas.model.Personal;
import com.peliculas.peliculas.model.Usuario;
import com.peliculas.peliculas.model.UsuarioDisplayDTO;
import com.peliculas.peliculas.repository.PersonalRepository;
import com.peliculas.peliculas.repository.UsuarioRepository; 

@Service
public class UsuarioService implements UserDetailsService {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/fotos_perfil/";

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PersonalRepository personalRepository; 

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder, PersonalRepository personalRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.personalRepository = personalRepository; 
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de subida: " + e.getMessage());
        }
    }

    public Usuario registrar(Usuario usuario, MultipartFile fotoPerfil) throws IOException {
        String hashedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(hashedPassword);

        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + fotoPerfil.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(fotoPerfil.getInputStream(), filePath);
            usuario.setFoto("/uploads/fotos_perfil/" + fileName);
        } else {
            usuario.setFoto("/assets/img/PersonaLogin.png");
        }

        if (usuario.getFechaDeRegistro() == null) {
            usuario.setFechaDeRegistro(LocalDateTime.now());
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        Collection<? extends GrantedAuthority> authorities = getAuthoritiesForUser(email);

        return new User(usuario.getEmail(), usuario.getPassword(), authorities);
    }

    private Collection<? extends GrantedAuthority> getAuthoritiesForUser(String email) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); 

        Optional<Personal> optionalPersonal = personalRepository.findByEmail(email);
        if (optionalPersonal.isPresent()) {
            Personal personal = optionalPersonal.get();
            if ("administrador".equalsIgnoreCase(personal.getCargo())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_TRABAJADOR")); 
            } else if ("trabajador".equalsIgnoreCase(personal.getCargo())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_TRABAJADOR"));
            }
        }
        return authorities;
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

    public List<UsuarioDisplayDTO> getAllUsuariosForDisplay() {
        List<Usuario> allUsers = usuarioRepository.findAll();
        List<UsuarioDisplayDTO> displayUsers = new ArrayList<>();

        for (Usuario user : allUsers) {
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_USER"); 

            Optional<Personal> optionalPersonal = personalRepository.findByEmail(user.getEmail());
            if (optionalPersonal.isPresent()) {
                Personal personal = optionalPersonal.get();
                if ("administrador".equalsIgnoreCase(personal.getCargo())) {
                    roles.add("ROLE_ADMIN");
                    roles.add("ROLE_TRABAJADOR");
                } else if ("trabajador".equalsIgnoreCase(personal.getCargo())) {
                    roles.add("ROLE_TRABAJADOR");
                }
            }
            displayUsers.add(new UsuarioDisplayDTO(user, roles));
        }
        return displayUsers;
    }

    public PersonalRepository getPersonalRepository() {
        return personalRepository;
    }
}
