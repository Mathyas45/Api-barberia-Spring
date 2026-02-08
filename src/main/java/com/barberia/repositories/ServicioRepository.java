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
    //    metodos para buscar en el service
    @Query("SELECT s FROM Servicio s " +
            "JOIN s.categoria c " +
            "WHERE s.nombre LIKE %:nombre% " +
            "AND s.regEstado != :regEstado " +
            "AND c.estado != false " +
            "AND s.negocio.id = :negocioId")
    List<Servicio> findByNombreAndCategoriaEstadoNotAndRegEstadoNotAndNegocioId(
            String nombre, int regEstado, Long negocioId);

    @Query("SELECT s FROM Servicio s " +
            "JOIN s.categoria c " +
            "WHERE s.nombre LIKE %:nombre% " +
            "AND c.estado != false " +
            "AND c.id = :categoriaId " +
            "AND s.regEstado != :regEstado " +
            "AND s.negocio.id = :negocioId")
    List<Servicio> findByNombreAndCategoriaIdAndCategoriaEstadoNotAndRegEstadoNotAndNegocioId(
            String nombre, Long categoriaId, int regEstado, Long negocioId);

    @Query("SELECT s FROM Servicio s " +
            "JOIN s.categoria c " +
            "WHERE s.regEstado != :regEstado " +
            "AND s.negocio.id = :negocioId " +
            "AND c.id = :categoriaId " +
            "AND c.estado != false")
    List<Servicio> findByRegEstadoNotAndNegocioIdAndCategoriaIdAndCategoriaEstadoNot(
            int regEstado, Long negocioId, Long categoriaId);

    @Query("SELECT s FROM Servicio s " +
            "JOIN s.categoria c " +
            "WHERE s.regEstado != :regEstado " +
            "AND s.negocio.id = :negocioId " +
            "AND c.estado != false " +
            "AND c.regEstado != 0"
    )
    List<Servicio> findByRegEstadoNotAndNegocioIdAndCategoriaEstadoNot(
            int regEstado, Long negocioId);

    @Query("SELECT COUNT(c) > 0 FROM Servicio c WHERE c.nombre = :nombre AND c.regEstado != 0 AND c.negocio.id = :negocioId")
    boolean existsByNombreAndRegEstadoNotEliminadoAndNegocioId(String nombre, Long negocioId);
}
