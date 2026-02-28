package com.barberia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuración para servir archivos estáticos (imágenes, logos, etc.)
 * 
 * FUNCIONALIDAD:
 * ══════════════════════════════════════════════════════════════════════════════
 * 
 * Permite acceder a archivos guardados localmente mediante URLs:
 * - GET http://localhost:8080/uploads/logos/uuid-logo.png
 * 
 * VENTAJAS:
 * - No requiere controlador adicional
 * - Manejo eficiente de archivos estáticos por Spring
 * - Soporte para caché automático
 * 
 * MIGRACIÓN A LA NUBE:
 * Cuando se migre a S3/Cloudinary, esta configuración puede deshabilitarse
 * ya que las URLs serán externas (https://s3.amazonaws.com/...)
 * 
 * @author Barberia Team
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convertir ruta relativa a absoluta
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();

        // Configurar handler para servir archivos desde /uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath + "/")
                .setCachePeriod(3600); // Cache de 1 hora
    }
}
