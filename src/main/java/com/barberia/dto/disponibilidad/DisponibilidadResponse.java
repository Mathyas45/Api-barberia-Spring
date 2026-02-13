package com.barberia.dto.disponibilidad;

import com.barberia.dto.Profesional.ProfesionalResponse;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class DisponibilidadResponse {

    private LocalDate fecha;
    private Long profesionalId;
    private ProfesionalResponse profesional;
    private List<LocalTime> horasDisponibles;
    private Integer cantidadHorasDisponibles;
}