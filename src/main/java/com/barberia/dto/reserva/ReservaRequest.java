package com.barberia.dto.reserva;

import com.barberia.models.enums.EstadoReserva;
import com.barberia.models.enums.TipoReserva;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ReservaRequest {

    @NotNull(message = "El profesional es obligatorio")
    private Long profesionalId;
    
    private Long clienteId; // puede ser null para walk-in

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;
    
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotEmpty(message = "Debe seleccionar al menos un servicio")
    private List<Long> serviciosIds;
}
