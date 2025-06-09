package com.peliculas.peliculas.controller;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.peliculas.peliculas.model.Personal;
import com.peliculas.peliculas.model.Usuario;
import com.peliculas.peliculas.repository.UsuarioRepository;
import com.peliculas.peliculas.service.PersonalService;

@Controller
public class PersonalController {

    private final PersonalService personalService;
    private final UsuarioRepository usuarioRepository; 

    public PersonalController(PersonalService personalService, UsuarioRepository usuarioRepository) {
        this.personalService = personalService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/registro-personal")
    public String mostrarRegistroPersonal(Model model) {
        
        model.addAttribute("personal", new Personal());
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
            }
        }
        return "registroPersonal";
    }

    @PostMapping("/registro-personal")
    public String procesarRegistroPersonal(@ModelAttribute Personal personal,
                                           @RequestParam("confirmarPassword") String confirmarPassword,
                                           RedirectAttributes redirectAttributes) {

        if (!personal.getPassword().equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/registro-personal";
        }
        
        personal.setCargo("trabajador");

        try {
            personalService.registrarPersonal(personal);
            redirectAttributes.addFlashAttribute("mensaje", "¡Personal registrado exitosamente!");
            return "redirect:/registro-personal"; 
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-personal";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Error de base de datos: " + e.getMessage());
            return "redirect:/registro-personal";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado: " + e.getMessage());
            return "redirect:/registro-personal";
        }
    }
}
