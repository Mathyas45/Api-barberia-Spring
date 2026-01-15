package com.barberia.dto.ConfiguracionReserva;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ConfiguracionReservaRequest{

     @NotNull(message = "El negocioId es obligatorio")
     public Long negocioId;

     @Positive(message = "La anticipacionHoras debe ser un número positivo")
     public Integer anticipacionHoras;

     @Positive(message = "La anticipacionMaximaDias debe ser un número positivo")
     public Integer anticipacionMaximaDias;

     public Boolean permiteMismoDia;

     @Positive(message = "El tiempoMinimoCancelacionHoras debe ser un número positivo")
     public Integer tiempoMinimoCancelacionHoras;

     @Positive(message = "El intervaloTurnosMinutos debe ser un número positivo")
     public Integer intervaloTurnosMinutos;

     @Positive(message = "Las horasMinimasCancelacion debe ser un número positivo")
     public Integer horasMinimasCancelacion;

}
