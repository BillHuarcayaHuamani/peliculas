package com.peliculas.peliculas.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.peliculas.peliculas.model.ProgresoVisualizacion;
import com.peliculas.peliculas.repository.ProgresoVisualizacionRepository;

@Service
public class ProgresoVisualizacionService {

    private final ProgresoVisualizacionRepository progresoVisualizacionRepository;

    public ProgresoVisualizacionService(ProgresoVisualizacionRepository progresoVisualizacionRepository) {
        this.progresoVisualizacionRepository = progresoVisualizacionRepository;
    }

    public ProgresoVisualizacion guardarOActualizarProgreso(Long usuarioId, Long peliculaId, Float posicion) {
        Optional<ProgresoVisualizacion> existingProgress = progresoVisualizacionRepository.findByUsuarioIdAndPeliculaId(usuarioId, peliculaId);

        ProgresoVisualizacion progreso;
        if (existingProgress.isPresent()) {
            progreso = existingProgress.get();
            progreso.setUltimaPosicion(posicion);
            progreso.setUltimaActualizacion(LocalDateTime.now());
        } else {
            progreso = new ProgresoVisualizacion(usuarioId, peliculaId, posicion);
        }
        return progresoVisualizacionRepository.save(progreso);
    }

    public Float obtenerUltimaPosicion(Long usuarioId, Long peliculaId) {
        return progresoVisualizacionRepository.findByUsuarioIdAndPeliculaId(usuarioId, peliculaId)
                .map(ProgresoVisualizacion::getUltimaPosicion)
                .orElse(0.0f);
    }
}
