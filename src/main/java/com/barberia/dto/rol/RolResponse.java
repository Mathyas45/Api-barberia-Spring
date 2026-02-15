package com.barberia.dto.rol;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RolResponse {
    private Long id;
    private String name;
    private String description;
    private Set<PermisoSimpleResponse> permissions;
    private LocalDateTime createdAt;

    @Data
    public static class PermisoSimpleResponse {
        private Long id;
        private String name;
        private String description;
    }
}
