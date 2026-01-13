package com.barberia.dto.HorarioProfesional;

import com.barberia.models.enums.DiaSemana;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class HorarioProfesionalResponse {
    public Long id;
    public Long profesional_id;
    public DiaSemana diaSemana;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public LocalDateTime createdAt;
    public Long negocioId;
    public Long usuarioRegistroId;
    public int regEstado;

}
