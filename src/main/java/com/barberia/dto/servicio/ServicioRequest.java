package com.barberia.dto.servicio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class ServicioRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    public String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    public String descripcion;

    public Integer duracionMinutos;

    public Double precio;

    public Long categoria;

    // usuarioRegistroId ya NO es necesario - se captura automáticamente con @LastModifiedBy
    // public Long usuarioRegistroId;

    // negocioId ya NO es necesario - se captura automáticamente con @CreatedBy del JWT
    // @NotNull(message = "El campo negocioId es obligatorio")
    // public Long negocioId;
}
