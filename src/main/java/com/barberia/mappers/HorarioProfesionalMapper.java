package com.barberia.mappers;

import com.barberia.dto.HorarioProfesional.HorarioProfesionalRequest;
import com.barberia.dto.HorarioProfesional.HorarioProfesionalResponse;
import com.barberia.models.Categoria;
import com.barberia.models.HorarioProfesional;
import com.barberia.models.Profesional;
import com.barberia.repositories.ProfesionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component// sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class HorarioProfesionalMapper {

    @Autowired
    private ProfesionalRepository profesionalRepository;

    public HorarioProfesional toEntity(HorarioProfesionalRequest request) {
        HorarioProfesional horario = new HorarioProfesional(); // esto sirve para crear una nueva instancia de la clase HorarioProfesional, la instancia es un objeto que representa un horario de profesional en el sistema
        Profesional profesional = profesionalRepository.findById(request.getProfesional_id())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrado con ID: " + request.getProfesional_id()));
        horario.setProfesional(profesional);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        return horario;
    }

    public HorarioProfesionalResponse toResponse(HorarioProfesional horario) {
        HorarioProfesionalResponse response = new HorarioProfesionalResponse(); // esto sirve para crear una nueva instancia de la clase HorarioProfesionalResponse, la instancia es un objeto que representa la respuesta del horario de profesional en el sistema
        response.setId(horario.getId());
        if(horario.getProfesional() != null) {
            response.setProfesional_id(horario.getProfesional().getId());
        }
        response.setDiaSemana(horario.getDiaSemana());
        response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFin(horario.getHoraFin());
        response.setRegEstado(horario.getRegEstado());
        response.setCreatedAt(horario.getCreatedAt());
        response.setUsuarioRegistroId(horario.getUsuarioRegistroId() != null ? horario.getUsuarioRegistroId().getId() : null);
        response.setNegocioId(horario.getNegocio() != null ? horario.getNegocio().getId() : null);
        return response;
    }

    public HorarioProfesional updateEntity(HorarioProfesional horario, HorarioProfesionalRequest request) {
        Profesional profesional = profesionalRepository.findById(request.getProfesional_id())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrado con ID: " + request.getProfesional_id()));
        horario.setProfesional(profesional);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setRegEstado(2); // Por defecto actualizado
        return horario;
    }

}
