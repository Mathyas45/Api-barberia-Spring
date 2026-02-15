package com.barberia.dto.permiso;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermisoResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
