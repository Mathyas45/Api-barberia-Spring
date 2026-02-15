package com.barberia.dto.usuario;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UsuarioResponse {
    private Long id;
    private String name;
    private String email;
    private Long negocioId;
    private String negocioNombre;
    private Set<RolSimpleResponse> roles;
    private Integer regEstado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class RolSimpleResponse {
        private Long id;
        private String name;
        private String description;
    }
}
