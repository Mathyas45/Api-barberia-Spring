package com.barberia.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para cada item individual dentro de un widget.
 * Se usa para representar reservas, actividades, etc.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WidgetItemDTO {

    private Long id;

    /** Hora de la reserva/actividad (e.g. "10:00") */
    private String hora;

    /** Hora de fin (e.g. "10:45") */
    private String horaFin;

    /** Nombre del cliente */
    private String cliente;

    /** Nombre del profesional */
    private String profesional;

    /** Nombre del servicio o descripción */
    private String servicio;

    /** Duración en minutos */
    private Integer duracionMinutos;

    /** Estado (e.g. "PENDIENTE", "CONFIRMADA") */
    private String estado;

    /** Fecha (e.g. "2026-02-26") */
    private String fecha;

    /** Precio total */
    private BigDecimal precio;

    /** Descripción o texto libre */
    private String descripcion;

    /** Timestamp del evento (para actividad reciente) */
    private String timestamp;
}
