package com.barberia.services;

import com.barberia.dto.disponibilidad.DisponibilidadRequest;
import com.barberia.dto.disponibilidad.DisponibilidadResponse;
import com.barberia.models.*;
import com.barberia.models.enums.DiaSemana;
import com.barberia.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DisponibilidadService
 * 
 * @ExtendWith(MockitoExtension.class) - Habilita Mockito para crear mocks
 * @Mock - Crea un objeto simulado (mock) del repositorio
 * @InjectMocks - Crea una instancia del servicio e inyecta los mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para DisponibilidadService")
class DisponibilidadServiceTest {

    // Mocks de los repositorios (objetos simulados)
    @Mock
    private HorarioProfesionalRepository horarioProfesionalRepo;
    
    @Mock
    private HorarioNegocioRepository horarioNegocioRepo;
    
    @Mock
    private ServicioRepository servicioRepo;
    
    @Mock
    private ConfiguracionReservaRepository configRepo;
    
    @Mock
    private ProfesionalRepository profesionalRepository;
    
    @Mock
    private ReservaRepository reservaRepository;

    // Servicio a probar con los mocks inyectados
    @InjectMocks
    private DisponibilidadService disponibilidadService;

    // Variables de prueba reutilizables
    private DisponibilidadRequest request;
    private ConfiguracionReserva configReserva;
    private Profesional profesional;
    private LocalDate fechaPrueba;

    /**
     * @BeforeEach - Se ejecuta ANTES de cada test
     * Aquí inicializamos los datos comunes para todos los tests
     */
    @BeforeEach
    void setUp() {
        // Fecha de prueba: lunes
        fechaPrueba = LocalDate.of(2026, 1, 19); // Lunes

        // Request básico
        request = new DisponibilidadRequest();
        request.setNegocioId(1L);
        request.setProfesionalId(1L);
        request.setServicioId(1L);
        request.setFecha(fechaPrueba);
        request.setEsInterno(true);

        // Configuración de reservas con intervalo de 15 minutos
        configReserva = new ConfiguracionReserva();
        configReserva.setId(1L);
        Negocio negocio = new Negocio();
        negocio.setId(1L);
        configReserva.setNegocio(negocio);
        configReserva.setIntervaloTurnosMinutos(15);

        // Profesional que usa horario del negocio
        profesional = new Profesional();
        profesional.setId(1L);
        profesional.setNombreCompleto("Juan Pérez");
        profesional.setUsaHorarioNegocio(true);
    }

    /**
     * TEST 1: Caso básico - Sin reservas
     * 
     * Escenario:
     * - Profesional con horario 08:00 - 12:00
     * - Sin reservas existentes
     * - Intervalo entre turnos: 15 minutos
     * 
     * Validaciones:
     * - La lista no debe estar vacía
     * - Debe contener la hora 08:00
     * - Debe generar slots cada 15 minutos
     */
    @Test
    @DisplayName("Debe generar horas disponibles cuando no hay reservas")
    void debeGenerarHorasDisponiblesSinReservas() {
        // ARRANGE (Preparar) - Configurar el comportamiento de los mocks
        
        // Mock: Configuración de reservas
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.of(configReserva));

        // Mock: Profesional
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        // Mock: Sin reservas
        when(reservaRepository.findByProfesionalIdAndFechaAndRegEstadoNot(1L, fechaPrueba, 0))
                .thenReturn(new ArrayList<>());

