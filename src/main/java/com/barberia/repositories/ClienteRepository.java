package com.barberia.repositories;

import com.barberia.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClienteRepository  extends JpaRepository<Cliente, Long> {

    boolean existsBydocumentoIdentidad(String documentoIdentidad);//Verifica si existe un dni con el nombre proporcionado
    boolean existsByTelefono(String telefono);//Verifica si existe un telefono con el nombre proporcionado

    Cliente findClienteByTelefono(String telefono);

    List<Cliente> findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadOrTelefonoContainingIgnoreCase(String nombreCompleto, String documentoIdentidad, String telefono); //buscar por nombre o apellido ignorando mayusculas y minusculas

    List<Cliente> findClientesByTelefono(String telefono);


}
