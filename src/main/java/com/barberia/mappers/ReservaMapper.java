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
        ReservaResponse response = new ReservaResponse();
        
        // Datos básicos
        response.setId(reserva.getId());
        response.setFecha(reserva.getFecha());
        response.setHoraInicio(reserva.getHoraInicio());
        response.setHoraFin(reserva.getHoraFin());
        response.setDuracionTotalMinutos(reserva.getDuracionTotalMinutos());
        response.setPrecioTotal(reserva.getPrecioTotal());
        response.setTipo(reserva.getTipo());
        response.setEstado(reserva.getEstado());
        
        // Negocio
        if (reserva.getNegocio() != null) {
            response.setNegocioId(reserva.getNegocio().getId());
            response.setNegocioNombre(reserva.getNegocio().getNombre());
        }
        
        // Profesional
        if (reserva.getProfesional() != null) {
            response.setProfesionalId(reserva.getProfesional().getId());
            response.setProfesionalNombre(reserva.getProfesional().getNombreCompleto()); // Agregar nombre del profesional
        }
        
        // Cliente
        if (reserva.getCliente() != null) {
            response.setClienteId(reserva.getCliente().getId());
            response.setClienteNombre(reserva.getCliente().getNombreCompleto());
        }
        
        // Servicios
        if (reserva.getServicios() != null && !reserva.getServicios().isEmpty()) {
            response.setServicios(
                reserva.getServicios().stream()
                    .map(rs -> {
                        ReservaResponse.ServicioReservaDTO dto = new ReservaResponse.ServicioReservaDTO();
                        dto.setServicioId(rs.getServicio().getId());
                        dto.setServicioNombre(rs.getServicio().getNombre());
                        dto.setDuracionMinutos(rs.getDuracionMinutos());
                        dto.setPrecio(rs.getPrecio());
                        return dto;
                    })
                    .toList()
            );
        }
        
        // Auditoría
        response.setCreatedAt(reserva.getCreatedAt());
        response.setUpdatedAt(reserva.getUpdatedAt());
        response.setRegEstado(reserva.getRegEstado());
        if (reserva.getUsuarioRegistroId() != null) {
            response.setUsuarioRegistroId(reserva.getUsuarioRegistroId().getId());
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
