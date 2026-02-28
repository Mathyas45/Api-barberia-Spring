package com.barberia.dto.Profesional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfesionalRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    public String nombreCompleto;

    @Size(max = 50, message = "El documento de identidad no puede exceder 50 caracteres")
    public String documentoIdentidad;

    public LocalDate fechaNacimiento;

    public String telefono;

    public String direccion;

    @Size(max = 150, message = "La especialidad no puede exceder 150 caracteres")
    public String especialidad;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    public String descripcion;

    public Boolean usaHorarioNegocio;
}
