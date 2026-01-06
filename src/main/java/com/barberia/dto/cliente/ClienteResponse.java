package com.barberia.dto.cliente;

import lombok.Data;

@Data
public class ClienteResponse {
    public long id;
    public String nombreCompleto;
    public String telefono;
    public String documentoIdentidad;
    public String email;
    public Long negocioId;
}
