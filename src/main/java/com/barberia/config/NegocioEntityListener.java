package com.barberia.config;

import com.barberia.models.Negocio;
import com.barberia.repositories.NegocioRepository;
import com.barberia.security.JwtService;
import jakarta.persistence.PrePersist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;

/**
 * Listener JPA para establecer automáticamente el negocio en la creación de entidades.
 * 
 * CÓMO FUNCIONA:
 * 1. Se ejecuta ANTES de insertar en la base de datos (@PrePersist)
 * 2. Busca el campo 'negocio' en la entidad
 * 3. Extrae el negocioId del JWT token
 * 4. Busca el Negocio en la BD
 * 5. Lo asigna automáticamente
 * 
 * IMPORTANTE: 
 * - Solo se ejecuta en INSERT, no en UPDATE (el negocio es inmutable)
 * - Si falla, LANZA EXCEPCIÓN y el INSERT se cancela (garantiza consistencia)
 * - Overhead: 1 SELECT Negocio por cada INSERT (aceptable porque es una vez)
 * 
 * VIABLE A GRAN ESCALA:
 * - El overhead es SOLO en creación (no en updates)
 * - El negocio nunca cambia después de creado
 * - Garantiza que NUNCA haya registros sin negocio (multi-tenant)
 */
@Component
public class NegocioEntityListener {

    private static ApplicationContext context;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @PrePersist
    public void setNegocio(Object entity) {
        try {
            // Buscar el campo 'negocio' en la entidad
            Field negocioField = findNegocioField(entity.getClass());
            if (negocioField == null) {
                return; // La entidad no tiene campo 'negocio'
            }

            negocioField.setAccessible(true);
            
            // Si ya tiene negocio, no sobrescribir
            Object currentNegocio = negocioField.get(entity);
            if (currentNegocio != null) {
                return;
            }

            // Extraer negocioId del JWT
            Long negocioId = extractNegocioIdFromJwt();
            if (negocioId == null) {
                throw new IllegalStateException("No se pudo extraer negocioId del JWT. El registro multi-tenant requiere un negocio.");
            }

            // Buscar el Negocio
            NegocioRepository negocioRepository = context.getBean(NegocioRepository.class);
            Negocio negocio = negocioRepository.findById(negocioId)
                    .orElseThrow(() -> new IllegalStateException("Negocio no encontrado con ID: " + negocioId));
            
            negocioField.set(entity, negocio);

        } catch (IllegalStateException e) {
            // Re-lanzar errores de negocio para FALLAR la transacción
            throw e;
        } catch (Exception e) {
            // Otros errores técnicos también deben fallar
            throw new IllegalStateException("Error al establecer negocio: " + e.getMessage(), e);
        }
    }

    private Field findNegocioField(Class<?> clazz) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField("negocio");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private Long extractNegocioIdFromJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String jwt = authHeader.substring(7);
                    JwtService jwtService = context.getBean(JwtService.class);
                    return jwtService.extractNegocioId(jwt);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
}
