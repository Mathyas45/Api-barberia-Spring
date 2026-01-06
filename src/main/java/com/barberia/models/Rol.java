package com.barberia.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // ADMIN, USER, MANAGER

    @Column(length = 255)
    private String description; // Descripción del rol

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Relación Many-to-Many con Permission
     *
     * @ManyToMany: Un rol puede tener muchos permisos Y un permiso puede estar en muchos roles
     *
     * TABLA INTERMEDIA (role_permissions):
     * - role_id: FK a roles.id
     * - permission_id: FK a permissions.id
     *
     * fetch = FetchType.EAGER: Carga los permisos siempre que se carga el rol
     *
     * EJEMPLO:
     * Role ADMIN podría tener: READ_CLIENTS, CREATE_CLIENTS, DELETE_CLIENTS
     * Role USER podría tener: READ_CLIENTS
     */

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private Set<Permiso> permissions = new HashSet<>();

    /**
     * Relación inversa con User
     *
     * mappedBy: Indica que la relación es manejada por el lado de User (campo 'roles')
     * LAZY: No se carga automáticamente para evitar bucles
     * JsonIgnore: Evita serialización circular
     * ToString.Exclude y EqualsAndHashCode.Exclude: Evita bucles infinitos en Lombok
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Usuario> users = new HashSet<>();

}


