package com.barberia.services;

import com.barberia.dto.permiso.PermisoResponse;
import com.barberia.mappers.PermisoMapper;
import com.barberia.models.Permiso;
import com.barberia.repositories.PermisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final PermisoMapper permisoMapper;

    public PermisoService(PermisoRepository permisoRepository, PermisoMapper permisoMapper) {
        this.permisoRepository = permisoRepository;
        this.permisoMapper = permisoMapper;
    }

    @Transactional(readOnly = true)
    public PermisoResponse findById(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));
        return permisoMapper.toResponse(permiso);
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> findAll(String query) {
        List<Permiso> permisos;

        if (query != null && !query.isBlank()) {
            permisos = permisoRepository.findByNameContainingIgnoreCase(query);
        } else {
            permisos = permisoRepository.findAll();
        }

        return permisos.stream()
                .map(permisoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
