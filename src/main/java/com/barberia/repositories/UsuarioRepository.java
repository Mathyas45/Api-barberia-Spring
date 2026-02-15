package com.barberia.repositories;

import com.barberia.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * 
 * JpaRepository nos proporciona métodos CRUD automáticos:
 * - save()
 * - findById()
 * - findAll()
 * - delete()
 * - etc.
 * 
 * Además definimos métodos personalizados para Spring Security
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por email
     *
     * Spring Data JPA genera automáticamente la query SQL:
     * SELECT * FROM usuarios WHERE email = ?
     *
     * Optional: Evita NullPointerException
     * Si no encuentra el usuario, devuelve Optional.empty()
     *
     * @param email Email del usuario
     * @return Optional con el usuario o vacío
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con ese email
     * 
     * Útil para validaciones antes de registrar
     * 
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca un usuario con sus roles y permisos cargados
     * 
     * @Query personalizada con JOIN FETCH para evitar N+1 queries
     * Carga todo en una sola consulta SQL
     * 
     * LEFT JOIN FETCH: Trae los roles aunque el usuario no tenga ninguno
     * 
     * @param email Email del usuario
     * @return Optional con el usuario y sus relaciones
     */
    @Query("SELECT u FROM Usuario u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.email = :email")
    Optional<Usuario> findByEmailWithRolesAndPermissions(@Param("email") String email);

    /**
     * Busca usuarios por negocioId excluyendo estado 0
     */
    @Query("SELECT u FROM Usuario u WHERE u.negocioId = :negocioId AND u.regEstado <> :regEstado")
    java.util.List<Usuario> findByNegocioIdAndRegEstadoNot(@Param("negocioId") Long negocioId, @Param("regEstado") Integer regEstado);

    /**
     * Busca usuarios por negocioId y nombre o email conteniendo texto
     */
    @Query("SELECT u FROM Usuario u WHERE u.negocioId = :negocioId AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND u.regEstado <> :regEstado")
    java.util.List<Usuario> findByNegocioIdAndNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRegEstadoNot(
            @Param("negocioId") Long negocioId,
            @Param("name") String name,
            @Param("email") String email,
            @Param("regEstado") Integer regEstado
    );

    /**
     * Busca usuario por email sin Optional
     */
}

