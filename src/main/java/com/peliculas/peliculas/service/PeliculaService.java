// src/main/java/com/peliculas/peliculas/service/PeliculaService.java
package com.peliculas.peliculas.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.peliculas.peliculas.model.Pelicula; // Importa Optional
import com.peliculas.peliculas.repository.PeliculaRepository;

@Service
public class PeliculaService {

    private final String UPLOAD_DIR_PORTADAS = "src/main/resources/static/uploads/peliculas/portadas/";
    private final String UPLOAD_DIR_VIDEOS = "src/main/resources/static/uploads/peliculas/videos/";

    private final PeliculaRepository peliculaRepository;

    public PeliculaService(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
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
}