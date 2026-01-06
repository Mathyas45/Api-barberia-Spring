# 🔐 Flujo Completo de Autenticación - Barbería API

## 📋 Tabla de Contenidos
- [Arquitectura General](#arquitectura-general)
- [Flujo de Registro](#flujo-de-registro)
- [Flujo de Login](#flujo-de-login)
- [Flujo de Request Protegido](#flujo-de-request-protegido)
- [Sistema Dinámico de Roles y Permisos](#sistema-dinámico-de-roles-y-permisos)
- [Interacciones Entre Componentes](#interacciones-entre-componentes)

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENTE (React/Postman)                      │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ HTTP Request
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      SPRING BOOT APPLICATION                         │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │               JwtAuthenticationFilter                         │  │
│  │  (Intercepta TODAS las requests, valida JWT)                 │  │
│  └─────────────────────┬────────────────────────────────────────┘  │
│                        │                                             │
│                        ▼                                             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    Controllers                                │  │
│  │  (AuthController, DemoController, etc.)                       │  │
│  └─────────────────────┬────────────────────────────────────────┘  │
│                        │                                             │
│                        ▼                                             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    Services                                   │  │
│  │  • AuthenticationService (login, register)                    │  │
│  │  • CustomUserDetailsService (cargar usuarios)                 │  │
│  │  • JwtService (generar/validar tokens)                        │  │
│  └─────────────────────┬────────────────────────────────────────┘  │
│                        │                                             │
│                        ▼                                             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                  Repositories                                 │  │
│  │  • UsuarioRepository                                          │  │
│  │  • RolRepository                                              │  │
│  │  • PermisoRepository                                          │  │
│  └─────────────────────┬────────────────────────────────────────┘  │
│                        │                                             │
└────────────────────────┼─────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           MySQL DATABASE                             │
│  • usuarios (id, name, email, password, regEstado)                   │
│  • roles (id, name, description)                                     │
│  • permisos (id, name, description)                                  │
│  • user_roles (usuario_id, rol_id)                                   │
│  • role_permissions (rol_id, permiso_id)                             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📝 Flujo de Registro

### 1️⃣ Request Inicial

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Juan Pérez",
  "email": "juan@barberia.com",
  "password": "123456"
}
```

### 2️⃣ Flujo Paso a Paso

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Cliente → POST /api/auth/register                                │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 2. AuthController.register(request)                                 │
│    • Valida @Valid annotations (@NotBlank, @Email, etc.)            │
│    • Si pasa validación, continúa                                   │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 3. AuthenticationService.register(request)                          │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.1. UsuarioRepository.existsByEmail(email)                │  │
│    │      ↓ MySQL: SELECT COUNT(*) FROM usuarios WHERE email=? │  │
│    │      ↓ Si existe → RuntimeException ❌                     │  │
│    │      ↓ Si NO existe → Continúa ✅                          │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.2. Determinar Rol Dinámico                               │  │
│    │      • UsuarioRepository.count()                           │  │
│    │      • Si count == 0 → RolRepository.findByName("ADMIN")   │  │
│    │      • Si count > 0 → RolRepository.findByName("USER")     │  │
│    │      ↓ MySQL: SELECT * FROM roles WHERE name = ?           │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.3. PasswordEncoder.encode(password)                      │  │
│    │      • BCrypt genera hash del password                     │  │
│    │      • Añade salt automático                               │  │
│    │      • "123456" → "$2a$10$xYz..." (irreversible)           │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.4. UsuarioRepository.save(usuario)                       │  │
│    │      ↓ MySQL: INSERT INTO usuarios ...                     │  │
│    │      ↓ MySQL: INSERT INTO user_roles ...                   │  │
│    │      ↓ @Transactional asegura atomicidad                   │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.5. UsuarioRepository.findByEmailWithRolesAndPermissions  │  │
│    │      ↓ MySQL: SELECT u.*, r.*, p.*                         │  │
│    │             FROM usuarios u                                │  │
│    │             JOIN user_roles ur ON u.id = ur.usuario_id     │  │
│    │             JOIN roles r ON ur.rol_id = r.id               │  │
│    │             JOIN role_permissions rp ON r.id = rp.rol_id   │  │
│    │             JOIN permisos p ON rp.permiso_id = p.id        │  │
│    │             WHERE u.email = ?                              │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.6. JwtService.generateToken(userWithRoles)               │  │
│    │      • Crea JWT con:                                       │  │
│    │        - sub: "juan@barberia.com"                          │  │
│    │        - roles: ["ROLE_USER"] o ["ROLE_ADMIN"]             │  │
│    │        - permissions: ["citas:crear", "citas:leer", ...]   │  │
│    │        - iat: 1735534800                                   │  │
│    │        - exp: 1735621200 (24h después)                     │  │
│    │      • Firma con JWT_SECRET_KEY                            │  │
│    │      • Retorna: "eyJhbGciOiJIUzI1NiIsInR5cCI6..."          │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.7. UsuarioMapper.toAuthResponse(usuario, token)          │  │
│    │      • Extrae roles del usuario                            │  │
│    │      • Extrae permisos de los roles                        │  │
│    │      • Construye AuthResponse                              │  │
│    └────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 4. AuthController retorna ResponseEntity<AuthResponse>              │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 5. Cliente recibe:                                                  │
│    {                                                                │
│      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",                   │
│      "type": "Bearer",                                              │
│      "email": "juan@barberia.com",                                  │
│      "name": "Juan Pérez",                                          │
│      "roles": ["USER"],                                             │
│      "permissions": ["citas:crear", "citas:leer"]                   │
│    }                                                                │
│                                                                     │
│    • Cliente guarda token en localStorage                           │
│    • Usará este token en futuras requests                           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 Flujo de Login

### 1️⃣ Request Inicial

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@barberia.com",
  "password": "admin123"
}
```

### 2️⃣ Flujo Paso a Paso

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Cliente → POST /api/auth/login                                   │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 2. AuthController.login(request)                                    │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 3. AuthenticationService.login(request)                             │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.1. AuthenticationManager.authenticate(...)               │  │
│    │      ↓                                                      │  │
│    │      └─> DaoAuthenticationProvider                         │  │
│    │          ↓                                                  │  │
│    │          └─> CustomUserDetailsService.loadUserByUsername() │  │
│    │              ↓                                              │  │
│    │              └─> UsuarioRepository.findByEmail...()        │  │
│    │                  ↓                                          │  │
│    │                  └─> MySQL: SELECT u.*, r.*, p.* ...       │  │
│    │                      ↓                                      │  │
│    │                      └─> Retorna Usuario con roles/perms   │  │
│    │              ↓                                              │  │
│    │              Convierte Usuario → UserDetails:              │  │
│    │              • username: "admin@barberia.com"              │  │
│    │              • password: "$2a$10$..."                      │  │
│    │              • authorities:                                │  │
│    │                ["ROLE_ADMIN",                              │  │
│    │                 "usuarios:crear", "usuarios:leer", ...]    │  │
│    │          ↓                                                  │  │
│    │          DaoAuthenticationProvider compara passwords:      │  │
│    │          passwordEncoder.matches(                          │  │
│    │              "admin123",      ← password ingresado         │  │
│    │              "$2a$10$..."     ← password en BD             │  │
│    │          )                                                  │  │
│    │          ↓                                                  │  │
│    │          Si NO coincide → BadCredentialsException ❌       │  │
│    │          Si coincide → retorna Authentication ✅           │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.2. Extraer UserDetails del Authentication                │  │
│    │      • authentication.getPrincipal()                       │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.3. JwtService.generateToken(userDetails)                 │  │
│    │      • Extrae authorities del UserDetails                  │  │
│    │      • Crea Claims:                                        │  │
│    │        {                                                   │  │
│    │          "sub": "admin@barberia.com",                      │  │
│    │          "roles": ["ROLE_ADMIN"],                          │  │
│    │          "permissions": ["usuarios:crear", "citas:leer"],  │  │
│    │          "iat": 1735534800,                                │  │
│    │          "exp": 1735621200                                 │  │
│    │        }                                                   │  │
│    │      • Firma con HMAC-SHA256 usando JWT_SECRET_KEY        │  │
│    │      • Retorna JWT firmado                                 │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.4. UsuarioRepository.findByEmailWithRolesAndPermissions  │  │
│    │      • Carga usuario completo para datos adicionales       │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 3.5. UsuarioMapper.toAuthResponse(userDetails, usuario)    │  │
│    └────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 4. Cliente recibe token y lo guarda                                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Flujo de Request Protegido

### 1️⃣ Request con JWT

```http
GET http://localhost:8080/api/demo/admin
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2️⃣ Flujo Paso a Paso

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Cliente → GET /api/demo/admin + Header Authorization            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 2. JwtAuthenticationFilter.doFilterInternal()                       │
│    ↓ (Intercepta la request ANTES del controller)                   │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 2.1. Extrae header Authorization                           │  │
│    │      • authHeader = "Bearer eyJhbGci..."                   │  │
│    │      • Si NO existe → continúa sin autenticar              │  │
│    │      • Si existe → extrae token: "eyJhbGci..."             │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 2.2. JwtService.extractUsername(token)                     │  │
│    │      • Parsea el JWT                                       │  │
│    │      • Verifica la firma con JWT_SECRET_KEY                │  │
│    │      • Si firma inválida → Exception ❌                    │  │
│    │      • Si firma válida → extrae "sub" (email)              │  │
│    │      • Retorna: "admin@barberia.com"                       │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 2.3. CustomUserDetailsService.loadUserByUsername(email)    │  │
│    │      ↓ UsuarioRepository.findByEmailWithRolesAndPermissions│  │
│    │      ↓ MySQL: SELECT u.*, r.*, p.* ...                     │  │
│    │      ↓ Retorna UserDetails con authorities                 │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 2.4. JwtService.isTokenValid(token, userDetails)           │  │
│    │      • Verifica que token no haya expirado                 │  │
│    │      • Verifica que username coincida                      │  │
│    │      • Si inválido → retorna false ❌                      │  │
│    │      • Si válido → retorna true ✅                         │  │
│    └────────────────────────────────────────────────────────────┘  │
│    ┌────────────────────────────────────────────────────────────┐  │
│    │ 2.5. Autentica en SecurityContext                          │  │
│    │      • Crea UsernamePasswordAuthenticationToken            │  │
│    │      • Con: userDetails, null, authorities                 │  │
│    │      • SecurityContextHolder.getContext()                  │  │
│    │           .setAuthentication(authToken)                    │  │
│    │      ↓ Usuario autenticado en el contexto de la request    │  │
│    └────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 3. Request continúa al Controller                                   │
│    @PreAuthorize("hasRole('ADMIN')")                                │
│    ↓ Spring Security verifica authorities del usuario               │
│    ↓ Si tiene "ROLE_ADMIN" → Ejecuta método ✅                      │
│    ↓ Si NO tiene → AccessDeniedException ❌                         │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 4. DemoController.adminEndpoint()                                   │
│    • Puede acceder a SecurityContextHolder.getContext()             │
│    • Para obtener información del usuario autenticado               │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 5. Cliente recibe respuesta                                         │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎭 Sistema Dinámico de Roles y Permisos

### Estructura de Base de Datos

```sql
-- Tabla usuarios
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    reg_estado INT
);

-- Tabla roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE,    -- ADMIN, BARBERO, USER, RECEPCIONISTA, etc.
    description VARCHAR(255)
);

-- Tabla permisos
CREATE TABLE permisos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE,   -- usuarios:crear, citas:leer, etc.
    description VARCHAR(255)
);

-- Relación Usuario ↔ Rol (Many-to-Many)
CREATE TABLE user_roles (
    usuario_id BIGINT,
    rol_id BIGINT,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Relación Rol ↔ Permiso (Many-to-Many)
CREATE TABLE role_permissions (
    rol_id BIGINT,
    permiso_id BIGINT,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id),
    FOREIGN KEY (permiso_id) REFERENCES permisos(id)
);
```

### Ejemplo de Datos

```sql
-- ROLES
INSERT INTO roles VALUES (1, 'ADMIN', 'Administrador del sistema');
INSERT INTO roles VALUES (2, 'BARBERO', 'Barbero que atiende citas');
INSERT INTO roles VALUES (3, 'USER', 'Cliente regular');
INSERT INTO roles VALUES (4, 'RECEPCIONISTA', 'Recepcionista');

-- PERMISOS
INSERT INTO permisos VALUES (1, 'usuarios:crear', 'Crear usuarios');
INSERT INTO permisos VALUES (2, 'usuarios:leer', 'Ver usuarios');
INSERT INTO permisos VALUES (3, 'usuarios:actualizar', 'Modificar usuarios');
INSERT INTO permisos VALUES (4, 'usuarios:eliminar', 'Eliminar usuarios');
INSERT INTO permisos VALUES (5, 'citas:crear', 'Crear citas');
INSERT INTO permisos VALUES (6, 'citas:leer', 'Ver citas');
INSERT INTO permisos VALUES (7, 'citas:actualizar', 'Modificar citas');
INSERT INTO permisos VALUES (8, 'citas:eliminar', 'Cancelar citas');

-- ASIGNACIÓN: ADMIN tiene TODOS los permisos
INSERT INTO role_permissions VALUES (1, 1);  -- ADMIN → usuarios:crear
INSERT INTO role_permissions VALUES (1, 2);  -- ADMIN → usuarios:leer
INSERT INTO role_permissions VALUES (1, 3);  -- ADMIN → usuarios:actualizar
INSERT INTO role_permissions VALUES (1, 4);  -- ADMIN → usuarios:eliminar
INSERT INTO role_permissions VALUES (1, 5);  -- ADMIN → citas:crear
INSERT INTO role_permissions VALUES (1, 6);  -- ADMIN → citas:leer
INSERT INTO role_permissions VALUES (1, 7);  -- ADMIN → citas:actualizar
INSERT INTO role_permissions VALUES (1, 8);  -- ADMIN → citas:eliminar

-- ASIGNACIÓN: BARBERO solo citas
INSERT INTO role_permissions VALUES (2, 6);  -- BARBERO → citas:leer
INSERT INTO role_permissions VALUES (2, 7);  -- BARBERO → citas:actualizar

-- ASIGNACIÓN: USER solo crear y ver SUS citas
INSERT INTO role_permissions VALUES (3, 5);  -- USER → citas:crear
INSERT INTO role_permissions VALUES (3, 6);  -- USER → citas:leer

-- ASIGNACIÓN: RECEPCIONISTA gestiona citas
INSERT INTO role_permissions VALUES (4, 5);  -- RECEPCIONISTA → citas:crear
INSERT INTO role_permissions VALUES (4, 6);  -- RECEPCIONISTA → citas:leer
INSERT INTO role_permissions VALUES (4, 7);  -- RECEPCIONISTA → citas:actualizar
```

### Cómo Funciona Dinámicamente

```java
// 1. Usuario se autentica (login)
// 2. CustomUserDetailsService carga usuario con esta query:

@Query("""
    SELECT DISTINCT u FROM Usuario u
    LEFT JOIN FETCH u.roles r
    LEFT JOIN FETCH r.permissions p
    WHERE u.email = :email
""")
Optional<Usuario> findByEmailWithRolesAndPermissions(@Param("email") String email);

// 3. Se ejecuta en MySQL:
SELECT DISTINCT
    u.id, u.name, u.email, u.password, u.reg_estado,
    r.id, r.name, r.description,
    p.id, p.name, p.description
FROM usuarios u
LEFT JOIN user_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.rol_id = r.id
LEFT JOIN role_permissions rp ON r.id = rp.rol_id
LEFT JOIN permisos p ON rp.permiso_id = p.id
WHERE u.email = 'admin@barberia.com';

// 4. Resultado (ejemplo):
Usuario {
    id: 1,
    name: "Admin User",
    email: "admin@barberia.com",
    roles: [
        Rol {
            name: "ADMIN",
            permissions: [
                Permiso { name: "usuarios:crear" },
                Permiso { name: "usuarios:leer" },
                Permiso { name: "citas:crear" },
                // ... todos los permisos
            ]
        }
    ]
}

// 5. CustomUserDetailsService convierte a authorities:
Set<GrantedAuthority> authorities = new HashSet<>();

// Agrega roles con prefijo ROLE_
usuario.getRoles().forEach(rol -> {
    authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getName()));
    // Resultado: "ROLE_ADMIN"
});

