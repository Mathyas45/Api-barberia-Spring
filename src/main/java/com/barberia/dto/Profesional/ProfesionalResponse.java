package com.barberia.dto.Profesional;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfesionalResponse {
    public long id;
    public String nombreCompleto;
    public String documentoIdentidad;
    public LocalDate fechaNacimiento;
    public Integer edad;
    public String telefono;
    public String direccion;
    public String especialidad;
    public String descripcion;
    public String fotoUrl;
    public Boolean usaHorarioNegocio;
    public int regEstado;
    public Long usuarioRegistroId;
    public Long negocioId;
}
