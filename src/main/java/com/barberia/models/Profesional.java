package com.barberia.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data//esto es para generar los metodos get y set
@Entity
@Table(name = "profesionales")
@EntityListeners({AuditingEntityListener.class, com.barberia.config.NegocioEntityListener.class})
public class Profesional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120, nullable = false)
    private String nombreCompleto;

    @Column(length = 50, nullable = true)
    private String documentoIdentidad;

    @Column(name = "fecha_nacimiento", nullable = true)
    private LocalDate fechaNacimiento;

    @Column(length = 15, nullable = true)
    private String telefono;

    @Column(length = 255, nullable = true)
    private String direccion;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;


}
