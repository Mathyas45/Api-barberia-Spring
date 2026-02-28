package com.barberia.repositories;

import com.barberia.models.GaleriaImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la galería de imágenes por negocio
 */
@Repository
public interface GaleriaImagenRepository extends JpaRepository<GaleriaImagen, Long> {

    /**
     * Obtener todas las imágenes de un negocio ordenadas por 'orden' ascendente
     */
    List<GaleriaImagen> findByNegocioIdOrderByOrdenAsc(Long negocioId);

    /**
     * Contar imágenes de un negocio
     */
    long countByNegocioId(Long negocioId);

    /**
     * Buscar imagen por ID y negocioId (seguridad multi-tenant)
     */
    Optional<GaleriaImagen> findByIdAndNegocioId(Long id, Long negocioId);

    /**
     * Obtener el máximo orden actual para un negocio
     */
    @Query("SELECT COALESCE(MAX(g.orden), 0) FROM GaleriaImagen g WHERE g.negocio.id = :negocioId")
    Integer findMaxOrdenByNegocioId(Long negocioId);
}
