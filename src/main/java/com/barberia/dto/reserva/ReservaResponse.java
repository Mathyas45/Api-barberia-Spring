package com.barberia.dto.reserva;

import com.barberia.models.enums.EstadoReserva;
import com.barberia.models.enums.TipoReserva;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Data
public class ReservaResponse {

    private Long id;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionTotal;
    private BigDecimal precioTotal;
    private TipoReserva tipo; // CLIENTE / ADMIN / WALK_IN
    private Long negocioId;
    public LocalDateTime createdAt;
    private Long profesionalId;
    private Long clienteId;
    public int regEstado;
    public Long usuarioRegistroId;
    private EstadoReserva estado;
}
