package com.barberia.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ============================================================
 * DTO PRINCIPAL - RESPUESTA DEL DASHBOARD
 * ============================================================
 * Estructura dinámica que varía según los permisos del usuario:
 *
 * - tipo_dashboard = "administrativo" → métricas globales del negocio + gráficos + widgets
 * - tipo_dashboard = "operativo"      → agenda personal del profesional + acciones
 *
 * El frontend renderiza condicionalmente según este campo.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResumenResponse {

    /** "administrativo" o "operativo" */
    private String tipoDashboard;

    /** Nombre del usuario autenticado */
    private String nombreUsuario;

    /** Nombre del negocio */
    private String nombreNegocio;

    /** Métricas numéricas (solo administrativo) */
    private List<MetricaDTO> metricas;

    /** Datos de gráficos (solo administrativo) */
    private List<GraficoDTO> graficos;

    /** Widgets dinámicos (próximas reservas, actividad, agenda) */
    private List<WidgetDTO> widgets;

    /** Periodo activo del filtro: "dia", "semana" o "mes" */
    private String periodoActivo;
}
