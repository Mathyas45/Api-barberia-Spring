package com.barberia.services;

import com.barberia.dto.auth.AuthResponse;
import com.barberia.dto.auth.LoginRequest;
import com.barberia.dto.auth.RegisterRequest;
import com.barberia.mappers.UsuarioMapper;
import com.barberia.models.Rol;
import com.barberia.models.Usuario;
import com.barberia.repositories.RolRepository;
import com.barberia.repositories.UsuarioRepository;
import com.barberia.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio de autenticación
 * 
 * RESPONSABILIDADES:
 * 1. Registrar nuevos usuarios
 * 2. Autenticar usuarios (login)
 * 3. Generar tokens JWT
 * 
 * @Transactional: Asegura que las operaciones de BD sean atómicas
 *                 Si algo falla, hace rollback automático
 */
@Service
public class AuthenticationService {
    
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioMapper usuarioMapper;
    
    public AuthenticationService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                                PasswordEncoder passwordEncoder, JwtService jwtService,
                                AuthenticationManager authenticationManager, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.usuarioMapper = usuarioMapper;
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════
     * REGISTRO DE NUEVO USUARIO
     * ═══════════════════════════════════════════════════════════════════════
     * 
     * FLUJO COMPLETO CON INTERACCIONES:
     * 
     * 1. [AuthController] Recibe POST /api/auth/register
     *    ↓
     * 2. [AuthenticationService] register(request) ← ESTAMOS AQUÍ
     *    ↓
     * 3. [UsuarioRepository] Valida que email no exista
     *    ↓
     * 4. [RolRepository] Busca rol por defecto (ADMIN si es 1er usuario, sino USER)
     *    ↓
     * 5. [PasswordEncoder] Encripta el password con BCrypt
     *    ↓
     * 6. [UsuarioRepository] Guarda usuario en BD con sus roles
     *    ↓
     * 7. [CustomUserDetailsService] Convierte Usuario → UserDetails
     *
     *    (UserDetails es la interfaz de Spring Security)
     *    ↓
     * 8. [JwtService] Genera token JWT firmado con:
     *    - Email del usuario
     *    - Roles del usuario (ROLE_ADMIN, ROLE_USER, etc.)
     *    - Permisos del usuario (usuarios:crear, citas:leer, etc.)
     *    - Fecha de expiración
     *    ↓
     * 9. [UsuarioMapper] Convierte Usuario → AuthResponse DTO
     *    ↓
     * 10. [AuthController] Retorna respuesta con token al cliente
     * 
     * ROLES DINÁMICOS:
     * - NO hardcodeamos roles en el código
     * - Los roles vienen de la tabla "roles" en BD
     * - Cada rol tiene sus permisos en la tabla "permisos"
     * - Relación: usuario → roles → permisos (Many-to-Many-to-Many)
     * 
     * LÓGICA DE ASIGNACIÓN:
     * - Primer usuario registrado → ROL ADMIN (super usuario inicial)
     * - Usuarios siguientes → ROL USER (por defecto)
     * - Los roles se pueden cambiar después desde un endpoint admin
     * 
     * @param request Datos del nuevo usuario (name, email, password)
     * @return AuthResponse con token JWT y datos del usuario
     * @throws RuntimeException si email ya existe o rol no se encuentra
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 1: VALIDAR QUE EL EMAIL NO EXISTA
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → UsuarioRepository
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 2: DETERMINAR ROL (DINÁMICO DESDE REQUEST)
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → RolRepository
        //
        // LÓGICA DE ASIGNACIÓN DE ROL:
        // 
        // - El campo roleName es OBLIGATORIO en todos los casos
        // - El rol ADMIN ya existe en la BD (creado con script SQL inicial)
        // - Este endpoint es usado por ADMIN para crear usuarios del sistema
        //   (barberos, recepcionistas, gerentes, otros admins, etc.)
        // - Los CLIENTES tendrán su propio endpoint de registro público
        //
        // IMPORTANTE: Los roles vienen de la BD, NO están hardcodeados
        // El DataInitializer crea estos roles al inicio:
        // - ADMIN (con permisos: usuarios:*, citas:*, servicios:*, reportes:*)
        // - BARBERO (con permisos: citas:leer, citas:actualizar)
        // - USER (con permisos: citas:crear, citas:leer)
        //
        // FUTURO: Puedes agregar más roles en BD sin cambiar código:
        // - RECEPCIONISTA (citas:crear, citas:leer, citas:actualizar)
        // - GERENTE (reportes:leer, reportes:generar)
        // - etc.
        
