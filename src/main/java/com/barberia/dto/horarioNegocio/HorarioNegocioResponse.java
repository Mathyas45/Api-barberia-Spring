package com.barberia.dto.horarioNegocio;

import com.barberia.models.enums.DiaSemana;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class HorarioNegocioResponse
{
    private Long id;
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Long negocio;
    public LocalDateTime createdAt;
    public Long usuarioRegistroId;
    public int regEstado;

}
