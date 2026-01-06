package com.barberia.repositories;

import com.barberia.models.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Permiso
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    /**
     * Busca un permiso por nombre
     *
     * @param name nombre del permiso (READ_CLIENTS, CREATE_LOAN, etc.)
     * @return Optional con el permiso si existe
     */
    Optional<Permiso> findByName(String name);

    /**
     * Verifica si existe un permiso con ese nombre
     *
     * @param name nombre del permiso
     * @return true si existe, false si no
     */
    Boolean existsByName(String name);
}
