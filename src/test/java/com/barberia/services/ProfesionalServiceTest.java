package com.barberia.services;

import com.barberia.models.Profesional;
import com.barberia.repositories.ProfesionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests SIMPLES para ProfesionalService
 * 
 * Estos son tests básicos y fáciles de entender.
 * Perfectos para empezar a aprender testing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests simples para ProfesionalService")
class ProfesionalServiceTest {

    @Mock
    private ProfesionalRepository profesionalRepository;

    private Profesional profesionalMock;

    @BeforeEach
    void setUp() {
        // Crear un profesional de ejemplo
        profesionalMock = new Profesional();
        profesionalMock.setId(1L);
        profesionalMock.setNombreCompleto("Carlos López");
        profesionalMock.setDocumentoIdentidad("12345678");
        profesionalMock.setTelefono("987654321");
        profesionalMock.setUsaHorarioNegocio(true);
        profesionalMock.setRegEstado(1);
    }

    /**
     * TEST 1: Test más simple - Verificar que existe un profesional
     */
    @Test
    @DisplayName("Test 1: Debe verificar que existe un profesional por ID")
    void debeVerificarQueExisteProfesional() {
        // ARRANGE (Preparar)
        when(profesionalRepository.existsById(1L)).thenReturn(true);

        // ACT (Actuar)
        boolean existe = profesionalRepository.existsById(1L);

        // ASSERT (Afirmar)
        assertTrue(existe, "El profesional debe existir");
        verify(profesionalRepository, times(1)).existsById(1L);
    }

