package com.barberia.dto.disponibilidad;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class DisponibilidadResponse {

    private LocalDate fecha;
    private Long profesionalId;
    private String profesionalNombre;
    private List<LocalTime> horasDisponibles;
    private Integer duracionServicioMinutos;
    private Integer cantidadHorasDisponibles;
}