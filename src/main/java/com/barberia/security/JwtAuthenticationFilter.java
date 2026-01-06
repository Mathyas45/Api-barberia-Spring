package com.barberia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT para autenticación stateless
 * 
 * QUÉ ES UN FILTRO:
 * Un filtro intercepta TODAS las peticiones HTTP antes de que lleguen al controller
 * Permite inspeccionar, modificar o rechazar requests
 * 
 * OncePerRequestFilter:
 * Garantiza que el filtro se ejecute UNA SOLA VEZ por request
 * (Importante porque a veces un request pasa por múltiples filtros)
 * 
 * FLUJO DE EJECUCIÓN:
 * 1. Cliente envía request con header: Authorization: Bearer <token>
 * 2. Este filtro intercepta el request
 * 3. Extrae el token del header
 * 4. Valida el token
 * 5. Si es válido, autentica al usuario en el SecurityContext
 * 6. El request continúa hacia el controller
 * 
 * STATELESS:
 * No guardamos sesión en el servidor
 * Cada request debe traer su JWT para ser autenticado
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    // Constructor explícito (reemplaza @RequiredArgsConstructor)
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * Método principal del filtro que se ejecuta en cada request
     * 
     * @param request HTTP request entrante
     * @param response HTTP response saliente
     * @param filterChain Cadena de filtros (permite continuar al siguiente filtro)
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. EXTRAE EL HEADER "Authorization"
        final String authHeader = request.getHeader("Authorization");
        
        // 2. VALIDA QUE EL HEADER EXISTA Y EMPIECE CON "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No hay token, continúa sin autenticar
            // El endpoint puede ser público o Spring Security lo bloqueará
            filterChain.doFilter(request, response);
            return;
        }
            
        // 3. EXTRAE EL TOKEN (quita "Bearer " del inicio)
        // "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." → "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        final String jwt = authHeader.substring(7);
        
        // 4. EXTRAE EL EMAIL DEL TOKEN
        final String userEmail;
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token inválido o mal formado
            filterChain.doFilter(request, response);
            return;
        }
        
        // 5. VALIDA QUE EL USUARIO NO ESTÉ YA AUTENTICADO
        // SecurityContextHolder.getContext().getAuthentication():
        // Contiene la autenticación actual del usuario en este request
        // Si ya está autenticado (no es null), no hacemos nada
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 6. CARGA LOS DETALLES DEL USUARIO DESDE LA BASE DE DATOS
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            
            // 7. VALIDA EL TOKEN
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 8. CREA EL OBJETO DE AUTENTICACIÓN
                // UsernamePasswordAuthenticationToken: Representa un usuario autenticado en Spring Security
                // Parámetros:
                // - userDetails: El usuario
                // - null: Credenciales (ya validamos el JWT, no necesitamos password)
                // - userDetails.getAuthorities(): Roles y permisos del usuario
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                // 9. AGREGA DETALLES DEL REQUEST AL TOKEN DE AUTENTICACIÓN
                // (IP, session, etc.) - Útil para auditoría
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 10. ESTABLECE LA AUTENTICACIÓN EN EL CONTEXTO DE SEGURIDAD
                // A partir de este punto, el usuario está AUTENTICADO
                // Spring Security puede acceder a esta autenticación en cualquier momento
                // Los @PreAuthorize funcionarán correctamente
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 11. CONTINÚA CON LA CADENA DE FILTROS
        // El request pasa al siguiente filtro o al controller
        filterChain.doFilter(request, response);
    }
}
