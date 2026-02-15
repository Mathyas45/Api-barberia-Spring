package com.barberia.repositories;

import com.barberia.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Rol
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {


    /**
     * Verifica si existe un rol con ese nombre
     *
     * @param name nombre del rol
     * @return true si existe, false si no
     */
    Boolean existsByName(String name);

    /**
     * Busca roles por nombre conteniendo texto (sin Optional)
     */
    java.util.List<Rol> findByNameContainingIgnoreCase(String name);

    /**
     * Busca un rol por nombre (sin Optional)
     */
    Rol findByName(String name);
}
