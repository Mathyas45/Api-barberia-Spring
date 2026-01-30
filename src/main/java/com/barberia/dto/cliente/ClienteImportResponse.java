package com.barberia.dto.cliente;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO de respuesta para importación masiva de clientes
 * 
 * Proporciona información detallada sobre el resultado de la importación:
 * - Cuántos se insertaron correctamente
 * - Cuántos fallaron
 * - Lista de errores específicos por fila
 */
@Data
@Builder
public class ClienteImportResponse {
    
    private int totalRecibidos;
    private int totalInsertados;
    private int totalErrores;
    private List<ClienteResponse> clientesInsertados;
    private List<ErrorImportacion> errores;
    
    /**
     * Detalle de cada error durante la importación
     */
    @Data
    @Builder
    public static class ErrorImportacion {
        private int fila;  // Número de fila en el Excel (para que el usuario sepa cuál corregir)
        private String nombreCompleto;
        private String telefono;
        private String mensaje;
    }
}
