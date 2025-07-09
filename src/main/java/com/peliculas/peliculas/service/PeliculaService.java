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
import java.util.UUID; 
import java.util.stream.Collectors; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.web.multipart.MultipartFile;

import com.peliculas.peliculas.model.Pelicula;
import com.peliculas.peliculas.model.ProgresoVisualizacion; 
import com.peliculas.peliculas.model.Usuario; 
import com.peliculas.peliculas.repository.PeliculaRepository;
import com.peliculas.peliculas.repository.ProgresoVisualizacionRepository; 
import com.peliculas.peliculas.repository.UsuarioRepository; 

@Service
public class PeliculaService {

    private final String UPLOAD_DIR_PORTADAS = "src/main/resources/static/uploads/peliculas/portadas/";
    private final String UPLOAD_DIR_VIDEOS = "src/main/resources/static/uploads/peliculas/videos/";

    private final PeliculaRepository peliculaRepository;
    private final ProgresoVisualizacionRepository progresoVisualizacionRepository; 
    private final UsuarioRepository usuarioRepository; 

    @Autowired 
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
            String fileName = UUID.randomUUID().toString() + "_" + portadaFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR_PORTADAS + fileName);
            Files.copy(portadaFile.getInputStream(), filePath);
            pelicula.setPortada("/uploads/peliculas/portadas/" + fileName);
        } else {
            pelicula.setPortada("/assets/img/portadaDefecto.png");
        }

        if (videoFile != null && !videoFile.isEmpty()) {
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

    public List<Pelicula> searchMovies(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return listarTodasLasPeliculas(); 
        }
        return peliculaRepository.searchByKeyword(searchTerm.toLowerCase());
    }

    @Transactional(readOnly = true) 
    public List<Pelicula> getRecommendedMovies(Long userId) {
        Optional<Usuario> optionalUser = usuarioRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return List.of(); 
        }
        Usuario user = optionalUser.get();

        List<ProgresoVisualizacion> watchedProgresses = progresoVisualizacionRepository.findByUsuarioId(user.getId());
        Set<String> watchedGenres = new HashSet<>();

        watchedProgresses.forEach(progress -> {
            peliculaRepository.findById(progress.getPeliculaId()).ifPresent(movie -> {
                if (movie.getGenero() != null && !movie.getGenero().isEmpty()) {
                    Arrays.stream(movie.getGenero().split(","))
                          .map(String::trim)
                          .forEach(watchedGenres::add);
                }
            });
        });

        if (watchedGenres.isEmpty()) {
            return peliculaRepository.findAll().stream().limit(5).collect(Collectors.toList());
        }

        List<Pelicula> unwatchedMovies = peliculaRepository.findAll().stream()
                .filter(movie -> !watchedProgresses.stream()
                                                    .anyMatch(p -> p.getPeliculaId().equals(movie.getId())))
                .collect(Collectors.toList());

        List<Pelicula> recommendations = unwatchedMovies.stream()
                .filter(movie -> {
                    if (movie.getGenero() == null || movie.getGenero().isEmpty()) {
                        return false;
                    }
                    return Arrays.stream(movie.getGenero().split(","))
                                 .map(String::trim)
                                 .anyMatch(watchedGenres::contains);
                })
                .limit(10) 
                .collect(Collectors.toList());

        return recommendations;
    }
}
