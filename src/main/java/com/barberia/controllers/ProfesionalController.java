package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.Profesional.ProfesionalRequest;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.services.ProfesionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {
    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }


    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<ProfesionalResponse>> create(@Valid @RequestBody ProfesionalRequest request) {
        try {
            ProfesionalResponse profesionalResponse = profesionalService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(201)
                            .success(true)
                            .message("Profesional creado exitosamente")
                            .data(profesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al crear el profesional: " + e.getMessage())
                            .build()
            );
        }
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('READ_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<ProfesionalResponse>> findById(@PathVariable Long id){
        try {
            ProfesionalResponse profesionalResponse = profesionalService.findById(id);
            return ResponseEntity.ok(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Profesional encontrado")
                            .data(profesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(404)
                            .success(false)
                            .message("Error al encontrar el profesional: " + e.getMessage())
                            .build()
            );
        }
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<List<ProfesionalResponse>>> findAll(@RequestParam(required = false) String query) {
        try {
            List<ProfesionalResponse> profesionalResponses = profesionalService.findAll(query);
            return ResponseEntity.ok(
                    ApiResponse.<List<ProfesionalResponse>>builder()
                            .code(200)
                            .success(true)
                            .message("Profesionales encontrados")
                            .data(profesionalResponses)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<ProfesionalResponse>>builder()
                            .code(500)
                            .success(false)
                            .message("Error al obtener los profesionales: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<ProfesionalResponse>> update(@PathVariable Long id, @Valid @RequestBody ProfesionalRequest request) {
        try {
            ProfesionalResponse profesionalResponse = profesionalService.update(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Profesional actualizado exitosamente")
                            .data(profesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el profesional: " + e.getMessage())
                            .build()
            );

        }
    }
    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<ProfesionalResponse>> cambiarEstado(@PathVariable Long id,    @Valid @RequestBody EstadoRequestGlobal request) {
        try {
            ProfesionalResponse profesionalResponse = profesionalService.cambiarEstado(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Estado del profesional actualizado exitosamente")
                            .data(profesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el estado del profesional: " + e.getMessage())
                            .build()
            );

        }
    }
    @PutMapping("/estado/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<ProfesionalResponse>> eliminar(@PathVariable Long id,    @Valid @RequestBody EstadoRequestGlobal request) {
        try {
            ProfesionalResponse profesionalResponse = profesionalService.cambiarEstado(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Estado del profesional actualizado exitosamente")
                            .data(profesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el estado del profesional: " + e.getMessage())
                            .build()
            );
        }

    }
}
