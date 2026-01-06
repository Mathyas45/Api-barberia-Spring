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
    Optional<Negocio> findByRuc(String ruc);
    
    /**
     * Verificar si existe un negocio con un RUC
     */
    boolean existsByRuc(String ruc);
    
    /**
     * Buscar negocio por nombre
     */
    Optional<Negocio> findByNombre(String nombre);
}