// Agrega permisos sin prefijo
usuario.getRoles().forEach(rol -> {
    rol.getPermissions().forEach(permiso -> {
        authorities.add(new SimpleGrantedAuthority(permiso.getName()));
        // Resultado: "usuarios:crear", "citas:leer", etc.
    });
});

// 6. UserDetails final:
UserDetails {
    username: "admin@barberia.com",
    password: "$2a$10$...",
    authorities: [
        "ROLE_ADMIN",
        "usuarios:crear",
        "usuarios:leer",
        "citas:crear",
        "citas:leer",
        // ... etc.
    ]
}
```

### Uso en Controllers

```java
// Verificar ROL
@PreAuthorize("hasRole('ADMIN')")  // Busca "ROLE_ADMIN" en authorities
public ResponseEntity<?> adminOnly() {
    // Solo usuarios con rol ADMIN pueden ejecutar esto
}

// Verificar PERMISO
@PreAuthorize("hasAuthority('usuarios:crear')")
public ResponseEntity<?> crearUsuario() {
    // Solo usuarios con permiso "usuarios:crear" pueden ejecutar esto
    // Ejemplo: ADMIN (sí), BARBERO (no), USER (no)
}

// Verificar MÚLTIPLES
@PreAuthorize("hasRole('ADMIN') OR hasAuthority('citas:leer')")
public ResponseEntity<?> verCitas() {
    // ADMIN (sí por rol), BARBERO (sí por permiso), USER (sí por permiso)
}

