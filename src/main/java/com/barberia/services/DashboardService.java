package com.barberia.services;

import com.barberia.dto.dashboard.*;
import com.barberia.models.Reserva;
import com.barberia.models.ReservaServicio;
import com.barberia.models.Usuario;
import com.barberia.repositories.DashboardRepository;
import com.barberia.repositories.UsuarioRepository;
import com.barberia.services.common.SecurityContextService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================
 * SERVICE - DASHBOARD
 * ============================================================
 * Genera las métricas, gráficos y widgets del dashboard.
 *
 * REGLA MULTITENANCY: Todas las consultas usan el negocio_id
 * extraído del JWT del usuario autenticado.
 *
 * REGLA DE ROLES:
 * - Si el usuario tiene permisos administrativos (FULL_ACCESS,
 *   VIEW_DASHBOARD_ADMIN) → Dashboard administrativo
 * - Si no → Dashboard operativo (agenda personal)
 *
 * FILTRO DE PERÍODO:
 * - "dia"    → Hoy
 * - "semana" → Últimos 7 días (lun-dom de la semana actual)
 * - "mes"    → Mes actual
 */
@Service
public class DashboardService {

    private static final String TIPO_ADMINISTRATIVO = "administrativo";
    private static final String TIPO_OPERATIVO = "operativo";

    private static final Set<String> PERIODOS_VALIDOS = Set.of("dia", "semana", "mes");

    // Permisos que otorgan acceso al dashboard administrativo
    private static final Set<String> PERMISOS_ADMIN = Set.of(
            "FULL_ACCESS",
            "VIEW_DASHBOARD_ADMIN"
    );

    // Roles que por nombre otorgan acceso administrativo
    private static final Set<String> ROLES_ADMIN = Set.of(
            "ROLE_ADMIN",
            "ROLE_MANAGER",
            "ROLE_SUPER_ADMIN"
    );

    private final DashboardRepository dashboardRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityContextService securityContextService;

    public DashboardService(DashboardRepository dashboardRepository,
                            UsuarioRepository usuarioRepository,
                            SecurityContextService securityContextService) {
        this.dashboardRepository = dashboardRepository;
        this.usuarioRepository = usuarioRepository;
        this.securityContextService = securityContextService;
    }

