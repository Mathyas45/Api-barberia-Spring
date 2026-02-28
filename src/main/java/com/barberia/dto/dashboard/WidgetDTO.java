package com.barberia.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para widgets del dashboard (próximas reservas, actividad reciente, etc.)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WidgetDTO {

    /** Identificador del widget (e.g. "proximas_reservas") */
    private String clave;

    /** Título del widget */
    private String titulo;

    /** Tipo de widget: "tabla", "lista", "timeline" */
    private String tipo;

    /** Items del widget */
    private List<WidgetItemDTO> items;
}
