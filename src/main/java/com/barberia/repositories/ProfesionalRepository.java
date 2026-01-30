package com.barberia.repositories;

import com.barberia.models.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesionalRepository  extends JpaRepository<Profesional, Long> {

    @Query("SELECT COUNT(c) > 0 FROM Profesional c WHERE c.nombreCompleto = :nombreCompleto AND c.regEstado != 0")
    boolean existsByNombreAndRegEstadoNotEliminado(String nombreCompleto);

    List<Profesional> findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(String nombreCompleto, int regEstado);

    List<Profesional> findByRegEstadoNot(int regEstado);

    // ========== MÉTODOS MULTI-TENANT (filtrado por negocioId) ==========
    
    @Query("SELECT p FROM Profesional p WHERE (LOWER(p.nombreCompleto) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.documentoIdentidad) LIKE LOWER(CONCAT('%', :query, '%'))) AND p.regEstado != :regEstado AND p.negocio.id = :negocioId")
    List<Profesional> findByNombreCompletoOrDocumentoIdentidadAndRegEstadoNotAndNegocioId(String query, int regEstado, Long negocioId);
    
    List<Profesional> findByRegEstadoNotAndNegocioId(int regEstado, Long negocioId);
    
    @Query("SELECT COUNT(c) > 0 FROM Profesional c WHERE c.nombreCompleto = :nombreCompleto AND c.regEstado != 0 AND c.negocio.id = :negocioId")
    boolean existsByNombreAndRegEstadoNotEliminadoAndNegocioId(String nombreCompleto, Long negocioId);
}
