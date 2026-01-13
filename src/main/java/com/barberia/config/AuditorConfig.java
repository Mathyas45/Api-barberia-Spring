package com.barberia.config;

import com.barberia.models.Negocio;
import com.barberia.models.Usuario;
import com.barberia.repositories.NegocioRepository;
import com.barberia.repositories.UsuarioRepository;
import com.barberia.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Configuración de auditoría multi-tenant.
 * 
 * Proporciona UN AuditorAware para Usuario:
 * - Para Usuario (usuarioRegistroId) - se actualiza en cada operación con @CreatedBy/@LastModifiedBy
 * 
 * IMPORTANTE: El negocio NO usa @CreatedBy
 * - Se establece automáticamente con @PrePersist en cada modelo
 * - Esto es porque JPA Auditing solo soporta UN tipo de AuditorAware
 * 
 * CONEXIÓN CON @CreatedBy y @LastModifiedBy:
 * 
 * 1. @EnableJpaAuditing en BarberiaApplication activa la auditoría
 * 2. @EntityListeners(AuditingEntityListener.class) en cada modelo activa el listener
 * 3. Cuando JPA detecta @CreatedBy o @LastModifiedBy:
 *    - Llama a usuarioAuditorAware
 *    - Obtiene el Usuario del JWT token
 *    - Asigna el valor al campo automáticamente
 * 
 * NO ES NATIVO - Spring necesita saber CÓMO obtener el usuario:
 * - Para usuario: Del JWT token (usuarioId)
 * - Para negocio: Se maneja con @PrePersist en cada modelo
 */
@Configuration
public class AuditorConfig {

    /**
     * AuditorAware para Usuario
     * Usado por campos: @CreatedBy @LastModifiedBy private Usuario usuarioRegistroId
     */
    @Bean
    public AuditorAware<Usuario> usuarioAuditorAware(
            UsuarioRepository usuarioRepository,
            JwtService jwtService) {
        
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }
            
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String jwt = authHeader.substring(7);
                        Long userId = jwtService.extractUsuarioId(jwt);
                        
                        if (userId != null) {
                            return usuarioRepository.findById(userId);
                        }
                    }
                }
                
                // Fallback: buscar por email
                String email = authentication.getName();
                return usuarioRepository.findByEmail(email);
                
            } catch (Exception e) {
                String email = authentication.getName();
                return usuarioRepository.findByEmail(email);
            }
        };
    }
}
