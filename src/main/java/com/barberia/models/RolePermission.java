package com.barberia.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad intermedia para la relación ManyToMany entre Rol y Permiso
 * 
 * ¿POR QUÉ CREAR ESTA ENTIDAD?
 * 
 * 1. CLARIDAD: Hace explícita la tabla intermedia role_permissions
 * 2. EXTENSIBILIDAD: Permite agregar campos adicionales en el futuro
 *    Ejemplo: fechaAsignacion, condiciones, restricciones, etc.
 * 3. CONTROL: Mayor control sobre la relación
 * 
 * TABLA EN BD:
 * CREATE TABLE role_permissions (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   rol_id BIGINT,
 *   permiso_id BIGINT,
 *   fecha_asignacion TIMESTAMP,
 *   FOREIGN KEY (rol_id) REFERENCES roles(id),
 *   FOREIGN KEY (permiso_id) REFERENCES permisos(id)
 * );
 * 
 * EJEMPLO DE USO FUTURO:
 * Podrías agregar:
 * - Permisos temporales con fecha de expiración
 * - Permisos condicionales (solo en horario laboral)
 * - Restricciones por IP o ubicación
 */
@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Relación ManyToOne con Rol
     * 
     * Muchos RolePermissions pueden apuntar al mismo Rol
     * (un rol puede tener múltiples permisos)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
    
    /**
     * Relación ManyToOne con Permiso
     * 
     * Muchos RolePermissions pueden apuntar al mismo Permiso
     * (un permiso puede estar en múltiples roles)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permiso_id", nullable = false)
    private Permiso permiso;
    
    // ======================================
    // CAMPOS OPCIONALES PARA FUTURO
    // ======================================
    // Descomenta estos campos si necesitas más control:
    
    // @Column(name = "fecha_asignacion")
    // private LocalDateTime fechaAsignacion;
    
    // @Column(name = "fecha_expiracion")
    // private LocalDateTime fechaExpiracion;
    
    // @Column(name = "condiciones")
    // private String condiciones;  // JSON con condiciones
    
    // @Column(name = "activo")
    // private Boolean activo = true;
}
