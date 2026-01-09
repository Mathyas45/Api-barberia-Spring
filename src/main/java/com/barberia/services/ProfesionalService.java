package com.barberia.services;

import com.barberia.dto.Profesional.ProfesionalRequest;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.mappers.ProfesionalMapper;
import com.barberia.models.Profesional;
import com.barberia.repositories.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfesionalService {
    private final ProfesionalRepository profesionalRepository;
    private final ProfesionalMapper profesionalMapper;

    public ProfesionalService(ProfesionalRepository profesionalRepository, ProfesionalMapper profesionalMapper) {
        this.profesionalRepository = profesionalRepository;
        this.profesionalMapper = profesionalMapper;
    }

    @Transactional
    public ProfesionalResponse create(ProfesionalRequest request) {
        Profesional profesional = profesionalMapper.toEntity(request);
        Profesional nuevoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(nuevoProfesional);
    }


}
