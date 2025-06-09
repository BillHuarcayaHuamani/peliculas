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
import com.peliculas.peliculas.repository.FavoritosRepository;
import com.peliculas.peliculas.repository.PeliculaRepository; 

@Service
public class FavoritosService {

    private final FavoritosRepository favoritosRepository;
    private final PeliculaRepository peliculaRepository; 

    public FavoritosService(FavoritosRepository favoritosRepository, PeliculaRepository peliculaRepository) {
        this.favoritosRepository = favoritosRepository;
        this.peliculaRepository = peliculaRepository;
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

    public byte[] generateExcel(List<Favoritos> favoritos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Películas Favoritas");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Favorito", "ID Usuario", "ID Película", "Título Película", "Género", "Año"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Favoritos favorito : favoritos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(favorito.getId());
                row.createCell(1).setCellValue(favorito.getUsuarioId());
                row.createCell(2).setCellValue(favorito.getPeliculaId());

                Optional<Pelicula> optionalPelicula = peliculaRepository.findById(favorito.getPeliculaId());
                if (optionalPelicula.isPresent()) {
                    Pelicula pelicula = optionalPelicula.get();
                    row.createCell(3).setCellValue(pelicula.getTitulo());
                    row.createCell(4).setCellValue(pelicula.getGenero());
                    row.createCell(5).setCellValue(Optional.ofNullable(pelicula.getAnio()).orElse(0));
                } else {
                    row.createCell(3).setCellValue("Película no encontrada");
                    row.createCell(4).setCellValue("");
                    row.createCell(5).setCellValue("");
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
