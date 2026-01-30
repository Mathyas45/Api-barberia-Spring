package com.barberia.dto.Categoria;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoriaResponse {
    public long id;
    public String nombre;
    public String descripcion;
    public Long usuarioRegistroId;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public Long negocioId;
    public boolean estado;
    public int regEstado;
}
