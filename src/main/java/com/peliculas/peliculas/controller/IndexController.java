/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peliculas.peliculas.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public IndexController(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository, PeliculaService peliculaService) {
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

        // Obtener el usuario autenticado de Spring Security y añadirlo al modelo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
            String email = authentication.getName();
            Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
            if (optionalUsuario.isPresent()) {
                Usuario usuarioAutenticado = optionalUsuario.get();
                model.addAttribute("usuario", usuarioAutenticado);
            }
        }
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
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
            String email = authentication.getName();
            Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
            if (optionalUsuario.isPresent()) {
                Usuario usuarioAutenticado = optionalUsuario.get();
                model.addAttribute("usuario", usuarioAutenticado);
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