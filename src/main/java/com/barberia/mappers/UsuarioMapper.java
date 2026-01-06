package com.barberia.mappers;

import com.barberia.dto.auth.AuthResponse;
import com.barberia.models.Permiso;
import com.barberia.models.Rol;
import com.barberia.models.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Usuario y DTOs
 * 
 * RESPONSABILIDAD:
 * Convertir objetos de dominio (entidades) a DTOs y viceversa
 * 
 * BENEFICIOS:
 * - Separa la capa de presentación de la capa de dominio
 * - Controla qué información se expone al cliente
 * - Evita exponer entidades JPA directamente
 * - Facilita cambios en la API sin afectar el modelo de datos
 */
@Component
public class UsuarioMapper {
    
    /**
     * Convierte Usuario + JWT a AuthResponse (CON MULTI-TENANT)
     * 
     * Se usa después de login/registro para construir la respuesta
     * 
     * @param usuario Usuario autenticado
     * @param token JWT generado
     * @return AuthResponse con toda la información del usuario incluyendo negocioId
     */
    public AuthResponse toAuthResponse(Usuario usuario, String token) {
        
        // Extrae nombres de roles
        Set<String> roles = usuario.getRoles().stream()
                .map(Rol::getName)
                .collect(Collectors.toSet());
        
        // Extrae nombres de permisos de todos los roles
        Set<String> permissions = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermissions().stream())
                .map(Permiso::getName)
                .collect(Collectors.toSet());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setType("Bearer");
        response.setEmail(usuario.getEmail());
        response.setName(usuario.getName());
        response.setNegocioId(usuario.getNegocioId());  // ← MULTI-TENANT
        response.setRoles(roles);
        response.setPermissions(permissions);
        
        return response;
    }
    
    /**
     * Convierte UserDetails + Usuario a AuthResponse
     * 
     * Alternativa cuando ya tienes el UserDetails de Spring Security
     * 
     * @param userDetails UserDetails de Spring Security
     * @param usuario Usuario desde BD
     * @param token JWT generado
     * @return AuthResponse
     */
    public AuthResponse toAuthResponse(UserDetails userDetails, Usuario usuario, String token) {
        
        // Extrae roles desde las authorities (quita el prefijo ROLE_)
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());
        
        // Extrae permisos (authorities sin prefijo ROLE_)
        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setType("Bearer");
        response.setEmail(usuario.getEmail());
        response.setName(usuario.getName());
        response.setNegocioId(usuario.getNegocioId());  // ← MULTI-TENANT
        response.setRoles(roles);
        response.setPermissions(permissions);
        
        return response;
    }
    
    /**
     * Extrae roles desde un Usuario
     * 
     * @param usuario Usuario con roles cargados
     * @return Set de nombres de roles
     */
    public Set<String> extractRoleNames(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(Rol::getName)
                .collect(Collectors.toSet());
    }
    
    /**
     * Extrae permisos desde un Usuario
     * 
     * @param usuario Usuario con roles y permisos cargados
     * @return Set de nombres de permisos
     */
    public Set<String> extractPermissionNames(Usuario usuario) {
        return usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermissions().stream())
                .map(Permiso::getName)
                .collect(Collectors.toSet());
    }
}