// Expresiones complejas
@PreAuthorize("hasRole('BARBERO') AND hasAuthority('citas:actualizar')")
public ResponseEntity<?> actualizarCita() {
    // Solo BARBERO que tenga el permiso específico
}
```

---

## 🔄 Interacciones Entre Componentes

### Mapa de Dependencias

```
AuthController
    ↓ depende de
AuthenticationService
    ↓ depende de
    ├─> UsuarioRepository (buscar/guardar usuarios)
    ├─> RolRepository (buscar roles)
    ├─> PasswordEncoder (encriptar passwords)
    ├─> JwtService (generar tokens)
    ├─> AuthenticationManager (validar credenciales)
    │   ↓ usa
    │   └─> CustomUserDetailsService (cargar usuarios)
    │       ↓ usa
    │       └─> UsuarioRepository
    └─> UsuarioMapper (convertir entities → DTOs)

JwtAuthenticationFilter
    ↓ depende de
    ├─> JwtService (extraer/validar token)
    └─> CustomUserDetailsService (cargar usuario)

SecurityConfig
    ↓ crea beans
    ├─> SecurityFilterChain (configura seguridad)
    ├─> AuthenticationManager (orquesta autenticación)
    ├─> AuthenticationProvider (provee lógica de autenticación)
    ├─> PasswordEncoder (BCrypt)
    └─> CorsConfigurationSource (CORS)