    /**
     * Genera el resumen del dashboard según permisos del usuario.
     * @param periodo "dia", "semana" o "mes" (default: "dia")
     */
    @Transactional(readOnly = true)
    public DashboardResumenResponse getResumen(String periodo) {
        // Validar periodo
        if (periodo == null || !PERIODOS_VALIDOS.contains(periodo.toLowerCase())) {
            periodo = "dia";
        }
        periodo = periodo.toLowerCase();

        Long negocioId = securityContextService.getNegocioIdFromContext();
        String username = securityContextService.getUsernameFromContext();

        // Obtener datos del usuario
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));

        String nombreNegocio = usuario.getNegocio() != null
                ? usuario.getNegocio().getNombre()
                : "Mi Negocio";

        // Determinar tipo de dashboard basado en permisos
        boolean esAdmin = tieneAccesoAdministrativo();

        if (esAdmin) {
            return buildDashboardAdministrativo(negocioId, usuario.getName(), nombreNegocio, periodo);
        } else {
            return buildDashboardOperativo(negocioId, usuario, nombreNegocio, periodo);
        }
    }

    // ============================================================
    // VALIDACIÓN DE PERMISOS
    // ============================================================

    /**
     * Verifica si el usuario autenticado tiene permisos administrativos.
     * No confía solo en el nombre del rol, también verifica permisos específicos.
     */
    private boolean tieneAccesoAdministrativo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        // Verificar por rol
        boolean tieneRolAdmin = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLES_ADMIN::contains);

        // Verificar por permiso específico
        boolean tienePermisoAdmin = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PERMISOS_ADMIN::contains);

        return tieneRolAdmin || tienePermisoAdmin;
    }

    // ============================================================
    // DASHBOARD ADMINISTRATIVO
    // ============================================================

    private DashboardResumenResponse buildDashboardAdministrativo(Long negocioId,
                                                                    String nombreUsuario,
                                                                    String nombreNegocio,
                                                                    String periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        // Calcular rango según periodo
        LocalDate inicioPeriodo;
        LocalDate finPeriodo = hoy;
        LocalDate inicioPeriodoAnterior;
        LocalDate finPeriodoAnterior;

        switch (periodo) {
            case "semana":
                inicioPeriodo = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1); // lunes
                finPeriodo = inicioPeriodo.plusDays(6);
                inicioPeriodoAnterior = inicioPeriodo.minusDays(7);
                finPeriodoAnterior = inicioPeriodo.minusDays(1);
                break;
            case "mes":
                inicioPeriodo = hoy.withDayOfMonth(1);
                finPeriodo = hoy;
                inicioPeriodoAnterior = inicioPeriodo.minusMonths(1);
                finPeriodoAnterior = inicioPeriodo.minusDays(1);
                break;
            default: // "dia"
                inicioPeriodo = hoy;
                finPeriodo = hoy;
                inicioPeriodoAnterior = ayer;
                finPeriodoAnterior = ayer;
                break;
        }

        String etiquetaPeriodo = getEtiquetaPeriodo(periodo);

        // ---- MÉTRICAS ----
        List<MetricaDTO> metricas = new ArrayList<>();

        // Reservas del período
        Long reservasPeriodo = "dia".equals(periodo)
                ? dashboardRepository.countReservasByFecha(negocioId, hoy)
                : dashboardRepository.countReservasByRangoFechas(negocioId, inicioPeriodo, finPeriodo);
        Long reservasPeriodoAnt = "dia".equals(periodo)
                ? dashboardRepository.countReservasByFecha(negocioId, ayer)
                : dashboardRepository.countReservasByRangoFechas(negocioId, inicioPeriodoAnterior, finPeriodoAnterior);
        Double cambioReservas = calcularCambioPorcentual(reservasPeriodoAnt, reservasPeriodo);
        metricas.add(MetricaDTO.builder()
                .clave("reservas_periodo")
                .etiqueta("Reservas " + etiquetaPeriodo)
                .valor(reservasPeriodo)
                .cambioPorcentual(cambioReservas)
                .icono("calendar")
                .build());

        // Clientes totales (no depende del período)
        Long clientesTotales = dashboardRepository.countClientesActivos(negocioId);
        metricas.add(MetricaDTO.builder()
                .clave("clientes_totales")
                .etiqueta("Clientes Totales")
                .valor(clientesTotales)
                .icono("users")
                .build());

        // Clientes nuevos en el período
        Long clientesNuevos = dashboardRepository.countClientesNuevosDesde(
                negocioId, inicioPeriodo.atStartOfDay());
        metricas.add(MetricaDTO.builder()
                .clave("clientes_nuevos")
                .etiqueta("Nuevos " + etiquetaPeriodo)
                .valor(clientesNuevos)
                .icono("user-plus")
                .build());

        // Profesionales activos (no depende del período)
        Long profesionalesActivos = dashboardRepository.countProfesionalesActivos(negocioId);
        metricas.add(MetricaDTO.builder()
                .clave("profesionales_activos")
                .etiqueta("Profesionales Activos")
                .valor(profesionalesActivos)
                .icono("briefcase")
                .build());

        // Cancelaciones del período
        Long cancelaciones = dashboardRepository.countCancelaciones(negocioId, inicioPeriodo, finPeriodo);
        metricas.add(MetricaDTO.builder()
                .clave("cancelaciones")
                .etiqueta("Cancelaciones " + etiquetaPeriodo)
                .valor(cancelaciones)
                .icono("x-circle")
                .build());

        // Ingreso estimado del período
        BigDecimal ingresoPeriodo = "dia".equals(periodo)
                ? dashboardRepository.sumaIngresosByFecha(negocioId, hoy)
                : dashboardRepository.sumaIngresosByRango(negocioId, inicioPeriodo, finPeriodo);
        BigDecimal ingresoPeriodoAnt = "dia".equals(periodo)
                ? dashboardRepository.sumaIngresosByFecha(negocioId, ayer)
                : dashboardRepository.sumaIngresosByRango(negocioId, inicioPeriodoAnterior, finPeriodoAnterior);
        Double cambioIngreso = calcularCambioPorcentualDecimal(ingresoPeriodoAnt, ingresoPeriodo);
        metricas.add(MetricaDTO.builder()
                .clave("ingreso_periodo")
                .etiqueta("Ingreso " + etiquetaPeriodo)
                .valor(ingresoPeriodo)
                .cambioPorcentual(cambioIngreso)
                .prefijo("S/")
                .icono("dollar-sign")
                .build());

        // Ingreso promedio diario del período
        long diasPeriodo = java.time.temporal.ChronoUnit.DAYS.between(inicioPeriodo, finPeriodo) + 1;
        BigDecimal ingresoPromedio = ingresoPeriodo != null && diasPeriodo > 0
                ? ingresoPeriodo.divide(BigDecimal.valueOf(diasPeriodo), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        metricas.add(MetricaDTO.builder()
                .clave("ingreso_promedio")
                .etiqueta("Ingreso Prom. Diario")
                .valor(ingresoPromedio)
                .prefijo("S/")
                .icono("trending-up")
                .build());

        // ---- GRÁFICOS ----
        List<GraficoDTO> graficos = new ArrayList<>();

        // Reservas agrupadas por día del período (línea)
        LocalDate desdeGrafico = "dia".equals(periodo) ? hoy.minusDays(6) : inicioPeriodo;
        List<Object[]> reservasPorDia = dashboardRepository.countReservasAgrupadasPorDia(
                negocioId, desdeGrafico, finPeriodo);
        graficos.add(buildGraficoReservasPorDia(reservasPorDia, desdeGrafico, finPeriodo));

        // Ocupación por horario (barra)
        List<Object[]> reservasPorHora = dashboardRepository.countReservasPorHora(
                negocioId, inicioPeriodo, finPeriodo);
        graficos.add(buildGraficoOcupacionHorario(reservasPorHora));

        // Top servicios (pastel)
        List<Object[]> topServicios = dashboardRepository.topServicios(negocioId, inicioPeriodo, finPeriodo);
        graficos.add(buildGraficoTopServicios(topServicios));

        // Rendimiento por profesional (barra) — reservas + ingresos
        List<Object[]> rendimiento = dashboardRepository.rendimientoPorProfesional(
                negocioId, inicioPeriodo, finPeriodo);
        graficos.add(buildGraficoRendimientoProfesional(rendimiento));

        // NUEVO: Desglose de servicios por profesional (barra horizontal)
        List<Object[]> serviciosPorPro = dashboardRepository.countServiciosPorProfesional(
                negocioId, inicioPeriodo, finPeriodo);
        graficos.add(buildGraficoServiciosPorProfesional(serviciosPorPro));

        // NUEVO: Ingresos por profesional con promedio (barra comparativa)
        List<Object[]> ingresosPorPro = dashboardRepository.ingresosPorProfesional(
                negocioId, inicioPeriodo, finPeriodo);
        graficos.add(buildGraficoIngresosPorProfesional(ingresosPorPro, diasPeriodo));

        // NUEVO: Comparativa de ingresos por día (area)
        List<Object[]> ingresosPorDia = dashboardRepository.ingresosAgrupadosPorDia(
                negocioId, desdeGrafico, finPeriodo);
        graficos.add(buildGraficoIngresosPorDia(ingresosPorDia, desdeGrafico, finPeriodo));

        // ---- WIDGETS ----
        List<WidgetDTO> widgets = new ArrayList<>();

        // Próximas reservas
        List<Reserva> proximasReservas = dashboardRepository.findProximasReservas(
                negocioId, hoy, LocalTime.now());
        widgets.add(buildWidgetProximasReservas(proximasReservas));

        // Actividad reciente
        List<Reserva> actividadReciente = dashboardRepository.findActividadReciente(
                negocioId, PageRequest.of(0, 10));
        widgets.add(buildWidgetActividadReciente(actividadReciente));

        return DashboardResumenResponse.builder()
                .tipoDashboard(TIPO_ADMINISTRATIVO)
                .nombreUsuario(nombreUsuario)
                .nombreNegocio(nombreNegocio)
                .periodoActivo(periodo)
                .metricas(metricas)
                .graficos(graficos)
                .widgets(widgets)
                .build();
    }

    // ============================================================
    // DASHBOARD OPERATIVO
    // ============================================================

    private DashboardResumenResponse buildDashboardOperativo(Long negocioId,
                                                              Usuario usuario,
                                                              String nombreNegocio,
                                                              String periodo) {
        LocalDate hoy = LocalDate.now();

        // Calcular rango según periodo
        LocalDate inicioPeriodo;
        LocalDate finPeriodo = hoy;
        switch (periodo) {
            case "semana":
                inicioPeriodo = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                finPeriodo = inicioPeriodo.plusDays(6);
                break;
            case "mes":
                inicioPeriodo = hoy.withDayOfMonth(1);
                break;
            default:
                inicioPeriodo = hoy;
                break;
        }

        List<WidgetDTO> widgets = new ArrayList<>();

        // Buscar si el usuario está asociado a un profesional
        Long profesionalId = findProfesionalIdByUsuario(usuario);

        if (profesionalId != null) {
            // Agenda del día del profesional
            List<Reserva> agendaDelDia = dashboardRepository.findAgendaProfesional(
                    negocioId, profesionalId, hoy);
            widgets.add(buildWidgetAgendaDelDia(agendaDelDia));
        } else {
            // Si no es profesional, mostrar las reservas del día del negocio (vista limitada)
            List<Reserva> reservasHoy = dashboardRepository.findReservasDelDia(negocioId, hoy);
            widgets.add(buildWidgetAgendaDelDia(reservasHoy));
        }

        // Métricas del período para el operativo
        List<MetricaDTO> metricas = new ArrayList<>();
        String etiquetaPeriodo = getEtiquetaPeriodo(periodo);

        // Reservas del período (todo el negocio o del profesional según disponibilidad)
        Long reservasPeriodo = "dia".equals(periodo)
                ? dashboardRepository.countReservasByFecha(negocioId, hoy)
                : dashboardRepository.countReservasByRangoFechas(negocioId, inicioPeriodo, finPeriodo);
        metricas.add(MetricaDTO.builder()
                .clave("reservas_periodo")
                .etiqueta("Reservas " + etiquetaPeriodo)
                .valor(reservasPeriodo)
                .icono("calendar")
                .build());

        if (profesionalId != null) {
            List<Reserva> agendaCompleta = dashboardRepository.findAgendaProfesional(
                    negocioId, profesionalId, hoy);
            long pendientes = agendaCompleta.stream()
                    .filter(r -> r.getEstado() != null && "PENDIENTE".equals(r.getEstado().name()))
                    .count();
            long atendidas = agendaCompleta.stream()
                    .filter(r -> r.getEstado() != null && "ATENDIDA".equals(r.getEstado().name()))
                    .count();

            metricas.add(MetricaDTO.builder()
                    .clave("pendientes").etiqueta("Pendientes Hoy").valor(pendientes).icono("clock").build());
            metricas.add(MetricaDTO.builder()
                    .clave("atendidas").etiqueta("Atendidas Hoy").valor(atendidas).icono("check-circle").build());
        }

        // Ingreso del período
        BigDecimal ingresoPeriodo = "dia".equals(periodo)
                ? dashboardRepository.sumaIngresosByFecha(negocioId, hoy)
                : dashboardRepository.sumaIngresosByRango(negocioId, inicioPeriodo, finPeriodo);
        metricas.add(MetricaDTO.builder()
                .clave("ingreso_periodo")
                .etiqueta("Ingreso " + etiquetaPeriodo)
                .valor(ingresoPeriodo)
                .prefijo("S/")
                .icono("dollar-sign")
                .build());

        return DashboardResumenResponse.builder()
                .tipoDashboard(TIPO_OPERATIVO)
                .nombreUsuario(usuario.getName())
                .nombreNegocio(nombreNegocio)
                .periodoActivo(periodo)
                .metricas(metricas)
                .graficos(Collections.emptyList())
                .widgets(widgets)
                .build();
    }

    // ============================================================
    // BUILDERS DE GRÁFICOS
    // ============================================================

    private GraficoDTO buildGraficoReservasPorDia(List<Object[]> datos,
                                                    LocalDate desde, LocalDate hasta) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        // Crear mapa con todos los días, incluso sin datos
        Map<LocalDate, Long> mapaReservas = new LinkedHashMap<>();
        LocalDate fecha = desde;
        while (!fecha.isAfter(hasta)) {
            mapaReservas.put(fecha, 0L);
            fecha = fecha.plusDays(1);
        }
        // Rellenar con datos reales
        for (Object[] row : datos) {
            LocalDate f = (LocalDate) row[0];
            Long count = (Long) row[1];
            mapaReservas.put(f, count);
        }

        List<Map<String, Object>> puntos = mapaReservas.entrySet().stream()
                .map(e -> {
                    Map<String, Object> punto = new LinkedHashMap<>();
                    punto.put("nombre", e.getKey().format(formatter));
                    punto.put("fecha", e.getKey().toString());
                    punto.put("reservas", e.getValue());
                    return punto;
                })
                .collect(Collectors.toList());

        return GraficoDTO.builder()
                .clave("reservas_ultimos_7_dias")
                .titulo("Reservas - Últimos 7 Días")
                .tipo("linea")
                .datos(puntos)
                .build();
    }

    private GraficoDTO buildGraficoOcupacionHorario(List<Object[]> datos) {
        List<Map<String, Object>> puntos = datos.stream()
                .map(row -> {
                    Map<String, Object> punto = new LinkedHashMap<>();
                    Integer hora = ((Number) row[0]).intValue();
                    punto.put("nombre", String.format("%02d:00", hora));
                    punto.put("hora", hora);
                    punto.put("reservas", ((Number) row[1]).longValue());
                    return punto;
                })
                .collect(Collectors.toList());

        return GraficoDTO.builder()
                .clave("ocupacion_por_horario")
                .titulo("Ocupación por Horario")
                .tipo("barra")
                .datos(puntos)
                .build();
    }

    private GraficoDTO buildGraficoTopServicios(List<Object[]> datos) {
        List<Map<String, Object>> puntos = datos.stream()
                .limit(8)
                .map(row -> {
                    Map<String, Object> punto = new LinkedHashMap<>();
                    punto.put("nombre", (String) row[0]);
                    punto.put("cantidad", ((Number) row[1]).longValue());
                    return punto;
                })
                .collect(Collectors.toList());

        return GraficoDTO.builder()
                .clave("top_servicios")
                .titulo("Servicios más Solicitados")
                .tipo("pastel")
                .datos(puntos)
                .build();
    }

    private GraficoDTO buildGraficoRendimientoProfesional(List<Object[]> datos) {
        List<Map<String, Object>> puntos = datos.stream()
                .map(row -> {
                    Map<String, Object> punto = new LinkedHashMap<>();
                    punto.put("nombre", (String) row[0]);
                    punto.put("reservas", ((Number) row[1]).longValue());
                    punto.put("ingresos", ((Number) row[2]).doubleValue());
                    return punto;
                })
                .collect(Collectors.toList());

        return GraficoDTO.builder()
                .clave("rendimiento_por_profesional")
                .titulo("Rendimiento por Profesional")
                .tipo("barra")
                .datos(puntos)
                .build();
    }

    private GraficoDTO buildGraficoServiciosPorProfesional(List<Object[]> datos) {
        List<Map<String, Object>> puntos = datos.stream()
                .map(row -> {
                    Map<String, Object> punto = new LinkedHashMap<>();
                    punto.put("nombre", (String) row[0]);
                    punto.put("servicios", ((Number) row[1]).longValue());
                    return punto;
                })
                .collect(Collectors.toList());

        return GraficoDTO.builder()
                .clave("servicios_por_profesional")
                .titulo("Servicios por Profesional")
                .tipo("barra")
                .datos(puntos)
                .build();
    }

    private GraficoDTO buildGraficoIngresosPorProfesional(List<Object[]> datos, long diasPeriodo) {
        List<Map<String, Object>> puntos = datos.stream()
                .map(row -> {
                    Map<String, Object> punto = new LinkedHashMap<>();
                    punto.put("nombre", (String) row[0]);
                    double totalIngresos = ((Number) row[1]).doubleValue();
                    long diasConReservas = ((Number) row[2]).longValue();
                    double promedioDiario = diasPeriodo > 0 ? totalIngresos / diasPeriodo : 0;
                    punto.put("total", Math.round(totalIngresos * 100.0) / 100.0);
                    punto.put("promedio", Math.round(promedioDiario * 100.0) / 100.0);
                    punto.put("diasActivos", diasConReservas);
                    return punto;
                })
                .collect(Collectors.toList());

        return GraficoDTO.builder()
                .clave("ingresos_por_profesional")
                .titulo("Ingresos por Profesional")
                .tipo("barra")
                .datos(puntos)
                .build();
    }

    private GraficoDTO buildGraficoIngresosPorDia(List<Object[]> datos,
                                                    LocalDate desde, LocalDate hasta) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, Double> mapaIngresos = new LinkedHashMap<>();
        LocalDate fecha = desde;
        while (!fecha.isAfter(hasta)) {
            mapaIngresos.put(fecha, 0.0);
            fecha = fecha.plusDays(1);
        }
        for (Object[] row : datos) {
            LocalDate f = (LocalDate) row[0];
            Double monto = ((Number) row[1]).doubleValue();
            mapaIngresos.put(f, monto);
        }

        List<Map<String, Object>> puntos = mapaIngresos.entrySet().stream()
                .map(e -> {
                    Map<String, Object> punto = new LinkedHashMap<>();
                    punto.put("nombre", e.getKey().format(formatter));
                    punto.put("fecha", e.getKey().toString());
                    punto.put("ingresos", Math.round(e.getValue() * 100.0) / 100.0);
                    return punto;
                })
                .collect(Collectors.toList());

        return GraficoDTO.builder()
                .clave("ingresos_por_dia")
                .titulo("Ingresos por Día")
                .tipo("area")
                .datos(puntos)
                .build();
    }

    // ============================================================
    // BUILDERS DE WIDGETS
    // ============================================================

    private WidgetDTO buildWidgetProximasReservas(List<Reserva> reservas) {
        List<WidgetItemDTO> items = reservas.stream()
                .limit(10)
                .map(r -> WidgetItemDTO.builder()
                        .id(r.getId())
                        .hora(r.getHoraInicio() != null ? r.getHoraInicio().toString() : null)
                        .horaFin(r.getHoraFin() != null ? r.getHoraFin().toString() : null)
                        .cliente(r.getCliente() != null ? r.getCliente().getNombreCompleto() : "Sin cliente")
                        .profesional(r.getProfesional() != null ? r.getProfesional().getNombreCompleto() : "Sin asignar")
                        .servicio(getServiciosResumen(r))
                        .duracionMinutos(r.getDuracionTotalMinutos())
                        .estado(r.getEstado() != null ? r.getEstado().name() : null)
                        .precio(r.getPrecioTotal())
                        .fecha(r.getFecha() != null ? r.getFecha().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return WidgetDTO.builder()
                .clave("proximas_reservas")
                .titulo("Próximas Reservas")
                .tipo("tabla")
                .items(items)
                .build();
    }

    private WidgetDTO buildWidgetActividadReciente(List<Reserva> reservas) {
        List<WidgetItemDTO> items = reservas.stream()
                .map(r -> WidgetItemDTO.builder()
                        .id(r.getId())
                        .descripcion(buildDescripcionActividad(r))
                        .estado(r.getEstado() != null ? r.getEstado().name() : null)
                        .timestamp(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                        .cliente(r.getCliente() != null ? r.getCliente().getNombreCompleto() : null)
                        .profesional(r.getProfesional() != null ? r.getProfesional().getNombreCompleto() : null)
                        .fecha(r.getFecha() != null ? r.getFecha().toString() : null)
                        .hora(r.getHoraInicio() != null ? r.getHoraInicio().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return WidgetDTO.builder()
                .clave("actividad_reciente")
                .titulo("Actividad Reciente")
                .tipo("timeline")
                .items(items)
                .build();
    }

    private WidgetDTO buildWidgetAgendaDelDia(List<Reserva> reservas) {
        List<WidgetItemDTO> items = reservas.stream()
                .map(r -> WidgetItemDTO.builder()
                        .id(r.getId())
                        .hora(r.getHoraInicio() != null ? r.getHoraInicio().toString() : null)
                        .horaFin(r.getHoraFin() != null ? r.getHoraFin().toString() : null)
                        .cliente(r.getCliente() != null ? r.getCliente().getNombreCompleto() : "Sin cliente")
                        .servicio(getServiciosResumen(r))
                        .duracionMinutos(r.getDuracionTotalMinutos())
                        .estado(r.getEstado() != null ? r.getEstado().name() : null)
                        .precio(r.getPrecioTotal())
                        .fecha(r.getFecha() != null ? r.getFecha().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return WidgetDTO.builder()
                .clave("agenda_del_dia")
                .titulo("Agenda del Día")
                .tipo("tabla")
                .items(items)
                .build();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private String getEtiquetaPeriodo(String periodo) {
        switch (periodo) {
            case "semana": return "(Semana)";
            case "mes": return "(Mes)";
            default: return "Hoy";
        }
    }

    private String getServiciosResumen(Reserva reserva) {
        if (reserva.getServicios() == null || reserva.getServicios().isEmpty()) {
            return "Sin servicios";
        }
        return reserva.getServicios().stream()
                .map(rs -> rs.getServicio() != null ? rs.getServicio().getNombre() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    private String buildDescripcionActividad(Reserva r) {
        String cliente = r.getCliente() != null ? r.getCliente().getNombreCompleto() : "Cliente";
        String estado = r.getEstado() != null ? r.getEstado().name().toLowerCase() : "registrada";
        return String.format("Reserva de %s - %s", cliente, estado);
    }

    private Double calcularCambioPorcentual(Long anterior, Long actual) {
        if (anterior == null || anterior == 0) {
            return actual != null && actual > 0 ? 100.0 : 0.0;
        }
        return Math.round(((actual.doubleValue() - anterior.doubleValue()) / anterior.doubleValue()) * 100 * 10) / 10.0;
    }

    private Double calcularCambioPorcentualDecimal(BigDecimal anterior, BigDecimal actual) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return actual != null && actual.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        double cambio = actual.subtract(anterior)
                .divide(anterior, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        return Math.round(cambio * 10) / 10.0;
    }

    /**
     * Busca el ID del profesional asociado al usuario.
     * Un usuario puede estar vinculado a un profesional por email.
     */
    private Long findProfesionalIdByUsuario(Usuario usuario) {
        // Intentar buscar por la relación directa usuario → profesional
        // En esta arquitectura, no hay relación directa. Se puede buscar por email o nombre.
        // Por ahora retornamos null para que use la vista general.
        // TODO: Implementar vinculación usuario-profesional cuando se agregue la relación
        return null;
    }
}
