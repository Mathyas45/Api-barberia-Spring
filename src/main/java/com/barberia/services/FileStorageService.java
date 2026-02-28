package com.barberia.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para gestión de archivos (imágenes, documentos, etc.)
 * 
 * ARQUITECTURA ESCALABLE:
 * ══════════════════════════════════════════════════════════════════════════════
 * 
 * IMPLEMENTACIÓN ACTUAL:
 * - Almacenamiento local en carpeta 'uploads/'
 * - URL relativa guardada en BD
 * - Ideal para desarrollo y servidores pequeños
 * 
 * MIGRACIÓN FUTURA A LA NUBE:
 * - Cambiar implementación a AWS S3, Google Cloud Storage, Cloudinary, etc.
 * - Solo modificar este servicio sin tocar controllers ni services
 * - Las URLs en BD seguirán funcionando (cambiarán de relativas a absolutas)
 * 
 * BUENAS PRÁCTICAS:
 * - Validación de tipos de archivo
 * - Nombres únicos con UUID para evitar colisiones
 * - Limpieza de archivos antiguos al actualizar
 * 
 * @author Barberia Team
 */
@Service
public class FileStorageService {

    private final Path uploadDir;
    private final Path logoDir;
    private final Path heroDir;
    private final Path galeriaDir;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDirectory) {
        this.uploadDir = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.logoDir = this.uploadDir.resolve("logos");
        this.heroDir = this.uploadDir.resolve("hero");
        this.galeriaDir = this.uploadDir.resolve("galeria");
        
        try {
            Files.createDirectories(this.uploadDir);
            Files.createDirectories(this.logoDir);
            Files.createDirectories(this.heroDir);
            Files.createDirectories(this.galeriaDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento: " + e.getMessage());
        }
    }

    /**
     * Guarda un logo y retorna la URL relativa
     * 
     * @param file Archivo MultipartFile
     * @return URL relativa del archivo (ej: "/uploads/logos/uuid-logo.png")
     */
    public String saveLogo(MultipartFile file) {
        return saveImage(file, "logos");
    }

    /**
     * Guarda una imagen de hero y retorna la URL relativa
     * 
     * @param file Archivo MultipartFile
     * @return URL relativa del archivo (ej: "/uploads/hero/uuid.jpg")
     */
    public String saveHero(MultipartFile file) {
        return saveImage(file, "hero");
    }

    /**
     * Guarda una imagen de galería y retorna la URL relativa
     * 
     * @param file Archivo MultipartFile
     * @return URL relativa del archivo (ej: "/uploads/galeria/uuid.jpg")
     */
    public String saveGaleria(MultipartFile file) {
        return saveImage(file, "galeria");
    }

    /**
     * Método genérico para guardar una imagen en cualquier subcarpeta
     * 
     * @param file Archivo MultipartFile
     * @param folder Subcarpeta dentro de uploads (ej: "logos", "hero", "galeria")
     * @return URL relativa del archivo
     */
    public String saveImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        // Validar tamaño (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("El archivo no puede superar 5MB");
        }

        try {
            // Crear directorio si no existe
            Path targetDir = this.uploadDir.resolve(folder);
            Files.createDirectories(targetDir);

            // Generar nombre único
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path targetLocation = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retornar URL relativa
            return "/uploads/" + folder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina un archivo del sistema
     * 
     * @param fileUrl URL relativa del archivo (ej: "/uploads/logos/uuid-logo.png")
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Construir ruta completa desde la URL relativa
            // fileUrl = "/uploads/logos/uuid.png" → "logos/uuid.png"
            String relativePath = fileUrl.startsWith("/uploads/") 
                    ? fileUrl.substring("/uploads/".length()) 
                    : fileUrl;
            Path filePath = this.uploadDir.resolve(relativePath);
            
            // Eliminar si existe
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error pero no fallar (el archivo podría no existir)
            System.err.println("No se pudo eliminar el archivo: " + e.getMessage());
        }
    }

    /**
     * Valida si un archivo es una imagen válida
     */
    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/webp")
        );
    }

    /**
     * Obtiene la ruta completa del directorio de logos
     * Útil para servir archivos estáticos
     */
    public Path getLogoDirectory() {
        return this.logoDir;
    }

    /**
     * Obtiene la ruta completa del directorio de uploads
     */
    public Path getUploadDirectory() {
        return this.uploadDir;
    }
}
