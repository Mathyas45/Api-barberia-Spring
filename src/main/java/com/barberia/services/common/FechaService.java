package com.barberia.services.common;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class FechaService {

    public Integer calcularEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return null;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        return calcularEdad(fechaNacimiento) != null &&
                calcularEdad(fechaNacimiento) >= 18;
    }

}
