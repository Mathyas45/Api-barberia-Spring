package com.barberia.services;

import com.barberia.dto.reserva.ReservaRequest;
import com.barberia.dto.reserva.ReservaResponse;
import com.barberia.mappers.ReservaMapper;
import com.barberia.models.*;
import com.barberia.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        if(reserva.getHoraInicio().isBefore(reserva.getHoraFin())){
            throw new RuntimeException("La hora de inicio debe ser menor  a la hora de fin");
        }


        Boolean permiteMismodia = configReserva.getPermiteMismoDia();
        Integer anticipacionDias = configReserva.getAnticipacionMaximaDias();
        Integer anticipacionHoras = configReserva.getAnticipacionHoras();


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

        LocalTime horaFinCalculada = reserva.getHoraInicio().plusMinutes(duracionTotal);

        if (reservaRepository.existsByProfesionalIdAndFechaAndHoraInicioLessThanEqualAndHoraFinGreaterThanEqual((request.getProfesionalId()),
                request.getFecha(), request.getHoraInicio(), horaFinCalculada)) {

            throw new RuntimeException("El horario se cruza con un horario existente para el mismo profesional en el mismo día.");
        }

        reserva.setDuracionTotalMinutos(duracionTotal);
        reserva.setHoraFin(reserva.getHoraInicio().plusMinutes(duracionTotal));
        reserva.setPrecioTotal(precioTotal);

        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }


}
