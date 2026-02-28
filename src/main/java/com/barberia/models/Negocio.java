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
     * Slug único para la URL pública del negocio
     * Ejemplo: "barberia-el-estilo" → /barberia-el-estilo
     * Se genera automáticamente del nombre si no se proporciona
     */
    @Column(unique = true, length = 100)
    private String slug;

    /**
     * Tipo de negocio para adaptar textos e íconos en la web pública
     * Valores: barberia, spa, estetica, peluqueria, otro
     */
    @Column(name = "tipo_negocio", length = 30)
    private String tipoNegocio = "barberia";
    
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
     * Descripción corta del negocio (se muestra en el hero de la web pública)
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Historia / descripción larga del negocio (sección "Sobre nosotros")
     */
    @Column(columnDefinition = "TEXT")
    private String historia;
    
    /**
     * Estado del negocio: ACTIVO, SUSPENDIDO, INACTIVO
     */
    private Boolean estado = true; // true = activo, false = inactivo/suspendido

    // ====== COLORES (theming dinámico) ======

    /**
     * Color principal del negocio (usado en dashboard y web pública)
     * Formato HEX: #RRGGBB
     */
    @Column(name = "color_principal", length = 7)
    private String colorPrincipal = "#000000";

    /**
     * Color secundario (fondo oscuro, textos, etc. en web pública)
     */
    @Column(name = "color_secundario", length = 7)
    private String colorSecundario = "#1e293b";

    /**
     * Color de acento (destacar botones, badges, etc. en web pública)
     */
    @Column(name = "color_acento", length = 7)
    private String colorAcento = "#f59e0b";

    // ====== IMÁGENES ======

    /**
     * URL o ruta del logo del negocio
     * Puede ser ruta local o URL externa (S3, Cloudinary, etc.)
     */
    @Column(length = 500)
    private String logoUrl;

    /**
     * Imagen principal del hero en la web pública
     */
    @Column(name = "hero_image_url", length = 500)
    private String heroImageUrl;

    // ====== REDES SOCIALES ======

    @Column(length = 255)
    private String facebook;

    @Column(length = 255)
    private String instagram;

    @Column(length = 255)
    private String tiktok;

    @Column(length = 255)
    private String whatsapp;

    /**
     * URL de Google Maps para la ubicación del negocio
     */
    @Column(name = "map_url", length = 500)
    private String mapUrl;

    /**
     * Modo de visualización del mapa en la web pública.
     * Valores: "mapa" (default) o "streetview"
     */
    @Column(name = "modo_mapa", length = 20)
    private String modoMapa = "mapa";

    // ====== CONTENIDO DINÁMICO (textos personalizables de la web pública) ======

    /**
     * Título personalizado del hero (si null, se usa el nombre del negocio).
     * Ejemplo: "Tu Estilo, Nuestra Pasión"
     */
    @Column(name = "hero_titulo", length = 150)
    private String heroTitulo;

    /**
     * Subtítulo del hero (si null, se genera automáticamente según tipoNegocio).
     * Ejemplo: "Cortes, barba y estilo que hablan por ti"
     */
    @Column(name = "hero_subtitulo", length = 300)
    private String heroSubtitulo;

    /**
     * Texto del botón CTA principal del hero (si null, se usa "Reservar Cita").
     * Ejemplo: "Agenda tu Turno"
     */
    @Column(name = "cta_texto", length = 60)
    private String ctaTexto;

    /**
     * Texto corto para la sección "¿Por qué elegirnos?" (features)
     */
    @Column(name = "por_que_elegirnos", columnDefinition = "TEXT")
    private String porQueElegirnos;

    /**
     * Ciudad / Localización del negocio (para SEO y mostrar en la web)
     */
    @Column(length = 100)
    private String ciudad;

    // ====== TESTIMONIOS (máximo 3, configurables desde el panel) ======

    @Column(name = "testimonio1_nombre", length = 100)
    private String testimonio1Nombre;

    @Column(name = "testimonio1_texto", columnDefinition = "TEXT")
    private String testimonio1Texto;

    @Column(name = "testimonio2_nombre", length = 100)
    private String testimonio2Nombre;

    @Column(name = "testimonio2_texto", columnDefinition = "TEXT")
    private String testimonio2Texto;

    @Column(name = "testimonio3_nombre", length = 100)
    private String testimonio3Nombre;

    @Column(name = "testimonio3_texto", columnDefinition = "TEXT")
    private String testimonio3Texto;

    // ====== SEO (metadatos para motores de búsqueda) ======

    /**
     * Título SEO personalizado (se usa en <title> y og:title).
     * Si null, se genera: "{nombre} — Reserva tu cita"
     */
    @Column(name = "seo_titulo", length = 70)
    private String seoTitulo;

    /**
     * Meta description para Google (máx 160 chars).
     * Si null, se genera de la descripción del negocio.
     */
    @Column(name = "seo_descripcion", length = 160)
    private String seoDescripcion;

    /**
     * Palabras clave para SEO (separadas por coma).
     * Ejemplo: "barbería lima, corte de cabello, barba"
     */
    @Column(name = "seo_keywords", length = 255)
    private String seoKeywords;

    /**
     * URL de imagen para OpenGraph / redes sociales.
     * Si null, se usa heroImageUrl.
     */
    @Column(name = "og_image_url", length = 500)
    private String ogImageUrl;

    // ====== BLOQUEO DE CONFIGURACIÓN ======

    /**
     * Si true, los campos sensibles (slug, colores, SEO, etc.) quedan
     * bloqueados en el formulario de configuración para evitar
     * cambios accidentales que rompan la web pública.
     * Solo ADMIN puede activar/desactivar este candado.
     */
    @Column(name = "configuracion_cerrada")
    private Boolean configuracionCerrada = false;
    
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
        // Generar slug automáticamente si no se proporcionó
        if (slug == null || slug.isBlank()) {
            slug = generarSlug(nombre);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Genera un slug URL-friendly a partir de un texto
     * "Barbería El Estilo" → "barberia-el-estilo"
     */
    public static String generarSlug(String texto) {
        if (texto == null) return "";
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
