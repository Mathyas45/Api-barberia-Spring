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


    @Query("""
    SELECT r
    FROM Reserva r
    WHERE r.negocio.id = :negocioId
      AND (:profesionalId IS NULL OR r.profesional.id = :profesionalId)
      AND (:fechaDesde IS NULL OR :fechaHasta IS NULL OR r.fecha BETWEEN :fechaDesde AND :fechaHasta)
      AND (:clienteId IS NULL OR r.cliente.id = :clienteId)
      AND (:estado IS NULL OR r.estado = :estado)
      AND r.regEstado <> 0
""")
    List<Reserva> findByFilters(
            @Param("negocioId") Long negocioId,
            @Param("profesionalId") Long profesionalId,
            @Param("clienteId") Long clienteId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("estado") String estado
    );

    // Buscar reservas por estado de registro (útil para activas/eliminadas)
    List<Reserva> findByRegEstadoNot(Integer regEstado);

    // Buscar reservas de un cliente específico
    List<Reserva> findByClienteIdAndRegEstadoNot(Long clienteId, Integer regEstado);

    // Buscar reservas de un profesional en una fecha
    List<Reserva> findByProfesionalIdAndFechaAndRegEstadoNot(Long profesionalId, LocalDate fecha, Integer regEstado);

    List<Reserva> findByProfesionalIdAndFecha(Long profesionalId, LocalDate fecha);
}
