package com.barberia.dto.disponibilidad;

import lombok.Data;

import java.time.LocalDate;


@Data
public class DisponibilidadRequest {
    private Long negocioId;

    // opcional
    private Long profesionalId;

    // obligatorio
    private Long servicioId;

    private LocalDate fecha;

    // para diferenciar cliente vs admin
    private Boolean esInterno; // true = admin/barbero
}
