package com.barberia.dto.horarioNegocio;

import com.barberia.models.enums.DiaSemana;
import lombok.Data;

import java.util.List;

@Data
public class CopiarHorarioNegocioRequest {
    private Long negocio;
    private DiaSemana origen;
    private List<DiaSemana> destinos;

}
