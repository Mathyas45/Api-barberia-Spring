package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.HorarioProfesional.HorarioProfesionalRequest;
import com.barberia.dto.HorarioProfesional.HorarioProfesionalResponse;
import com.barberia.models.enums.DiaSemana;
import com.barberia.services.HorarioProfesionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horariosProfesionales")
public class HorarioProfesionalController {
    private final HorarioProfesionalService horarioProfesionalService;

    public HorarioProfesionalController(HorarioProfesionalService horarioProfesionalService) {
        this.horarioProfesionalService = horarioProfesionalService;
    }

    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_PROFESSIONAL_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioProfesionalResponse>> create(@Valid @RequestBody HorarioProfesionalRequest request) {
        try {
            HorarioProfesionalResponse horarioProfesionalResponse = horarioProfesionalService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(201)
                            .success(true)
                            .message("Horario Profesional creado exitosamente")
                            .data(horarioProfesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al crear el horario profesional: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('READ_PROFESSIONAL_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioProfesionalResponse>> findById(@PathVariable Long id){
        try {
            HorarioProfesionalResponse horarioProfesionalResponse = horarioProfesionalService.findById(id);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Horario Profesional encontrado")
                            .data(horarioProfesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(404)
                            .success(false)
                            .message("Error al encontrar el horario profesional: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping // Cambiar la ruta para evitar conflicto
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('READ_PROFESSIONAL_SCHEDULES')")
    public ResponseEntity<ApiResponse<List<HorarioProfesionalResponse>>> findAll(@RequestParam(required = false) Long profesionalId, @RequestParam(required = false) DiaSemana diaSemana) {
        try {
            List<HorarioProfesionalResponse> horariosProfesionales = horarioProfesionalService.findAll(profesionalId, diaSemana);
            return ResponseEntity.ok(
                    ApiResponse.<List<HorarioProfesionalResponse>>builder()
                            .code(200)
                            .success(true)
                            .message("Horarios Profesionales encontrados")
                            .data(horariosProfesionales)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<HorarioProfesionalResponse>>builder()
                            .code(400)
                            .success(false)
                            .message("Error al encontrar los horarios profesionales: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_PROFESSIONAL_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioProfesionalResponse>> update(@PathVariable Long id, @Valid @RequestBody HorarioProfesionalRequest request) {
        try {
            HorarioProfesionalResponse horarioProfesionalResponse = horarioProfesionalService.update(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Horario Profesional actualizado exitosamente")
                            .data(horarioProfesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el horario profesional: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_PROFESSIONAL_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioProfesionalResponse>> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequestGlobal request) {
        try {
            HorarioProfesionalResponse horarioProfesionalResponse = horarioProfesionalService.cambiarEstado(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Estado del Horario Profesional actualizado exitosamente")
                            .data(horarioProfesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el estado del horario profesional: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_PROFESSIONAL_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioProfesionalResponse>> eliminar(@PathVariable Long id) {
        try {
            HorarioProfesionalResponse horarioProfesionalResponse = horarioProfesionalService.eliminar(id);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Horario Profesional eliminado exitosamente")
                            .data(horarioProfesionalResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioProfesionalResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al eliminar el horario profesional: " + e.getMessage())
                            .build()
            );
        }
    }


}
