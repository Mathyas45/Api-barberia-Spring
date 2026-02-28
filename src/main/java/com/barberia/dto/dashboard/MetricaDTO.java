package com.barberia.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para métricas del dashboard.
 * Cada métrica tiene una clave, un valor numérico, una etiqueta
 * y opcionalmente un indicador de cambio porcentual.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricaDTO {

    /** Identificador de la métrica (e.g. "reservas_hoy") */
    private String clave;

    /** Etiqueta para mostrar en la UI */
    private String etiqueta;

    /** Valor numérico principal */
    private Number valor;

    /** Cambio porcentual respecto al periodo anterior (nullable) */
    private Double cambioPorcentual;

    /** Icono sugerido para la UI (e.g. "calendar", "users", "dollar-sign") */
    private String icono;

    /** Sufijo del valor (e.g. "S/", "%") */
    private String sufijo;

    /** Prefijo del valor (e.g. "S/") */
    private String prefijo;
}
