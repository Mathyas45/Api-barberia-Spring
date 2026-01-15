package com.barberia.dto.cliente;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClienteResponse {
    public long id;
    public String nombreCompleto;
    public String telefono;
    public String documentoIdentidad;
    public String email;
    public Long negocioId;
    public Long usuarioRegistroId;
    public int regEstado;
}
