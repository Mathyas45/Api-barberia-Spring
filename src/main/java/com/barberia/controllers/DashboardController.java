package com.barberia.controllers;

import com.barberia.dto.ApiResponse;
import com.barberia.dto.dashboard.DashboardResumenResponse;
import com.barberia.services.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================
 * CONTROLLER - DASHBOARD
 * ============================================================
 * Endpoint para obtener el resumen del dashboard.
 *
 * GET /api/dashboard/resumen
 *
 * Retorna métricas, gráficos y widgets según los permisos
 * del usuario autenticado y su negocio.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Obtiene el resumen completo del dashboard.
     * La respuesta varía según los permisos:
     * - ADMIN/MANAGER → Dashboard administrativo con métricas globales
     * - Otros roles    → Dashboard operativo con agenda personal
     */
    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<DashboardResumenResponse>> getResumen(
            @RequestParam(defaultValue = "dia") String periodo) {
        try {
            DashboardResumenResponse resumen = dashboardService.getResumen(periodo);
            return ResponseEntity.ok(
                    ApiResponse.<DashboardResumenResponse>builder()
                            .code(200)
                            .success(true)
                            .message("Dashboard cargado exitosamente")
                            .data(resumen)
                            .build()
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.<DashboardResumenResponse>builder()
                            .code(401)
                            .success(false)
                            .message("Error de autenticación: " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<DashboardResumenResponse>builder()
                            .code(500)
                            .success(false)
                            .message("Error al cargar dashboard: " + e.getMessage())
                            .build()
            );
        }
    }
}
