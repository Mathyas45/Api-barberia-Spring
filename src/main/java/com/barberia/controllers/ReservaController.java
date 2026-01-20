package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.reserva.ReservaRequest;
import com.barberia.dto.reserva.ReservaResponse;
import com.barberia.services.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping({"/register"})
    public ResponseEntity<ApiResponse<ReservaResponse>> create(@Valid @RequestBody ReservaRequest request) {
        try {
            ReservaResponse reservaResponse = reservaService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(201)
                            .success(true)
                            .message("Reserva creada exitosamente")
                            .data(reservaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al crear la reserva: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping({"/register/admin"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_RESERVAS')")
    public ResponseEntity<ApiResponse<ReservaResponse>> createAdmin(@Valid @RequestBody ReservaRequest request) {
        try {
            ReservaResponse reservaResponse = reservaService.createAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(201)
                            .success(true)
                            .message("Reserva creada exitosamente por admin")
                            .data(reservaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al crear la reserva por admin: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_RESERVAS')")
    public ResponseEntity<ApiResponse<ReservaResponse>> findById(@PathVariable Long id){
        try {
            ReservaResponse reservaResponse = reservaService.findById(id);
            return ResponseEntity.ok(
                    ApiResponse.<ReservaResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Reserva encontrada")
                            .data(reservaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(404)
                            .success(false)
                            .message("Error al encontrar la reserva: " + e.getMessage())
                            .build()
            );
        }
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_RESERVAS')")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> findAll(
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String estado
    ){
        try {
            List<ReservaResponse> reservas = reservaService.findAll(profesionalId, clienteId, fechaDesde, fechaHasta, estado);
            return ResponseEntity.ok(
                    ApiResponse.<List<ReservaResponse>>builder()
                            .code(200)
                            .success(true)
                            .message("Reservas encontradas")
                            .data(reservas)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<ReservaResponse>>builder()
                            .code(400)
                            .success(false)
                            .message("Error al encontrar las reservas: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_RESERVAS')")
    public ResponseEntity<ApiResponse<ReservaResponse>> cancelarReserva(@PathVariable Long id) {
        try {
            ReservaResponse reservaResponse = reservaService.cancelarReserva(id);
            return ResponseEntity.ok(
                    ApiResponse.<ReservaResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Estado de la reserva actualizado exitosamente")
                            .data(reservaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar el estado de la reserva: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_RESERVAS')")
    public ResponseEntity<ApiResponse<ReservaResponse>> eliminar(@PathVariable Long id) {
        try {
            ReservaResponse reservaResponse = reservaService.eliminar(id);
            return ResponseEntity.ok(
                    ApiResponse.<ReservaResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Reserva eliminada exitosamente")
                            .data(reservaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al eliminar la reserva: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_RESERVAS')")
    public ResponseEntity<ApiResponse<ReservaResponse>> update(@PathVariable Long id, @Valid @RequestBody ReservaRequest request) {
        try {
            ReservaResponse reservaResponse = reservaService.update(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<ReservaResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Reserva actualizada exitosamente")
                            .data(reservaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<ReservaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al actualizar la reserva: " + e.getMessage())
                            .build()
            );

        }
    }

}
