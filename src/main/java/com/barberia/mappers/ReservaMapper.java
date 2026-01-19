package com.barberia.mappers;

import com.barberia.dto.reserva.ReservaRequest;
import com.barberia.dto.reserva.ReservaResponse;
import com.barberia.models.*;
import com.barberia.models.enums.EstadoReserva;

import org.springframework.stereotype.Component;



@Component// sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class ReservaMapper {





    public Reserva toEntity(ReservaRequest request) {
        Reserva reserva = new Reserva();

        reserva.setFecha(request.getFecha());
        reserva.setHoraInicio(request.getHoraInicio());
        reserva.setTipo(request.getTipo());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setRegEstado(1);

        return reserva;
    }

    public ReservaResponse toResponse(Reserva reserva) {
        ReservaResponse response = new ReservaResponse();//esto sirve para crear una nueva instancia de la clase ReservaResponse, la instancia es un objeto que representa la respuesta de la reserva en el sistema
        response.setId(reserva.getId());
        response.setFecha(reserva.getFecha());
        response.setHoraInicio(reserva.getHoraInicio());
        response.setHoraFin(reserva.getHoraFin());
        response.setTipo(reserva.getTipo());
        response.setEstado(reserva.getEstado());
        response.setRegEstado(reserva.getRegEstado());
        response.setCreatedAt(reserva.getCreatedAt());
        if (reserva.getNegocio() != null) {
            response.setNegocioId(reserva.getNegocio().getId());
        }
        if (reserva.getProfesional() != null) {
            response.setProfesionalId(reserva.getProfesional().getId());
        }
        if (reserva.getCliente() != null) {
            response.setClienteId(reserva.getCliente().getId());
        }
        return response;
    }

    public Reserva updateEntity(Reserva reserva, ReservaRequest request) {
        reserva.setFecha(request.getFecha());
        reserva.setHoraInicio(request.getHoraInicio());
        reserva.setTipo(request.getTipo());
        reserva.setRegEstado(2); // Por defecto actualizado
        return reserva;
    }

}
