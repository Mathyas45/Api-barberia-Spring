package com.barberia.services;

import com.barberia.dto.reserva.ReservaRequest;
import com.barberia.dto.reserva.ReservaResponse;
import com.barberia.mappers.ReservaMapper;
import com.barberia.models.*;
import com.barberia.models.enums.DiaSemana;
import com.barberia.models.enums.EstadoReserva;
import com.barberia.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaService {


    @Autowired
    private ProfesionalRepository profesionalRepository;
    @Autowired
    private NegocioRepository negocioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ServicioRepository servicioRepository;


    private final ReservaRepository reservaRepository;
    private final ConfiguracionReservaRepository configuracionReservaRepository;
    private final ReservaMapper reservaMapper;


    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper , ConfiguracionReservaRepository configuracionReservaRepository) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.configuracionReservaRepository = configuracionReservaRepository;
    }
    @Transactional
    public ReservaResponse create(ReservaRequest request) {

        Reserva reserva = reservaMapper.toEntity(request);
        Negocio negocio = negocioRepository.findById(request.getNegocioId())
                .orElseThrow(() -> new RuntimeException("Negocio no existe"));
        reserva.setNegocio(negocio);

        Profesional profesional = profesionalRepository.findById(request.getProfesionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no existe"));
        reserva.setProfesional(profesional);

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no existe"));
        reserva.setCliente(cliente);

        ConfiguracionReserva configReserva = configuracionReservaRepository
                .findByNegocioId(request.getNegocioId())
                .orElseThrow(() -> new RuntimeException("Configuración de reserva no encontrada para el negocio"));

        Boolean permiteMismodia = configReserva.getPermiteMismoDia();
        Integer anticipacionDias = configReserva.getAnticipacionMaximaDias();
        Integer anticipacionHoras = configReserva.getAnticipacionHoras();
        Integer anticipacionMinimaHoras = configReserva.getAnticipacionMinimaHoras();
        LocalDate fechaReserva = request.getFecha();
        LocalDate fechaActual = LocalDate.now();
        LocalDateTime fechaYHoraActual = LocalDateTime.now();

        if (fechaReserva == null || request.getHoraInicio() == null) {
            throw new RuntimeException("Fecha y hora son obligatorias");
        }

        if (fechaReserva.isBefore(fechaActual)) {
            throw new RuntimeException("No se puede reservar en fechas pasadas");
        }
        if (fechaReserva.isEqual(fechaActual)) {
            if (request.getHoraInicio().isBefore(LocalTime.now())) {
                throw new RuntimeException("No se puede reservar en una hora pasada");
            }
        }

        if (anticipacionMinimaHoras != null) {
            LocalDateTime inicioReserva =LocalDateTime.of(request.getFecha(), request.getHoraInicio());

            if (inicioReserva.isBefore(fechaYHoraActual.plusHours(anticipacionMinimaHoras))) {
                throw new RuntimeException(
                        "Debe reservar con al menos " + anticipacionMinimaHoras + " horas de anticipación"
                );
            }
        }

        if (fechaReserva.isBefore(fechaActual)) {
            throw new RuntimeException("No se puede reservar en fechas pasadas");
        }

        if(!permiteMismodia){
            if(fechaReserva.isEqual(fechaActual)){
                throw new RuntimeException("No se permiten reservas para el mismo día");
            }
        }
        if(anticipacionDias != null){
            LocalDate fechaLimiteDias = fechaActual.plusDays(anticipacionDias);
            if(fechaReserva.isAfter(fechaLimiteDias)){
                throw new RuntimeException("La reserva excede la anticipación máxima de días permitida" + anticipacionDias + " días)");
            }
        }
        if(anticipacionHoras != null){
            LocalDate fechaLimiteHoras = fechaActual.plusDays(anticipacionHoras / 24);
            if(fechaReserva.isAfter(fechaLimiteHoras)){
                throw new RuntimeException("La reserva excede la anticipación máxima de horas permitida(tiene que con anticipación de "+ anticipacionHoras + " horas)");
            }
        }
        List<Servicio> servicios = servicioRepository.findAllById(request.getServiciosIds());

        if (servicios == null || servicios.isEmpty()) {
            throw new RuntimeException("No se encontraron servicios o no se seleccionó ninguno.");
        }

        int duracionTotal = 0;
        BigDecimal precioTotal = BigDecimal.ZERO;
        for (Servicio servicio : servicios) {
            ReservaServicio rs = new ReservaServicio();
            rs.setReserva(reserva);
            rs.setServicio(servicio);
            rs.setDuracionMinutos(servicio.getDuracionMinutosAprox());
            rs.setPrecio(servicio.getPrecio());

            reserva.getServicios().add(rs);

            duracionTotal += servicio.getDuracionMinutosAprox();// suma la duración del servicio al total
            precioTotal = precioTotal.add(servicio.getPrecio());// suma el precio del servicio al total
        }

        LocalTime horaFinCalculada = reserva.getHoraInicio().plusMinutes(duracionTotal);// calcula la hora de fin sumando la duración total a la hora de inicio

        if (reservaRepository.existsOverlappingReserva( // verifica si hay reservas que se crucen en el mismo horario
                request.getProfesionalId(),
                request.getFecha(),
                request.getHoraInicio(),
                horaFinCalculada)) {
            throw new RuntimeException("El horario se cruza con un horario existente para el mismo profesional en el mismo día.");
        }

        reserva.setDuracionTotalMinutos(duracionTotal);
        reserva.setHoraFin(reserva.getHoraInicio().plusMinutes(duracionTotal));
        reserva.setPrecioTotal(precioTotal);

        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    @Transactional
    public ReservaResponse createAdmin(ReservaRequest request) {

        Reserva reserva = reservaMapper.toEntity(request);

        Negocio negocio = negocioRepository.findById(request.getNegocioId())
                .orElseThrow(() -> new RuntimeException("Negocio no existe"));
        reserva.setNegocio(negocio);

        Profesional profesional = profesionalRepository.findById(request.getProfesionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no existe"));
        reserva.setProfesional(profesional);

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no existe"));
        reserva.setCliente(cliente);

        ConfiguracionReserva configReserva = configuracionReservaRepository
                .findByNegocioId(request.getNegocioId())
                .orElseThrow(() -> new RuntimeException("Configuración de reserva no encontrada para el negocio"));


        if (request.getFecha() == null || request.getHoraInicio() == null) {
            throw new RuntimeException("Fecha y hora son obligatorias");
        }

        if (request.getFecha().isEqual(LocalDate.now())
                && request.getHoraInicio().isBefore(LocalTime.now())) {
            throw new RuntimeException("No se puede reservar en una hora pasada");
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);

        // Servicios
        List<Servicio> servicios = servicioRepository.findAllById(request.getServiciosIds());
        if (servicios.isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un servicio");
        }

        int duracionTotal = 0;
        BigDecimal precioTotal = BigDecimal.ZERO;

        for (Servicio servicio : servicios) {
            ReservaServicio rs = new ReservaServicio();
            rs.setReserva(reserva);
            rs.setServicio(servicio);
            rs.setDuracionMinutos(servicio.getDuracionMinutosAprox());
            rs.setPrecio(servicio.getPrecio());

            reserva.getServicios().add(rs);

            duracionTotal += servicio.getDuracionMinutosAprox();
            precioTotal = precioTotal.add(servicio.getPrecio());
        }

        LocalTime horaFin = request.getHoraInicio().plusMinutes(duracionTotal); // Calcula la hora de fin

        if (reservaRepository.existsOverlappingReserva(
                request.getProfesionalId(),
                request.getFecha(),
                request.getHoraInicio(),
                horaFin)) {
            throw new RuntimeException("El horario se cruza con otra reserva");
        }

        reserva.setDuracionTotalMinutos(duracionTotal);
        reserva.setHoraFin(horaFin);
        reserva.setPrecioTotal(precioTotal);

        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    @Transactional(readOnly = true)
    public ReservaResponse findById(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
        return reservaMapper.toResponse(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> findAll(Long profesionalId, LocalDate fecha, Long clienteId) {
        List<Reserva> reservas = reservaRepository.findByFilters(profesionalId, fecha, clienteId);
        return reservas.stream()
                .map(reservaMapper::toResponse)
                .toList();
    }
    @Transactional
    public ReservaResponse cambiarEstado(Long id, EstadoReserva nuevoEstado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
        reserva.setEstado(nuevoEstado);
        reserva.setEstado(EstadoReserva.CANCELADA);
        Reserva reservaActualizada = reservaRepository.save(reserva);
        return reservaMapper.toResponse(reservaActualizada);
    }
    @Transactional
    public ReservaResponse eliminar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
        reserva.setRegEstado(0);
        Reserva reservaActualizada = reservaRepository.save(reserva);
        return reservaMapper.toResponse(reservaActualizada);
    }

    @Transactional
    public ReservaResponse update(Long id, ReservaRequest request) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no existe"));

        reservaMapper.updateEntity(reserva, request);

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new RuntimeException("No se puede modificar una reserva cancelada");
        }
        reserva.getServicios().clear(); // orphanRemoval hace magia aquí, nos sirve para eliminar los servicios asociados anteriores

        List<Servicio> servicios = servicioRepository.findAllById(request.getServiciosIds());

        int duracionTotal = 0;
        BigDecimal precioTotal = BigDecimal.ZERO;

        for (Servicio servicio : servicios) {
            ReservaServicio rs = new ReservaServicio();
            rs.setReserva(reserva);
            rs.setServicio(servicio);
            rs.setDuracionMinutos(servicio.getDuracionMinutosAprox());
            rs.setPrecio(servicio.getPrecio());

            reserva.getServicios().add(rs);

            duracionTotal += servicio.getDuracionMinutosAprox();
            precioTotal = precioTotal.add(servicio.getPrecio());
        }

        reserva.setFecha(request.getFecha());
        reserva.setHoraInicio(request.getHoraInicio());
        reserva.setHoraFin(request.getHoraInicio().plusMinutes(duracionTotal));
        reserva.setDuracionTotalMinutos(duracionTotal);
        reserva.setPrecioTotal(precioTotal);

        // Validar solapamiento (excluyendo la misma reserva)
        if (reservaRepository.existsOverlappingReservaExcludingId(
                reserva.getId(),
                reserva.getProfesional().getId(),
                reserva.getFecha(),
                reserva.getHoraInicio(),
                reserva.getHoraFin())) {
            throw new RuntimeException("La nueva hora se cruza con otra reserva");
        }

        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }
}
