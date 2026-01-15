package com.barberia.repositories;

import com.barberia.models.ConfiguracionReserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionReservaRepository extends JpaRepository<ConfiguracionReserva, Long> { //es interface porque extiende de JpaRepository, una interfaz sirve para definir metodos sin implementacion
}
