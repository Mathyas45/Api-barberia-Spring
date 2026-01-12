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
}
