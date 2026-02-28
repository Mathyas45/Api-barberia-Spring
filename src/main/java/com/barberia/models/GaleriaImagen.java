package com.barberia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para las imágenes de la galería de cada negocio.
 * Se muestran en la sección "Galería" de la web pública del tenant.
 *
 * RELACIÓN: Un Negocio tiene muchas imágenes de galería.
 *
 * USO:
 * - El admin sube imágenes desde el panel de Configuración
 * - El endpoint público las devuelve como galeriaUrls[]
 * - Se ordenan por el campo 'orden' (ascendente)
 */
@Entity
@Table(name = "galeria_imagenes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaleriaImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    /**
     * URL o ruta de la imagen (relativa o absoluta)
     * Ejemplo: "/uploads/galeria/uuid-imagen.jpg"
     */
    @Column(name = "imagen_url", nullable = false, length = 500)
    private String imagenUrl;

    /**
     * Orden de visualización en la galería (menor = primero)
     */
    @Column(nullable = false)
    private Integer orden = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
