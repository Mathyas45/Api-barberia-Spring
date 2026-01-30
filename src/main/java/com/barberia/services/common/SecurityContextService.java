package com.barberia.services.common;

import com.barberia.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servicio utilitario para extraer información del contexto de seguridad.
 * 
 * USO PRINCIPAL: Obtener el negocioId del usuario autenticado para filtrar
 * datos en arquitectura multi-tenant.
 * 
 * VENTAJAS:
 * 1. SEGURIDAD: El negocioId viene del JWT validado, no del frontend
 * 2. SIMPLICIDAD: Una sola línea para obtener el negocioId
 * 3. REUTILIZABLE: Usado por todos los servicios que necesitan filtrar por negocio
 * 4. CONSISTENTE: Misma lógica en toda la aplicación
 * 
 * EJEMPLO DE USO:
 * <pre>
 * {@code
 * @Service
 * public class ClienteService {
 *     private final SecurityContextService securityContextService;
 *     
 *     public List<ClienteResponse> findAll() {
 *         Long negocioId = securityContextService.getNegocioIdFromContext();
 *         return clienteRepository.findAllByNegocioId(negocioId);
 *     }
 * }
 * }
 * </pre>
 */
@Service
public class SecurityContextService {

    private final JwtService jwtService;

    public SecurityContextService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Obtiene el negocioId del usuario autenticado desde el JWT.
     * 
     * FLUJO:
     * 1. Obtiene la autenticación del SecurityContext
     * 2. Extrae el token JWT del header Authorization
     * 3. Decodifica y retorna el negocioId del token
     * 
     * @return Long negocioId del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado o no tiene negocioId
     */
    public Long getNegocioIdFromContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                throw new IllegalStateException("No hay usuario autenticado");
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String jwt = authHeader.substring(7);
                    Long negocioId = jwtService.extractNegocioId(jwt);
                    
                    if (negocioId == null) {
                        throw new IllegalStateException("El token JWT no contiene negocioId");
                    }
                    
                    return negocioId;
                }
            }
            
            throw new IllegalStateException("No se pudo extraer el token JWT");
            
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error al extraer negocioId del contexto: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el email/username del usuario autenticado.
     * 
     * @return String email del usuario autenticado
     */
    public String getUsernameFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        return authentication.getName();
    }
}