```

### Ciclo de Vida de una Request

```
1. Request HTTP llega al servidor
   ↓
2. JwtAuthenticationFilter intercepta
   ↓
3. Extrae y valida JWT
   ↓
4. Carga usuario y establece SecurityContext
   ↓
5. Request continúa a DispatcherServlet
   ↓
6. Controller recibe request
   ↓
7. @PreAuthorize valida permisos
   ↓
8. Si autorizado, ejecuta método
   ↓
9. Retorna respuesta
   ↓
10. Cliente recibe respuesta
```

---

## ✅ Ventajas del Sistema Dinámico

### 1. **Flexibilidad Total**
```sql
-- Agregar nuevo rol sin cambiar código
INSERT INTO roles VALUES (5, 'SUPERVISOR', 'Supervisor de operaciones');

-- Asignar permisos al nuevo rol
INSERT INTO role_permissions VALUES (5, 6);  -- citas:leer
INSERT INTO role_permissions VALUES (5, 7);  -- citas:actualizar
INSERT INTO role_permissions VALUES (5, 2);  -- usuarios:leer

-- ¡Ya funciona! No se requieren cambios en código
```

### 2. **Granularidad de Permisos**
```java
// Puedes tener permisos muy específicos
permisos:
  - reportes:generar
  - reportes:exportar:pdf
  - reportes:exportar:excel
  - configuracion:modificar:horarios
  - configuracion:modificar:precios
