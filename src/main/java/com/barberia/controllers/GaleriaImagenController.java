package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.models.GaleriaImagen;
import com.barberia.services.GaleriaImagenService;
import com.barberia.services.common.SecurityContextService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar la galería de imágenes del negocio.
 * Las imágenes se muestran en la sección "Galería" de la web pública.
 *
 * SEGURIDAD: Requiere ADMIN o permiso UPDATE_NEGOCIOS
 *
 * ENDPOINTS:
 * - GET    /api/negocios/galeria         → Listar imágenes del negocio actual
 * - POST   /api/negocios/galeria         → Subir nueva imagen
 * - DELETE /api/negocios/galeria/{id}    → Eliminar imagen
 * - PUT    /api/negocios/galeria/orden   → Reordenar imágenes
 */
@RestController
@RequestMapping("/api/negocios/galeria")
public class GaleriaImagenController {

    private final GaleriaImagenService galeriaImagenService;
    private final SecurityContextService securityContextService;

    public GaleriaImagenController(
            GaleriaImagenService galeriaImagenService,
            SecurityContextService securityContextService) {
        this.galeriaImagenService = galeriaImagenService;
        this.securityContextService = securityContextService;
    }

    /**
     * DTO simple para la respuesta de galería
     */
    public record GaleriaImagenResponse(Long id, String imagenUrl, Integer orden) {}

    /**
     * Listar todas las imágenes de galería del negocio actual
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_NEGOCIOS')")
    public ResponseEntity<ApiResponse<List<GaleriaImagenResponse>>> listar() {
        try {
            Long negocioId = securityContextService.getNegocioIdFromContext();
            List<GaleriaImagen> imagenes = galeriaImagenService.findByNegocioId(negocioId);

            List<GaleriaImagenResponse> data = imagenes.stream()
                    .map(img -> new GaleriaImagenResponse(img.getId(), img.getImagenUrl(), img.getOrden()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.<List<GaleriaImagenResponse>>builder()
                    .code(200)
                    .success(true)
                    .message("Imágenes de galería encontradas")
                    .data(data)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<GaleriaImagenResponse>>builder()
                    .code(400)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Subir una nueva imagen a la galería
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_NEGOCIOS')")
    public ResponseEntity<ApiResponse<GaleriaImagenResponse>> subir(
            @RequestPart("imagen") MultipartFile imagen) {
        try {
            Long negocioId = securityContextService.getNegocioIdFromContext();
            GaleriaImagen nueva = galeriaImagenService.upload(negocioId, imagen);

            GaleriaImagenResponse data = new GaleriaImagenResponse(
                    nueva.getId(), nueva.getImagenUrl(), nueva.getOrden());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<GaleriaImagenResponse>builder()
                            .code(201)
                            .success(true)
                            .message("Imagen subida exitosamente")
                            .data(data)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<GaleriaImagenResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al subir imagen: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Eliminar una imagen de la galería
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_NEGOCIOS')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        try {
            Long negocioId = securityContextService.getNegocioIdFromContext();
            galeriaImagenService.delete(negocioId, id);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .success(true)
                    .message("Imagen eliminada exitosamente")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al eliminar imagen: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Reordenar las imágenes de la galería
     * Enviar array de IDs en el nuevo orden deseado
     */
    @PutMapping("/orden")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_NEGOCIOS')")
    public ResponseEntity<ApiResponse<Void>> reordenar(@RequestBody List<Long> imagenesIds) {
        try {
            Long negocioId = securityContextService.getNegocioIdFromContext();
            galeriaImagenService.reordenar(negocioId, imagenesIds);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(200)
                    .success(true)
                    .message("Orden actualizado exitosamente")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al reordenar: " + e.getMessage())
                    .build());
        }
    }
}
