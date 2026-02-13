package com.barberia.dto.disponibilidad;

import lombok.Data;

import java.time.LocalDate;


@Data
public class DisponibilidadRequest {
    // opcional
    private Long profesionalId;

    private LocalDate fecha;
}
