package com.barberia.dto.negocio;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NegocioResponse {
    private Long id;
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String email;
    private String estado;
    private String colorPrincipal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
