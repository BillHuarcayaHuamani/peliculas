// src/main/java/com/peliculas/peliculas/controller/IndexController.java
package com.peliculas.peliculas.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.peliculas.peliculas.model.Categoria;
import com.peliculas.peliculas.model.Pelicula;
import com.peliculas.peliculas.model.PeliculaInicio;
import com.peliculas.peliculas.model.Usuario;
import com.peliculas.peliculas.repository.CategoriaRepository;
import com.peliculas.peliculas.repository.UsuarioRepository;
import com.peliculas.peliculas.service.PeliculaService;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PeliculaService peliculaService;

    public IndexController(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository,
                           PeliculaService peliculaService) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.peliculaService = peliculaService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);

        List<PeliculaInicio> peliculaIniciosHardcodeadas = Arrays.asList(
            new PeliculaInicio("Acción", "/accion", "/assets/img/accion.jpg"),
            new PeliculaInicio("Aventura", "/aventura", "/assets/img/aventura.jpg"),
            new PeliculaInicio("Ciencia Ficción", "/ciencia", "/assets/img/cienciaficcion.jpg"),
            new PeliculaInicio("Comedia", "/comedia", "/assets/img/comedia.jpg"),
            new PeliculaInicio("Drama", "/drama", "/assets/img/drama.jpg")
        );
        model.addAttribute("peliculaIniciosHardcodeadas", peliculaIniciosHardcodeadas);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Inicializar isPersonal e isAdmin a false por defecto para evitar errores de Thymeleaf
        model.addAttribute("isPersonal", false);
        model.addAttribute("isAdmin", false);

        if (authentication != null && authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof UserDetails) {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
            if (optionalUsuario.isPresent()) {
                Usuario usuarioAutenticado = optionalUsuario.get();
                model.addAttribute("usuario", usuarioAutenticado);

                boolean isPersonalUser = userDetails.getAuthorities().stream()
                                                    .anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isPersonal", isPersonalUser);

                boolean isAdminUser = userDetails.getAuthorities().stream()
                                                   .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdminUser);
            }
        }
        // No es necesario un else if para anonymousUser aquí, ya que isPersonal/isAdmin ya están en false por defecto
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    private String cargarPeliculasPorGenero(String genero, Model model) {
        List<Pelicula> peliculas = peliculaService.listarPeliculasPorGenero(genero);
        model.addAttribute("peliculas", peliculas);
        model.addAttribute("generoActual", genero);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Inicializar isPersonal e isAdmin a false por defecto
        model.addAttribute("isPersonal", false);
        model.addAttribute("isAdmin", false);

        if (authentication != null && authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof UserDetails) {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
            if (optionalUsuario.isPresent()) {
                Usuario usuarioAutenticado = optionalUsuario.get();
                model.addAttribute("usuario", usuarioAutenticado);

                boolean isPersonalUser = userDetails.getAuthorities().stream()
                                                    .anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isPersonal", isPersonalUser);

                boolean isAdminUser = userDetails.getAuthorities().stream()
                                                   .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdminUser);
            }
        }
        return "peliculasPorGenero";
    }

    @GetMapping("/accion")
    public String mostrarPeliculasAccion(Model model) {
        return cargarPeliculasPorGenero("Acción", model);
    }

    @GetMapping("/aventura")
    public String mostrarPeliculasAventura(Model model) {
        return cargarPeliculasPorGenero("Aventura", model);
    }

    @GetMapping("/ciencia")
    public String mostrarPeliculasCienciaFiccion(Model model) {
        return cargarPeliculasPorGenero("Ciencia Ficción", model);
    }

    @GetMapping("/comedia")
    public String mostrarPeliculasComedia(Model model) {
        return cargarPeliculasPorGenero("Comedia", model);
    }

    @GetMapping("/drama")
    public String mostrarPeliculasDrama(Model model) {
        return cargarPeliculasPorGenero("Drama", model);
    }
}
