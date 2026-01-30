package com.barberia.services;

import com.barberia.dto.disponibilidad.DisponibilidadRequest;
import com.barberia.dto.disponibilidad.DisponibilidadResponse;
import com.barberia.models.*;
import com.barberia.models.enums.DiaSemana;
import com.barberia.repositories.*;
import com.barberia.services.common.SecurityContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadService {
    private final HorarioProfesionalRepository horarioProfesionalRepo;
    private final HorarioNegocioRepository horarioNegocioRepo;
    private final ServicioRepository servicioRepo;
    private final ConfiguracionReservaRepository configRepo;
    private final ProfesionalRepository profesionalRepository;
    private final ReservaRepository reservaRepository;
    private final SecurityContextService securityContextService;

    public DisponibilidadService(HorarioProfesionalRepository horarioProfesionalRepo,
                                 HorarioNegocioRepository horarioNegocioRepo,
                                 ServicioRepository servicioRepo,
                                 ConfiguracionReservaRepository configRepo,
                                 ProfesionalRepository profesionalRepository,
                                 ReservaRepository reservaRepository,
                                 SecurityContextService securityContextService) {
        this.reservaRepository = reservaRepository;
        this.horarioProfesionalRepo = horarioProfesionalRepo;
        this.horarioNegocioRepo = horarioNegocioRepo;
        this.servicioRepo = servicioRepo;
        this.configRepo = configRepo;
        this.profesionalRepository = profesionalRepository;
        this.securityContextService = securityContextService;
    }


    /**
     * Calcula la disponibilidad de un profesional para una fecha específica.
     * 
     * La disponibilidad se basa exclusivamente en:
     * 1. El horario del profesional (propio o del negocio)
     * 2. Las reservas existentes del profesional en esa fecha
     * 3. El intervalo entre turnos (tiempo de preparación después de cada reserva, puede ser 0)
     * 
     * El servicio NO define la disponibilidad base, pero debe validarse posteriormente
     * que su duración encaje en las horas disponibles mostradas.
     */
    @Transactional
    public DisponibilidadResponse disponibilidad(DisponibilidadRequest request) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();
        
        LocalDate fecha = request.getFecha();

        // Convertir el día de la semana
        DiaSemana diaSemana = DiaSemana.fromDayOfWeek(fecha.getDayOfWeek());

        // Obtener configuración del negocio para el intervalo entre turnos
        ConfiguracionReserva configReserva = configRepo.findByNegocioId(negocioId)
                .orElseThrow(() -> new RuntimeException("Configuración de reserva no existe"));
        int intervaloTurnosMinutos = configReserva.getIntervaloTurnosMinutos(); // Puede ser 0

        // Obtener información del profesional
        Profesional profesional = profesionalRepository.findById(request.getProfesionalId())
                .orElseThrow(() -> new RuntimeException("Profesional con ID " + request.getProfesionalId() + " no existe"));

        Boolean usaHorarioNegocio = profesional.getUsaHorarioNegocio();

        // Obtener todas las reservas activas del profesional para la fecha
        // (excluye las eliminadas para calcular disponibilidad real)
        List<Reserva> reservas = reservaRepository.findByProfesionalIdAndFechaAndRegEstadoNot(
                request.getProfesionalId(), 
                fecha, 
                0  // Excluir eliminadas (reg_estado = 0)
        );

        // Generar slots cada 15 minutos para mostrar disponibilidad granular
        // El cliente/frontend validará posteriormente si el servicio seleccionado encaja
        int slotMinutos = 15;
        List<LocalTime> horasDisponibles = new ArrayList<>();

        if(usaHorarioNegocio){
            // Usar horarios del negocio
            List<HorarioNegocio> horarios = horarioNegocioRepo.findByNegocioIdAndDiaSemana(
                    negocioId,
                    diaSemana
            );
            if(horarios.isEmpty()){
                throw new RuntimeException("El negocio no tiene horarios establecidos para el día: " + diaSemana);
            }

            // Procesar cada bloque de horario del negocio
            for (HorarioNegocio horario : horarios) {
                List<LocalTime> slots = generarSlotsDisponibles(
                        horario.getHoraInicio(), 
                        horario.getHoraFin(), 
                        slotMinutos, 
                        reservas,
                        intervaloTurnosMinutos
                );
                horasDisponibles.addAll(slots);
            }

        } else {
            // Usar horarios del profesional
            List<HorarioProfesional> horarios = horarioProfesionalRepo.findByProfesionalIdAndDiaSemana(
                    request.getProfesionalId(),
                    diaSemana
            );
            if(horarios.isEmpty()){
                throw new RuntimeException("El profesional no tiene horarios establecidos para el día: " + diaSemana);
            }
            
            System.out.println("Cantidad de horarios encontrados: " + horarios.size());

            // Procesar cada bloque de horario del profesional
            for (HorarioProfesional horario : horarios) {
                System.out.println("Procesando horario: " + horario.getId() + " - " + 
                                   horario.getDiaSemana() + " " + 
                                   horario.getHoraInicio() + "-" + horario.getHoraFin());
                
                List<LocalTime> slots = generarSlotsDisponibles(
                        horario.getHoraInicio(), 
                        horario.getHoraFin(), 
                        slotMinutos, 
                        reservas,
                        intervaloTurnosMinutos
                );
                horasDisponibles.addAll(slots);
            }
        }

        // Ordenar las horas disponibles
        horasDisponibles.sort(LocalTime::compareTo);

        // Crear respuesta con todas las horas disponibles
        DisponibilidadResponse response = new DisponibilidadResponse();
        response.setProfesionalId(request.getProfesionalId());
        response.setProfesionalNombre(profesional.getNombreCompleto());
        response.setFecha(fecha);
        response.setHorasDisponibles(horasDisponibles);
        response.setCantidadHorasDisponibles(horasDisponibles.size());

        return response;
    }

    /**
     * Genera los slots de tiempo disponibles para un bloque horario.
     * 
     * PASO 1: Calcula los rangos libres reales (sin redondeos)
     * PASO 2: Genera slots visuales dentro de cada rango libre
     * 
     * LÓGICA DE BLOQUEO:
     * - Una reserva bloquea: [horaInicioReserva, horaFinReserva + intervaloTurnosMinutos)
     * - El inicio real disponible es EXACTAMENTE: horaFinReserva + intervaloTurnosMinutos
     * - NO se redondea ni ajusta
     * 
     * EJEMPLO:
     * - Horario: 08:00-20:00
     * - Reserva: 08:21-11:01, intervalo: 15 minutos
     * - Bloqueo real: [08:21, 11:16)
     * - Rangos libres reales: [08:00, 08:21) y [11:16, 20:00)
     * - Slots generados: 08:00, 08:15 (dentro del primer rango) y 11:16, 11:31, 11:46... (dentro del segundo rango)
     * 
     * @param horaInicio Hora de inicio del bloque horario
     * @param horaFin Hora de fin del bloque horario
     * @param slotMinutos Tamaño del slot para la grilla visual (cada cuántos minutos mostrar)
     * @param reservas Lista de reservas existentes del profesional
     * @param intervaloTurnosMinutos Intervalo entre turnos (tiempo de preparación, puede ser 0)
     * @return Lista de horas disponibles (slots libres)
     */
    private List<LocalTime> generarSlotsDisponibles(
            LocalTime horaInicio, 
            LocalTime horaFin, 
            int slotMinutos, 
            List<Reserva> reservas,
            int intervaloTurnosMinutos
    ) {
        // PASO 1: Calcular rangos libres reales
        List<RangoHorario> rangosLibres = calcularRangosLibres(horaInicio, horaFin, reservas, intervaloTurnosMinutos);
        
        // PASO 2: Generar slots visuales dentro de cada rango libre
        List<LocalTime> slots = new ArrayList<>();
        for (RangoHorario rango : rangosLibres) {
            LocalTime horaActual = rango.inicio;
            
            while (horaActual.isBefore(rango.fin)) {
                slots.add(horaActual);
                horaActual = horaActual.plusMinutes(slotMinutos);
            }
        }
        
        return slots;
    }
    
    /**
     * Calcula los rangos libres reales dentro de un bloque horario.
     * 
     * @param horaInicio Hora de inicio del bloque horario
     * @param horaFin Hora de fin del bloque horario
     * @param reservas Lista de reservas existentes
     * @param intervaloTurnosMinutos Intervalo entre turnos
     * @return Lista de rangos libres [inicio, fin)
     */
    private List<RangoHorario> calcularRangosLibres(
            LocalTime horaInicio,
            LocalTime horaFin,
            List<Reserva> reservas,
            int intervaloTurnosMinutos
    ) {
        List<RangoHorario> rangosLibres = new ArrayList<>();
        
        // Ordenar reservas por hora de inicio
        List<Reserva> reservasOrdenadas = new ArrayList<>(reservas);
        reservasOrdenadas.sort((r1, r2) -> r1.getHoraInicio().compareTo(r2.getHoraInicio()));
        
        LocalTime inicioDisponible = horaInicio;
        
        for (Reserva reserva : reservasOrdenadas) {
            // Bloqueo real: [horaInicio, horaFin + intervalo)
            LocalTime inicioBloqueo = reserva.getHoraInicio();
            LocalTime finBloqueo = reserva.getHoraFin().plusMinutes(intervaloTurnosMinutos);
            
            // Si hay espacio libre antes de esta reserva, agregarlo como rango
            if (inicioDisponible.isBefore(inicioBloqueo)) {
                rangosLibres.add(new RangoHorario(inicioDisponible, inicioBloqueo));
            }
            
            // Mover el inicio disponible al fin del bloqueo
            if (finBloqueo.isAfter(inicioDisponible)) {
                inicioDisponible = finBloqueo;
            }
        }
        
        // Agregar el último rango libre (desde la última reserva hasta el fin del horario)
        if (inicioDisponible.isBefore(horaFin)) {
            rangosLibres.add(new RangoHorario(inicioDisponible, horaFin));
        }
        
        return rangosLibres;
    }
    
    /**
     * Clase interna para representar un rango de tiempo [inicio, fin)
     */
    private static class RangoHorario {
        final LocalTime inicio;
        final LocalTime fin;
        
        RangoHorario(LocalTime inicio, LocalTime fin) {
            this.inicio = inicio;
            this.fin = fin;
        }
    }


}
