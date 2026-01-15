package com.barberia.repositories;

import com.barberia.models.HorarioNegocio;
import com.barberia.models.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalTime;
import java.util.List;

public interface HorarioNegocioRepository extends JpaRepository<HorarioNegocio, Long> {


    @Query("""
    SELECT COUNT(h) > 0
    FROM HorarioNegocio h
    WHERE h.negocio.id = :negocio
      AND h.diaSemana = :dia
      AND h.regEstado != 0
      AND (:inicio < h.horaFin AND :fin > h.horaInicio)
    """)
    boolean existeSolapamiento(
            Long negocio,
            DiaSemana dia,
            LocalTime inicio,
            LocalTime fin
    );

    @Query("""
    SELECT COUNT(h) > 0
    FROM HorarioNegocio h
    WHERE h.id <> :id
      AND h.negocio.id = :negocio
      AND h.diaSemana = :dia
      AND h.regEstado != 0
      AND (:inicio < h.horaFin AND :fin > h.horaInicio)
    """)
    boolean existeSolapamientoExcluyendoId(
            Long id,
            Long negocio,
            DiaSemana dia,
            LocalTime inicio,
            LocalTime fin
    );


    @Query("""
    SELECT h
    FROM HorarioNegocio h
    WHERE (:negocio IS NULL OR h.negocio.id = :negocio)
      AND (:diaSemana IS NULL OR h.diaSemana = :diaSemana)
      AND h.regEstado != 0
    """)
    List<HorarioNegocio> findByNegocioAndDiaSemana(Long negocio, DiaSemana diaSemana);


    @Modifying
    @Query("""
        UPDATE HorarioNegocio h
        SET h.regEstado = 0
        WHERE h.negocio.id = :negocio
          AND h.diaSemana = :diaSemana
        """)
    void desactivarPorNegocioYDiaSemana(Long negocio, DiaSemana diaSemana);
}