package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.negocio.BusinessPublicResponse;
import com.barberia.services.PublicBusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador PÚBLICO para la web multi-tenant.
 *
 * SEGURIDAD: NO requiere autenticación.
 * La ruta /api/public/** está permitida en SecurityConfig.java
 *
 * ENDPOINT:
 *   GET /api/public/business/{slug}
 *
 * Retorna todos los datos necesarios para renderizar la web pública
 * de un negocio: servicios, profesionales, horarios, galería, colores, etc.
 */
@RestController
@RequestMapping("/api/public")
public class PublicBusinessController {

    private final PublicBusinessService publicBusinessService;

    public PublicBusinessController(PublicBusinessService publicBusinessService) {
        this.publicBusinessService = publicBusinessService;
    }

    /**
     * Obtener datos públicos de un negocio por su slug
     *
     * @param slug Slug único del negocio (ej: "barberia-el-estilo")
     * @return BusinessPublicResponse con todos los datos para la web pública
     *
     * Ejemplo: GET /api/public/business/barberia-el-estilo
     */
    @GetMapping("/business/{slug}")
    public ResponseEntity<ApiResponse<BusinessPublicResponse>> getBusinessBySlug(
            @PathVariable String slug) {

        BusinessPublicResponse business = publicBusinessService.getBySlug(slug);

        if (business == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.<BusinessPublicResponse>builder()
                            .code(404)
                            .success(false)
                            .message("Negocio no encontrado con slug: " + slug)
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponse.<BusinessPublicResponse>builder()
                        .code(200)
                        .success(true)
                        .message("Negocio encontrado")
                        .data(business)
                        .build()
        );
    }
}
