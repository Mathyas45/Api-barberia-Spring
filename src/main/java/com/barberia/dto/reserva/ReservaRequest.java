package com.barberia.dto.reserva;

import com.barberia.models.enums.EstadoReserva;
import com.barberia.models.enums.TipoReserva;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ReservaRequest {

    private Long negocioId;
    private Long profesionalId; // puede ser null si es automático
    private Long clienteId;      // null si es walk-in

    private LocalDate fecha;
    private LocalTime horaInicio;

    private List<Long> serviciosIds;

    private TipoReserva tipo; // CLIENTE / ADMIN / WALK_IN
}