        // Mock: Horario del negocio (08:00 - 12:00)
        HorarioNegocio horario = new HorarioNegocio();
        horario.setId(1L);
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(12, 0));
        
        when(horarioNegocioRepo.findByNegocioIdAndDiaSemana(1L, DiaSemana.LUNES))
                .thenReturn(List.of(horario));

        // ACT (Actuar) - Ejecutar el método a probar
        DisponibilidadResponse response = disponibilidadService.disponibilidad(request);

        // ASSERT (Afirmar) - Verificar los resultados
        assertNotNull(response, "La respuesta no debe ser null");
        assertNotNull(response.getHorasDisponibles(), "La lista de horas no debe ser null");
        assertFalse(response.getHorasDisponibles().isEmpty(), "La lista de horas no debe estar vacía");
        
        // Verificar que contiene la hora 08:00
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(8, 0)),
                "Debe contener la hora 08:00");
        
        // Verificar que los slots son cada 15 minutos
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(8, 15)),
                "Debe contener la hora 08:15");
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(8, 30)),
                "Debe contener la hora 08:30");
        
        // Verificar datos básicos en la respuesta
        assertEquals(fechaPrueba, response.getFecha());
        assertNotNull(response.getHorasDisponibles());
        
        // Verificar que se llamaron los métodos esperados
        verify(configRepo, times(1)).findByNegocioId(1L);
        verify(profesionalRepository, times(1)).findById(1L);
        verify(reservaRepository, times(1))
                .findByProfesionalIdAndFechaAndRegEstadoNot(1L, fechaPrueba, 0);
    }

    /**
     * TEST 2: Con reserva que bloquea parte del horario
     * 
     * Escenario:
     * - Horario: 08:00 - 12:00
     * - Reserva: 10:00 - 10:30
     * - Intervalo: 15 minutos
     * 
     * Resultado esperado:
     * - Debe haber slots disponibles de 08:00 a 09:45
     * - Bloqueado de 10:00 a 10:45 (reserva + intervalo)
     * - Disponible desde 10:45 hasta 12:00
     */
    @Test
    @DisplayName("Debe excluir las horas bloqueadas por reservas + intervalo")
    void debeExcluirHorasBloqueasPorReservas() {
        // ARRANGE
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.of(configReserva));
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        // Crear una reserva de 10:00 a 10:30
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setFecha(fechaPrueba);
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(10, 30));
        reserva.setProfesional(profesional);
        
        when(reservaRepository.findByProfesionalIdAndFechaAndRegEstadoNot(1L, fechaPrueba, 0))
                .thenReturn(List.of(reserva));

        // Horario 08:00 - 12:00
        HorarioNegocio horario = new HorarioNegocio();
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(12, 0));
        
        when(horarioNegocioRepo.findByNegocioIdAndDiaSemana(1L, DiaSemana.LUNES))
                .thenReturn(List.of(horario));

        // ACT
        DisponibilidadResponse response = disponibilidadService.disponibilidad(request);

        // ASSERT
        assertNotNull(response.getHorasDisponibles());
        
        // Debe haber slots disponibles ANTES de la reserva
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(8, 0)));
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(9, 45)));
        
        // NO debe haber slots durante la reserva y el intervalo
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(10, 0)),
                "10:00 debe estar bloqueado (hora de inicio de reserva)");
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(10, 15)),
                "10:15 debe estar bloqueado (durante reserva)");
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(10, 30)),
                "10:30 debe estar bloqueado (fin de reserva)");
        
        // Debe haber slots disponibles DESPUÉS del intervalo (10:30 + 15 = 10:45)
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(10, 45)),
                "10:45 debe estar disponible (después de reserva + intervalo)");
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(11, 0)));
    }

    /**
     * TEST 3: Profesional con horario personalizado
     * 
     * Escenario:
     * - Profesional NO usa horario del negocio
     * - Tiene su propio horario: 10:00 - 19:00
     */
    @Test
    @DisplayName("Debe usar horario personalizado cuando usaHorarioNegocio es false")
    void debeUsarHorarioPersonalizado() {
        // ARRANGE
        profesional.setUsaHorarioNegocio(false); // Horario personalizado
        
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.of(configReserva));
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));
        when(reservaRepository.findByProfesionalIdAndFechaAndRegEstadoNot(1L, fechaPrueba, 0))
                .thenReturn(new ArrayList<>());

        // Horario personalizado del profesional
        HorarioProfesional horarioProfesional = new HorarioProfesional();
        horarioProfesional.setDiaSemana(DiaSemana.LUNES);
        horarioProfesional.setHoraInicio(LocalTime.of(10, 0));
        horarioProfesional.setHoraFin(LocalTime.of(19, 0));
        
        when(horarioProfesionalRepo.findByProfesionalIdAndDiaSemana(1L, DiaSemana.LUNES))
                .thenReturn(List.of(horarioProfesional));

        // ACT
        DisponibilidadResponse response = disponibilidadService.disponibilidad(request);

        // ASSERT
        assertNotNull(response.getHorasDisponibles());
        assertFalse(response.getHorasDisponibles().isEmpty());
        
        // Debe iniciar a las 10:00 (no a las 08:00 del negocio)
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(10, 0)));
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(8, 0)),
                "No debe contener 08:00 porque el horario inicia a las 10:00");
        
        // Verificar que se llamó al repositorio correcto
        verify(horarioProfesionalRepo, times(1))
                .findByProfesionalIdAndDiaSemana(1L, DiaSemana.LUNES);
        verify(horarioNegocioRepo, never())
                .findByNegocioIdAndDiaSemana(anyLong(), any());
    }

    /**
     * TEST 4: Múltiples reservas
     * 
     * Escenario:
     * - Horario: 08:00 - 14:00
     * - Reserva 1: 09:00 - 09:30
     * - Reserva 2: 11:00 - 11:45
     * - Intervalo: 15 minutos
     */
    @Test
    @DisplayName("Debe manejar múltiples reservas correctamente")
    void debeManjarMultiplesReservas() {
        // ARRANGE
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.of(configReserva));
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        // Dos reservas
        Reserva reserva1 = new Reserva();
        reserva1.setHoraInicio(LocalTime.of(9, 0));
        reserva1.setHoraFin(LocalTime.of(9, 30));
        
        Reserva reserva2 = new Reserva();
        reserva2.setHoraInicio(LocalTime.of(11, 0));
        reserva2.setHoraFin(LocalTime.of(11, 45));
        
        when(reservaRepository.findByProfesionalIdAndFechaAndRegEstadoNot(1L, fechaPrueba, 0))
                .thenReturn(List.of(reserva1, reserva2));

        HorarioNegocio horario = new HorarioNegocio();
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(14, 0));
        
        when(horarioNegocioRepo.findByNegocioIdAndDiaSemana(1L, DiaSemana.LUNES))
                .thenReturn(List.of(horario));

        // ACT
        DisponibilidadResponse response = disponibilidadService.disponibilidad(request);

        // ASSERT
        // Disponible antes de primera reserva
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(8, 0)));
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(8, 45)));
        
        // Bloqueado durante primera reserva (9:00-9:45)
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(9, 0)));
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(9, 30)));
        
        // Disponible entre reservas (9:45-11:00)
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(9, 45)));
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(10, 30)));
        
        // Bloqueado durante segunda reserva (11:00-12:00)
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(11, 0)));
        assertFalse(response.getHorasDisponibles().contains(LocalTime.of(11, 45)));
        
        // Disponible después de segunda reserva
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(12, 0)));
    }

    /**
     * TEST 5: Intervalo de 0 minutos
     * 
     * Escenario:
     * - Intervalo entre turnos = 0
     * - Reserva: 10:00 - 10:30
     * 
     * Resultado: Disponible inmediatamente después (10:30)
     */
    @Test
    @DisplayName("Debe manejar intervalo de 0 minutos correctamente")
    void debeManjarIntervaloCero() {
        // ARRANGE
        configReserva.setIntervaloTurnosMinutos(0); // Sin intervalo
        
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.of(configReserva));
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        Reserva reserva = new Reserva();
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(10, 30));
        
        when(reservaRepository.findByProfesionalIdAndFechaAndRegEstadoNot(1L, fechaPrueba, 0))
                .thenReturn(List.of(reserva));

        HorarioNegocio horario = new HorarioNegocio();
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(12, 0));
        
        when(horarioNegocioRepo.findByNegocioIdAndDiaSemana(1L, DiaSemana.LUNES))
                .thenReturn(List.of(horario));

        // ACT
        DisponibilidadResponse response = disponibilidadService.disponibilidad(request);

        // ASSERT
        // Con intervalo 0, debe estar disponible inmediatamente después
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(10, 30)),
                "Con intervalo 0, debe estar disponible en 10:30");
        assertTrue(response.getHorasDisponibles().contains(LocalTime.of(10, 45)));
    }

    /**
     * TEST 6: Excepción cuando no existe configuración
     */
    @Test
    @DisplayName("Debe lanzar excepción cuando no existe configuración de reserva")
    void debeLanzarExcepcionSinConfiguracion() {
        // ARRANGE
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.empty()); // No existe

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            disponibilidadService.disponibilidad(request);
        });
        
        assertTrue(exception.getMessage().contains("Configuración de reserva no existe"));
    }

    /**
     * TEST 7: Excepción cuando no existe profesional
     */
    @Test
    @DisplayName("Debe lanzar excepción cuando no existe el profesional")
    void debeLanzarExcepcionSinProfesional() {
        // ARRANGE
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.of(configReserva));
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.empty()); // No existe

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            disponibilidadService.disponibilidad(request);
        });
        
        assertTrue(exception.getMessage().contains("no existe"));
    }

    /**
     * TEST 8: Sin horarios configurados
     */
    @Test
    @DisplayName("Debe lanzar excepción cuando no hay horarios configurados")
    void debeLanzarExcepcionSinHorarios() {
        // ARRANGE
        when(configRepo.findByNegocioId(1L))
                .thenReturn(Optional.of(configReserva));
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));
        when(reservaRepository.findByProfesionalIdAndFechaAndRegEstadoNot(1L, fechaPrueba, 0))
                .thenReturn(new ArrayList<>());
        
        // Sin horarios
        when(horarioNegocioRepo.findByNegocioIdAndDiaSemana(1L, DiaSemana.LUNES))
                .thenReturn(new ArrayList<>());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            disponibilidadService.disponibilidad(request);
        });
        
        assertTrue(exception.getMessage().contains("no tiene horarios establecidos"));
    }
}