        // Validar que rolId esté presente
        if (request.getRolId() == null) {
            throw new RuntimeException(
                    "El campo 'rolId' es obligatorio. " +
                            "Debe enviar el ID de un rol existente (1=ADMIN, 2=MANAGER, 3=USER, etc.)"
            );
        }
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 2.1: VALIDAR NEGOCIO (MULTI-TENANT)
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → NegocioRepository
        //
        // MULTI-TENANT:
        // - Todo usuario debe pertenecer a un negocio
        // - Valida que el negocioId exista y esté activo
        
        if (request.getNegocioId() == null) {
            throw new RuntimeException(
                    "El campo 'negocioId' es obligatorio. " +
                            "Todo usuario debe pertenecer a un negocio."
            );
        }
        
        // Verificar que el negocio existe (se agregará después)
        // Por ahora, solo validamos que venga el negocioId

        // Buscar el rol en la base de datos por ID
        Rol assignedRole = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new RuntimeException(
                        "Rol con ID '" + request.getRolId() + "' no encontrado. " +
                                "Verifica que el rol exista en la tabla 'roles'."
                ));
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 3: CREAR EL USUARIO (CON MULTI-TENANT)
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → PasswordEncoder
        //
        // PasswordEncoder (BCrypt):
        // - Genera un hash one-way del password
        // - Añade salt automático (misma contraseña → diferentes hashes)
        // - Imposible desencriptar (solo se puede verificar)
        //
        // SEGURIDAD:
        // ⚠️ NUNCA guardar passwords en texto plano
        // ⚠️ SIEMPRE usar passwordEncoder.encode()
        //
        // MULTI-TENANT:
        // ⚠️ SIEMPRE asignar negocioId al crear usuario
        
        Usuario usuario = Usuario.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // ← ENCRIPTADO
                .negocioId(request.getNegocioId())  // ← MULTI-TENANT
                .regEstado(1)  // 1 = Activo, 0 = Inactivo
                .roles(Set.of(assignedRole))  // ← Asigna el rol determinado dinámicamente
                .build();
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 4: GUARDAR EN BASE DE DATOS
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → UsuarioRepository → MySQL
        //
        // @Transactional asegura que:
        // - Si algo falla, se hace ROLLBACK completo
        // - Usuario y sus roles se guardan en la misma transacción
        
        Usuario savedUser = usuarioRepository.save(usuario);
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 5: CARGAR USUARIO CON ROLES Y PERMISOS
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → UsuarioRepository → MySQL
        //
        // ¿Por qué recargamos?
        // - save() puede no cargar las relaciones lazy (roles/permisos)
        // - findByEmailWithRolesAndPermissions hace JOIN FETCH
        // - Necesitamos roles y permisos para el JWT
        
        Usuario userWithRoles = usuarioRepository.findByEmailWithRolesAndPermissions(savedUser.getEmail())
                .orElseThrow(() -> new RuntimeException("Error al cargar usuario guardado"));
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 6: GENERAR TOKEN JWT
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → JwtService
        //
        // JwtService necesita un UserDetails de Spring Security
        // UserDetails contiene:
        // - username (en nuestro caso: email)
        // - password (encriptado)
        // - authorities (roles + permisos como GrantedAuthority)
        //
        // El JWT contendrá:
        // - sub (subject): email del usuario
        // - roles: ["ROLE_ADMIN"] o ["ROLE_USER"]
        // - permissions: ["usuarios:crear", "citas:leer", etc.]
        // - iat (issued at): fecha de creación
        // - exp (expiration): fecha de expiración
        //
        // IMPORTANTE: CustomUserDetailsService convierte Usuario → UserDetails
        // incluyendo TODOS los roles y permisos dinámicamente desde BD
        //
        // INTERACCIÓN: AuthenticationService → CustomUserDetailsService
        // CustomUserDetailsService.loadUserByUsername() convierte Usuario → UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userWithRoles.getEmail())
                .password(userWithRoles.getPassword())
                .authorities(getAuthoritiesFromUsuario(userWithRoles))
                .build();
        
        // MULTI-TENANT: Generar token con negocioId incluido
        String jwtToken = jwtService.generateToken(userDetails, userWithRoles.getNegocioId());
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 7: CONSTRUIR RESPUESTA (CON NEGOCIO_ID)
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → UsuarioMapper
        //
        // UsuarioMapper convierte:
        // - Usuario (entidad JPA) → AuthResponse (DTO)
        //
        // AuthResponse contiene:
        // - token: JWT firmado (con negocioId en los claims)
        // - type: "Bearer"
        // - email: email del usuario
        // - name: nombre del usuario
        // - negocioId: ID del negocio (MULTI-TENANT) ← NUEVO
        // - roles: ["ADMIN"] o ["USER", "BARBERO"]
        // - permissions: ["usuarios:crear", "citas:leer", ...]
        //
        // FRONTEND:
        // El cliente debe guardar negocioId en localStorage:
        // localStorage.setItem('negocioId', response.negocioId);
        //
        // El cliente guardará el token y lo enviará en futuras requests:
        // Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
        
        return usuarioMapper.toAuthResponse(userWithRoles, jwtToken);
    }
    
