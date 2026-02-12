package com.barberia.dto.reserva;

import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.dto.cliente.ClienteResponse;
import com.barberia.models.Cliente;
import com.barberia.models.Profesional;
import com.barberia.models.enums.EstadoReserva;
import com.barberia.models.enums.TipoReserva;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class ReservaResponse {

    private Long id;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionTotalMinutos;
    private BigDecimal precioTotal;
    private TipoReserva tipo;
    private EstadoReserva estado;
    
    // IDs para referencias
    private Long negocioId;
    private Long profesionalId;
    private Long clienteId;
    private Long usuarioRegistroId;

    // Información legible (nombres)
    private String negocioNombre;
    private ProfesionalResponse profesional;
    private ClienteResponse cliente;
    
    // Lista de servicios incluidos
    private List<ServicioReservaDTO> servicios;
    
    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer regEstado;
    
    @Data
    public static class ServicioReservaDTO {
        private Long servicioId;
        private String servicioNombre;
        private Integer duracionMinutos;
        private BigDecimal precio;
    }

}
