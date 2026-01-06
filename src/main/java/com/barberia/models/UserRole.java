package com.barberia.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad intermedia para la relación ManyToMany entre Usuario y Rol
 * 
 * ¿POR QUÉ CREAR ESTA ENTIDAD?
 * 
 * 1. CLARIDAD: Hace explícita la tabla intermedia user_roles
 * 2. EXTENSIBILIDAD: Permite agregar campos adicionales en el futuro
 *    Ejemplo: fechaAsignacion, asignadoPor, motivo, etc.
 * 3. CONTROL: Mayor control sobre la relación (queries, cascades, etc.)
 * 
 * TABLA EN BD:
 * CREATE TABLE user_roles (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   usuario_id BIGINT,
 *   rol_id BIGINT,
 *   fecha_asignacion TIMESTAMP,
 *   FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
 *   FOREIGN KEY (rol_id) REFERENCES roles(id)
 * );
 * 
 * USO ACTUAL:
 * Por ahora solo relaciona Usuario y Rol, pero está lista para crecer.
 */
@Entity
@Table(name = "user_roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Relación ManyToOne con Usuario
     * 
     * Muchos UserRoles pueden apuntar al mismo Usuario
     * (un usuario puede tener múltiples roles)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    /**
     * Relación ManyToOne con Rol
     * 
     * Muchos UserRoles pueden apuntar al mismo Rol
     * (un rol puede estar asignado a múltiples usuarios)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
    
    // ======================================
    // CAMPOS OPCIONALES PARA FUTURO
    // ======================================
    // Descomenta estos campos si necesitas más información:
    
    // @Column(name = "fecha_asignacion")
    // private LocalDateTime fechaAsignacion;
    
    // @Column(name = "asignado_por")
    // private String asignadoPor;
    
    // @Column(name = "activo")
    // private Boolean activo = true;
}
