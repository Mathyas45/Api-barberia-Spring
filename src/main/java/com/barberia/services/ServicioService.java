package com.barberia.services;

import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.Profesional.ProfesionalResponse;
import com.barberia.dto.servicio.ServicioRequest;
import com.barberia.dto.servicio.ServicioResponse;
import com.barberia.dto.servicio.ServicioUpdateRequest;
import com.barberia.mappers.ServicioMapper;
import com.barberia.models.Profesional;
import com.barberia.models.Servicio;
import com.barberia.repositories.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;

    public ServicioService(ServicioRepository servicioRepository, ServicioMapper servicioMapper) {
        this.servicioRepository = servicioRepository;
        this.servicioMapper = servicioMapper;
    }

    @Transactional
    public ServicioResponse create(ServicioRequest request) {
        Servicio servicio = servicioMapper.toEntity(request);

        boolean  nombreServicioRepetido = servicioRepository.existsByNombreAndRegEstadoNotEliminado(servicio.getNombre());
        if (nombreServicioRepetido) {
            throw new RuntimeException("El nombre del servicio ya está registrado.");
        }

        if (servicio.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio no puede ser negativo.");
        }

        Servicio nuevoServicio =  servicioRepository.save(servicio);
        return servicioMapper.toResponse(nuevoServicio);
    }

    @Transactional(readOnly = true)
    public  ServicioResponse findById(Long id) {
            Servicio servicio = servicioRepository.findById(id).orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
            return servicioMapper.toResponse(servicio);
    }
    @Transactional(readOnly = true)
    public  List<ServicioResponse> findAll(String query) {
        List<Servicio> servicios;
        if (query != null && !query.isEmpty()) {
            servicios = servicioRepository.findByNombreContainingIgnoreCaseAndRegEstadoNot(query, 0);
        } else {
            servicios = servicioRepository.findByRegEstadoNot(0);
        }
        return servicios.stream()
                .map(servicioMapper::toResponse)
                .collect(Collectors.toList()); //esto es para convertir la lista de entidades a lista de responses nos sirve para evitar codigo repetitivo
    }
    @Transactional
    public ServicioResponse update(Long id, ServicioUpdateRequest request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " +id));
        servicioMapper.updateEntity(servicio, request);
        Servicio nuevoServicio =  servicioRepository.save(servicio);
        return servicioMapper.toResponse(nuevoServicio);
    }
    @Transactional
    public ServicioResponse cambiarEstado(Long id, EstadoRequestGlobal request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        servicio.setRegEstado(request.getRegEstado());
        Servicio nuevoEstado =  servicioRepository.save(servicio);
        return servicioMapper.toResponse(nuevoEstado);
    }

    @Transactional
    public ServicioResponse eliminar(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        servicio.setRegEstado(0);
        Servicio nuevoEstado =  servicioRepository.save(servicio);
        return servicioMapper.toResponse(nuevoEstado);
    }

}
