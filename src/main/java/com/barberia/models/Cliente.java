package com.barberia.models;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data//esto es para generar los metodos get y set
@Entity//esto es para indicar que es una entidad de base de datos
@Table(name = "clientes")
@EntityListeners({AuditingEntityListener.class, com.barberia.config.NegocioEntityListener.class})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false)
    private String nombreCompleto;

    @Column(length = 50)
    private String documentoIdentidad;

    @Column(length = 20, nullable = false)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(name = "reg_estado", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer regEstado;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Este método se ejecuta automáticamente ANTES de cada UPDATE
    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @CreatedBy
    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro")
    private Usuario usuarioRegistroId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;


}