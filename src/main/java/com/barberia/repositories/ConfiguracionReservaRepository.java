package com.barberia.repositories;

import com.barberia.models.ConfiguracionReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionReservaRepository extends JpaRepository<ConfiguracionReserva, Long> { //es interface porque extiende de JpaRepository, una interfaz sirve para definir metodos sin implementacion
    Boolean existsByNegocioId(Long negocioId);

    Optional <ConfiguracionReserva> findByNegocioId(Long negocioId);

    @Query("""
        SELECT c
        FROM ConfiguracionReserva c
        WHERE c.negocio.id = :negocioId AND c.regEstado = 1
    """)
    List<ConfiguracionReserva> findActiveByNegocioId(Long negocioId);
}
