package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.horarioNegocio.HorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioResponse;
import com.barberia.services.HorarioNegocioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horariosNegocio")
public class HorarioNegocioController {
    private final HorarioNegocioService horarioNegocioService;

    public HorarioNegocioController(HorarioNegocioService horarioNegocioService) {
        this.horarioNegocioService = horarioNegocioService;
    }

    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_BUSINESS_SCHEDULES')")
    public ResponseEntity<ApiResponse<HorarioNegocioResponse>> create(@Valid @RequestBody
                                                                          HorarioNegocioRequest request) {
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


}
