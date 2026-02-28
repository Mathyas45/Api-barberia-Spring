package com.barberia.dto.usuario;

import com.barberia.dto.Categoria.CategoriaResponse;
import com.barberia.dto.rol.RolResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;   
    private String telefono;
    private Integer tipoUsuario;  // 1 = Interno, 2 = Cliente
    private Long negocioId;
    private String negocioNombre;
    public RolResponse roles;

    private Integer regEstado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
