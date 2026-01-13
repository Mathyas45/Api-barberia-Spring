package com.barberia.mappers;

import com.barberia.dto.cliente.ClienteRequest;
import com.barberia.dto.cliente.ClienteRequestCliente;
import com.barberia.dto.cliente.ClienteResponse;
import com.barberia.models.Cliente;
import com.barberia.models.Negocio;
import com.barberia.models.Usuario;
import com.barberia.repositories.NegocioRepository;
import com.barberia.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component // sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente(); // esto sirve para crear una nueva instancia de la clase Cliente, la instancia es un objeto que representa un cliente en el sistema
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDocumentoIdentidad(request.getDocumentoIdentidad());

        cliente.setRegEstado(1); // Por defecto activo
        return cliente;
    }
    public Cliente toEntityCliente(ClienteRequestCliente request) {
        Cliente cliente = new Cliente(); // esto sirve para crear una nueva instancia de la clase Cliente, la instancia es un objeto que representa un cliente en el sistema
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        cliente.setRegEstado(1); // Por defecto activo
        return cliente;
    }


    public ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse(); // esto sirve para crear una nueva instancia de la clase ClienteResponse, la instancia es un objeto que representa la respuesta del cliente en el sistema
        response.setId(cliente.getId());
        response.setNombreCompleto(cliente.getNombreCompleto());
        response.setEmail(cliente.getEmail());
        response.setTelefono(cliente.getTelefono());
        response.setDocumentoIdentidad(cliente.getDocumentoIdentidad());
        if(cliente.getUsuarioRegistroId() != null) {
            response.setUsuarioRegistroId(cliente.getUsuarioRegistroId().getId());
        }
        if (cliente.getNegocio() != null) {
            response.setNegocioId(cliente.getNegocio().getId());
        }
        return response;
    }

    public Cliente updateEntity(Cliente cliente, ClienteRequest request) {
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        cliente.setRegEstado (2); // Por defecto actualizado

        return cliente;
    }
}
