package com.barberia.mappers;

import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaRequest;
import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaResponse;
import com.barberia.models.ConfiguracionReserva;
import com.barberia.models.Negocio;
import com.barberia.repositories.NegocioRepository;
import com.barberia.services.common.SecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component// un componente en spring es una clase que es gestionada por el contenedor de spring, sirve para mapear configuraciones de reserva entre diferentes representaciones

public class ConfiguracionReservaMapper {
    @Autowired
    private NegocioRepository negocioRepository;
    
    @Autowired
    private SecurityContextService securityContextService;

    public ConfiguracionReserva toEntity(ConfiguracionReservaRequest request) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();
        
        ConfiguracionReserva config = new ConfiguracionReserva();//esto sirve para crear una nueva instancia de la clase ConfiguracionReserva, la instancia es un objeto que representa una configuracion de reserva en el sistema
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado con ID: " + negocioId));
        config.setNegocio(negocio);
        config.setAnticipacionHoras(request.getAnticipacionHoras());
        config.setAnticipacionMaximaDias(request.getAnticipacionMaximaDias());
        config.setAnticipacionMinimaHoras(request.getAnticipacionMinimaHoras());
        config.setPermiteCancelacion(request.getPermiteCancelacion());
        config.setPermiteMismoDia(request.getPermiteMismoDia());
        config.setIntervaloTurnosMinutos(request.getIntervaloTurnosMinutos());
        config.setHorasMinimasCancelacion(request.getHorasMinimasCancelacion());
        config.setRegEstado(1);// Por defecto activo
        return config;
    }

    public ConfiguracionReservaResponse toResponse(ConfiguracionReserva config) {
        ConfiguracionReservaResponse response = new ConfiguracionReservaResponse();//esto sirve para crear una nueva instancia de la clase ConfiguracionReservaResponse, la instancia es un objeto que representa la respuesta de la configuracion de reserva en el sistema
        response.setId(config.getId());
        if(config.getNegocio() != null) {
            response.setNegocioId(config.getNegocio().getId());
        }
        response.setAnticipacionHoras(config.getAnticipacionHoras());
        response.setAnticipacionMaximaDias(config.getAnticipacionMaximaDias());
        response.setAnticipacionMinimaHoras(config.getAnticipacionMinimaHoras());
        response.setPermiteCancelacion(config.getPermiteCancelacion());
        response.setPermiteMismoDia(config.getPermiteMismoDia());
        response.setIntervaloTurnosMinutos(config.getIntervaloTurnosMinutos());
        response.setHorasMinimasCancelacion(config.getHorasMinimasCancelacion());
        response.setRegEstado(config.getRegEstado());
        response.setCreatedAt(config.getCreatedAt());
        response.setUsuarioRegistroId(config.getUsuarioRegistroId() != null ? config.getUsuarioRegistroId().getId() : null);
        return response;
    }

    public ConfiguracionReserva updateEntity(ConfiguracionReserva config, ConfiguracionReservaRequest request) {
        // En update no cambiamos el negocio, solo los otros campos
        config.setAnticipacionHoras(request.getAnticipacionHoras());
        config.setAnticipacionMaximaDias(request.getAnticipacionMaximaDias());
        config.setAnticipacionMinimaHoras(request.getAnticipacionMinimaHoras());
        config.setPermiteCancelacion(request.getPermiteCancelacion());
        config.setPermiteMismoDia(request.getPermiteMismoDia());
        config.setIntervaloTurnosMinutos(request.getIntervaloTurnosMinutos());
        config.setHorasMinimasCancelacion(request.getHorasMinimasCancelacion());
        config.setRegEstado(2); // Por defecto actualizado
        return config;
    }

}
