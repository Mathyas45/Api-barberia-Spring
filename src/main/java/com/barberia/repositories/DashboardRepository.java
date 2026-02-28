package com.barberia.repositories;

import com.barberia.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================
 * REPOSITORY - DASHBOARD
 * ============================================================
 * Consultas optimizadas con agregaciones SQL para el dashboard.
 * Todas las queries están filtradas por negocio_id (multitenancy).
 *
 * Se usa la entidad Reserva como base ya que el dashboard gira
 * en torno a reservas, pero incluye queries cruzadas con clientes,
 * profesionales y servicios.
 */
@Repository
public interface DashboardRepository extends JpaRepository<Reserva, Long> {

    // ========== MÉTRICAS DE RESERVAS ==========

    /** Cuenta reservas activas de un negocio en una fecha dada */
    @Query("SELECT COUNT(r) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha = :fecha " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA'")
    Long countReservasByFecha(@Param("negocioId") Long negocioId,
                              @Param("fecha") LocalDate fecha);

    /** Cuenta reservas activas en un rango de fechas */
    @Query("SELECT COUNT(r) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA'")
    Long countReservasByRangoFechas(@Param("negocioId") Long negocioId,
                                    @Param("fechaDesde") LocalDate fechaDesde,
                                    @Param("fechaHasta") LocalDate fechaHasta);

    /** Cuenta cancelaciones en un rango de fechas */
    @Query("SELECT COUNT(r) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado = 'CANCELADA'")
    Long countCancelaciones(@Param("negocioId") Long negocioId,
                            @Param("fechaDesde") LocalDate fechaDesde,
                            @Param("fechaHasta") LocalDate fechaHasta);

    // ========== MÉTRICAS DE CLIENTES ==========

    /** Total de clientes activos del negocio */
    @Query("SELECT COUNT(c) FROM Cliente c " +
            "WHERE c.negocio.id = :negocioId " +
            "AND c.regEstado <> 0")
    Long countClientesActivos(@Param("negocioId") Long negocioId);

    /** Clientes nuevos registrados en un rango de fechas */
    @Query("SELECT COUNT(c) FROM Cliente c " +
            "WHERE c.negocio.id = :negocioId " +
            "AND c.regEstado <> 0 " +
            "AND c.createdAt >= :desde")
    Long countClientesNuevosDesde(@Param("negocioId") Long negocioId,
                                   @Param("desde") java.time.LocalDateTime desde);

    // ========== MÉTRICAS DE PROFESIONALES ==========

    /** Profesionales activos del negocio */
    @Query("SELECT COUNT(p) FROM Profesional p " +
            "WHERE p.negocio.id = :negocioId " +
            "AND p.regEstado <> 0")
    Long countProfesionalesActivos(@Param("negocioId") Long negocioId);

    // ========== INGRESOS ESTIMADOS ==========

    /** Ingreso estimado (suma de precios) de reservas no canceladas en una fecha */
    @Query("SELECT COALESCE(SUM(r.precioTotal), 0) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha = :fecha " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA'")
    java.math.BigDecimal sumaIngresosByFecha(@Param("negocioId") Long negocioId,
                                              @Param("fecha") LocalDate fecha);

    /** Ingreso estimado en un rango de fechas */
    @Query("SELECT COALESCE(SUM(r.precioTotal), 0) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA'")
    java.math.BigDecimal sumaIngresosByRango(@Param("negocioId") Long negocioId,
                                              @Param("fechaDesde") LocalDate fechaDesde,
                                              @Param("fechaHasta") LocalDate fechaHasta);

    // ========== GRÁFICOS: RESERVAS POR DÍA (últimos N días) ==========

    /** Reservas agrupadas por día en un rango. Retorna [fecha, count] */
    @Query("SELECT r.fecha, COUNT(r) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA' " +
            "GROUP BY r.fecha " +
            "ORDER BY r.fecha")
    List<Object[]> countReservasAgrupadasPorDia(@Param("negocioId") Long negocioId,
                                                 @Param("fechaDesde") LocalDate fechaDesde,
                                                 @Param("fechaHasta") LocalDate fechaHasta);

    // ========== GRÁFICOS: OCUPACIÓN POR HORARIO ==========

    /** Reservas agrupadas por hora de inicio. Retorna [horaInicio, count] */
    @Query("SELECT FUNCTION('HOUR', r.horaInicio), COUNT(r) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA' " +
            "GROUP BY FUNCTION('HOUR', r.horaInicio) " +
            "ORDER BY FUNCTION('HOUR', r.horaInicio)")
    List<Object[]> countReservasPorHora(@Param("negocioId") Long negocioId,
                                         @Param("fechaDesde") LocalDate fechaDesde,
                                         @Param("fechaHasta") LocalDate fechaHasta);

    // ========== GRÁFICOS: TOP SERVICIOS ==========

    /** Top servicios más solicitados. Retorna [servicioNombre, count] */
    @Query("SELECT rs.servicio.nombre, COUNT(rs) FROM ReservaServicio rs " +
            "WHERE rs.reserva.negocio.id = :negocioId " +
            "AND rs.reserva.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND rs.reserva.regEstado <> 0 " +
            "AND rs.reserva.estado <> 'CANCELADA' " +
            "GROUP BY rs.servicio.nombre " +
            "ORDER BY COUNT(rs) DESC")
    List<Object[]> topServicios(@Param("negocioId") Long negocioId,
                                 @Param("fechaDesde") LocalDate fechaDesde,
                                 @Param("fechaHasta") LocalDate fechaHasta);

    // ========== GRÁFICOS: RENDIMIENTO POR PROFESIONAL ==========

    /** Reservas por profesional. Retorna [profesionalNombre, count, sumaIngresos] */
    @Query("SELECT r.profesional.nombreCompleto, COUNT(r), COALESCE(SUM(r.precioTotal), 0) " +
            "FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA' " +
            "GROUP BY r.profesional.nombreCompleto " +
            "ORDER BY COUNT(r) DESC")
    List<Object[]> rendimientoPorProfesional(@Param("negocioId") Long negocioId,
                                              @Param("fechaDesde") LocalDate fechaDesde,
                                              @Param("fechaHasta") LocalDate fechaHasta);

    // ========== WIDGETS: PRÓXIMAS RESERVAS ==========

    /** Próximas reservas del negocio (hoy, después de la hora actual) */
    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN FETCH r.cliente " +
            "LEFT JOIN FETCH r.profesional " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha = :fecha " +
            "AND r.horaInicio >= :horaActual " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA' " +
            "ORDER BY r.horaInicio ASC")
    List<Reserva> findProximasReservas(@Param("negocioId") Long negocioId,
                                        @Param("fecha") LocalDate fecha,
                                        @Param("horaActual") java.time.LocalTime horaActual);

    /** Todas las reservas de hoy del negocio (para widget) */
    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN FETCH r.cliente " +
            "LEFT JOIN FETCH r.profesional " +
            "LEFT JOIN FETCH r.servicios rs " +
            "LEFT JOIN FETCH rs.servicio " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha = :fecha " +
            "AND r.regEstado <> 0 " +
            "ORDER BY r.horaInicio ASC")
    List<Reserva> findReservasDelDia(@Param("negocioId") Long negocioId,
                                      @Param("fecha") LocalDate fecha);

