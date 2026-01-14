package com.barberia.services;

import com.barberia.dto.horarioNegocio.HorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioResponse;
import com.barberia.mappers.HorarioNegocioMapper;
import com.barberia.models.HorarioNegocio;
import com.barberia.repositories.HorarioNegocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HorarioNegocioService {
    private final HorarioNegocioRepository horarioNegocioRepository;
    private final HorarioNegocioMapper horarioNegocioMapper;

    public HorarioNegocioService(HorarioNegocioRepository horarioNegocioRepository, HorarioNegocioMapper horarioNegocioMapper) {
        this.horarioNegocioRepository = horarioNegocioRepository;
        this.horarioNegocioMapper = horarioNegocioMapper;
    }
    @Transactional
    public HorarioNegocioResponse create(HorarioNegocioRequest request) {
        HorarioNegocio horarioNegocio = horarioNegocioMapper.toEntity(request);

        if (!horarioNegocio.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser menor a la hora fin");
        }

        HorarioNegocio nuevoHorarioNegocio = horarioNegocioRepository.save(horarioNegocio);
        return horarioNegocioMapper.toResponse(nuevoHorarioNegocio);
    }
}
