package com.barberia.repositories;

import com.barberia.models.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByNombreContainingIgnoreCase(String nombre);

    List<Servicio> findByNombreContainingIgnoreCaseAndRegEstadoNot(String nombre, int regEstado);

    List<Servicio> findByRegEstadoNot(int regEstado);


    @Query("SELECT COUNT(c) > 0 FROM Servicio c WHERE c.nombre = :nombre AND c.regEstado != 0")
    boolean existsByNombreAndRegEstadoNotEliminado(String nombre);

    Optional<Servicio> findById(Long id);

    // ========== MÉTODOS MULTI-TENANT (filtrado por negocioId) ==========
    
    List<Servicio> findByNombreContainingIgnoreCaseAndRegEstadoNotAndNegocioId(String nombre, int regEstado, Long negocioId);
    
    List<Servicio> findByRegEstadoNotAndNegocioId(int regEstado, Long negocioId);
    
    @Query("SELECT COUNT(c) > 0 FROM Servicio c WHERE c.nombre = :nombre AND c.regEstado != 0 AND c.negocio.id = :negocioId")
    boolean existsByNombreAndRegEstadoNotEliminadoAndNegocioId(String nombre, Long negocioId);
}
