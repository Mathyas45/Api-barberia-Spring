package com.barberia.services;

import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.Profesional.ProfesionalRequest;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.mappers.ProfesionalMapper;
import com.barberia.models.Profesional;
import com.barberia.repositories.ProfesionalRepository;
import com.barberia.services.common.FechaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProfesionalService {
    private final ProfesionalRepository profesionalRepository;
    private final ProfesionalMapper profesionalMapper;
    private final FechaService fechaService;


    public ProfesionalService(ProfesionalRepository profesionalRepository, ProfesionalMapper profesionalMapper, FechaService fechaService) {
        this.profesionalRepository = profesionalRepository;
        this.profesionalMapper = profesionalMapper;
        this.fechaService = fechaService;
    }

    @Transactional
    public ProfesionalResponse create(ProfesionalRequest request) {
        Profesional profesional = profesionalMapper.toEntity(request);
        boolean  nombreProfesionaRepetido = profesionalRepository.existsByNombreAndRegEstadoNotEliminado(profesional.getNombreCompleto());
        if (nombreProfesionaRepetido) {
            throw new RuntimeException("El nombre del profesional ya está registrado.");
        }
        Profesional nuevoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(nuevoProfesional);
    }

    @Transactional(readOnly = true)
    public ProfesionalResponse findById(Long id) {
        Profesional profesional = profesionalRepository.findById(id).orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));
        ProfesionalResponse response = profesionalMapper.toResponse(profesional);

        if (profesional.getFechaNacimiento() != null) {
            response.setEdad(fechaService.calcularEdad(profesional.getFechaNacimiento()));//lama al servicio para calcular la edad
        }
        return response;
    }
    @Transactional(readOnly = true)
    public List<ProfesionalResponse> findAll(String query) {
        List<Profesional> profesionales;

        if (query != null && !query.isEmpty()) {
            profesionales = profesionalRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(query, 0);
        } else {
            profesionales = profesionalRepository.findByRegEstadoNot(0);
        }

        return profesionales.stream()
                .map(profesional-> {
                        ProfesionalResponse response = profesionalMapper.toResponse(profesional);
                        response.setEdad(fechaService.calcularEdad(profesional.getFechaNacimiento()));
                        return response;
                })
                .collect(Collectors.toList()); //esto es para convertir la lista de entidades a lista de responses nos sirve para evitar codigo repetitivo
    }

    @Transactional
    public ProfesionalResponse update(Long id, ProfesionalRequest request) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));

        profesionalMapper.updateEntity(profesional, request);
        Profesional nuevoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(nuevoProfesional);
    }
    @Transactional
    public ProfesionalResponse cambiarEstado(Long id, EstadoRequestGlobal request) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));
        profesional.setRegEstado(request.getRegEstado());
        Profesional actualizadoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(actualizadoProfesional);
    }

    @Transactional
    public ProfesionalResponse eliminar(Long id) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + id));
        profesional.setRegEstado(0);
        Profesional actualizadoProfesional = profesionalRepository.save(profesional);
        return profesionalMapper.toResponse(actualizadoProfesional);
    }
}
