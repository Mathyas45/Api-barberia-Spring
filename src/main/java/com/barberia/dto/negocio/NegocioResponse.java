package com.barberia.dto.negocio;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NegocioResponse {
    private Long id;
    private String nombre;
    private String slug;
    private String tipoNegocio;
    private String ruc;
    private String direccion;
    private String telefono;
    private String email;
    private String descripcion;
    private String historia;
    private Boolean estado;

    // Colores
    private String colorPrincipal;
    private String colorSecundario;
    private String colorAcento;

    // Imágenes
    private String logoUrl;
    private String heroImageUrl;

    // Redes sociales
    private String facebook;
    private String instagram;
    private String tiktok;
    private String whatsapp;
    private String mapUrl;
    private String modoMapa;

    // Contenido dinámico
    private String heroTitulo;
    private String heroSubtitulo;
    private String ctaTexto;
    private String porQueElegirnos;
    private String ciudad;

    // Testimonios
    private String testimonio1Nombre;
    private String testimonio1Texto;
    private String testimonio2Nombre;
    private String testimonio2Texto;
    private String testimonio3Nombre;
    private String testimonio3Texto;

    // SEO
    private String seoTitulo;
    private String seoDescripcion;
    private String seoKeywords;
    private String ogImageUrl;

    // Bloqueo
    private Boolean configuracionCerrada;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