//    /**
//     * ═══════════════════════════════════════════════════════════════════════
//     * LOGIN DE USUARIO
//     * ═══════════════════════════════════════════════════════════════════════
//     *
//     * FLUJO COMPLETO CON INTERACCIONES:
//     *
//     * 1. [AuthController] Recibe POST /api/auth/login (email + password)
//     *    ↓
//     * 2. [AuthenticationService] login(request) ← ESTAMOS AQUÍ
//     *    ↓
//     * 3. [AuthenticationManager] authenticate(email, password)
//     *    ↓
//     * 4. [AuthenticationManager] → llama a → [DaoAuthenticationProvider]
//     *    ↓
//     * 5. [DaoAuthenticationProvider] → llama a → [CustomUserDetailsService]
//     *    loadUserByUsername(email)
//     *    ↓
//     * 6. [CustomUserDetailsService] → llama a → [UsuarioRepository]
//     *    findByEmailWithRolesAndPermissions(email)
//     *    ↓
//     * 7. [UsuarioRepository] → [MySQL]
//     *    SELECT usuario JOIN roles JOIN permisos
//     *    ↓
//     * 8. [CustomUserDetailsService] Convierte Usuario → UserDetails con:
//     *    - username: email
//     *    - password: password encriptado
//     *    - authorities: TODOS los roles y permisos del usuario
//     *      Ejemplo: ["ROLE_ADMIN", "usuarios:crear", "citas:leer", ...]
//     *    ↓
//     * 9. [DaoAuthenticationProvider] Compara passwords:
//     *    passwordEncoder.matches(passwordIngresado, passwordEnBD)
//     *    ↓
//     * 10. Si passwords NO coinciden → lanza BadCredentialsException ❌
//     *     Si passwords coinciden → retorna Authentication ✅
//     *    ↓
//     * 11. [AuthenticationService] Recibe Authentication exitosa
//     *    ↓
//     * 12. [JwtService] Genera token JWT con:
//     *     - sub: email del usuario
//     *     - roles: TODOS los roles del usuario (dinámicos desde BD)
//     *     - permissions: TODOS los permisos del usuario (dinámicos desde BD)
//     *     - exp: fecha de expiración
//     *    ↓
//     * 13. [UsuarioMapper] Convierte Usuario → AuthResponse
//     *    ↓
//     * 14. [AuthController] Retorna token al cliente
//     *
//     * ROLES Y PERMISOS DINÁMICOS:
//     * - NO se hardcodean en el código
//     * - Se cargan DINÁMICAMENTE de las tablas:
//     *   * usuarios (usuario)
//     *   * roles (rol)
//     *   * permisos (permiso)
//     *   * user_roles (relación usuario ↔ rol)
//     *   * role_permissions (relación rol ↔ permiso)
//     *
//     * - Un usuario puede tener MÚLTIPLES roles
//     * - Un rol puede tener MÚLTIPLES permisos
//     * - Todo se carga automáticamente desde BD
//     *
//     * EJEMPLOS:
//     * Usuario ADMIN:
//     *   roles: ["ADMIN"]
//     *   permisos: ["usuarios:crear", "usuarios:leer", "usuarios:actualizar",
//     *              "usuarios:eliminar", "citas:crear", "citas:leer", ...]
//     *
//     * Usuario BARBERO:
//     *   roles: ["BARBERO"]
//     *   permisos: ["citas:leer", "citas:actualizar"]
//     *
//     * Usuario con múltiples roles:
//     *   roles: ["USER", "BARBERO"]
//     *   permisos: ["citas:crear", "citas:leer", "citas:actualizar"]
//     *
//     * @param request Credenciales (email y password)
//     * @return AuthResponse con token JWT y datos del usuario
//     * @throws BadCredentialsException si credenciales son incorrectas
//     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 1: AUTENTICAR CREDENCIALES
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → AuthenticationManager
        //
        // AuthenticationManager orquesta el proceso de autenticación:
        // 1. Llama a UserDetailsService para cargar el usuario
        // 2. Llama a PasswordEncoder para comparar passwords
        // 3. Si todo OK, retorna Authentication
        // 4. Si falla, lanza BadCredentialsException
        //
        // ⚠️ IMPORTANTE: 
        // - NO comparamos passwords manualmente
        // - NO accedemos directamente a la BD aquí
        // - Delegamos TODO a AuthenticationManager
        // - Esto sigue las mejores prácticas de Spring Security
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),    // principal (username)
                        request.getPassword()  // credentials (password sin encriptar)
                )
        );
        
        // Si llegamos aquí, las credenciales son CORRECTAS ✅
        // Si fueran incorrectas, BadCredentialsException ya se habría lanzado
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 2: EXTRAER USERDETAILS DEL AUTHENTICATION
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService ← AuthenticationManager
        //
        // Authentication.getPrincipal() retorna el UserDetails
        // que CustomUserDetailsService cargó desde la BD
        //
        // Este UserDetails ya contiene:
        // - username (email)
        // - password (encriptado)
        // - authorities (TODOS los roles y permisos dinámicos)
        
        var userDetails = (org.springframework.security.core.userdetails.UserDetails) 
                authentication.getPrincipal();
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 3: GENERAR TOKEN JWT
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → JwtService
        //
        // JwtService genera un JWT firmado que contiene:
        // - sub: email del usuario
        // - negocioId: ID del negocio (MULTI-TENANT) ← NUEVO
        // - roles: ["ROLE_ADMIN"] o ["ROLE_USER", "ROLE_BARBERO"]
        // - permissions: ["usuarios:crear", "citas:leer", ...]
        // - iat: timestamp de creación
        // - exp: timestamp de expiración
        //
        // El token se firma con JWT_SECRET_KEY (solo el servidor la conoce)
        // Esto garantiza que nadie puede modificar el token sin invalidarlo
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 3.1: CARGAR USUARIO COMPLETO PARA OBTENER NEGOCIO_ID
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → UsuarioRepository → MySQL
        //
        // Necesitamos el negocioId del usuario para incluirlo en el token
        
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // MULTI-TENANT: Generar token con negocioId incluido
        String jwtToken = jwtService.generateToken(userDetails, usuario.getNegocioId());
        
        // ═══════════════════════════════════════════════════════════════════
        // PASO 4: CONSTRUIR RESPUESTA (CON NEGOCIO_ID)
        // ═══════════════════════════════════════════════════════════════════
        // INTERACCIÓN: AuthenticationService → UsuarioMapper
        //
        // UsuarioMapper extrae DINÁMICAMENTE:
        // - Nombres de todos los roles del usuario
        // - Nombres de todos los permisos de esos roles
        // - negocioId del usuario (MULTI-TENANT) ← NUEVO
        //
        // AuthResponse final:
        // {
        //   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
        //   "type": "Bearer",
        //   "email": "admin@barberia.com",
        //   "name": "Admin User",
        //   "roles": ["ADMIN"],
        //   "permissions": ["usuarios:crear", "citas:leer", ...]
        // }
        //
        // El cliente guardará este token (localStorage/sessionStorage)
        // y lo enviará en futuras requests:
        // Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
        
        return usuarioMapper.toAuthResponse(userDetails, usuario, jwtToken);
    }
    
    /**
     * Extrae las authorities (roles + permisos) de un Usuario
     * 
     * CONVENCIÓN DE SPRING SECURITY:
     * - Roles: Prefijo "ROLE_" → "ROLE_ADMIN", "ROLE_USER"
     * - Permisos: Sin prefijo → "usuarios:crear", "citas:leer"
     * 
     * Esta convención permite usar:
     * - @PreAuthorize("hasRole('ADMIN')") → busca "ROLE_ADMIN"
     * - @PreAuthorize("hasAuthority('usuarios:crear')") → busca "usuarios:crear"
     * 
     * @param usuario Usuario con roles y permisos cargados
     * @return Collection de GrantedAuthority
     */
    private Collection<org.springframework.security.core.GrantedAuthority> getAuthoritiesFromUsuario(Usuario usuario) {
        Set<org.springframework.security.core.GrantedAuthority> authorities = new HashSet<>();
        
        // Agrega roles con prefijo ROLE_
        usuario.getRoles().forEach(rol -> {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol.getName()));
        });
        
        // Agrega permisos sin prefijo
        usuario.getRoles().forEach(rol -> {
            rol.getPermissions().forEach(permiso -> {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permiso.getName()));
            });
        });
        
        return authorities;
    }
}
