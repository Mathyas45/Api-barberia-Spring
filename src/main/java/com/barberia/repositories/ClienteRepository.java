package com.barberia.repositories;

import com.barberia.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClienteRepository  extends JpaRepository<Cliente, Long> {



    Cliente findClienteByTelefono(String telefono);

    List<Cliente> findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadOrTelefonoContainingIgnoreCase(String nombreCompleto, String documentoIdentidad, String telefono); //buscar por nombre o apellido ignorando mayusculas y minusculas



}
