package com.barberia.services;

import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.HorarioProfesional.CopiarHorariosRequest;
import com.barberia.dto.HorarioProfesional.HorarioProfesionalRequest;
import com.barberia.dto.HorarioProfesional.HorarioProfesionalResponse;
import com.barberia.mappers.HorarioProfesionalMapper;
import com.barberia.models.HorarioProfesional;
import com.barberia.models.enums.DiaSemana;
import com.barberia.repositories.HorarioProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorarioProfesionalService {
    private final HorarioProfesionalRepository horarioProfesionalRepository;
    private final HorarioProfesionalMapper horarioProfesionalMapper;

    public HorarioProfesionalService(HorarioProfesionalRepository horarioProfesionalRepository, HorarioProfesionalMapper horarioProfesionalMapper) {
        this.horarioProfesionalRepository = horarioProfesionalRepository;
        this.horarioProfesionalMapper = horarioProfesionalMapper;
    }

    @Transactional
    public HorarioProfesionalResponse create(HorarioProfesionalRequest request) {
        HorarioProfesional horarioProfesional = horarioProfesionalMapper.toEntity(request);

        if (!horarioProfesional.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser menor a la hora fin");
        }

        if (horarioProfesionalRepository.existeSolapamiento(
                request.getProfesional_id(), request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin())) {
            throw new RuntimeException("El horario se cruza con un horario existente para el mismo profesional en el mismo día.");
        }

        if (Duration.between(request.getHoraInicio(), request.getHoraFin()).toMinutes() < 15) {
            throw new RuntimeException("El horario debe ser de al menos 15 minutos");
        }

        HorarioProfesional nuevoHorarioProfesional = horarioProfesionalRepository.save(horarioProfesional);
        return horarioProfesionalMapper.toResponse(nuevoHorarioProfesional);
    }
    @Transactional(readOnly = true)
    public HorarioProfesionalResponse findById(Long id) {
        HorarioProfesional horarioProfesional = horarioProfesionalRepository.findById(id).orElseThrow(() -> new RuntimeException("Horario Profesional no encontrado con ID: " + id));
        return horarioProfesionalMapper.toResponse(horarioProfesional);
    }

    @Transactional(readOnly = true)
    public List<HorarioProfesionalResponse> findAll(Long profesionalId, DiaSemana diaSemana) {
        List<HorarioProfesional> horariosProfesionales = horarioProfesionalRepository.findByProfesionalAndDiaSemana(profesionalId, diaSemana);

        return horariosProfesionales.stream()
                .map(horarioProfesionalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HorarioProfesionalResponse update(Long id, HorarioProfesionalRequest request) {
        HorarioProfesional horarioProfesionalExistente = horarioProfesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario Profesional no encontrado con ID: " + id));

        if (!request.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser menor a la hora fin");
        }

        if (horarioProfesionalRepository.existeSolapamientoExcluyendoId(id,
                request.getProfesional_id(), request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin())) {
            throw new RuntimeException("El horario se cruza con un horario existente para el mismo profesional en el mismo día.");
        }

        if (Duration.between(request.getHoraInicio(), request.getHoraFin()).toMinutes() < 15) {
            throw new RuntimeException("El horario debe ser de al menos 15 minutos");
        }

        HorarioProfesional horarioActualizado = horarioProfesionalMapper.updateEntity(horarioProfesionalExistente, request);
        HorarioProfesional guardadoHorarioProfesional = horarioProfesionalRepository.save(horarioActualizado);
        return horarioProfesionalMapper.toResponse(guardadoHorarioProfesional);
    }

    @Transactional
    public HorarioProfesionalResponse cambiarEstado(Long id, EstadoRequestGlobal request) {
        HorarioProfesional horarioProfesional = horarioProfesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario Profesional no encontrado con ID: " + id));
        horarioProfesional.setRegEstado(request.getRegEstado());
        HorarioProfesional actualizadoHorarioProfesional = horarioProfesionalRepository.save(horarioProfesional);
        return horarioProfesionalMapper.toResponse(actualizadoHorarioProfesional);
    }
    @Transactional
    public HorarioProfesionalResponse eliminar(Long id) {
        HorarioProfesional horarioProfesional = horarioProfesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario Profesional no encontrado con ID: " + id));
        horarioProfesional.setRegEstado(0);
        HorarioProfesional actualizadoHorarioProfesional = horarioProfesionalRepository.save(horarioProfesional);
        return horarioProfesionalMapper.toResponse(actualizadoHorarioProfesional);
    }

    @Transactional
    public void copiarHorarios(CopiarHorariosRequest request) {

        List<HorarioProfesional> base = horarioProfesionalRepository.findByProfesionalAndDiaSemana(request.getProfesionalId(), request.getOrigen());

        for (DiaSemana DiaDestino : request.getDestinos()) {

            horarioProfesionalRepository.desactivarPorProfesionalYDiaSemana(request.getProfesionalId(), DiaDestino);

            for (HorarioProfesional h : base) {
                HorarioProfesional nuevo = new HorarioProfesional();
                nuevo.setProfesional(h.getProfesional());
                nuevo.setDiaSemana(DiaDestino);
                nuevo.setHoraInicio(h.getHoraInicio());
                nuevo.setHoraFin(h.getHoraFin());
                nuevo.setRegEstado(1); // Activo

                horarioProfesionalRepository.save(nuevo);
            }
        }
    }

}
