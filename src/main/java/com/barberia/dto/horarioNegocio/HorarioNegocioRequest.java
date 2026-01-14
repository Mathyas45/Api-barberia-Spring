package com.barberia.dto.horarioNegocio;

import com.barberia.models.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
@Data
public class HorarioNegocioRequest {
    public Long negocio;

    @NotNull(message = "El dia de Semana es obligatorio")
    public DiaSemana diaSemana;

    @NotNull(message = "La hora de Inicio es obligatoria")
    public LocalTime horaInicio;

    @NotNull(message = "La hora de Fin es obligatoria")
    public LocalTime horaFin;

}

