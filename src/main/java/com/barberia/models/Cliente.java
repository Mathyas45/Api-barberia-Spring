package com.barberia.models;


import jakarta.persistence.*;
import lombok.Data;

@Data//esto es para generar los metodos get y set
@Entity//esto es para indicar que es una entidad de base de datos
@Table(name = "clientes")
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

    /**
     * Usuario asociado (opcional)
     * Solo existe si el cliente crea una cuenta
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Negocio (barbería / estética)
     * Multi-tenant
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    // -------------------------
    // ESTADO
    // -------------------------

    @Column(name = "reg_estado", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer regEstado;
}