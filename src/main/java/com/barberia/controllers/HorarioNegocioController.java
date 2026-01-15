package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.horarioNegocio.CopiarHorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioResponse;
import com.barberia.models.enums.DiaSemana;
import com.barberia.services.HorarioNegocioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horariosNegocio")
public class HorarioNegocioController {
    private final HorarioNegocioService horarioNegocioService;

    public HorarioNegocioController(HorarioNegocioService horarioNegocioService) {
        this.horarioNegocioService = horarioNegocioService;
    }

    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioNegocioResponse>> create(@Valid @RequestBody HorarioNegocioRequest request) {
        try {
            HorarioNegocioResponse horarioNegocioResponse = horarioNegocioService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(201)
                            .success(true)
                            .message("Horario Negocio creado exitosamente")
                            .data(horarioNegocioResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al crear el horario negocio: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('READ_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioNegocioResponse>> findById(@PathVariable Long id){
        try {
            HorarioNegocioResponse horarioNegocioResponse = horarioNegocioService.findById(id);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Horario Negocio encontrado")
                            .data(horarioNegocioResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(404)
                            .success(false)
                            .message("Error al encontrar el horario negocio: " + e.getMessage())
                            .build()
            );

        }
    }

    @GetMapping // Cambiar la ruta para evitar conflicto
    @PreAuthorize("hasRole('ADMIN') or  hasAuthority('READ_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<List<HorarioNegocioResponse>>> findAll(
            @RequestParam(required = false) Long negocioId,
            @RequestParam(required = false) DiaSemana diaSemana) {
        try {
            List<HorarioNegocioResponse> horariosNegocio = horarioNegocioService.findAll(negocioId, diaSemana);
            return ResponseEntity.ok(
                    ApiResponse.<List<HorarioNegocioResponse>>builder()
                            .code(200)
                            .success(true)
                            .message("Horarios Negocio encontrados")
                            .data(horariosNegocio)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<HorarioNegocioResponse>>builder()
                            .code(400)
                            .success(false)
                            .message("Error al obtener los horarios negocio: " + e.getMessage())
                            .build()
            );
        }
    }


    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioNegocioResponse>> update(@PathVariable Long id, @Valid @RequestBody HorarioNegocioRequest request) {
        try {
            HorarioNegocioResponse horarioNegocioResponse = horarioNegocioService.update(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Horario Negocio actualizado exitosamente")
                            .data(horarioNegocioResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el horario negocio: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioNegocioResponse>> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequestGlobal request) {
        try {
            HorarioNegocioResponse horarioNegocioResponse = horarioNegocioService.cambiarEstado(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Horario Negocio actualizado exitosamente")
                            .data(horarioNegocioResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el horario negocio: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioNegocioResponse>> eliminar(@PathVariable Long id) {
        try {
            HorarioNegocioResponse horarioNegocioResponse = horarioNegocioService.eliminar(id);
            return ResponseEntity.ok(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Horario Negocio eliminado exitosamente")
                            .data(horarioNegocioResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HorarioNegocioResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al eliminar el horario negocio: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/copiarHorarios")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<String>> copiarHorarios(@RequestBody CopiarHorarioNegocioRequest request) {
        try {
            horarioNegocioService.copiarHorarios(request);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .success(true)
                            .message("Horarios copiados exitosamente")
                            .data("Copia completada")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(400)
                            .success(false)
                            .message("Error al copiar los horarios: " + e.getMessage())
                            .build()
            );
        }
    }
}
