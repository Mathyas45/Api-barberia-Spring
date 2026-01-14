package com.barberia.models;

import com.barberia.models.enums.DiaSemana;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;
import java.time.LocalTime;
@Data//esto es para generar los metodos get y set
@Entity//esto es para indicar que es una entidad de base de datos
@Table(name = "horarios_negocio")
public class HorarioNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Integer regEstado;

    @CreatedBy
    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro", nullable = false)
    private Usuario usuarioRegistroId;

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
}