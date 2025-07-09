package com.peliculas.peliculas.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.peliculas.peliculas.model.Pelicula;
import com.peliculas.peliculas.model.Personal;
import com.peliculas.peliculas.model.Usuario; 
import com.peliculas.peliculas.repository.PersonalRepository;
import com.peliculas.peliculas.repository.UsuarioRepository; 
import com.peliculas.peliculas.service.FavoritosService;
import com.peliculas.peliculas.service.PeliculaService;
import com.peliculas.peliculas.service.ProgresoVisualizacionService;

@Controller
public class PeliculaController {

    private static final Logger logger = LoggerFactory.getLogger(PeliculaController.class);

    private final PeliculaService peliculaService;
    private final ProgresoVisualizacionService progresoVisualizacionService;
    private final UsuarioRepository usuarioRepository;
    private final PersonalRepository personalRepository;
    private final FavoritosService favoritosService;

    public PeliculaController(PeliculaService peliculaService,
                              ProgresoVisualizacionService progresoVisualizacionService,
                              UsuarioRepository usuarioRepository,
                              PersonalRepository personalRepository,
                              FavoritosService favoritosService) {
        this.peliculaService = peliculaService;
        this.progresoVisualizacionService = progresoVisualizacionService;
        this.usuarioRepository = usuarioRepository;
        this.personalRepository = personalRepository;
        this.favoritosService = favoritosService;
    }

