package com.barberia.dto.negocio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NegocioRequest {

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

    // Testimonios (opcionales)
    private String testimonio1Nombre;
    private String testimonio1Texto;
    private String testimonio2Nombre;
    private String testimonio2Texto;
    private String testimonio3Nombre;
    private String testimonio3Texto;

    private String colorPrincipal;
    private String colorSecundario;
    private String colorAcento;

    private String facebook;
    private String instagram;
    private String tiktok;
    private String whatsapp;
    private String mapUrl;
    private String modoMapa;

    private Boolean configuracionCerrada;
}
