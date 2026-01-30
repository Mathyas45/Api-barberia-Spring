package com.barberia.services;

import com.barberia.dto.EstadoRequestGlobal;
import com.barberia.dto.cliente.ClienteImportResponse;
import com.barberia.dto.cliente.ClienteRequest;
import com.barberia.dto.cliente.ClienteRequestCliente;
import com.barberia.dto.cliente.ClienteResponse;
import com.barberia.mappers.ClienteMapper;
import com.barberia.models.Cliente;
import com.barberia.repositories.ClienteRepository;
import com.barberia.services.common.SecurityContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final SecurityContextService securityContextService;

    //Inyección por constructor (mejor práctica)
    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper, SecurityContextService securityContextService) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.securityContextService = securityContextService;
    }

    @Transactional
    public ClienteResponse createClienteProtegido (ClienteRequest request) {
        Cliente cliente = clienteMapper.toEntity(request);
        if(cliente.getTelefono().length() != 9 ){
            throw new RuntimeException("El teléfono debe tener 9 dígitos.");
        }
        // Verificar si el cliente ya existe por telefono
        List<Cliente> clientesExistentes = clienteRepository.findClientesByTelefono(cliente.getTelefono());
        if (clientesExistentes.size() > 1) {
            throw new RuntimeException("Se encontraron múltiples clientes con el mismo teléfono. Verifique los datos.");
        } else if (!clientesExistentes.isEmpty()) {
            throw new RuntimeException("El teléfono ya está registrado para otro cliente.");
        }
        Cliente nuevoCliente =  clienteRepository.save(cliente);
        return clienteMapper.toResponse(nuevoCliente);
    }

    @Transactional
    public ClienteResponse create(ClienteRequestCliente request) {
        Cliente cliente = clienteMapper.toEntityCliente(request);
        // Verificar si el cliente ya existe por documentoIdentidad
        Cliente clienteExistente = clienteRepository.findClienteByTelefono(cliente.getTelefono());
        if (clienteExistente != null) {
            return clienteMapper.toResponse(clienteExistente);
        }
        Cliente nuevoCliente =  clienteRepository.save(cliente);
        return clienteMapper.toResponse(nuevoCliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return clienteMapper.toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> findAll(String query) {
        // MULTI-TENANT: Obtener negocioId del JWT (no del frontend)
        Long negocioId = securityContextService.getNegocioIdFromContext();
        
        List<Cliente> clientes;
    
        if (query != null && !query.isBlank()) {
            clientes = clienteRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadOrTelefonoContainingIgnoreCaseAndRegEstadoNotAndNegocioId(
                    query,
                    query,
                    query,
                    0,
                    negocioId
            );
        } else {
            clientes = clienteRepository.findAllByNegocioIdAndRegEstadoNot(negocioId, 0);
        }

        return clientes.stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        clienteMapper.updateEntity(cliente,  request);
        Cliente nuevoCliente =  clienteRepository.save(cliente);
        return clienteMapper.toResponse(nuevoCliente);
    }

    @Transactional
    public ClienteResponse cambiarEstado(Long id, EstadoRequestGlobal request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        cliente.setRegEstado(request.getRegEstado());
        Cliente nuevoCliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(nuevoCliente);
    }

    @Transactional
    public ClienteResponse eliminar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        cliente.setRegEstado(0);
        Cliente nuevoCliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(nuevoCliente);
    }

    /**
     * Importación masiva de clientes desde Excel
     * 
     * Recorre la lista de clientes y los inserta uno por uno.
     * Si un cliente falla (ej: teléfono duplicado), se registra el error
     * pero continúa con los demás.
     * 
     * @param clientes Lista de clientes a importar
     * @return Resumen de la importación con éxitos y errores
     */
    @Transactional
    public ClienteImportResponse importarClientes(List<ClienteRequest> clientes) {
        List<ClienteResponse> insertados = new ArrayList<>();
        List<ClienteImportResponse.ErrorImportacion> errores = new ArrayList<>();
        
        int fila = 1; // Para indicar qué fila del Excel falló
        
        for (ClienteRequest request : clientes) {
            fila++;
            try {
                // Validar teléfono
                if (request.getTelefono() == null || request.getTelefono().length() != 9) {
                    errores.add(ClienteImportResponse.ErrorImportacion.builder()
                            .fila(fila)
                            .nombreCompleto(request.getNombreCompleto())
                            .telefono(request.getTelefono())
                            .mensaje("El teléfono debe tener 9 dígitos")
                            .build());
                    continue;
                }
                
                // Verificar si ya existe
                List<Cliente> existentes = clienteRepository.findClientesByTelefono(request.getTelefono());
                if (!existentes.isEmpty()) {
                    errores.add(ClienteImportResponse.ErrorImportacion.builder()
                            .fila(fila)
                            .nombreCompleto(request.getNombreCompleto())
                            .telefono(request.getTelefono())
                            .mensaje("El teléfono ya está registrado")
                            .build());
                    continue;
                }
                
                // Crear el cliente
                Cliente cliente = clienteMapper.toEntity(request);
                Cliente nuevoCliente = clienteRepository.save(cliente);
                insertados.add(clienteMapper.toResponse(nuevoCliente));
                
            } catch (Exception e) {
                errores.add(ClienteImportResponse.ErrorImportacion.builder()
                        .fila(fila)
                        .nombreCompleto(request.getNombreCompleto())
                        .telefono(request.getTelefono())
                        .mensaje("Error: " + e.getMessage())
                        .build());
            }
        }
        
        return ClienteImportResponse.builder()
                .totalRecibidos(clientes.size())
                .totalInsertados(insertados.size())
                .totalErrores(errores.size())
                .clientesInsertados(insertados)
                .errores(errores)
                .build();
    }
}