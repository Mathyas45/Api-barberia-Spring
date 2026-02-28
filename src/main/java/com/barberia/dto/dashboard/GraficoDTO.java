package com.barberia.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para datos de gráficos del dashboard.
 * Soporta múltiples tipos: línea, barra, pastel, etc.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraficoDTO {

    /** Identificador del gráfico (e.g. "reservas_ultimos_7_dias") */
    private String clave;

    /** Título para mostrar */
    private String titulo;

    /** Tipo de gráfico: "linea", "barra", "pastel", "area" */
    private String tipo;

    /** Datos del gráfico como lista de puntos clave-valor */
    private List<Map<String, Object>> datos;
}
