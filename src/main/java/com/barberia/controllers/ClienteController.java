package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.dto.cliente.ClienteImportRequest;
import com.barberia.dto.cliente.ClienteImportResponse;
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
     * Importar clientes desde Excel (importación masiva)
     * 
     * Recibe una lista de clientes y los inserta uno por uno.
     * Retorna un resumen con los éxitos y errores.
     * 
     * SEGURIDAD:
     * - ADMIN: Puede importar siempre
     * - Otros roles: Necesitan el permiso CREATE_CLIENTS
     * 
     * Uso desde Angular:
     * 1. Leer el Excel con xlsx o similar
     * 2. Convertir filas a array de objetos
     * 3. POST a /api/clientes/import con { clientes: [...] }
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_CLIENTS')")
    public ResponseEntity<ApiResponse<ClienteImportResponse>> importarClientes(
            @Valid @RequestBody ClienteImportRequest request) {
        try {
            ClienteImportResponse resultado = clienteService.importarClientes(request.getClientes());
            
            String mensaje = String.format("Importación completada: %d insertados, %d errores",
                    resultado.getTotalInsertados(), resultado.getTotalErrores());
            
            return ResponseEntity.ok(
                    ApiResponse.<ClienteImportResponse>builder()
                            .code(200)
                            .success(true)
                            .message(mensaje)
                            .data(resultado)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ClienteImportResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error en la importación: " + e.getMessage())
                            .build()
            );
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
     * 
     * MULTI-TENANT:
     * - El negocioId se obtiene automáticamente del JWT
     * - No es necesario enviarlo desde el frontend (más seguro)
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


    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClienteResponse>> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequestGlobal request) {
        try {
            ClienteResponse clienteResponse = clienteService.cambiarEstado(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<ClienteResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Estado del Cliente actualizado exitosamente")
                            .data(clienteResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ClienteResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el estado del Cliente: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Eliminar cliente
     * 
     * SEGURIDAD:
     * - Solo ADMIN puede eliminar
     * - Esta operación es crítica, por eso solo ADMIN
     */
    @PutMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClienteResponse>> eliminar(@PathVariable Long id) {
        try {
            ClienteResponse clienteResponse = clienteService.eliminar(id);
            return ResponseEntity.ok(
                    ApiResponse.<ClienteResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Estado del Cliente actualizado exitosamente")
                            .data(clienteResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ClienteResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el estado del Cliente: " + e.getMessage())
                            .build()
            );
        }
    }

}
