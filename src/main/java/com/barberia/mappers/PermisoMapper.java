package com.barberia.mappers;

import com.barberia.dto.permiso.PermisoResponse;
import com.barberia.models.Permiso;
import org.springframework.stereotype.Component;

@Component
public class PermisoMapper {

    /**
     * Convierte Permiso a PermisoResponse
     */
    public PermisoResponse toResponse(Permiso permiso) {
        PermisoResponse response = new PermisoResponse();
        response.setId(permiso.getId());
        response.setName(permiso.getName());
        response.setDescription(permiso.getDescription());
        response.setCreatedAt(permiso.getCreatedAt());
        return response;
    }
}
