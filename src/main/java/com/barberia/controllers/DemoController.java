package com.barberia.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de ejemplo para demostrar protección de endpoints
 * 
 * ESTE CONTROLADOR DEMUESTRA:
 * 1. Endpoints protegidos que requieren autenticación
 * 2. Uso de @PreAuthorize con roles
 * 3. Uso de @PreAuthorize con permisos
 * 4. Combinación de roles y permisos
 * 5. Acceso a información del usuario autenticado
 * 
 * IMPORTANTE:
 * Todos estos endpoints requieren JWT válido en el header:
 * Authorization: Bearer <token>
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    
    /**
     * Endpoint protegido básico
     * 
     * GET /api/demo/protected
     * 
     * Requiere: Estar autenticado (cualquier usuario con JWT válido)
     * 
     * @return Mensaje de bienvenida con información del usuario
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        
        // Obtiene la autenticación actual del SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Acceso concedido a endpoint protegido");
        response.put("user", authentication.getName());  // Email del usuario
        response.put("authorities", authentication.getAuthorities());  // Roles y permisos
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint solo para ADMIN
     * 
     * GET /api/demo/admin
     * 
     * @PreAuthorize("hasRole('ADMIN')"):
     * - Verifica que el usuario tenga el rol ADMIN
     * - Si no lo tiene, retorna 403 Forbidden
     * - hasRole() busca automáticamente "ROLE_ADMIN"
     * 
     * Requiere: JWT válido + Rol ADMIN
     * 
     * @return Mensaje para administradores
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Solo los ADMIN pueden ver este contenido");
    }
    
    /**
     * Endpoint solo para USER
     * 
     * GET /api/demo/user
     * 
     * Requiere: JWT válido + Rol USER
     * 
     * @return Mensaje para usuarios
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> userEndpoint() {
        return ResponseEntity.ok("Solo los USER pueden ver este contenido");
    }
    
    /**
     * Endpoint con permiso específico
     * 
     * GET /api/demo/read-clients
     * 
     * @PreAuthorize("hasAuthority('READ_CLIENTS')"):
     * - Verifica que el usuario tenga el permiso READ_CLIENTS
     * - hasAuthority() busca exactamente "READ_CLIENTS" (sin prefijo)
     * - Más granular que hasRole()
     * 
     * Requiere: JWT válido + Permiso READ_CLIENTS
     * 
     * USO REAL:
     * En una barbería:
     * - ADMIN tiene READ_CLIENTS, CREATE_CLIENTS, DELETE_CLIENTS
     * - RECEPCIONISTA tiene READ_CLIENTS, CREATE_CLIENTS
     * - BARBERO tiene solo READ_CLIENTS
     * 
     * @return Lista simulada de clientes
     */
    @GetMapping("/read-clients")
    @PreAuthorize("hasAuthority('READ_CLIENTS')")
    public ResponseEntity<String> readClients() {
        return ResponseEntity.ok("Aquí estarían los clientes (requiere permiso READ_CLIENTS)");
    }
    
    /**
     * Endpoint con múltiples permisos (OR)
     * 
     * GET /api/demo/create-booking
     * 
     * @PreAuthorize("hasAnyAuthority('CREATE_BOOKING', 'MANAGE_BOOKING')"):
     * - Permite acceso si tiene CREATE_BOOKING O MANAGE_BOOKING
     * - Útil cuando varios permisos permiten la misma acción
     * 
     * Requiere: JWT válido + (CREATE_BOOKING O MANAGE_BOOKING)
     * 
     * @return Confirmación de reserva
     */
    @PostMapping("/create-booking")
    @PreAuthorize("hasAnyAuthority('CREATE_BOOKING', 'MANAGE_BOOKING')")
    public ResponseEntity<String> createBooking() {
        return ResponseEntity.ok("Reserva creada exitosamente");
    }
    
    /**
     * Endpoint con múltiples roles (OR)
     * 
     * GET /api/demo/manager-or-admin
     * 
     * @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')"):
     * - Permite acceso si tiene rol ADMIN O MANAGER
     * 
     * Requiere: JWT válido + (ADMIN O MANAGER)
     * 
     * @return Datos de gestión
     */
    @GetMapping("/manager-or-admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> managerOrAdmin() {
        return ResponseEntity.ok("Acceso concedido: eres ADMIN o MANAGER");
    }
    
    /**
     * Endpoint con condiciones combinadas (AND)
     * 
     * DELETE /api/demo/delete-user/{id}
     * 
     * @PreAuthorize("hasRole('ADMIN') and hasAuthority('DELETE_USERS')"):
     * - Requiere AMBAS condiciones: Rol ADMIN Y Permiso DELETE_USERS
     * - Útil para acciones críticas
     * 
     * Requiere: JWT válido + Rol ADMIN + Permiso DELETE_USERS
     * 
     * USO REAL:
     * Eliminar usuarios es crítico, debe ser ADMIN + permiso específico
     * 
     * @param id ID del usuario a eliminar
     * @return Confirmación de eliminación
     */
    @DeleteMapping("/delete-user/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('DELETE_USERS')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok("Usuario " + id + " eliminado (requiere ADMIN + DELETE_USERS)");
    }
    
    /**
     * Endpoint con expresión SpEL compleja
     * 
     * GET /api/demo/special-access
     * 
     * @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and hasAuthority('SPECIAL_ACCESS'))"):
     * - ADMIN: Acceso directo
     * - MANAGER: Solo si tiene permiso SPECIAL_ACCESS
     * - SpEL (Spring Expression Language): Permite lógica compleja
     * 
     * Requiere: JWT válido + (ADMIN O (MANAGER + SPECIAL_ACCESS))
     * 
     * @return Contenido especial
     */
    @GetMapping("/special-access")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and hasAuthority('SPECIAL_ACCESS'))")
    public ResponseEntity<String> specialAccess() {
        return ResponseEntity.ok("Acceso especial concedido");
    }
    
    /**
     * Endpoint sin autenticación (público dentro de ruta protegida)
     * 
     * GET /api/demo/info
     * 
     * NOTA: En SecurityConfig, /api/demo/** está protegido por defecto
     * Este endpoint es una excepción que deberías agregar en SecurityConfig si lo necesitas público
     * 
     * Por ahora requiere autenticación porque toda la ruta /api/demo/** lo requiere
     * 
     * @return Información general
     */
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok("Información general del sistema");
    }
}
