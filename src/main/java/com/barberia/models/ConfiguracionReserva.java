package com.barberia.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "configuracion_reservas",
        uniqueConstraints = @UniqueConstraint(columnNames = "negocio_id"))// unique constraint sirve para que no haya dos configuraciones para el mismo negocio
@EntityListeners({AuditingEntityListener.class})
public class ConfiguracionReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    //  No reservar con poca anticipación
    @Column(nullable = true)
    private Integer anticipacionHoras;

    // Hasta cuántos días adelante
    @Column(nullable = true)
    private Integer anticipacionMaximaDias;

    //  Permite reservar el mismo día
    @Column(nullable = false)
    private Boolean permiteMismoDia = true;

    //  Intervalo entre turnos
    @Column(nullable = true)
    private Integer intervaloTurnosMinutos;

    @Column(nullable = true)
    private Integer anticipacionMinimaHoras;

    @Column(nullable = false)
    private Boolean permiteCancelacion = true;

    // Cancelaciones permitidas hasta
    @Column(nullable = true)
    private Integer horasMinimasCancelacion;

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
