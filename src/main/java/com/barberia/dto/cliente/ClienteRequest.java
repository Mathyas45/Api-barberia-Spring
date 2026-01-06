package com.barberia.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data //esto genera getters y setters automáticamente
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    public String nombreCompleto;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, max = 20, message = "El teléfono debe tener entre 9 y 20 caracteres")
    public String telefono;

    @Size(min = 9, max = 20, message = "El teléfono debe tener entre 9 y 20 caracteres")
    public String documentoIdentidad;


    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    public String email;

    public Long negocioId;



}
