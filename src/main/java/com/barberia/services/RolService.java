package com.barberia.services;

import com.barberia.dto.rol.RolRequest;
import com.barberia.dto.rol.RolResponse;
import com.barberia.mappers.RolMapper;
import com.barberia.models.Permiso;
import com.barberia.models.Rol;
import com.barberia.repositories.PermisoRepository;
import com.barberia.repositories.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolMapper rolMapper;

    public RolService(RolRepository rolRepository, PermisoRepository permisoRepository, RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.rolMapper = rolMapper;
    }

    @Transactional
    public RolResponse create(RolRequest request) {
        // Verificar si el nombre del rol ya existe
        if (rolRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe un rol con ese nombre");
        }

        Rol rol = rolMapper.toEntity(request);

        // Asignar permisos
        Set<Permiso> permisos = new HashSet<>();
        for (Long permisoId : request.getPermisosIds()) {
            Permiso permiso = permisoRepository.findById(permisoId)
                    .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permisoId));
            permisos.add(permiso);
        }
        rol.setPermissions(permisos);

        Rol nuevoRol = rolRepository.save(rol);
        return rolMapper.toResponse(nuevoRol);
    }

    @Transactional(readOnly = true)
    public RolResponse findById(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        return rolMapper.toResponse(rol);
    }

    @Transactional(readOnly = true)
    public List<RolResponse> findAll(String query) {
        List<Rol> roles;

        if (query != null && !query.isBlank()) {
            roles = rolRepository.findByNameContainingIgnoreCase(query);
        } else {
            roles = rolRepository.findAll();
        }

        return roles.stream()
                .map(rolMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RolResponse update(Long id, RolRequest request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Verificar si el nombre del rol ya existe en otro rol
        Rol rolConNombre = rolRepository.findByName(request.getName());
        if (rolConNombre != null && !rolConNombre.getId().equals(id)) {
            throw new RuntimeException("Ya existe otro rol con ese nombre");
        }

        rolMapper.updateEntity(rol, request);

        // Actualizar permisos
        Set<Permiso> permisos = new HashSet<>();
        for (Long permisoId : request.getPermisosIds()) {
            Permiso permiso = permisoRepository.findById(permisoId)
                    .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permisoId));
            permisos.add(permiso);
        }
        rol.getPermissions().clear();
        rol.getPermissions().addAll(permisos);

        Rol rolActualizado = rolRepository.save(rol);
        return rolMapper.toResponse(rolActualizado);
    }

    @Transactional
    public void delete(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Verificar si el rol está siendo usado por usuarios
        if (!rol.getUsers().isEmpty()) {
            throw new RuntimeException("No se puede eliminar el rol porque está siendo usado por usuarios");
        }

        rolRepository.delete(rol);
    }
}
