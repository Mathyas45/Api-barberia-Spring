package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.cliente.ClienteRequest;
import com.barberia.dto.cliente.ClienteRequestCliente;
import com.barberia.dto.cliente.ClienteResponse;
import com.barberia.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Clientes con seguridad basada en Roles y Permisos
 * 
 * IMPORTANTE: Cada endpoint está protegido con @PreAuthorize
 * 
 * TIPOS DE VALIDACIÓN:
 * 1. @PreAuthorize("hasRole('ADMIN')") - Solo usuarios con rol ADMIN
 * 2. @PreAuthorize("hasAuthority('CREATE_CLIENTS')") - Solo con permiso específico
 * 3. @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_CLIENTS')") - ADMIN o permiso
 * 
 * ¿CÓMO FUNCIONA?
 * - Spring Security intercepta la petición ANTES de ejecutar el método
 * - Extrae los roles/permisos del JWT (desde el token en header Authorization)
 * - Compara con la expresión @PreAuthorize
 * - Si no cumple → HTTP 403 Forbidden
 * - Si cumple → Ejecuta el método
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Crear cliente
     * 
     * SEGURIDAD:
     * - ADMIN: Puede crear siempre (tiene todos los permisos)
     * - Otros roles: Necesitan el permiso CREATE_CLIENTS
     * 
     * @PreAuthorize evalúa la expresión ANTES de ejecutar el método
     * Si retorna false → HTTP 403 Forbidden
     */
    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_CLIENTS')")
    public ResponseEntity<ApiResponse<ClienteResponse>> createClienteProtegido(
            @Valid @RequestBody ClienteRequest request) {
        try {
            ClienteResponse clienteResponse = clienteService.createClienteProtegido(request);
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Cliente creado exitosamente")
                    .data(clienteResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al crear el cliente: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping({"registerCliente"})
    public ResponseEntity<ApiResponse<ClienteResponse>> createCliente(
            @Valid @RequestBody ClienteRequestCliente request) {
        try {
            ClienteResponse clienteResponse = clienteService.create(request);
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Cliente creado exitosamente")
                    .data(clienteResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al crear el cliente: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }


    /**
     * Obtener cliente por ID
     * 
     * SEGURIDAD:
     * - ADMIN: Puede leer siempre
     * - Otros roles: Necesitan el permiso READ_CLIENTS
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_CLIENTS')")
    public ResponseEntity<ApiResponse<ClienteResponse>> findById(@PathVariable Long id){
        try {
            ClienteResponse response = clienteService.findById(id);
            return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Cliente encontrado")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ClienteResponse>builder()
                            .code(400)
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * Listar todos los clientes
     * 
     * SEGURIDAD:
     * - ADMIN: Puede listar siempre
     * - Otros roles: Necesitan el permiso READ_CLIENTS
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_CLIENTS')")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> findAll(@RequestParam(required = false) String query) {
        try {
            List<ClienteResponse> response = clienteService.findAll(query);
            return ResponseEntity.ok(ApiResponse.<List<ClienteResponse>>builder()
                    .code(201)
                    .success(true)
                    .message("Clientes encontrados")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<List<ClienteResponse>>builder()
                    .code(400)
                    .success(false)
                    .message("Error al listar clientes: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Actualizar cliente
     * 
     * SEGURIDAD:
     * - ADMIN: Puede actualizar siempre
     * - Otros roles: Necesitan el permiso UPDATE_CLIENTS
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_CLIENTS')")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        try {
            ClienteResponse clienteResponse = clienteService.update(id, request);
            ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Cliente actualizado exitosamente")
                    .data(clienteResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ClienteResponse>builder()
                    .code(400)
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * Eliminar cliente
     * 
     * SEGURIDAD:
     * - Solo ADMIN puede eliminar
     * - Esta operación es crítica, por eso solo ADMIN
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            clienteService.delete(id);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(201)
                    .success(true)
                    .message("Cliente eliminado exitosamente")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<Void>builder()
                    .code(400)
                    .success(false)
                    .message("Error al eliminar el cliente: " + e.getMessage())
                    .build());
        }
    }

}