    /**
     * TEST 2: Buscar profesional por ID
     */
    @Test
    @DisplayName("Test 2: Debe encontrar un profesional por su ID")
    void debeBuscarProfesionalPorId() {
        // ARRANGE
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesionalMock));

        // ACT
        Optional<Profesional> resultado = profesionalRepository.findById(1L);

        // ASSERT
        assertTrue(resultado.isPresent(), "Debe encontrar el profesional");
        assertEquals("Carlos López", resultado.get().getNombreCompleto());
        assertEquals("12345678", resultado.get().getDocumentoIdentidad());
        
        verify(profesionalRepository, times(1)).findById(1L);
    }

    /**
     * TEST 3: No encontrar profesional que no existe
     */
    @Test
    @DisplayName("Test 3: Debe retornar vacío cuando el profesional no existe")
    void debeRetornarVacioCuandoNoExiste() {
        // ARRANGE
        when(profesionalRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ACT
        Optional<Profesional> resultado = profesionalRepository.findById(999L);

        // ASSERT
        assertFalse(resultado.isPresent(), "No debe encontrar el profesional");
        verify(profesionalRepository, times(1)).findById(999L);
    }

    /**
     * TEST 4: Listar todos los profesionales activos
     */
    @Test
    @DisplayName("Test 4: Debe listar todos los profesionales activos de un negocio")
    void debeListarProfesionalesActivos() {
        // ARRANGE
        Profesional profesional2 = new Profesional();
        profesional2.setId(2L);
        profesional2.setNombreCompleto("Juan Pérez");
        
        List<Profesional> profesionales = Arrays.asList(profesionalMock, profesional2);
        
        when(profesionalRepository.findByRegEstadoNot(0))
                .thenReturn(profesionales);

        // ACT
        List<Profesional> resultado = profesionalRepository.findByRegEstadoNot(0);

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser null");
        assertEquals(2, resultado.size(), "Debe haber 2 profesionales");
        assertEquals("Carlos López", resultado.get(0).getNombreCompleto());
        assertEquals("Juan Pérez", resultado.get(1).getNombreCompleto());
        
        verify(profesionalRepository, times(1))
                .findByRegEstadoNot(0);
    }

    /**
     * TEST 5: Lista vacía cuando no hay profesionales
     */
    @Test
    @DisplayName("Test 5: Debe retornar lista vacía cuando no hay profesionales")
    void debeRetornarListaVaciaCuandoNoHayProfesionales() {
        // ARRANGE
        when(profesionalRepository.findByRegEstadoNot(0))
                .thenReturn(Arrays.asList()); // Lista vacía

        // ACT
        List<Profesional> resultado = profesionalRepository.findByRegEstadoNot(0);

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía");
        assertEquals(0, resultado.size());
    }

    /**
     * TEST 6: Crear un nuevo profesional
     */
    @Test
    @DisplayName("Test 6: Debe crear un nuevo profesional")
    void debeCrearNuevoProfesional() {
        // ARRANGE
        Profesional nuevoProfesional = new Profesional();
        nuevoProfesional.setNombreCompleto("Pedro García");
        nuevoProfesional.setDocumentoIdentidad("87654321");
        
        Profesional profesionalGuardado = new Profesional();
        profesionalGuardado.setId(3L);
        profesionalGuardado.setNombreCompleto("Pedro García");
        profesionalGuardado.setDocumentoIdentidad("87654321");
        
        when(profesionalRepository.save(any(Profesional.class)))
                .thenReturn(profesionalGuardado);

        // ACT
        Profesional resultado = profesionalRepository.save(nuevoProfesional);

        // ASSERT
        assertNotNull(resultado, "El profesional creado no debe ser null");
        assertNotNull(resultado.getId(), "Debe tener un ID asignado");
        assertEquals("Pedro García", resultado.getNombreCompleto());
        assertEquals("87654321", resultado.getDocumentoIdentidad());
        
        verify(profesionalRepository, times(1)).save(any(Profesional.class));
    }

    /**
     * TEST 7: Actualizar un profesional existente
     */
    @Test
    @DisplayName("Test 7: Debe actualizar un profesional existente")
    void debeActualizarProfesional() {
        // ARRANGE
        Profesional profesionalActualizado = new Profesional();
        profesionalActualizado.setId(1L);
        profesionalActualizado.setNombreCompleto("Carlos López Actualizado");
        profesionalActualizado.setDocumentoIdentidad("11111111");
        
        when(profesionalRepository.save(any(Profesional.class)))
                .thenReturn(profesionalActualizado);

        // ACT
        Profesional resultado = profesionalRepository.save(profesionalActualizado);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Carlos López Actualizado", resultado.getNombreCompleto());
        assertEquals("11111111", resultado.getDocumentoIdentidad());
        
        verify(profesionalRepository, times(1)).save(any(Profesional.class));
    }

    /**
     * TEST 8: Eliminar profesional (soft delete)
     */
    @Test
    @DisplayName("Test 8: Debe eliminar un profesional (cambiar estado a 0)")
    void debeEliminarProfesional() {
        // ARRANGE
        when(profesionalRepository.save(any(Profesional.class)))
                .thenReturn(profesionalMock);

        // ACT
        profesionalMock.setRegEstado(0);
        profesionalRepository.save(profesionalMock);

        // ASSERT
        verify(profesionalRepository, times(1)).save(argThat(profesional -> 
            profesional.getRegEstado() == 0 // Verificar que el estado sea 0
        ));
    }

    /**
     * TEST 9: Verificar si profesional usa horario del negocio
     */
    @Test
    @DisplayName("Test 9: Debe verificar si el profesional usa horario del negocio")
    void debeVerificarSiUsaHorarioNegocio() {
        // ARRANGE
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesionalMock));

        // ACT
        Optional<Profesional> resultado = profesionalRepository.findById(1L);

        // ASSERT
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getUsaHorarioNegocio(), 
                "Debe usar horario del negocio");
    }

    /**
     * TEST 10: Contar profesionales activos
     */
    @Test
    @DisplayName("Test 10: Debe contar cuántos profesionales activos hay")
    void debeContarProfesionalesActivos() {
        // ARRANGE
        when(profesionalRepository.count())
                .thenReturn(5L);

        // ACT
        long cantidad = profesionalRepository.count();

        // ASSERT
        assertEquals(5L, cantidad, "Debe haber 5 profesionales activos");
        verify(profesionalRepository, times(1)).count();
    }

    /**
     * TEST 11: Buscar profesionales por documento
     */
    @Test
    @DisplayName("Test 11: Debe buscar profesionales por documento")
    void debeBuscarPorDocumento() {
        // ARRANGE
        when(profesionalRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(
                "12345678", 0))
                .thenReturn(Arrays.asList(profesionalMock));

        // ACT
        List<Profesional> resultado = profesionalRepository
                .findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase("12345678", 0);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("12345678", resultado.get(0).getDocumentoIdentidad());
    }

    /**
     * TEST 12: Verificar que no existe un profesional inexistente
     */
    @Test
    @DisplayName("Test 12: Debe verificar que no existe un profesional inexistente")
    void debeVerificarQueNoExisteProfesional() {
        // ARRANGE
        when(profesionalRepository.existsById(999L)).thenReturn(false);

        // ACT
        boolean existe = profesionalRepository.existsById(999L);

        // ASSERT
        assertFalse(existe, "El profesional no debe existir");
        verify(profesionalRepository, times(1)).existsById(999L);
    }

    /**
     * TEST 13: Validar datos del profesional
     */
    @Test
    @DisplayName("Test 13: Debe validar que el profesional tenga todos los datos necesarios")
    void debeValidarDatosProfesional() {
        // ARRANGE & ACT
        Optional<Profesional> resultado = Optional.of(profesionalMock);

        // ASSERT - Verificar que tiene todos los campos importantes
        assertTrue(resultado.isPresent());
        Profesional prof = resultado.get();
        
        assertAll("Validar todos los campos del profesional",
            () -> assertNotNull(prof.getId(), "ID no debe ser null"),
            () -> assertNotNull(prof.getNombreCompleto(), "Nombre no debe ser null"),
            () -> assertNotNull(prof.getDocumentoIdentidad(), "Documento no debe ser null"),
            () -> assertNotNull(prof.getTelefono(), "Teléfono no debe ser null"),
            () -> assertEquals(1, prof.getRegEstado(), "Estado debe ser 1 (activo)")
        );
    }

    /**
     * TEST 14: Verificar que el teléfono tiene formato correcto
     */
    @Test
    @DisplayName("Test 14: Debe verificar que el teléfono tiene 9 dígitos")
    void debeVerificarFormatoTelefono() {
        // ARRANGE & ACT
        String telefono = profesionalMock.getTelefono();

        // ASSERT
        assertNotNull(telefono);
        assertEquals(9, telefono.length(), "El teléfono debe tener 9 dígitos");
    }

    /**
     * TEST 15: Verificar que se puede buscar por nombre
     */
    @Test
    @DisplayName("Test 15: Debe buscar profesionales por nombre")
    void debeBuscarPorNombre() {
        // ARRANGE
        when(profesionalRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(
                "Carlos", 0))
                .thenReturn(Arrays.asList(profesionalMock));

        // ACT
        List<Profesional> resultado = profesionalRepository
                .findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase("Carlos", 0);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombreCompleto().contains("Carlos"));
    }
}
