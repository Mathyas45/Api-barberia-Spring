package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.disponibilidad.DisponibilidadRequest;
import com.barberia.dto.disponibilidad.DisponibilidadResponse;
import com.barberia.services.DisponibilidadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @PostMapping({"/disponibilidad"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_AVAILABILITIES')")
    public ResponseEntity<ApiResponse<DisponibilidadResponse>> disponibilidad(@Valid @RequestBody DisponibilidadRequest request) {
        try {
            DisponibilidadResponse disponibilidadResponse = disponibilidadService.disponibilidad(request);
            return ResponseEntity.status(200).body(
                    ApiResponse.<DisponibilidadResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Disponibilidad obtenida exitosamente")
                            .data(disponibilidadResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                    ApiResponse.<DisponibilidadResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al obtener la disponibilidad: " + e.getMessage())
                            .build()
            );
        }
    }

}