    @GetMapping("/peliculas/nueva")
    public String mostrarFormularioNuevaPelicula(Model model) {
        model.addAttribute("pelicula", new Pelicula());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
            if (optionalUsuario.isPresent()) {
                model.addAttribute("usuario", optionalUsuario.get());
                boolean isAdmin = userDetails.getAuthorities().stream()
                                             .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdmin);
                boolean isPersonal = userDetails.getAuthorities().stream()
                                                 .anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isPersonal", isPersonal);
            }
        }

        List<String> generosFijos = Arrays.asList("Acción", "Aventura", "Ciencia Ficción", "Comedia", "Drama", "Contenido Nacional", "Romance", "Thriller", "Musical", "Terror", "Animación", "Documental");
        model.addAttribute("generosFijos", generosFijos);

        return "nuevaPelicula";
    }

    @PostMapping("/peliculas")
    public String guardarPelicula(@ModelAttribute Pelicula pelicula,
                                  @RequestParam(value = "portadaFile", required = false) MultipartFile portadaFile,
                                  @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                                  RedirectAttributes redirectAttributes,
                                  @AuthenticationPrincipal UserDetails currentUser) {
        try {
            if (currentUser == null || currentUser.getUsername().equals("anonymousUser")) {
                throw new IllegalStateException("Solo el personal autenticado puede subir películas.");
            }

            boolean hasWorkerOrAdminRole = currentUser.getAuthorities().stream()
                                                     .anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));

            if (!hasWorkerOrAdminRole) {
                throw new IllegalStateException("Acceso denegado. Solo el personal (trabajadores o administradores) puede subir películas.");
            }

            String email = currentUser.getUsername();
            Optional<Personal> optionalPersonal = personalRepository.findByEmail(email);

            if (optionalPersonal.isPresent()) {
                pelicula.setPersonalId(optionalPersonal.get().getId());
            } else {
                throw new IllegalStateException("No se pudo encontrar la información del personal para asignar la película.");
            }

            peliculaService.guardarPelicula(pelicula, portadaFile, videoFile);
            redirectAttributes.addFlashAttribute("mensaje", "¡Película guardada exitosamente!");
            return "redirect:/";
        } catch (DataIntegrityViolationException e) {
            logger.error("Error al guardar película (duplicado): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: Ya existe una película con este título.");
            return "redirect:/peliculas/nueva";
        } catch (IOException e) {
            logger.error("Error al subir archivos (portada/video): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al subir los archivos (portada/video). Inténtalo de nuevo.");
            return "redirect:/peliculas/nueva";
        } catch (IllegalStateException e) {
            logger.error("Error de permisos o usuario no personal al guardar película: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/peliculas/nueva";
        } catch (Exception e) {
            logger.error("Ocurrió un error inesperado al guardar la película: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Ocurrió un error inesperado al guardar la película. Por favor, inténtalo más tarde.");
            return "redirect:/peliculas/nueva";
        }
    }

    @GetMapping("/pelicula/{id}")
    public String mostrarDetallePelicula(@PathVariable Long id, Model model,
                                         @AuthenticationPrincipal UserDetails currentUser,
                                         RedirectAttributes redirectAttributes) {

        Optional<Pelicula> optionalPelicula = peliculaService.findById(id);

        if (optionalPelicula.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La película solicitada no fue encontrada.");
            return "redirect:/";
        }

        Pelicula pelicula = optionalPelicula.get();
        model.addAttribute("pelicula", pelicula);
        model.addAttribute("jsPeliculaId", id); 

        if (currentUser != null && !currentUser.getUsername().equals("anonymousUser")) {
            usuarioRepository.findByEmail(currentUser.getUsername()).ifPresent(user -> {
                model.addAttribute("usuario", user);
                Float ultimaPosicion = progresoVisualizacionService.obtenerUltimaPosicion(user.getId(), id);
                model.addAttribute("ultimaPosicion", ultimaPosicion);

                boolean isFavorited = favoritosService.isFavorited(user.getId(), id);
                model.addAttribute("isFavorited", isFavorited);
            });
            boolean isPersonal = currentUser.getAuthorities().stream()
                                             .anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isPersonal", isPersonal);
            boolean isAdmin = currentUser.getAuthorities().stream()
                                         .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        } else {
            model.addAttribute("ultimaPosicion", 0.0f);
            model.addAttribute("isFavorited", false);
            model.addAttribute("isPersonal", false);
            model.addAttribute("isAdmin", false);
        }

        return "detallePelicula";
    }

    @PostMapping("/api/peliculas/{peliculaId}/progreso")
    @ResponseBody
    public Map<String, String> actualizarProgreso(@PathVariable Long peliculaId,
                                                  @RequestBody Map<String, Float> payload,
                                                  @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null || currentUser.getUsername().equals("anonymousUser")) {
            return Map.of("status", "error", "message", "Usuario no autenticado");
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(currentUser.getUsername());
        if (optionalUsuario.isEmpty()) {
            return Map.of("status", "error", "message", "Usuario no encontrado en la base de datos");
        }
        Long usuarioId = optionalUsuario.get().getId();

        Float posicion = payload.get("posicion");

        if (posicion == null) {
            return Map.of("status", "error", "message", "Posición no proporcionada");
        }

        try {
            progresoVisualizacionService.guardarOActualizarProgreso(usuarioId, peliculaId, posicion);
            return Map.of("status", "success", "message", "Progreso guardado");
        } catch (Exception e) {
            logger.error("Error al guardar progreso para usuario {} pelicula {}: {}", usuarioId, peliculaId,
                    e.getMessage(), e);
            return Map.of("status", "error", "message", "Error al guardar progreso: " + e.getMessage());
        }
    }

    @PostMapping("/api/peliculas/{peliculaId}/toggle-favorito")
    @ResponseBody
    public Map<String, Object> toggleFavorito(@PathVariable Long peliculaId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null || currentUser.getUsername().equals("anonymousUser")) {
            return Map.of("status", "error", "message", "Usuario no autenticado", "isFavorited", false);
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(currentUser.getUsername());
        if (optionalUsuario.isEmpty()) {
            return Map.of("status", "error", "message", "Usuario no encontrado en la base de datos", "isFavorited", false);
        }
        Long usuarioId = optionalUsuario.get().getId();

        try {
            boolean newFavoriteStatus = favoritosService.toggleFavorite(usuarioId, peliculaId);
            return Map.of("status", "success", "message", "Estado de favorito actualizado", "isFavorited", newFavoriteStatus);
        } catch (Exception e) {
            logger.error("Error al alternar favorito para usuario {} pelicula {}: {}", usuarioId, peliculaId, e.getMessage(), e);
            return Map.of("status", "error", "message", "Error al alternar favorito: " + e.getMessage(), "isFavorited", false);
        }
    }

    @GetMapping("/api/movies/search")
    @ResponseBody 
    public List<Pelicula> searchMovies(@RequestParam String query) {
        return peliculaService.searchMovies(query);
    }

    @GetMapping("/api/movies/recommendations")
    @ResponseBody
    public List<Pelicula> getRecommendations(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null || currentUser.getUsername().equals("anonymousUser")) {
            return List.of();
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(currentUser.getUsername());
        if (optionalUsuario.isEmpty()) {
            return List.of(); 
        }
        Long userId = optionalUsuario.get().getId();

        return peliculaService.getRecommendedMovies(userId);
    }
}
