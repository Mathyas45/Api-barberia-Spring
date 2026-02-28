package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.EstadoRegistroRequest;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.servicio.ServicioRequest;
import com.barberia.dto.servicio.ServicioResponse;
import com.barberia.dto.servicio.ServicioUpdateRequest;
import com.barberia.services.ServicioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    @Autowired
    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('CREATE_SERVICIOS')")
    public ResponseEntity<ApiResponse<ServicioResponse>> create(
            @Valid @RequestPart("servicio") ServicioRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        try {
            ServicioResponse servicioResponse = servicioService.create(request, imagen);
            ApiResponse<ServicioResponse> response = ApiResponse.<ServicioResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Servicio creado exitosamente")
                    .data(servicioResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<ServicioResponse> response = ApiResponse.<ServicioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al crear el servicio: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);

        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('READ_SERVICIOS')")
    public ResponseEntity<ApiResponse<ServicioResponse>> findById(@PathVariable Long id){
        try {
            ServicioResponse servicioResponse = servicioService.findById(id);
            ApiResponse<ServicioResponse> response = ApiResponse.<ServicioResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Servicio encontrado")
                    .data(servicioResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ServicioResponse> response = ApiResponse.<ServicioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al encontrar el servicio: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_SERVICIOS')")
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> findAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoriaId) {
        try {
            List<ServicioResponse> servicioResponses = servicioService.findAll(query, categoriaId);
            return ResponseEntity.ok(ApiResponse.<List<ServicioResponse>>builder()
                    .code(201)
                    .success(true)
                    .message("Servicios encontrados")
                    .data(servicioResponses)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<List<ServicioResponse>>builder()
                    .code(400)
                    .success(false)
                    .message("Error al listar Servicios: " + e.getMessage())
                    .build());
        }
    }
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_SERVICIOS')")
    public ResponseEntity<ApiResponse<ServicioResponse>> update(
            @PathVariable Long id,
            @Valid @RequestPart("servicio") ServicioUpdateRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        try {
            ServicioResponse response = servicioService.update(id, request, imagen);
            return ResponseEntity.ok(ApiResponse.<ServicioResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Servicio actualizado")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ServicioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al actualizar el servicio: " + e.getMessage())
                    .build());

        }
    }
    @PatchMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_SERVICIOS')")
    public ResponseEntity<ApiResponse<ServicioResponse>> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRegistroRequest request) {
        try {
            ServicioResponse response = servicioService.cambiarEstado(id, request);
            return ResponseEntity.ok(ApiResponse.<ServicioResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Estado del servicio actualizado")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ServicioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al actualizar el estado del servicio: " + e.getMessage())
                    .build());

        }
    }

    @PutMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_SERVICIOS')")
    public ResponseEntity<ApiResponse<ServicioResponse>> eliminar(@PathVariable Long id) {
        try {
            ServicioResponse response = servicioService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.<ServicioResponse>builder()
                    .code(201)
                    .success(true)
                    .message("Estado del servicio actualizado")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ServicioResponse>builder()
                    .code(400)
                    .success(false)
                    .message("Error al actualizar el estado del servicio: " + e.getMessage())
                    .build());

        }
    }



}

