package com.barberia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
 */
@SpringBootApplication
public class BarberiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarberiaApplication.class, args);
	}

}
