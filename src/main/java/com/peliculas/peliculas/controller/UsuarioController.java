/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peliculas.peliculas.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List; 
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.peliculas.peliculas.model.Favoritos;
import com.peliculas.peliculas.model.Pelicula;
import com.peliculas.peliculas.model.Usuario;
import com.peliculas.peliculas.repository.PeliculaRepository; 
import com.peliculas.peliculas.repository.UsuarioRepository;
import com.peliculas.peliculas.service.FavoritosService; 
import com.peliculas.peliculas.service.UsuarioService;

import jakarta.servlet.http.HttpServletResponse; 

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final FavoritosService favoritosService; 
    private final PeliculaRepository peliculaRepository; 
    private final UsuarioRepository usuarioRepository; 

    public UsuarioController(UsuarioService usuarioService, FavoritosService favoritosService, PeliculaRepository peliculaRepository, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.favoritosService = favoritosService;
        this.peliculaRepository = peliculaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) { 
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername(); 
            Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
            if (optionalUsuario.isPresent()) {
                model.addAttribute("usuario", optionalUsuario.get());
                boolean isPersonal = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isPersonal", isPersonal);
                boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdmin);
            }
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario,
                                   @RequestParam("confirmarPassword") String confirmarPassword,
                                   @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
                                   RedirectAttributes redirectAttributes) {

        if (!usuario.getPassword().equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/registro";
        }

        try {
            usuarioService.registrar(usuario, fotoPerfil);
            redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ahora puedes iniciar sesión.");
            return "redirect:/login";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está registrado. Por favor, usa otro.");
            return "redirect:/registro";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la foto de perfil. Inténtalo de nuevo.");
            return "redirect:/registro";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al registrar. " + e.getMessage());
            return "redirect:/registro";
        }
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas. Inténtalo de nuevo.");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) { 
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername(); 
            Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
            if (optionalUsuario.isPresent()) {
                model.addAttribute("usuario", optionalUsuario.get());
                boolean isPersonal = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isPersonal", isPersonal);
                boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdmin);
            }
        }
        return "login";
    }

    @GetMapping("/mis-favoritos")
    public String mostrarMisFavoritos(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return "redirect:/login?error=not-authenticated";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
        if (optionalUsuario.isEmpty()) {
            return "redirect:/login?error=user-not-found";
        }
        Long usuarioId = optionalUsuario.get().getId();

        model.addAttribute("usuario", optionalUsuario.get());
        boolean isPersonal = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isPersonal", isPersonal);
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        List<Favoritos> favoritos = favoritosService.getFavoritosByUsuario(usuarioId);
        
        List<Pelicula> peliculasFavoritas = new ArrayList<>();
        for (Favoritos fav : favoritos) {
            peliculaRepository.findById(fav.getPeliculaId()).ifPresent(peliculasFavoritas::add);
        }

        model.addAttribute("peliculasFavoritas", peliculasFavoritas);
        return "misFavoritos";
    }

    @GetMapping("/favoritos/export-excel")
    public void exportarFavoritosExcel(HttpServletResponse response, @AuthenticationPrincipal UserDetails currentUser) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Necesitas estar autenticado para realizar esta acción.");
            return;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        boolean hasWorkerOrAdminRole = userDetails.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!hasWorkerOrAdminRole) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Solo el personal puede exportar.");
            return;
        }

        String email = userDetails.getUsername(); 
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
        if (optionalUsuario.isEmpty()) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado.");
             return;
        }
        Long usuarioId = optionalUsuario.get().getId();

        List<Favoritos> allFavoritos = favoritosService.getFavoritosByUsuario(usuarioId); 

        byte[] excelBytes = favoritosService.generateExcel(allFavoritos);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"peliculas_favoritas.xlsx\"");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }
}
