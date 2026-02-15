package com.barberia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad Negocio para modelo Multi-tenant
 * 
 * ARQUITECTURA MULTI-TENANT:
 * ════════════════════════════════════════════════════════════════════════════════
 * - Cada barbería es un "Negocio" independiente
 * - Todas las barberías comparten la misma base de datos
 * - La columna negocio_id distingue los datos de cada barbería
 * 
 * EJEMPLO:
 * - Barbería "El Estilo" → negocio_id = 1
 * - Barbería "Corte Moderno" → negocio_id = 2
 * - Barbería "Look Premium" → negocio_id = 3
 * 
 * RELACIONES:
 * - Un Negocio tiene muchos Usuarios (usuarios del panel administrativo)
 * - Un Negocio tiene muchas Citas
 * - Un Negocio tiene muchos Servicios
 * - Un Negocio tiene muchos Clientes
 * 
 * @author Barberia Team
 */
@Entity
@Table(name = "negocios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Negocio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nombre comercial de la barbería
     * Ejemplo: "Barbería El Estilo", "Corte Moderno"
     */
    @Column(nullable = false, length = 100)
    private String nombre;
    
    /**
     * RUC o identificación fiscal del negocio
     */
    @Column(unique = true, length = 20)
    private String ruc;
    
    /**
     * Dirección física del negocio
     */
    @Column(length = 200)
    private String direccion;
    
    /**
     * Teléfono de contacto
     */
    @Column(length = 20)
    private String telefono;
    
    /**
     * Email de contacto del negocio
     */
    @Column(length = 100)
    private String email;
    
    /**
     * Estado del negocio: ACTIVO, SUSPENDIDO, INACTIVO
     */
    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

//color elegido para el negocio, para personalizar la apariencia del panel administrativo
    @Column(length = 7)
    private String colorPrincipal = "#000000"; // valor por defecto
    
    /**
     * Fecha de registro del negocio
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Fecha de última actualización
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