```

### 3. **Múltiples Roles por Usuario**
```sql
-- Un usuario puede tener varios roles
INSERT INTO user_roles VALUES (2, 2);  -- Usuario 2 = BARBERO
INSERT INTO user_roles VALUES (2, 4);  -- Usuario 2 = RECEPCIONISTA

-- Tendrá permisos de ambos roles automáticamente
```

### 4. **Cambios en Tiempo Real**
```sql
-- Cambiar permisos de un rol afecta inmediatamente
-- a todos los usuarios con ese rol
DELETE FROM role_permissions WHERE rol_id = 3 AND permiso_id = 5;
-- Los USER ya no pueden crear citas (próximo login)
```

---

## 🔐 Seguridad

### Mejores Prácticas Implementadas

✅ **Passwords encriptados** con BCrypt  
✅ **JWT firmados** con HMAC-SHA256  
✅ **Secret key** en variables de entorno  
✅ **Validación fail-fast** al iniciar  
✅ **Sesiones stateless** (sin HttpSession)  
✅ **CORS configurado** por perfil  
✅ **@Transactional** para atomicidad  
✅ **Roles/permisos dinámicos** desde BD  
✅ **Separación de concerns** (Service/Repository/Controller)  
✅ **DTOs** para no exponer entidades  

---

## 📚 Resumen

Este sistema implementa:

1. **Autenticación robusta** con Spring Security + JWT
2. **Roles y permisos completamente dinámicos** desde base de datos
3. **Arquitectura limpia** con clara separación de responsabilidades
4. **Seguridad multicapa** (filtros, autenticación, autorización)
5. **Escalabilidad** para agregar roles/permisos sin modificar código
6. **Buenas prácticas de la industria** (BCrypt, JWT, stateless, etc.)

### Componentes Clave

| Componente | Responsabilidad |
|------------|----------------|
| `AuthenticationService` | Orquesta login y registro |
| `CustomUserDetailsService` | Carga usuarios con roles/permisos |
| `JwtService` | Genera y valida tokens JWT |
| `JwtAuthenticationFilter` | Intercepta requests y autentica |
| `SecurityConfig` | Configura Spring Security |
| `UsuarioRepository` | Acceso a datos de usuarios |
| `RolRepository` | Acceso a roles |
| `PermisoRepository` | Acceso a permisos |

---

**¿Necesitas más detalles sobre algún componente específico?**
