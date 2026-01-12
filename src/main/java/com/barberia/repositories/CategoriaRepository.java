package com.barberia.repositories;

import com.barberia.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoriaRepository  extends JpaRepository<Categoria, Long> {


    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE c.nombre = :nombre AND c.regEstado != 0")
    boolean existsByNombreAndRegEstadoNotEliminado(String nombre);


    @Query("SELECT c FROM Categoria c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.regEstado = :regEstado")
    List<Categoria> findByNombreAndRegEstado(String nombre, int regEstado);
    List<Categoria> findByRegEstadoNot(int regEstado);
}
