package com.barberia.dto.HorarioProfesional;

import com.barberia.models.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HorarioProfesionalRequest {

    public Long profesionalId;
    @NotNull(message = "El dia de Semana es obligatorio")
    public DiaSemana diaSemana;
    @NotNull(message = "La hora de Inicio es obligatoria")
    public LocalTime horaInicio;
    @NotNull(message = "La hora de Fin es obligatoria")
    public LocalTime horaFin;
}
