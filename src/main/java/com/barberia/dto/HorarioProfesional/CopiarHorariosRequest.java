package com.barberia.dto.HorarioProfesional;

import com.barberia.models.enums.DiaSemana;
import lombok.Data;

import java.util.List;

@Data
public class CopiarHorariosRequest {
    private Long profesionalId;
    private DiaSemana origen;
    private List<DiaSemana> destinos;

}