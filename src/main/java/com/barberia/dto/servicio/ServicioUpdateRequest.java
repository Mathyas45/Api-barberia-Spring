package com.barberia.dto.servicio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ServicioUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    private Integer duracionMinutos;

    private Double precio;

    private Long categoria;

    // usuarioRegistroId ya NO es necesario - se captura automáticamente con @LastModifiedBy
    // private Long usuarioRegistroId;
}