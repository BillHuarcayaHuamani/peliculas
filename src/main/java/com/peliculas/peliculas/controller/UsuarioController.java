/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peliculas.peliculas.controller;

import java.io.ByteArrayOutputStream; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List; 
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row; 
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.peliculas.peliculas.model.PeliculaConFavoritoInfo;
import com.peliculas.peliculas.model.Usuario;
import com.peliculas.peliculas.model.UsuarioDisplayDTO;
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
        
        Optional<Usuario> optionalUsuarioAutenticado = usuarioRepository.findByEmail(email);
        if (optionalUsuarioAutenticado.isEmpty()) {
            return "redirect:/login?error=user-not-found";
        }
        Long usuarioIdAutenticado = optionalUsuarioAutenticado.get().getId();

        model.addAttribute("usuario", optionalUsuarioAutenticado.get());
        boolean isPersonal = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR") || a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isPersonal", isPersonal);
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        List<Favoritos> favoritosRawList;

        if (isPersonal) {
            favoritosRawList = favoritosService.getAllFavoritos(); 
            model.addAttribute("mostrarTodosLosFavoritos", true); 
        } else {
            favoritosRawList = favoritosService.getFavoritosByUsuario(usuarioIdAutenticado); 
            model.addAttribute("mostrarTodosLosFavoritos", false);
        }
        
        List<PeliculaConFavoritoInfo> peliculasFavoritasDisplay = new ArrayList<>();
        for (Favoritos fav : favoritosRawList) {
            Optional<Pelicula> optionalPelicula = peliculaRepository.findById(fav.getPeliculaId());
            if (optionalPelicula.isPresent()) {
                Pelicula pelicula = optionalPelicula.get();
                
                String usuarioFavoritoNombre = "Usuario Desconocido";
                Optional<Usuario> favUser = usuarioRepository.findById(fav.getUsuarioId());
                if (favUser.isPresent()) {
                    usuarioFavoritoNombre = favUser.get().getNombres() + " " + favUser.get().getApellidos();
                }
                
                peliculasFavoritasDisplay.add(new PeliculaConFavoritoInfo(pelicula, usuarioFavoritoNombre));
            }
        }

        model.addAttribute("peliculasFavoritas", peliculasFavoritasDisplay); 
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

        List<Favoritos> allFavoritos = favoritosService.getAllFavoritos(); 

        byte[] excelBytes = favoritosService.generateExcel(allFavoritos);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"peliculas_favoritas.xlsx\"");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return "redirect:/login?error=not-authenticated";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            return "redirect:/acceso-denegado"; 
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(userDetails.getUsername());
        if (optionalUsuario.isPresent()) {
            model.addAttribute("usuario", optionalUsuario.get());
            model.addAttribute("isAdmin", true); 
            model.addAttribute("isPersonal", true); 
        } else {
            return "redirect:/login?error=user-not-found";
        }

        List<UsuarioDisplayDTO> usuarios = usuarioService.getAllUsuariosForDisplay();
        model.addAttribute("usuarios", usuarios);

        return "listaUsuarios";
    }

    @GetMapping("/admin/usuarios/export-excel")
    public void exportarUsuariosExcel(HttpServletResponse response, @AuthenticationPrincipal UserDetails currentUser) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Necesitas estar autenticado para realizar esta acción.");
            return;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Solo los administradores pueden exportar la lista de usuarios.");
            return;
        }

        List<UsuarioDisplayDTO> usuarios = usuarioService.getAllUsuariosForDisplay();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lista de Usuarios");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nombres", "Apellidos", "Email", "Fecha de Registro", "Roles"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (UsuarioDisplayDTO usuario : usuarios) { 
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(usuario.getId());
                row.createCell(1).setCellValue(usuario.getNombres());
                row.createCell(2).setCellValue(usuario.getApellidos());
                row.createCell(3).setCellValue(usuario.getEmail());
                row.createCell(4).setCellValue(usuario.getFechaDeRegistro() != null ? usuario.getFechaDeRegistro().toString() : "");
                
                String rolesString = String.join(", ", usuario.getRoles());
                row.createCell(5).setCellValue(rolesString);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"lista_usuarios.xlsx\"");
            response.getOutputStream().write(outputStream.toByteArray());
            response.getOutputStream().flush();
        }
    }
}
