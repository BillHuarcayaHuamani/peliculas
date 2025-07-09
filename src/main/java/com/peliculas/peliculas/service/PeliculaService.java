// src/main/java/com/peliculas/peliculas/service/PeliculaService.java
package com.peliculas.peliculas.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID; // Importar UUID para generar nombres de archivo únicos
import java.util.stream.Collectors; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar Transactional
import org.springframework.web.multipart.MultipartFile;

import com.peliculas.peliculas.model.Pelicula;
import com.peliculas.peliculas.model.ProgresoVisualizacion; // Importar ProgresoVisualizacion
import com.peliculas.peliculas.model.Usuario; // Importar Usuario
import com.peliculas.peliculas.repository.PeliculaRepository;
import com.peliculas.peliculas.repository.ProgresoVisualizacionRepository; // Importar ProgresoVisualizacionRepository
import com.peliculas.peliculas.repository.UsuarioRepository; // Importar UsuarioRepository

@Service
public class PeliculaService {

    private final String UPLOAD_DIR_PORTADAS = "src/main/resources/static/uploads/peliculas/portadas/";
    private final String UPLOAD_DIR_VIDEOS = "src/main/resources/static/uploads/peliculas/videos/";

    private final PeliculaRepository peliculaRepository;
    private final ProgresoVisualizacionRepository progresoVisualizacionRepository; // Inyectar
    private final UsuarioRepository usuarioRepository; // Inyectar

    @Autowired // Asegúrate de que los constructores estén bien definidos para la inyección de dependencias
    public PeliculaService(PeliculaRepository peliculaRepository,
                           ProgresoVisualizacionRepository progresoVisualizacionRepository,
                           UsuarioRepository usuarioRepository) {
        this.peliculaRepository = peliculaRepository;
        this.progresoVisualizacionRepository = progresoVisualizacionRepository;
        this.usuarioRepository = usuarioRepository;
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR_PORTADAS));
            Files.createDirectories(Paths.get(UPLOAD_DIR_VIDEOS));
        } catch (IOException e) {
            System.err.println("Error al crear los directorios de subida para películas: " + e.getMessage());
        }
    }

    public Pelicula guardarPelicula(Pelicula pelicula, MultipartFile portadaFile, MultipartFile videoFile) throws IOException {
        if (portadaFile != null && !portadaFile.isEmpty()) {
            // Generar un nombre de archivo único usando UUID
            String fileName = UUID.randomUUID().toString() + "_" + portadaFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR_PORTADAS + fileName);
            Files.copy(portadaFile.getInputStream(), filePath);
            pelicula.setPortada("/uploads/peliculas/portadas/" + fileName);
        } else {
            pelicula.setPortada("/assets/img/portadaDefecto.png");
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            // Generar un nombre de archivo único usando UUID
            String fileName = UUID.randomUUID().toString() + "_" + videoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR_VIDEOS + fileName);
            Files.copy(videoFile.getInputStream(), filePath);
            pelicula.setVideo("/uploads/peliculas/videos/" + fileName);
        } else {
            pelicula.setVideo(null);
        }

        return peliculaRepository.save(pelicula);
    }

    public List<Pelicula> listarTodasLasPeliculas() {
        return peliculaRepository.findAll();
    }

    public List<Pelicula> listarPeliculasPorGenero(String genero) {
        return peliculaRepository.findByGenero(genero);
    }

    public Optional<Pelicula> findById(Long id) {
        return peliculaRepository.findById(id);
    }

    // --- NUEVOS MÉTODOS PARA BÚSQUEDA Y RECOMENDACIÓN ---

    /**
     * Busca películas por título, director, actores principales o género.
     * @param searchTerm El término de búsqueda.
     * @return Una lista de películas que coinciden con el término de búsqueda.
     */
    public List<Pelicula> searchMovies(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return listarTodasLasPeliculas(); // O una lista vacía, según la UX deseada
        }
        return peliculaRepository.searchByKeyword(searchTerm.toLowerCase());
    }

    /**
     * Genera recomendaciones de películas basadas en los géneros de las películas vistas por el usuario.
     * @param userId El ID del usuario para quien generar las recomendaciones.
     * @return Una lista de películas recomendadas.
     */
    @Transactional(readOnly = true) // Solo lectura, no modifica datos
    public List<Pelicula> getRecommendedMovies(Long userId) {
        Optional<Usuario> optionalUser = usuarioRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return List.of(); // Retorna lista vacía si el usuario no existe
        }
        Usuario user = optionalUser.get();

        // Obtener todas las películas que el usuario ha visto
        // Usar findByUsuarioId del repositorio de ProgresoVisualizacion
        List<ProgresoVisualizacion> watchedProgresses = progresoVisualizacionRepository.findByUsuarioId(user.getId());
        Set<String> watchedGenres = new HashSet<>();

        // Recopilar todos los géneros de las películas vistas
        watchedProgresses.forEach(progress -> {
            peliculaRepository.findById(progress.getPeliculaId()).ifPresent(movie -> {
                if (movie.getGenero() != null && !movie.getGenero().isEmpty()) {
                    // Asumiendo que los géneros pueden ser una cadena separada por comas
                    Arrays.stream(movie.getGenero().split(","))
                          .map(String::trim)
                          .forEach(watchedGenres::add);
                }
            });
        });

        if (watchedGenres.isEmpty()) {
            // Si el usuario no ha visto películas o no tienen géneros,
            // puedes retornar películas populares, aleatorias o simplemente una lista vacía.
            // Por ahora, retornamos las 5 primeras películas generales como sugerencia inicial.
            return peliculaRepository.findAll().stream().limit(5).collect(Collectors.toList());
        }

        // Obtener todas las películas no vistas por el usuario
        List<Pelicula> unwatchedMovies = peliculaRepository.findAll().stream()
                .filter(movie -> !watchedProgresses.stream()
                                                    .anyMatch(p -> p.getPeliculaId().equals(movie.getId())))
                .collect(Collectors.toList());

        // Filtrar películas no vistas que compartan al menos un género con las películas vistas
        List<Pelicula> recommendations = unwatchedMovies.stream()
                .filter(movie -> {
                    if (movie.getGenero() == null || movie.getGenero().isEmpty()) {
                        return false;
                    }
                    return Arrays.stream(movie.getGenero().split(","))
                                 .map(String::trim)
                                 .anyMatch(watchedGenres::contains);
                })
                .limit(10) // Limitar el número de recomendaciones, por ejemplo, a 10
                .collect(Collectors.toList());

        return recommendations;
    }
}