    // ========== DASHBOARD OPERATIVO: AGENDA DEL PROFESIONAL ==========

    /** Reservas del día de un profesional específico */
    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN FETCH r.cliente " +
            "LEFT JOIN FETCH r.servicios rs " +
            "LEFT JOIN FETCH rs.servicio " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.profesional.id = :profesionalId " +
            "AND r.fecha = :fecha " +
            "AND r.regEstado <> 0 " +
            "ORDER BY r.horaInicio ASC")
    List<Reserva> findAgendaProfesional(@Param("negocioId") Long negocioId,
                                         @Param("profesionalId") Long profesionalId,
                                         @Param("fecha") LocalDate fecha);

    /** Actividad reciente: últimas reservas creadas o actualizadas */
    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN FETCH r.cliente " +
            "LEFT JOIN FETCH r.profesional " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.regEstado <> 0 " +
            "ORDER BY r.createdAt DESC")
    List<Reserva> findActividadReciente(@Param("negocioId") Long negocioId,
                                         org.springframework.data.domain.Pageable pageable);

    // ========== DESGLOSE POR PROFESIONAL CON PERÍODO ==========

    /**
     * Servicios (ReservaServicio count) por profesional en un rango.
     * Retorna [profesionalNombre, countServicios]
     */
    @Query("SELECT r.profesional.nombreCompleto, COUNT(DISTINCT rs) " +
            "FROM ReservaServicio rs " +
            "JOIN rs.reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA' " +
            "GROUP BY r.profesional.nombreCompleto " +
            "ORDER BY COUNT(DISTINCT rs) DESC")
    List<Object[]> countServiciosPorProfesional(@Param("negocioId") Long negocioId,
                                                 @Param("fechaDesde") LocalDate fechaDesde,
                                                 @Param("fechaHasta") LocalDate fechaHasta);

    /**
     * Ingreso promedio diario por profesional en un rango.
     * Retorna [profesionalNombre, promedioIngresoDiario, totalIngresos, diasConReservas]
     */
    @Query("SELECT r.profesional.nombreCompleto, " +
            "COALESCE(SUM(r.precioTotal), 0), " +
            "COUNT(DISTINCT r.fecha) " +
            "FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA' " +
            "GROUP BY r.profesional.nombreCompleto " +
            "ORDER BY SUM(r.precioTotal) DESC")
    List<Object[]> ingresosPorProfesional(@Param("negocioId") Long negocioId,
                                           @Param("fechaDesde") LocalDate fechaDesde,
                                           @Param("fechaHasta") LocalDate fechaHasta);

    /**
     * Ingresos agrupados por día en un rango (para comparativas).
     * Retorna [fecha, sumaIngresos]
     */
    @Query("SELECT r.fecha, COALESCE(SUM(r.precioTotal), 0) FROM Reserva r " +
            "WHERE r.negocio.id = :negocioId " +
            "AND r.fecha BETWEEN :fechaDesde AND :fechaHasta " +
            "AND r.regEstado <> 0 " +
            "AND r.estado <> 'CANCELADA' " +
            "GROUP BY r.fecha " +
            "ORDER BY r.fecha")
    List<Object[]> ingresosAgrupadosPorDia(@Param("negocioId") Long negocioId,
                                            @Param("fechaDesde") LocalDate fechaDesde,
                                            @Param("fechaHasta") LocalDate fechaHasta);
}
