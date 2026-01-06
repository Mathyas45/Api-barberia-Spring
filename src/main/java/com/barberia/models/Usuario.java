package com.barberia.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Usuario del sistema (MULTI-TENANT)
 * 
 * ARQUITECTURA:
 * - Representa usuarios del panel administrativo (ADMIN, BARBERO, RECEPCIONISTA)
 * - NO representa clientes de la barbería
 * - Relación Many-to-Many con Rol
 * - Cada rol tiene permisos específicos
 * 
 * MULTI-TENANT:
 * ════════════════════════════════════════════════════════════════════════════════
 * - Cada usuario pertenece a UN negocio específico (negocio_id)
 * - Los usuarios solo pueden ver/editar datos de su propio negocio
 * - El negocio_id se usa como discriminador en todas las consultas
 * 
 * EJEMPLO:
 * - Juan (ADMIN, negocio_id=1) → Solo ve datos de Barbería "El Estilo"
 * - Pedro (BARBERO, negocio_id=2) → Solo ve datos de Barbería "Corte Moderno"
 */
@Data
@Builder

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;
    
    /**
     * MULTI-TENANT: ID del negocio al que pertenece este usuario
     * 
     * IMPORTANTE:
     * - Este campo es OBLIGATORIO para todos los usuarios
     * - Define qué barbería puede gestionar este usuario
     * - Se usa como filtro en TODAS las consultas
     * 
     * EJEMPLOS:
     * - negocio_id = 1 → Usuario de "Barbería El Estilo"
     * - negocio_id = 2 → Usuario de "Barbería Corte Moderno"
     */
    @Column(name = "negocio_id", nullable = false)
    private Long negocioId;
    
    /**
     * Relación con Negocio (Many-to-One)
     * Un negocio tiene muchos usuarios
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Negocio negocio;

    @Column(name = "reg_estado", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer regEstado;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relación Many-to-Many con Role
     *
     * fetch = FetchType.EAGER: Carga los roles SIEMPRE que se carga el usuario
     * (necesario para Spring Security)
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
