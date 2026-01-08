package com.barberia.dto.servicio;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ServicioResponse {
    public long id;
    public String nombre;
    public String descripcion;
    public Integer duracionMinutos;
    public Double precio;
    public String categoria;
    public Long usuarioRegistroId;
    public Long negocioId;
    public int regEstado;


}
