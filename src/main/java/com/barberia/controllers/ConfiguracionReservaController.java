package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaRequest;
import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaResponse;
import com.barberia.services.ConfiguracionReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracion-reserva")
public class ConfiguracionReservaController {

    private final ConfiguracionReservaService configuracionReservaService;

    public ConfiguracionReservaController(ConfiguracionReservaService configuracionReservaService) {
        this.configuracionReservaService = configuracionReservaService;
    }

    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_RESERVATION_CONFIGURATIONS')")
    public ResponseEntity<ApiResponse<ConfiguracionReservaResponse>> create(@Valid @RequestBody ConfiguracionReservaRequest request) {
        try {
            ConfiguracionReservaResponse configuracionReservaResponse = configuracionReservaService.create(request);
            return ResponseEntity.status(200).body(
                    ApiResponse.<ConfiguracionReservaResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Configuración de Reserva creada exitosamente")
                            .data(configuracionReservaResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                    ApiResponse.<ConfiguracionReservaResponse>builder()
                            .code(400)
                            .success(false)
                            .message("Error al crear la configuración de reserva: " + e.getMessage())
                            .build()
            );
        }
    }
}
