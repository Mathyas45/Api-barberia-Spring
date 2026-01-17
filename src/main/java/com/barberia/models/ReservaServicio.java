package com.barberia.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "reserva_servicios")
public class ReservaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    private Servicio servicio;

    @Column(nullable = false)
    private Integer duracionMinutos;

    @Column(nullable = false)
    private BigDecimal precio;
}
