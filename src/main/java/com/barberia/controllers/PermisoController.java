package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.permiso.PermisoResponse;
import com.barberia.services.PermisoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Permisos con seguridad basada en Roles
 * 
 * SEGURIDAD:
 * - Solo usuarios con rol ADMIN pueden consultar permisos
 * 
 * NOTA: Los permisos se gestionan directamente desde la base de datos,
 * este controlador es solo para consulta
 */
@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    /**
     * Obtener permiso por ID
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_PERMISSIONS
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_PERMISSIONS')")
    public ResponseEntity<ApiResponse<PermisoResponse>> findById(@PathVariable Long id) {
        try {
            PermisoResponse permisoResponse = permisoService.findById(id);
            ApiResponse<PermisoResponse> response = ApiResponse.<PermisoResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Permiso encontrado")
                    .data(permisoResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<PermisoResponse> response = ApiResponse.<PermisoResponse>builder()
                    .code(404)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Listar todos los permisos con búsqueda opcional
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_PERMISSIONS
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_PERMISSIONS')")
    public ResponseEntity<ApiResponse<List<PermisoResponse>>> findAll(
            @RequestParam(required = false) String query) {
        try {
            List<PermisoResponse> permisos = permisoService.findAll(query);
            ApiResponse<List<PermisoResponse>> response = ApiResponse.<List<PermisoResponse>>builder()
                    .code(200)
                    .success(true)
                    .message("Permisos encontrados")
                    .data(permisos)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<PermisoResponse>> response = ApiResponse.<List<PermisoResponse>>builder()
                    .code(400)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
