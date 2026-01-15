package com.barberia.mappers;

import com.barberia.dto.Profesional.ProfesionalRequest;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.models.Negocio;
import com.barberia.models.Profesional;
import com.barberia.models.Usuario;
import com.barberia.repositories.NegocioRepository;
import com.barberia.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component// sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class ProfesionalMapper {
    @Autowired
    private UsuarioMapper usuarioMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private NegocioRepository negocioRepository;

    public Profesional toEntity(ProfesionalRequest request) {
        Profesional profesional = new Profesional(); // esto sirve para crear una nueva instancia de la clase Profesional, la instancia es un objeto que representa un profesional en el sistema
        profesional.setNombreCompleto(request.getNombreCompleto());
        profesional.setDocumentoIdentidad(request.getDocumentoIdentidad());
        profesional.setFechaNacimiento(request.getFechaNacimiento());
        profesional.setTelefono(request.getTelefono());
        profesional.setDireccion(request.getDireccion());
        profesional.setUsaHorarioNegocio(request.getUsaHorarioNegocio());
        profesional.setRegEstado(1); // Por defecto activo
        return profesional;
    }

    public ProfesionalResponse toResponse(Profesional profesional) {
        ProfesionalResponse response = new ProfesionalResponse(); // esto sirve para crear una nueva instancia de la clase ProfesionalResponse, la instancia es un objeto que representa la respuesta del profesional en el sistema
        response.setId(profesional.getId());
        response.setNombreCompleto(profesional.getNombreCompleto());
        response.setDocumentoIdentidad(profesional.getDocumentoIdentidad());
        response.setFechaNacimiento(profesional.getFechaNacimiento());
        response.setDireccion(profesional.getDireccion());
        response.setTelefono(profesional.getTelefono());
        response.setRegEstado(profesional.getRegEstado());
        response.setUsaHorarioNegocio(profesional.getUsaHorarioNegocio());
        if(profesional.getUsuarioRegistroId() != null) {
            response.setUsuarioRegistroId(profesional.getUsuarioRegistroId().getId());
        }
        if (profesional.getNegocio() != null) {
            response.setNegocioId(profesional.getNegocio().getId());
        }

        return response;
    }
    public Profesional updateEntity(Profesional profesional, ProfesionalRequest request) {
        profesional.setNombreCompleto(request.getNombreCompleto());
        profesional.setDocumentoIdentidad(request.getDocumentoIdentidad());
        profesional.setFechaNacimiento(request.getFechaNacimiento());
        profesional.setTelefono(request.getTelefono());
        profesional.setDireccion(request.getDireccion());
        profesional.setUsaHorarioNegocio(request.getUsaHorarioNegocio());
        profesional.setRegEstado(2); // Por defecto actualizado
        return profesional;
    }

}
