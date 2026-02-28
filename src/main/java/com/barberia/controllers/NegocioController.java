package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.negocio.NegocioRequest;
import com.barberia.dto.negocio.NegocioResponse;
import com.barberia.dto.negocio.NegocioUpdateRequest;
import com.barberia.services.NegocioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador de Negocios con seguridad basada en Roles
 * 
 * SEGURIDAD:
 * - Solo usuarios con rol ADMIN pueden gestionar negocios
 * - Este controlador es para super administradores del sistema
 */
@RestController
@RequestMapping("/api/negocios")
public class NegocioController {

    private final NegocioService negocioService;

    public NegocioController(NegocioService negocioService) {
        this.negocioService = negocioService;
    }

    /**
     * Crear negocio
     * SEGURIDAD: Requiere rol ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NegocioResponse>> create(
            @Valid @RequestBody NegocioRequest request) {
        try {
            NegocioResponse negocioResponse = negocioService.create(request);
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Negocio creado exitosamente")
                    .data(negocioResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al crear el negocio: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtener negocio por ID
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_NEGOCIOS
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_NEGOCIOS')")
    public ResponseEntity<ApiResponse<NegocioResponse>> findById(@PathVariable Long id) {
        try {
            NegocioResponse negocioResponse = negocioService.findById(id);
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Negocio encontrado")
                    .data(negocioResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(404)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Listar todos los negocios con búsqueda opcional
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_NEGOCIOS
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NegocioResponse>>> findAll(

            @RequestParam(required = false) String query) {
        try {
            List<NegocioResponse> negocios = negocioService.findAll(query);
            ApiResponse<List<NegocioResponse>> response = ApiResponse.<List<NegocioResponse>>builder()
                    .code(200)
                    .success(true)
                    .message("Negocios encontrados")
                    .data(negocios)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<NegocioResponse>> response = ApiResponse.<List<NegocioResponse>>builder()
                    .code(400)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Actualizar configuración completa del negocio (con o sin logo/hero)
     * SEGURIDAD: Requiere rol ADMIN
     * 
     * IMPORTANTE: Este endpoint acepta multipart/form-data
     * - 'negocio': JSON con los datos (required)
     * - 'logo': archivo de imagen del logo (opcional)
     * - 'heroImage': archivo de imagen del hero/portada (opcional)
     * 
     * El logo y heroImage son OPCIONALES - solo se actualizan si se envían
     */
    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_NEGOCIOS')")
    public ResponseEntity<ApiResponse<NegocioResponse>> update(
            @PathVariable Long id,
            @RequestPart("negocio") @Valid NegocioUpdateRequest updateRequest,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "heroImage", required = false) MultipartFile heroImage) {
        try {
            NegocioResponse negocioResponse = negocioService.updateConfiguracion(id, updateRequest, logo, heroImage);
            
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Negocio actualizado exitosamente")
                    .data(negocioResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al actualizar el negocio: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Desactivar negocio
     * SEGURIDAD: Requiere rol ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            negocioService.delete(id);
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(200)
                    .success(true)
                    .message("Negocio desactivado exitosamente")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al desactivar el negocio: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Activar negocio
     * SEGURIDAD: Requiere rol ADMIN
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        try {
            negocioService.activate(id);
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(200)
                    .success(true)
                    .message("Negocio activado exitosamente")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al activar el negocio: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Eliminar logo del negocio
     * SEGURIDAD: Requiere rol ADMIN
     */
    @DeleteMapping("/{id}/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NegocioResponse>> deleteLogo(@PathVariable Long id) {
        try {
            NegocioResponse negocioResponse = negocioService.deleteLogo(id);
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Logo eliminado exitosamente")
                    .data(negocioResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<NegocioResponse> response = ApiResponse.<NegocioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al eliminar el logo: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
