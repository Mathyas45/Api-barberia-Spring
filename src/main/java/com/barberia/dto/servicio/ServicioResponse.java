package com.barberia.dto.servicio;

import java.math.BigDecimal;

import com.barberia.dto.Categoria.CategoriaResponse;
import lombok.Data;

@Data
public class ServicioResponse {
    public long id;
    public String nombre;
    public String descripcion;
    public Integer duracionMinutos;
    public BigDecimal precio;
    public CategoriaResponse categoria;
    public Long usuarioRegistroId;
    public Long negocioId;
    public boolean estado;
    public int regEstado;


}
