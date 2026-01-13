package com.barberia.dto.Categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoriaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    public String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    public String descripcion;

    // usuarioRegistroId ya NO es necesario - se captura automáticamente con @LastModifiedBy
    // public Long usuarioRegistroId;

    // negocioId ya NO es necesario - se captura automáticamente con @CreatedBy del JWT
    // public Long negocioId;
}
