package com.barberia.services;

import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.horarioNegocio.CopiarHorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioResponse;
import com.barberia.mappers.HorarioNegocioMapper;
import com.barberia.models.HorarioNegocio;
import com.barberia.models.enums.DiaSemana;
import com.barberia.repositories.HorarioNegocioRepository;
import com.barberia.services.common.SecurityContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HorarioNegocioService {
    private final HorarioNegocioRepository horarioNegocioRepository;
    private final HorarioNegocioMapper horarioNegocioMapper;
    private final SecurityContextService securityContextService;

    public HorarioNegocioService(HorarioNegocioRepository horarioNegocioRepository, HorarioNegocioMapper horarioNegocioMapper, SecurityContextService securityContextService) {
        this.horarioNegocioRepository = horarioNegocioRepository;
        this.horarioNegocioMapper = horarioNegocioMapper;
        this.securityContextService = securityContextService;
    }
    @Transactional
    public HorarioNegocioResponse create(HorarioNegocioRequest request) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();
        
        HorarioNegocio horarioNegocio = horarioNegocioMapper.toEntity(request);

        if (!horarioNegocio.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser menor a la hora fin");
        }

        if (horarioNegocioRepository.existeSolapamiento(
                negocioId, request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin())) {
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
    public List<HorarioNegocioResponse> findAll(DiaSemana diaSemana) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();
        
        List<HorarioNegocio> horariosNegocio = horarioNegocioRepository.findByNegocioAndDiaSemana(negocioId, diaSemana);

        return horariosNegocio.stream()
                .map(horarioNegocioMapper::toResponse)
                .toList();
    }

    @Transactional
    public HorarioNegocioResponse update(Long id, HorarioNegocioRequest request) {

        HorarioNegocio horarioNegocioExistente = horarioNegocioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario Negocio no encontrado con ID: " + id));

        HorarioNegocio horarioNegocioActualizado =
                horarioNegocioMapper.updateEntity(horarioNegocioExistente, request);

        if (horarioNegocioRepository.existeSolapamientoExcluyendoId(id,
                horarioNegocioActualizado.getNegocio().getId(), horarioNegocioActualizado.getDiaSemana(), horarioNegocioActualizado.getHoraInicio(), horarioNegocioActualizado.getHoraFin())) {
            throw new RuntimeException("El horario se cruza con un horario existente para el mismo profesional en el mismo día.");
        }

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
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();
        
        List<HorarioNegocio> horariosOrigen = horarioNegocioRepository.findByNegocioAndDiaSemana(negocioId, request.getOrigen());

            for (DiaSemana DiaDestino : request.getDestinos()) {

                horarioNegocioRepository.desactivarPorNegocioYDiaSemana(negocioId, DiaDestino);

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