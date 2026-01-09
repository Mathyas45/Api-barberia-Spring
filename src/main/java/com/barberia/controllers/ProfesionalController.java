package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.Profesional.ProfesionalRequest;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.services.ProfesionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {
    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }


    @PostMapping({"/register"})
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CREATE_PROFESSIONALS')")
    public ResponseEntity<ApiResponse<ProfesionalResponse>> createProfesionalProtegido(@Valid @RequestBody ProfesionalRequest request) {
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


}
