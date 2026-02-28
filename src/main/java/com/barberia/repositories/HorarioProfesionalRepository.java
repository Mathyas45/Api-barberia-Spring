package com.barberia.repositories;

import com.barberia.models.HorarioProfesional;
import com.barberia.models.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalTime;
import java.util.List;

public interface HorarioProfesionalRepository  extends JpaRepository<HorarioProfesional, Long> {

    @Query("""
    SELECT COUNT(h) > 0
    FROM HorarioProfesional h
    WHERE h.profesional.id = :profesionalId
      AND h.diaSemana = :dia
      AND h.regEstado != 0
      AND (:inicio < h.horaFin AND :fin > h.horaInicio)
    """)
    boolean existeSolapamiento(
            Long profesionalId,
            DiaSemana dia,
            LocalTime inicio,
            LocalTime fin
    );

    @Query("""
    SELECT COUNT(h) > 0
    FROM HorarioProfesional h
    WHERE h.id <> :id
      AND h.profesional.id = :profesionalId
      AND h.diaSemana = :dia
      AND h.regEstado != 0
      AND (:inicio < h.horaFin AND :fin > h.horaInicio)
    """)
    boolean existeSolapamientoExcluyendoId(
            Long id,
            Long profesionalId,
            DiaSemana dia,
            LocalTime inicio,
            LocalTime fin
    );


    @Query("""
    SELECT h
    FROM HorarioProfesional h
    WHERE (:profesionalId IS NULL OR h.profesional.id = :profesionalId)
      AND h.regEstado != 0
    """)
    List<HorarioProfesional> findByProfesional(Long profesionalId);

    @Query("""
    SELECT h
    FROM HorarioProfesional h
    WHERE (:profesionalId IS NULL OR h.profesional.id = :profesionalId)
      AND (:diaSemana IS NULL OR h.diaSemana = :diaSemana)
      AND h.regEstado != 0
    """)
    List<HorarioProfesional>findByProfesionalAndDiaSemana (Long profesionalId, DiaSemana diaSemana);



    @Modifying
    @Query("""
        UPDATE HorarioProfesional h
        SET h.regEstado = 0
        WHERE h.profesional.id = :profesionalId
          AND h.diaSemana = :diaSemana
        """)
    void desactivarPorProfesionalYDiaSemana(Long profesionalId, DiaSemana diaSemana);

    @Query("""
    SELECT h
    FROM HorarioProfesional h
    WHERE h.profesional.id = :id and h.regEstado != 0 and h.diaSemana = :diaSemana
    """)
    List<HorarioProfesional> findByProfesionalIdAndDiaSemana(Long id, DiaSemana diaSemana);

}
