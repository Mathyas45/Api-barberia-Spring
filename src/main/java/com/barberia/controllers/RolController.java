package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.rol.RolRequest;
import com.barberia.dto.rol.RolResponse;
import com.barberia.services.RolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Roles con seguridad basada en Roles y Permisos
 * 
 * SEGURIDAD:
 * - Solo usuarios con rol ADMIN pueden gestionar roles
 */
@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    /**
     * Crear rol
     * SEGURIDAD: Requiere rol ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RolResponse>> create(
            @Valid @RequestBody RolRequest request) {
        try {
            RolResponse rolResponse = rolService.create(request);
            ApiResponse<RolResponse> response = ApiResponse.<RolResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Rol creado exitosamente")
                    .data(rolResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<RolResponse> response = ApiResponse.<RolResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al crear el rol: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtener rol por ID
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_ROLES
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_ROLES')")
    public ResponseEntity<ApiResponse<RolResponse>> findById(@PathVariable Long id) {
        try {
            RolResponse rolResponse = rolService.findById(id);
            ApiResponse<RolResponse> response = ApiResponse.<RolResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Rol encontrado")
                    .data(rolResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<RolResponse> response = ApiResponse.<RolResponse>builder()
                    .code(404)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Listar todos los roles con búsqueda opcional
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_ROLES
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_ROLES')")
    public ResponseEntity<ApiResponse<List<RolResponse>>> findAll(
            @RequestParam(required = false) String query) {
        try {
            List<RolResponse> roles = rolService.findAll(query);
            ApiResponse<List<RolResponse>> response = ApiResponse.<List<RolResponse>>builder()
                    .code(200)
                    .success(true)
                    .message("Roles encontrados")
                    .data(roles)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<RolResponse>> response = ApiResponse.<List<RolResponse>>builder()
                    .code(400)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Actualizar rol
     * SEGURIDAD: Requiere rol ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RolResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RolRequest request) {
        try {
            RolResponse rolResponse = rolService.update(id, request);
            ApiResponse<RolResponse> response = ApiResponse.<RolResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Rol actualizado exitosamente")
                    .data(rolResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<RolResponse> response = ApiResponse.<RolResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al actualizar el rol: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Eliminar rol
     * SEGURIDAD: Requiere rol ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            rolService.delete(id);
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(200)
                    .success(true)
                    .message("Rol eliminado exitosamente")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al eliminar el rol: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
