package com.barberia.dto.ConfiguracionReserva;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfiguracionReservaResponse
{
        public Long id;
        public Long negocioId;
        public Integer anticipacionHoras;
        public Integer anticipacionMaximaDias;
        public Integer anticipacionMinimaHoras;
        public Boolean permiteCancelacion;
        public Boolean permiteMismoDia;
        public Integer tiempoMinimoCancelacionHoras;
        public Integer intervaloTurnosMinutos;
        public Integer horasMinimasCancelacion;
        public Long usuarioRegistroId;
        public int regEstado;
        public LocalDateTime createdAt;

}
