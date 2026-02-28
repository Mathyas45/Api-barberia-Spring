package com.barberia.services;

import com.barberia.dto.usuario.UsuarioPerfilRequest;
import com.barberia.dto.usuario.UsuarioRequest;
import com.barberia.dto.usuario.UsuarioResponse;
import com.barberia.dto.usuario.UsuarioUpdateRequest;
import com.barberia.mappers.UsuarioMapper;
import com.barberia.models.Rol;
import com.barberia.models.Usuario;
import com.barberia.repositories.RolRepository;
import com.barberia.repositories.UsuarioRepository;
import com.barberia.services.common.SecurityContextService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextService securityContextService;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, 
                         UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder,
                         SecurityContextService securityContextService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.securityContextService = securityContextService;
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();

        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = usuarioMapper.toEntity(request, passwordEncoder);
        usuario.setNegocioId(negocioId);

        // Asignar roles
        Set<Rol> roles = new HashSet<>();
        for (Long rolId : request.getRolesIds()) {
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));
            roles.add(rol);
        }
        usuario.setRoles(roles);

        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(Long id) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Verificar que el usuario pertenece al negocio
        if (!usuario.getNegocioId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para ver este usuario");
        }

        System.out.println("Usuario encontrado: " + usuario.getEmail() + " con negocioId: " + usuario.getNegocioId());
        return usuarioMapper.toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll(String query) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();

        List<Usuario> usuarios;

        if (query != null && !query.isBlank()) {
            usuarios = usuarioRepository.findByNegocioIdAndNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRegEstadoNot(
                    negocioId, query, query, 0);
        } else {
            usuarios = usuarioRepository.findByNegocioIdAndRegEstadoNot(negocioId, 0);
        }

        return usuarios.stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioUpdateRequest request) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Verificar que el usuario pertenece al negocio
        if (!usuario.getNegocioId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para actualizar este usuario");
        }

        // Verificar si el email ya existe en otro usuario
        Optional<Usuario> usuarioConEmailOptional = usuarioRepository.findByEmail(request.getEmail());
        if (usuarioConEmailOptional.isPresent() && !usuarioConEmailOptional.get().getId().equals(id)) {
            throw new RuntimeException("El email ya está siendo usado por otro usuario");
        }

        usuarioMapper.updateEntity(usuario, request);

        // Actualizar roles
        Set<Rol> roles = new HashSet<>();
        for (Long rolId : request.getRolesIds()) {
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));
            roles.add(rol);
        }
        usuario.getRoles().clear();
        usuario.getRoles().addAll(roles);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuarioActualizado);
    }

    @Transactional
    public void delete(Long id) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Verificar que el usuario pertenece al negocio
        if (!usuario.getNegocioId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para eliminar este usuario");
        }

        usuario.setRegEstado(0); // Soft delete
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void activate(Long id) {
        // MULTI-TENANT: Obtener negocioId del JWT
        Long negocioId = securityContextService.getNegocioIdFromContext();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Verificar que el usuario pertenece al negocio
        if (!usuario.getNegocioId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para activar este usuario");
        }

        usuario.setRegEstado(1);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse updatePerfil(Long id, UsuarioPerfilRequest request) {
        Long negocioId = securityContextService.getNegocioIdFromContext();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        if (!usuario.getNegocioId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para actualizar este perfil");
        }

        // Verificar que el email no esté en uso por otro usuario
        Optional<Usuario> conMismoEmail = usuarioRepository.findByEmail(request.getEmail());
        if (conMismoEmail.isPresent() && !conMismoEmail.get().getId().equals(id)) {
            throw new RuntimeException("El email ya está siendo usado por otro usuario");
        }

        usuarioMapper.updatePerfilEntity(usuario, request, passwordEncoder);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }
}
