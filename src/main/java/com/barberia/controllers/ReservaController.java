package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.reserva.ReservaRequest;
import com.barberia.dto.reserva.ReservaResponse;
import com.barberia.services.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_PROFESSIONALS')")
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



}
