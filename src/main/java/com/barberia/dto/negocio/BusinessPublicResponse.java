package com.barberia.dto.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para el endpoint público /api/public/business/{slug}
 * Contiene TODOS los datos necesarios para renderizar la web pública del tenant.
 *
 * IMPORTANTE: Este DTO NO expone información sensible (RUC, estado, auditoría)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessPublicResponse {

    // ====== IDENTIFICACIÓN ======
    private String slug;
    private String nombre;
    private String tipoNegocio;

    // ====== TEXTOS ======
    private String descripcion;
    private String historia;

    // ====== VISUAL / IMÁGENES ======
    private String logoUrl;
    private String heroImageUrl;
    private List<String> galeriaUrls;

    // ====== COLORES (theming dinámico) ======
    private String colorPrimary;
    private String colorSecondary;
    private String colorAccent;

    // ====== CONTACTO ======
    private String direccion;
    private String telefono;
    private String email;
    private String mapUrl;
    /** Modo de visualización del mapa: "mapa" o "streetview" */
    private String modoMapa;

    // ====== CONTENIDO DINÁMICO ======
    /** Título personalizado del hero (null = usar nombre) */
    private String heroTitulo;
    /** Subtítulo del hero (null = generar según tipoNegocio) */
    private String heroSubtitulo;
    /** Texto del botón CTA principal (null = "Reservar Cita") */
    private String ctaTexto;
    /** Texto para sección "¿Por qué elegirnos?" */
    private String porQueElegirnos;
    /** Ciudad / ubicación (para SEO y display) */
    private String ciudad;

    // ====== SEO ======
    private String seoTitulo;
    private String seoDescripcion;
    private String seoKeywords;
    private String ogImageUrl;

    // ====== REDES SOCIALES ======
    private RedesSocialesDto redesSociales;

    // ====== DATOS DE NEGOCIO ======
    private List<HorarioPublicoDto> horarios;
    private List<ServicioPublicoDto> servicios;
    private List<ProfesionalPublicoDto> profesionales;

    // ====== TESTIMONIOS ======
    private List<TestimonioPublicoDto> testimonios;

    // ========== DTOs internos ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedesSocialesDto {
        private String facebook;
        private String instagram;
        private String tiktok;
        private String whatsapp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HorarioPublicoDto {
        private String dia;        // "Lunes", "Martes", etc.
        private String apertura;   // "09:00"
        private String cierre;     // "20:00"
        private boolean cerrado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioPublicoDto {
        private Long id;
        private String nombre;
        private String descripcion;
        private Double precio;
        private Integer duracionMinutos;
        private String categoria;
        private String imagenUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfesionalPublicoDto {
        private Long id;
        private String nombre;
        private String especialidad;
        private String fotoUrl;
        private String descripcion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestimonioPublicoDto {
        /** Nombre del cliente que deja el testimonio */
        private String autor;
        /** Texto del testimonio */
        private String contenido;
    }
}
