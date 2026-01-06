package com.barberia.security;

import com.barberia.models.Permiso;
import com.barberia.models.Rol;
import com.barberia.models.Usuario;
import com.barberia.repositories.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio personalizado para cargar usuarios desde la base de datos
 * 
 * UserDetailsService:
 * Interfaz de Spring Security que define cómo cargar un usuario
 * Spring Security la usa para autenticación
 * 
 * RESPONSABILIDAD:
 * Cargar el usuario y sus authorities (roles + permisos) desde la base de datos
 * 
 * IMPORTANTE:
 * Este servicio es usado automáticamente por:
 * 1. JwtAuthenticationFilter (para validar tokens)
 * 2. AuthenticationManager (para login)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UsuarioRepository usuarioRepository;
    
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    /**
     * Carga un usuario por su email (username)
     * 
     * FLUJO:
     * 1. Busca el usuario en BD por email
     * 2. Si no existe, lanza UsernameNotFoundException
     * 3. Extrae sus roles y permisos
     * 4. Convierte todo a GrantedAuthority
     * 5. Retorna un UserDetails con toda la información
     * 
     * UserDetails:
     * Interfaz de Spring Security que representa un usuario autenticado
     * Contiene: username, password, authorities, flags (enabled, locked, etc.)
     * 
     * @param username Email del usuario (en nuestro caso)
     * @return UserDetails con el usuario y sus authorities
     * @throws UsernameNotFoundException Si no se encuentra el usuario
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. BUSCA EL USUARIO EN LA BASE DE DATOS
        // Usa la query personalizada que hace JOIN FETCH con roles y permisos
        // Esto evita el problema N+1 (múltiples queries)
        Usuario usuario = usuarioRepository.findByEmailWithRolesAndPermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + username
                ));
        
        // 2. EXTRAE LAS AUTHORITIES (ROLES + PERMISOS)
        Collection<GrantedAuthority> authorities = getAuthorities(usuario);
        
        // 3. RETORNA UN USERDETAILS
        // User.builder() es una clase de Spring Security que implementa UserDetails
        return User.builder()
                .username(usuario.getEmail())      // Email como username
                .password(usuario.getPassword())    // Password encriptado
                .authorities(authorities)           // Roles y permisos
                .accountExpired(false)              // Cuenta no expirada
                .accountLocked(false)               // Cuenta no bloqueada
                .credentialsExpired(false)          // Credenciales no expiradas
                .disabled(usuario.getRegEstado() != 1) // Deshabilitado si regEstado != 1
                .build();
    }
    
    /**
     * Extrae las authorities del usuario
     * 
     * AUTHORITIES EN SPRING SECURITY:
     * Son las "capacidades" del usuario (roles + permisos)
     * 
     * CONVENCIÓN:
     * - Roles: Prefijo "ROLE_" → "ROLE_ADMIN", "ROLE_USER"
     * - Permisos: Sin prefijo → "READ_CLIENTS", "CREATE_BOOKING"
     * 
     * ¿POR QUÉ ESTA CONVENCIÓN?
     * - @PreAuthorize("hasRole('ADMIN')") busca "ROLE_ADMIN"
     * - @PreAuthorize("hasAuthority('READ_CLIENTS')") busca "READ_CLIENTS"
     * 
     * EJEMPLO:
     * Usuario con rol ADMIN que tiene permisos READ_CLIENTS y CREATE_BOOKING
     * 
     * Authorities resultantes:
     * - ROLE_ADMIN
     * - READ_CLIENTS
     * - CREATE_BOOKING
     * 
     * @param usuario Usuario desde la BD
     * @return Collection de GrantedAuthority
     */
    private Collection<GrantedAuthority> getAuthorities(Usuario usuario) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // ITERA CADA ROL DEL USUARIO
        for (Rol rol : usuario.getRoles()) {
            
            // 1. AGREGA EL ROL CON EL PREFIJO "ROLE_"
            // "ADMIN" → "ROLE_ADMIN"
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getName()));
            
            // 2. AGREGA TODOS LOS PERMISOS DEL ROL
            // No llevan prefijo, se agregan tal cual
            Set<GrantedAuthority> permissions = rol.getPermissions().stream()
                    .map(permiso -> new SimpleGrantedAuthority(permiso.getName()))
                    .collect(Collectors.toSet());
            
            authorities.addAll(permissions);
        }
        
        return authorities;
    }
}
