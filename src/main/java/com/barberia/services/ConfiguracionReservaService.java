package com.barberia.services;

import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaRequest;
import com.barberia.dto.ConfiguracionReserva.ConfiguracionReservaResponse;
import com.barberia.mappers.ConfiguracionReservaMapper;
import com.barberia.models.ConfiguracionReserva;
import com.barberia.repositories.ConfiguracionReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionReservaService {
    private final ConfiguracionReservaRepository configuracionReservaRepository;
    private final ConfiguracionReservaMapper configuracionReservaMapper;

    public ConfiguracionReservaService(ConfiguracionReservaRepository configuracionReservaRepository, ConfiguracionReservaMapper configuracionReservaMapper) {
        this.configuracionReservaRepository = configuracionReservaRepository;
        this.configuracionReservaMapper = configuracionReservaMapper;
    }

    @Transactional
    public ConfiguracionReservaResponse create(ConfiguracionReservaRequest request) {
        ConfiguracionReserva configuracionReserva = configuracionReservaMapper.toEntity(request);

        ConfiguracionReserva nuevaConfiguracionReserva = configuracionReservaRepository.save(configuracionReserva);
        return configuracionReservaMapper.toResponse(nuevaConfiguracionReserva);
    }
}
