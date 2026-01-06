package com.barberia.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de autenticación
 * 
 * Se devuelve al cliente después de un login exitoso
 * 
 * Contiene:
 * - token: El JWT firmado que el cliente debe enviar en próximas peticiones
 * - type: Tipo de token (siempre "Bearer" para JWT)
 * - email: Email del usuario autenticado
 * - roles: Lista de roles asignados al usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private String email;
    
    private String name;
    
    /**
     * ID del negocio al que pertenece el usuario (MULTI-TENANT)
     * Este campo debe guardarse en localStorage del frontend
     */
    private Long negocioId;
    
    private java.util.Set<String> roles;
    
    private java.util.Set<String> permissions;
}
