package com.barberia.services;

import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaRequest;
import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaResponse;
import com.barberia.dto.horarioNegocio.HorarioNegocioResponse;
import com.barberia.mappers.ConfiguracionReservaMapper;
import com.barberia.models.ConfiguracionReserva;
import com.barberia.repositories.ConfiguracionReservaRepository;
import com.barberia.services.common.SecurityContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfiguracionReservaService {
    private final ConfiguracionReservaRepository configuracionReservaRepository;
    private final ConfiguracionReservaMapper configuracionReservaMapper;
    private final SecurityContextService securityContextService;

    public ConfiguracionReservaService(ConfiguracionReservaRepository configuracionReservaRepository, ConfiguracionReservaMapper configuracionReservaMapper, SecurityContextService securityContextService) {
        this.configuracionReservaRepository = configuracionReservaRepository;
        this.configuracionReservaMapper = configuracionReservaMapper;
        this.securityContextService = securityContextService;
    }

    @Transactional
    public ConfiguracionReservaResponse create(ConfiguracionReservaRequest request) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();
        
        ConfiguracionReserva configuracionReserva = configuracionReservaMapper.toEntity(request);

        if (configuracionReservaRepository.existsByNegocioId(negocioId)) {
            throw new RuntimeException("El negocio ya tiene una configuración de reservas.");
        }

        if (Boolean.FALSE.equals(configuracionReserva.getPermiteMismoDia())
                && configuracionReserva.getAnticipacionHoras() == null) {
            throw new RuntimeException("Si no se permite reservas el mismo día, debe especificar las horas de anticipación.");
        }

        ConfiguracionReserva nuevaConfiguracionReserva = configuracionReservaRepository.save(configuracionReserva);
        return configuracionReservaMapper.toResponse(nuevaConfiguracionReserva);
    }

    @Transactional(readOnly = true)
    public ConfiguracionReservaResponse findById(Long id) {
        ConfiguracionReserva configuracionReserva = configuracionReservaRepository.findById(id).orElseThrow(() -> new RuntimeException("Configuración de Reserva no encontrada con ID: " + id));
        return configuracionReservaMapper.toResponse(configuracionReserva);
    }

    @Transactional
    public ConfiguracionReservaResponse update(Long id, ConfiguracionReservaRequest request) {

        ConfiguracionReserva configuracionReservaExistente = configuracionReservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuración de Reserva no encontrada con ID: " + id));


        ConfiguracionReserva actualizada =
                configuracionReservaMapper.updateEntity(configuracionReservaExistente, request);

        if (Boolean.FALSE.equals(actualizada.getPermiteMismoDia())
                && actualizada.getAnticipacionHoras() == null) {
            throw new RuntimeException(
                    "Si no se permite reservas el mismo día, debe especificar las horas de anticipación."
            );
        }

        ConfiguracionReserva guardada = configuracionReservaRepository.save(actualizada);
        return configuracionReservaMapper.toResponse(guardada);
    }

}
