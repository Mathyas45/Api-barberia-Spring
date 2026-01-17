package com.barberia.repositories;

import com.barberia.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Boolean existsByProfesionalIdAndFechaAndHoraInicioLessThanEqualAndHoraFinGreaterThanEqual(Long profesionalId, java.time.LocalDate fecha, java.time.LocalTime horaFin, java.time.LocalTime horaInicio);


}
