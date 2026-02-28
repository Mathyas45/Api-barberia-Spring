package com.barberia.dto.negocio;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para actualizar un negocio
 * Se usa con multipart/form-data cuando se puede incluir logo y heroImage
 *
 * Incluye campos para la web pública del tenant:
 * - Colores (principal, secundario, acento)
 * - Descripción y historia
 * - Redes sociales
 * - Tipo de negocio y slug
 */
@Data
public class NegocioUpdateRequest {

    @NotBlank(message = "El nombre del negocio es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El slug no puede exceder 100 caracteres")
    private String slug;

    @Size(max = 30, message = "El tipo de negocio no puede exceder 30 caracteres")
    private String tipoNegocio;

    @Size(max = 20, message = "El RUC no puede exceder 20 caracteres")
    private String ruc;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    private String descripcion;
    private String historia;

    // ====== COLORES ======
    private String colorPrincipal;
    private String colorSecundario;
    private String colorAcento;

    // ====== REDES SOCIALES ======
    @Size(max = 255)
    private String facebook;
    @Size(max = 255)
    private String instagram;
    @Size(max = 255)
    private String tiktok;
    @Size(max = 255)
    private String whatsapp;

    @Size(max = 500)
    private String mapUrl;

    @Size(max = 20, message = "El modo de mapa no puede exceder 20 caracteres")
    private String modoMapa;

    // ====== CONTENIDO DINÁMICO ======
    @Size(max = 150, message = "El título del hero no puede exceder 150 caracteres")
    private String heroTitulo;

    @Size(max = 300, message = "El subtítulo del hero no puede exceder 300 caracteres")
    private String heroSubtitulo;

    @Size(max = 60, message = "El texto del CTA no puede exceder 60 caracteres")
    private String ctaTexto;

    private String porQueElegirnos;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    // ====== TESTIMONIOS ======
    @Size(max = 100, message = "El nombre del testimonio no puede exceder 100 caracteres")
    private String testimonio1Nombre;
    private String testimonio1Texto;

    @Size(max = 100, message = "El nombre del testimonio no puede exceder 100 caracteres")
    private String testimonio2Nombre;
    private String testimonio2Texto;

    @Size(max = 100, message = "El nombre del testimonio no puede exceder 100 caracteres")
    private String testimonio3Nombre;
    private String testimonio3Texto;

    // ====== SEO ======
    @Size(max = 70, message = "El título SEO no puede exceder 70 caracteres")
    private String seoTitulo;

    @Size(max = 160, message = "La descripción SEO no puede exceder 160 caracteres")
    private String seoDescripcion;

    @Size(max = 255, message = "Las keywords no pueden exceder 255 caracteres")
    private String seoKeywords;

    @Size(max = 500)
    private String ogImageUrl;

    // ====== BLOQUEO ======
    private Boolean configuracionCerrada;
}
