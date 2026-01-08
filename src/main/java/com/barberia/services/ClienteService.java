package com.barberia.services;

import com.barberia.dto.cliente.ClienteRequest;
import com.barberia.dto.cliente.ClienteResponse;
import com.barberia.mappers.ClienteMapper;
import com.barberia.models.Cliente;
import com.barberia.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper ;

    //Inyección por constructor (mejor práctica)
    public ClienteService(ClienteRepository clienteRepository , ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public ClienteResponse createClienteProtegido (ClienteRequest request) {
        Cliente cliente = clienteMapper.toEntity(request);
        if(cliente.getTelefono().length()   != 9 ){
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
    public ClienteResponse create(ClienteRequest request) {
        Cliente cliente = clienteMapper.toEntity(request);
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
        List<Cliente> clientes;

        String nombreCompleto = query;
        String documentoIdentidad = query;
        String telefono = query;
        if (nombreCompleto != null && !documentoIdentidad.isBlank()) {
            clientes = clienteRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadOrTelefonoContainingIgnoreCase(nombreCompleto, documentoIdentidad, telefono);
        } else {
            clientes = clienteRepository.findAll();
        }

        return clientes.stream()//stream es para trabajar con colecciones de datos nos permite aplicar operaciones funcionales como map, filter, reduce, etc.
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList()); //esto es para convertir la lista de entidades a lista de responses nos sirve para evitar codigo repetitivo
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
    public void delete(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(cliente);
    }
}