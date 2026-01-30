package com.barberia.dto.cliente;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * DTO para importación masiva de clientes desde Excel
 * 
 * Uso desde Angular:
 * 1. Leer el archivo Excel en el frontend
 * 2. Convertir las filas a un array de objetos
 * 3. Enviar como JSON al endpoint /api/clientes/import
 */
@Data
public class ClienteImportRequest {
    
    @NotEmpty(message = "La lista de clientes no puede estar vacía")
    @Valid
    private List<ClienteRequest> clientes;
}
