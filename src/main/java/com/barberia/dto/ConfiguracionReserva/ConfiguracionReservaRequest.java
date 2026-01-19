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

     @Positive(message = "La anticipacionMinimaHoras debe ser un número positivo")
     public Integer anticipacionMinimaHoras;

     @NotNull(message = "El campo permiteCacelacion no puede ser nulo")
     public Boolean  permiteCancelacion = true;

     @NotNull(message = "El campo permiteMismoDia no puede ser nulo")
     private Boolean permiteMismoDia = true;

     @Positive(message = "El tiempoMinimoCancelacionHoras debe ser un número positivo")
     public Integer tiempoMinimoCancelacionHoras;

     @Positive(message = "El intervaloTurnosMinutos debe ser un número positivo")
     public Integer intervaloTurnosMinutos;

     @Positive(message = "Las horasMinimasCancelacion debe ser un número positivo")
     public Integer horasMinimasCancelacion;

}
