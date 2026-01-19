package com.barberia.repositories;

import com.barberia.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Boolean existsByProfesionalIdAndFechaAndHoraInicioLessThanEqualAndHoraFinGreaterThanEqual(Long profesionalId, java.time.LocalDate fecha, java.time.LocalTime horaFin, java.time.LocalTime horaInicio);

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reserva r
        WHERE r.profesional.id = :profesionalId
          AND r.fecha = :fecha
          AND (
            (:horaInicioNueva >= r.horaInicio AND :horaInicioNueva < r.horaFin) OR
            (:horaFinNueva > r.horaInicio AND :horaFinNueva <= r.horaFin) OR
            (:horaInicioNueva <= r.horaInicio AND :horaFinNueva >= r.horaFin)
          )
          AND r.regEstado <> 0
          AND r.estado <> 'CANCELADA'
    """)
    Boolean existsOverlappingReserva(
        @Param("profesionalId") Long profesionalId,
        @Param("fecha") LocalDate fecha,
        @Param("horaInicioNueva") LocalTime horaInicioNueva,
        @Param("horaFinNueva") LocalTime horaFinNueva
    );

    @Query("""
    SELECT COUNT(r) > 0
    FROM Reserva r
    WHERE r.profesional.id = :profesionalId
      AND r.id <> :reservaId
      AND r.fecha = :fecha
      AND (
        (:horaInicio >= r.horaInicio AND :horaInicio < r.horaFin) OR
        (:horaFin > r.horaInicio AND :horaFin <= r.horaFin) OR
        (:horaInicio <= r.horaInicio AND :horaFin >= r.horaFin)
      )
""")
    Boolean existsOverlappingReservaExcludingId(
            Long reservaId,
            Long profesionalId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    );


    List<Reserva> findByProfesionalIdOrfechaContainingIgnoreCase(Long profesionalId, LocalDate fecha);
    List<Reserva> findByRegEstadoNot(int regEstado);

    @Query("""
        SELECT r
        FROM Reserva r
        WHERE (:profesionalId IS NULL OR r.profesional.id = :profesionalId)
          AND (:fecha IS NULL OR r.fecha = :fecha)
          AND (:clienteId IS NULL OR r.cliente.id = :clienteId)
          AND r.regEstado <> 0
    """)
    List<Reserva> findByFilters(
        @Param("profesionalId") Long profesionalId,
        @Param("fecha") LocalDate fecha,
        @Param("clienteId") Long clienteId
    );


}
