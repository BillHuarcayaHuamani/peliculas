package com.peliculas.peliculas.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.peliculas.peliculas.model.Pelicula;
import com.peliculas.peliculas.service.PeliculaService; 

@Controller
public class PeliculaController {

    private static final Logger logger = LoggerFactory.getLogger(PeliculaController.class);

    private final PeliculaService peliculaService;

    public PeliculaController(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    @GetMapping("/peliculas/nueva")
    public String mostrarFormularioNuevaPelicula(Model model) {
        model.addAttribute("pelicula", new Pelicula());
        return "nuevaPelicula";
    }

    @PostMapping("/peliculas")
    public String guardarPelicula(@ModelAttribute Pelicula pelicula,
                                  @RequestParam(value = "portadaFile", required = false) MultipartFile portadaFile,
                                  @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                                  RedirectAttributes redirectAttributes) {
        try {
            peliculaService.guardarPelicula(pelicula, portadaFile, videoFile);
            redirectAttributes.addFlashAttribute("mensaje", "¡Película guardada exitosamente!");
            return "redirect:/";
        } catch (DataIntegrityViolationException e) {
            logger.error("Error al guardar película (duplicado): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: Ya existe una película con este título.");
            return "redirect:/peliculas/nueva";
        } catch (IOException e) {
            logger.error("Error al subir archivos (portada/video): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al subir los archivos (portada/video). Inténtalo de nuevo.");
            return "redirect:/peliculas/nueva";
        } catch (Exception e) {
            logger.error("Ocurrió un error inesperado al guardar la película: {}", e.getMessage(), e); // Incluye 'e' para la pila completa
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al guardar la película. Por favor, inténtalo más tarde.");
            return "redirect:/peliculas/nueva";
        }
    }
}