package com.barberia.mappers;

import com.barberia.dto.horarioNegocio.HorarioNegocioRequest;
import com.barberia.dto.horarioNegocio.HorarioNegocioResponse;
import com.barberia.models.HorarioNegocio;
import com.barberia.models.Negocio;
import com.barberia.models.Profesional;
import com.barberia.repositories.NegocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component// sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class HorarioNegocioMapper {
    @Autowired
    private NegocioRepository negocioRepository;

    public HorarioNegocio toEntity(HorarioNegocioRequest request) {
        HorarioNegocio horario = new HorarioNegocio(); // esto sirve para crear una nueva instancia de la clase HorarioNegocio, la instancia es un objeto que representa un horario de negocio en el sistema
        Negocio negocio = negocioRepository.findById(request.getNegocio())
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado con ID: " + request.getNegocio()));
        horario.setNegocio(negocio);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setRegEstado(1); // Por defecto activo
        return horario;
    }

    public HorarioNegocioResponse toResponse(HorarioNegocio horario) {
        HorarioNegocioResponse response = new HorarioNegocioResponse(); // esto sirve para crear una nueva instancia de la clase HorarioNegocioResponse, la instancia es un objeto que representa la respuesta del horario de negocio en el sistema
        response.setId(horario.getId());
        if(horario.getNegocio() != null) {
            response.setNegocio(horario.getNegocio().getId());
        }
        response.setDiaSemana(horario.getDiaSemana());
        response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFin(horario.getHoraFin());
        response.setRegEstado(horario.getRegEstado());
        response.setCreatedAt(horario.getCreatedAt());
        response.setUsuarioRegistroId(horario.getUsuarioRegistroId() != null ? horario.getUsuarioRegistroId().getId() : null);
        return response;
    }

    public HorarioNegocio updateEntity(HorarioNegocio horario, HorarioNegocio request) {
        Negocio negocio = negocioRepository.findById(request.getNegocio().getId())
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado con ID: " + request.getNegocio()));
        horario.setNegocio(negocio);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setRegEstado(2); // Por defecto actualizado
        return horario;
    }
}
