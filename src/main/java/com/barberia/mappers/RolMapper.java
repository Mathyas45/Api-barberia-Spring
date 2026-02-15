package com.barberia.mappers;

import com.barberia.dto.rol.RolRequest;
import com.barberia.dto.rol.RolResponse;
import com.barberia.models.Rol;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class RolMapper {

    /**
     * Convierte RolRequest a Rol (entidad)
     * Utilizado al crear un nuevo rol
     */
    public Rol toEntity(RolRequest request) {
        Rol rol = new Rol();
        rol.setName(request.getName());
        rol.setDescription(request.getDescription());
        rol.setPermissions(new HashSet<>());
        return rol;
    }

    /**
     * Convierte Rol a RolResponse
     * Incluye los permisos asociados
     */
    public RolResponse toResponse(Rol rol) {
        RolResponse response = new RolResponse();
        response.setId(rol.getId());
        response.setName(rol.getName());
        response.setDescription(rol.getDescription());
        response.setCreatedAt(rol.getCreatedAt());

        // Mapear permisos
        var permisosResponse = rol.getPermissions().stream()
                .map(permiso -> {
                    RolResponse.PermisoSimpleResponse permisoResponse = new RolResponse.PermisoSimpleResponse();
                    permisoResponse.setId(permiso.getId());
                    permisoResponse.setName(permiso.getName());
                    permisoResponse.setDescription(permiso.getDescription());
                    return permisoResponse;
                })
                .collect(Collectors.toSet());
        
        response.setPermissions(permisosResponse);
        
        return response;
    }

    /**
     * Actualiza los datos de un rol existente
     */
    public Rol updateEntity(Rol rol, RolRequest request) {
        rol.setName(request.getName());
        rol.setDescription(request.getDescription());
        return rol;
    }
}
