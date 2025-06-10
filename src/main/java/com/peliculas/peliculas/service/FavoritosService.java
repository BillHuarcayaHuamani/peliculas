package com.peliculas.peliculas.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell; 
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.peliculas.peliculas.model.Favoritos;
import com.peliculas.peliculas.model.Pelicula;
import com.peliculas.peliculas.model.Usuario; 
import com.peliculas.peliculas.repository.FavoritosRepository;
import com.peliculas.peliculas.repository.PeliculaRepository;
import com.peliculas.peliculas.repository.UsuarioRepository; 

@Service
public class FavoritosService {

    private final FavoritosRepository favoritosRepository;
    private final PeliculaRepository peliculaRepository; 
    private final UsuarioRepository usuarioRepository; 

    public FavoritosService(FavoritosRepository favoritosRepository, 
                            PeliculaRepository peliculaRepository,
                            UsuarioRepository usuarioRepository) { 
        this.favoritosRepository = favoritosRepository;
        this.peliculaRepository = peliculaRepository;
        this.usuarioRepository = usuarioRepository; 
    }

    public boolean toggleFavorite(Long usuarioId, Long peliculaId) {
        Optional<Favoritos> existingFavorite = favoritosRepository.findByUsuarioIdAndPeliculaId(usuarioId, peliculaId);
        if (existingFavorite.isPresent()) {
            favoritosRepository.delete(existingFavorite.get());
            return false; 
        } else {
            Favoritos newFavorite = new Favoritos(usuarioId, peliculaId);
            favoritosRepository.save(newFavorite);
            return true; 
        }
    }

    public boolean isFavorited(Long usuarioId, Long peliculaId) {
        return favoritosRepository.existsByUsuarioIdAndPeliculaId(usuarioId, peliculaId);
    }

    public List<Favoritos> getFavoritosByUsuario(Long usuarioId) {
        return favoritosRepository.findByUsuarioId(usuarioId);
    }

    public List<Favoritos> getAllFavoritos() {
        return favoritosRepository.findAll();
    }

    public byte[] generateExcel(List<Favoritos> favoritos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Películas Favoritas");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Favorito", "ID Usuario", "Nombre Usuario", "ID Película", "Título Película", "Género", "Año"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Favoritos favorito : favoritos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(favorito.getId());
                row.createCell(1).setCellValue(favorito.getUsuarioId());
                
                Optional<Usuario> optionalUsuario = usuarioRepository.findById(favorito.getUsuarioId());
                String nombreUsuario = optionalUsuario.map(u -> u.getNombres() + " " + u.getApellidos()).orElse("Usuario Desconocido");
                row.createCell(2).setCellValue(nombreUsuario);

                row.createCell(3).setCellValue(favorito.getPeliculaId());

                Optional<Pelicula> optionalPelicula = peliculaRepository.findById(favorito.getPeliculaId());
                if (optionalPelicula.isPresent()) {
                    Pelicula pelicula = optionalPelicula.get();
                    row.createCell(4).setCellValue(pelicula.getTitulo());
                    row.createCell(5).setCellValue(pelicula.getGenero());
                    row.createCell(6).setCellValue(Optional.ofNullable(pelicula.getAnio()).orElse(0));
                } else {
                    row.createCell(4).setCellValue("Película no encontrada");
                    row.createCell(5).setCellValue("");
                    row.createCell(6).setCellValue("");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
