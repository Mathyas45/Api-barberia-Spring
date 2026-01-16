package com.barberia.services;

import com.barberia.dto.disponibilidad.DisponibilidadRequest;
import com.barberia.dto.disponibilidad.DisponibilidadResponse;
import com.barberia.models.*;
import com.barberia.models.enums.DiaSemana;
import com.barberia.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class DisponibilidadService {
    private final HorarioProfesionalRepository horarioProfesionalRepo;
    private final HorarioNegocioRepository horarioNegocioRepo;
    private final ServicioRepository servicioRepo;
    private final ConfiguracionReservaRepository configRepo;
    private final ProfesionalRepository profesionalRepository;

    public DisponibilidadService(HorarioProfesionalRepository horarioProfesionalRepo,
                                 HorarioNegocioRepository horarioNegocioRepo,
                                 ServicioRepository servicioRepo,
                                 ConfiguracionReservaRepository configRepo,
                                 ProfesionalRepository profesionalRepository) {
        this.horarioProfesionalRepo = horarioProfesionalRepo;
        this.horarioNegocioRepo = horarioNegocioRepo;
        this.servicioRepo = servicioRepo;
        this.configRepo = configRepo;
        this.profesionalRepository = profesionalRepository;}


    @Transactional
    public DisponibilidadResponse disponibilidad(DisponibilidadRequest request) {
        LocalDate fecha = request.getFecha();

        DiaSemana diaSemana; // Declarar la variable diaSemana aquí

        // Usar el método estático del enum para convertir el día de la semana
        diaSemana = DiaSemana.fromDayOfWeek(fecha.getDayOfWeek());


        Servicio servicio = servicioRepo.findById(request.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio con ID " + request.getServicioId() + " no existe"));
        int duracionServicio = servicio.getDuracionMinutosAprox();
        System.out.println("Duración del servicio: " + duracionServicio);

        ConfiguracionReserva configReserva = configRepo.findByNegocioId(request.getNegocioId())
                .orElseThrow(() -> new RuntimeException("Configuración de reserva no existe"));
        int getIntervaloTurnosMinutos = configReserva.getIntervaloTurnosMinutos();
        System.out.println("Intervalo entre turnos: " + getIntervaloTurnosMinutos);


        Profesional profesional = profesionalRepository.findById(request.getProfesionalId())
                .orElseThrow(() -> new RuntimeException("Profesional con ID " + request.getProfesionalId() + " no existe"));

        Boolean usaHorarioNegocio = profesional.getUsaHorarioNegocio();
        System.out.println("Usa horario negocio: " + usaHorarioNegocio);


        if(usaHorarioNegocio){
            List<HorarioNegocio> horarioNegocio = horarioNegocioRepo.findByNegocioIdAndDiaSemana(
                    request.getNegocioId(),
                    diaSemana
            );
            if(horarioNegocio.isEmpty()){
                throw new RuntimeException("El negocio no tiene horarios establecidos para el día: " + diaSemana);
            }
            for (HorarioNegocio hn : horarioNegocio) {
                System.out.println("Horario Negocio: " + hn.getId() + " - " + hn.getDiaSemana() + " " + hn.getHoraInicio() + "-" + hn.getHoraFin());
            }
        }

        List<HorarioProfesional> horariosProfesional = horarioProfesionalRepo.findByProfesionalIdAndDiaSemana(
                request.getProfesionalId(),
                diaSemana
        );
        if(horariosProfesional.isEmpty()){
            throw new RuntimeException("El profesional no tiene horarios establecidos para el día: " + diaSemana);
        }
        System.out.println("Cantidad de horarios encontrados: " + horariosProfesional.size());


        for (HorarioProfesional h : horariosProfesional) {
            System.out.println("Horario: " + h.getId() + " - " + h.getDiaSemana() + " " + h.getHoraInicio() + "-" + h.getHoraFin());
        }

        // Crear un objeto de respuesta básico
        DisponibilidadResponse response = new DisponibilidadResponse();
        response.setProfesionalId(request.getProfesionalId());

        System.out.println("Respuesta generada: " + response);

        return response;
    }



}
