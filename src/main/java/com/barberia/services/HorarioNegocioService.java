package com.barberia.services;

import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.horarioNegocio.CopiarHorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioResponse;
import com.barberia.mappers.HorarioNegocioMapper;
import com.barberia.models.HorarioNegocio;
import com.barberia.models.enums.DiaSemana;
import com.barberia.repositories.HorarioNegocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        if (horarioNegocioRepository.existeSolapamiento(
                request.getNegocio(), request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin())) {
            throw new RuntimeException("El horario se cruza con un horario existente en el mismo día.");
        }


        HorarioNegocio nuevoHorarioNegocio = horarioNegocioRepository.save(horarioNegocio);
        return horarioNegocioMapper.toResponse(nuevoHorarioNegocio);
    }

    @Transactional(readOnly = true)
    public HorarioNegocioResponse findById(Long id) {
        HorarioNegocio horarioNegocio = horarioNegocioRepository.findById(id).orElseThrow(() -> new RuntimeException("Horario Negocio no encontrado con ID: " + id));
        return horarioNegocioMapper.toResponse(horarioNegocio);
    }

    @Transactional(readOnly = true)
    public List<HorarioNegocioResponse> findAll(Long negocioId,  DiaSemana diaSemana) {
        List<HorarioNegocio> horariosNegocio = horarioNegocioRepository.findByNegocioAndDiaSemana(negocioId, diaSemana);

        return horariosNegocio.stream()
                .map(horarioNegocioMapper::toResponse)
                .toList();
    }

    @Transactional
    public HorarioNegocioResponse update(Long id, HorarioNegocioRequest request) {
        HorarioNegocio horarioNegocioExistente = horarioNegocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario Negocio no encontrado con ID: " + id));

        if (horarioNegocioRepository.existeSolapamientoExcluyendoId(id,
                request.getNegocio(), request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin())) {
            throw new RuntimeException("El horario se cruza con un horario existente para el mismo profesional en el mismo día.");
        }

        HorarioNegocio horarioNegocioActualizado = horarioNegocioMapper.updateEntity(horarioNegocioExistente, request);
        HorarioNegocio guardadoHorarioNegocio = horarioNegocioRepository.save(horarioNegocioActualizado);
        return horarioNegocioMapper.toResponse(guardadoHorarioNegocio);
    }
    @Transactional
    public HorarioNegocioResponse cambiarEstado(Long id, EstadoRequestGlobal request) {
        HorarioNegocio horarioNegocio = horarioNegocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario Negocio no encontrado con ID: " + id));

        horarioNegocio.setRegEstado(request.getRegEstado());
        HorarioNegocio actualizadoHorarioNegocio = horarioNegocioRepository.save(horarioNegocio);
        return horarioNegocioMapper.toResponse(actualizadoHorarioNegocio);
    }

    @Transactional
    public HorarioNegocioResponse eliminar(Long id) {
        HorarioNegocio horarioNegocio = horarioNegocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario Negocio no encontrado con ID: " + id));

        horarioNegocio.setRegEstado(0); // Estado eliminado
        HorarioNegocio eliminadoHorarioNegocio = horarioNegocioRepository.save(horarioNegocio);
        return horarioNegocioMapper.toResponse(eliminadoHorarioNegocio);
    }

    @Transactional
    public void copiarHorarios(CopiarHorarioNegocioRequest request) {
        List<HorarioNegocio> horariosOrigen = horarioNegocioRepository.findByNegocioAndDiaSemana(request.getNegocio(), request.getOrigen());

            for (DiaSemana DiaDestino : request.getDestinos()) {

                horarioNegocioRepository.desactivarPorNegocioYDiaSemana(request.getNegocio(), DiaDestino);

                for (HorarioNegocio h : horariosOrigen) {

                HorarioNegocio nuevoHorario = new HorarioNegocio();
                nuevoHorario.setNegocio(h.getNegocio());
                nuevoHorario.setDiaSemana(DiaDestino);
                nuevoHorario.setHoraInicio(h.getHoraInicio());
                nuevoHorario.setHoraFin(h.getHoraFin());
                nuevoHorario.setRegEstado(1); // Activo

            horarioNegocioRepository.save(nuevoHorario);
            }
        }
    }
}