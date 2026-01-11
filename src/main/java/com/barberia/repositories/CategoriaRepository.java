package com.barberia.repositories;

import com.barberia.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoriaRepository  extends JpaRepository<Categoria, Long> {


    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE c.nombre = :nombre AND c.regEstado != 0")
    boolean existsByNombreAndRegEstadoNotEliminado(String nombre);
}
