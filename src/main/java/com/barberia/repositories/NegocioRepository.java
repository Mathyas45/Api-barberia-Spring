package com.barberia.repositories;

import com.barberia.models.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestión de Negocios (Multi-tenant)
 */
@Repository
public interface NegocioRepository extends JpaRepository<Negocio, Long> {


//    findNegocioById

    Optional<Negocio> findNegocioById(Long id);

    /**
     *
     * Buscar negocio por RUC
     */

    /**
     * Verificar si existe un negocio con un RUC
     */
    boolean existsByRuc(String ruc);

    /**
     * Buscar negocio por nombre
     */
    Optional<Negocio> findByNombre(String nombre);

    /**
     * Buscar negocios por nombre o RUC conteniendo texto
     */
    java.util.List<Negocio> findByNombreContainingIgnoreCaseOrRucContainingIgnoreCase(String nombre, String ruc);

    /**
     * Buscar negocio por RUC sin Optional
     */
    Negocio findByRuc(String ruc);
}
