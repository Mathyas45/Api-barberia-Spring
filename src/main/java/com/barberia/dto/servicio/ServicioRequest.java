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

    public Long usuarioRegistroId;

    @NotNull(message = "El campo negocioId es obligatorio")
    public Long negocioId;
}
