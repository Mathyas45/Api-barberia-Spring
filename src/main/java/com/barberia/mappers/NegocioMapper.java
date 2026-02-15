package com.barberia.mappers;

import com.barberia.dto.negocio.NegocioRequest;
import com.barberia.dto.negocio.NegocioResponse;
import com.barberia.models.Negocio;
import org.springframework.stereotype.Component;

@Component
public class NegocioMapper {

    /**
     * Convierte NegocioRequest a Negocio (entidad)
     * Utilizado al crear un nuevo negocio
     */
    public Negocio toEntity(NegocioRequest request) {
        Negocio negocio = new Negocio();
        negocio.setNombre(request.getNombre());
        negocio.setRuc(request.getRuc());
        negocio.setDireccion(request.getDireccion());
        negocio.setTelefono(request.getTelefono());
        negocio.setEmail(request.getEmail());
        negocio.setEstado("ACTIVO");
        negocio.setColorPrincipal(request.getColorPrincipal());
        return negocio;
    }

    /**
     * Convierte Negocio a NegocioResponse
     */
    public NegocioResponse toResponse(Negocio negocio) {
        NegocioResponse response = new NegocioResponse();
        response.setId(negocio.getId());
        response.setNombre(negocio.getNombre());
        response.setRuc(negocio.getRuc());
        response.setDireccion(negocio.getDireccion());
        response.setTelefono(negocio.getTelefono());
        response.setEmail(negocio.getEmail());
        response.setEstado(negocio.getEstado());
        response.setColorPrincipal(negocio.getColorPrincipal());
        response.setCreatedAt(negocio.getCreatedAt());
        response.setUpdatedAt(negocio.getUpdatedAt());
        return response;
    }

    /**
     * Actualiza los datos de un negocio existente
     */
    public Negocio updateEntity(Negocio negocio, NegocioRequest request) {
        negocio.setNombre(request.getNombre());
        negocio.setRuc(request.getRuc());
        negocio.setDireccion(request.getDireccion());
        negocio.setTelefono(request.getTelefono());
        negocio.setEmail(request.getEmail());
        negocio.setColorPrincipal(request.getColorPrincipal());
        return negocio;
    }
}
