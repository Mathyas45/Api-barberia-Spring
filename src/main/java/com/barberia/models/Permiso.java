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
@Table(name = "permisos")
public class Permiso {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name; // READ_CLIENTS, CREATE_LOAN, etc.

    @Column(length = 255)
    private String description; // Descripción legible del permiso

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    /**
     * Relación inversa con Role
     *
     * mappedBy: La relación es manejada por Role (campo 'permissions')
     * LAZY: No se carga automáticamente para evitar bucles
     * JsonIgnore: Evita serialización circular
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();
}
