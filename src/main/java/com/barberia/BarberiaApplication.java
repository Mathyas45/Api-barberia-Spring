package com.barberia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal de la aplicación Spring Boot
 * 
 * @SpringBootApplication escanea automáticamente:
 * - com.barberia.models (Entidades JPA)
 * - com.barberia.repositories (Repositorios)
 * - com.barberia.services (Servicios)
 * - com.barberia.controllers (Controladores REST)
 * - com.barberia.config (Configuraciones)
 * - com.barberia.security (Seguridad JWT)
 * 
 * @EnableJpaAuditing habilita la auditoría automática de JPA
 * Spring detecta automáticamente los beans AuditorAware por tipo:
 * - AuditorAware<Usuario> para campos @CreatedBy/@LastModifiedBy de tipo Usuario
 * - AuditorAware<Negocio> para campos @CreatedBy de tipo Negocio
 */
@SpringBootApplication
@EnableJpaAuditing
public class BarberiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarberiaApplication.class, args);
	}

}
