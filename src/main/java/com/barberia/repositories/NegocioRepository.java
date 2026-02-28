package com.barberia.repositories;

import com.barberia.models.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
     * Buscar negocio por slug (para web pública)
     */
    Optional<Negocio> findBySlugAndEstadoTrue(String slug);

    /**
     * Verificar si existe un slug
     */
    boolean existsBySlug(String slug);

    /**
     * Buscar negocios por nombre o RUC conteniendo texto
     */

    @Query("SELECT  n FROM Negocio n WHERE (LOWER(n.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.ruc) LIKE LOWER(CONCAT('%', :query, '%'))) AND n.estado != false AND n.id = :negocioId")
   List<Negocio> findByNombreContainingIgnoreCaseOrRucContainingIgnoreCaseAndNegocioId(String query, Long negocioId);

    @Query("SELECT n FROM Negocio n WHERE n.estado != false AND n.id = :negocioId")
    List <Negocio> findAllByNegocioId(Long negocioId);

    /**
     * Buscar negocio por RUC sin Optional
     */
    Negocio findByRuc(String ruc);
}
