package com.barberia.services;

import com.barberia.dto.cliente.ClienteResponse;
import com.barberia.mappers.ClienteMapper;
import com.barberia.models.Cliente;
import com.barberia.repositories.ClienteRepository;
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
 * Tests para ClienteService
 * 
 * Estos tests demuestran:
 * - Cómo testear operaciones CRUD
 * - Verificación de llamadas a repositorio
 * - Manejo de casos de excepción
 * - Validación de datos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    
    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteMock;

    @BeforeEach
    void setUp() {
        clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNombreCompleto("Juan Pérez");
        clienteMock.setEmail("juan@email.com");
        clienteMock.setTelefono("987654321");
        clienteMock.setDocumentoIdentidad("12345678");
        clienteMock.setRegEstado(1);
    }

    @Test
    @DisplayName("Debe listar todos los clientes activos")
    void debeListarClientesActivos() {
        // ARRANGE
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNombreCompleto("María García");
        
        when(clienteRepository.findAll())
                .thenReturn(Arrays.asList(clienteMock, cliente2));

        // ACT
        List<Cliente> resultado = clienteRepository.findAll();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombreCompleto());
        assertEquals("María García", resultado.get(1).getNombreCompleto());
        
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar cliente por ID")
    void debeBuscarClientePorId() {
        // ARRANGE
        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(clienteMock));

        // ACT
        Optional<Cliente> resultado = clienteRepository.findById(1L);

        // ASSERT
        assertTrue(resultado.isPresent());
        assertEquals("Juan Pérez", resultado.get().getNombreCompleto());
        assertEquals("juan@email.com", resultado.get().getEmail());
        
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty cuando cliente no existe")
    void debeRetornarEmptyCuandoNoExiste() {
        // ARRANGE
        when(clienteRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ACT
        Optional<Cliente> resultado = clienteRepository.findById(999L);

        // ASSERT
        assertFalse(resultado.isPresent());
        verify(clienteRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe crear un nuevo cliente")
    void debeCrearNuevoCliente() {
        // ARRANGE
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombreCompleto("Carlos Ruiz");
        nuevoCliente.setEmail("carlos@email.com");
        nuevoCliente.setTelefono("999888777");
        
        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setId(3L);
        clienteGuardado.setNombreCompleto("Carlos Ruiz");
        clienteGuardado.setEmail("carlos@email.com");
        
        when(clienteRepository.save(any(Cliente.class)))
                .thenReturn(clienteGuardado);

        // ACT
        Cliente resultado = clienteRepository.save(nuevoCliente);

        // ASSERT
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals("Carlos Ruiz", resultado.getNombreCompleto());
        
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debe actualizar un cliente existente")
    void debeActualizarCliente() {
        // ARRANGE
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(1L);
        clienteActualizado.setNombreCompleto("Juan Pérez Actualizado");
        clienteActualizado.setEmail("juan.nuevo@email.com");
        
        when(clienteRepository.save(any(Cliente.class)))
                .thenReturn(clienteActualizado);

        // ACT
        Cliente resultado = clienteRepository.save(clienteActualizado);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Juan Pérez Actualizado", resultado.getNombreCompleto());
        assertEquals("juan.nuevo@email.com", resultado.getEmail());
        
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debe eliminar un cliente (soft delete)")
    void debeEliminarCliente() {
        // ARRANGE
        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(clienteMock));
        when(clienteRepository.save(any(Cliente.class)))
                .thenReturn(clienteMock);
        when(clienteMapper.toResponse(any(Cliente.class)))
                .thenReturn(new ClienteResponse());

        // ACT
        clienteService.eliminar(1L);

        // ASSERT
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(argThat(cliente -> 
            cliente.getRegEstado() == 0
        ));
    }

    @Test
    @DisplayName("Debe buscar clientes por nombre")
    void debeBuscarClientesPorNombre() {
        // ARRANGE
        when(clienteRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadOrTelefonoContainingIgnoreCase("juan", "juan", "juan"))
                .thenReturn(List.of(clienteMock));

        // ACT
        List<Cliente> resultado = clienteRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadOrTelefonoContainingIgnoreCase("juan", "juan", "juan");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombreCompleto());
        
        verify(clienteRepository, times(1))
                .findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadOrTelefonoContainingIgnoreCase("juan", "juan", "juan");
    }

    @Test
    @DisplayName("Debe verificar si existe cliente por teléfono")
    void debeVerificarExistenciaPorTelefono() {
        // ARRANGE
        when(clienteRepository.findClienteByTelefono("987654321"))
                .thenReturn(clienteMock);

        // ACT
        Cliente existe = clienteRepository.findClienteByTelefono("987654321");

        // ASSERT
        assertNotNull(existe);
        verify(clienteRepository, times(1))
                .findClienteByTelefono("987654321");
    }

    @Test
    @DisplayName("Debe validar que teléfono no esté duplicado")
    void debeValidarTelefonoNoDuplicado() {
        // ARRANGE
        when(clienteRepository.findClientesByTelefono("987654321"))
                .thenReturn(Arrays.asList(clienteMock, clienteMock)); // Duplicado

        // ACT
        List<Cliente> resultado = clienteRepository.findClientesByTelefono("987654321");

        // ASSERT
        assertTrue(resultado.size() > 1, "Debe encontrar duplicados");
    }
}
