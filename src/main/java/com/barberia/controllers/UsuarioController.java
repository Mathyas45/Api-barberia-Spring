package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.usuario.UsuarioRequest;
import com.barberia.dto.usuario.UsuarioResponse;
import com.barberia.dto.usuario.UsuarioUpdateRequest;
import com.barberia.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Usuarios con seguridad basada en Roles y Permisos
 * 
 * SEGURIDAD:
 * - Solo usuarios con rol ADMIN o permiso CREATE_USERS pueden crear usuarios
 * - Solo usuarios con rol ADMIN o permiso READ_USERS pueden listar usuarios
 * - Solo usuarios con rol ADMIN o permiso UPDATE_USERS pueden actualizar usuarios
 * - Solo usuarios con rol ADMIN o permiso DELETE_USERS pueden eliminar usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Crear usuario
     * SEGURIDAD: Requiere rol ADMIN o permiso CREATE_USERS
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_USERS')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(
            @Valid @RequestBody UsuarioRequest request) {
        try {
            UsuarioResponse usuarioResponse = usuarioService.create(request);
            ApiResponse<UsuarioResponse> response = ApiResponse.<UsuarioResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Usuario creado exitosamente")
                    .data(usuarioResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<UsuarioResponse> response = ApiResponse.<UsuarioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al crear el usuario: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtener usuario por ID
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_USERS
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_USERS')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> findById(@PathVariable Long id) {
        try {
            UsuarioResponse usuarioResponse = usuarioService.findById(id);
            ApiResponse<UsuarioResponse> response = ApiResponse.<UsuarioResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Usuario encontrado")
                    .data(usuarioResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UsuarioResponse> response = ApiResponse.<UsuarioResponse>builder()
                    .code(404)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Listar todos los usuarios con búsqueda opcional
     * SEGURIDAD: Requiere rol ADMIN o permiso READ_USERS
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_USERS')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> findAll(
            @RequestParam(required = false) String query) {
        try {
            List<UsuarioResponse> usuarios = usuarioService.findAll(query);
            ApiResponse<List<UsuarioResponse>> response = ApiResponse.<List<UsuarioResponse>>builder()
                    .code(200)
                    .success(true)
                    .message("Usuarios encontrados")
                    .data(usuarios)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<UsuarioResponse>> response = ApiResponse.<List<UsuarioResponse>>builder()
                    .code(400)
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Actualizar usuario
     * SEGURIDAD: Requiere rol ADMIN o permiso UPDATE_USERS
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_USERS')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        try {
            UsuarioResponse usuarioResponse = usuarioService.update(id, request);
            ApiResponse<UsuarioResponse> response = ApiResponse.<UsuarioResponse>builder()
                    .code(200)
                    .success(true)
                    .message("Usuario actualizado exitosamente")
                    .data(usuarioResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UsuarioResponse> response = ApiResponse.<UsuarioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al actualizar el usuario: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Eliminar usuario (soft delete)
     * SEGURIDAD: Requiere rol ADMIN o permiso DELETE_USERS
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_USERS')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            usuarioService.delete(id);
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(200)
                    .success(true)
                    .message("Usuario eliminado exitosamente")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al eliminar el usuario: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Activar usuario
     * SEGURIDAD: Requiere rol ADMIN o permiso UPDATE_USERS
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_USERS')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        try {
            usuarioService.activate(id);
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(200)
                    .success(true)
                    .message("Usuario activado exitosamente")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al activar el usuario: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
