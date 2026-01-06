# 🗺️ GUÍA ARCHIVO POR ARCHIVO - Conexiones y Flujos

## 📊 Mapa Visual del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CLIENTE (Postman/Frontend)                       │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │
                          │ HTTP Request
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CAPA DE CONTROLLERS (API REST)                        │
│  📄 AuthController.java          📄 DemoController.java                 │
│  - POST /api/auth/login          - GET /api/demo/admin                  │
│  - POST /api/auth/register       - GET /api/demo/protected              │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │
                          │ Llama a Services
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CAPA DE SERVICIOS (Lógica de Negocio)                │
│  📄 AuthenticationService.java                                           │
│  - register()  →  Crea usuario y genera JWT                             │
│  - login()     →  Valida credenciales y genera JWT                      │
└─────┬───────────────────┬───────────────────┬───────────────────────────┘
      │                   │                   │
      │                   │                   │
      ▼                   ▼                   ▼
┌──────────┐      ┌──────────────┐    ┌─────────────┐
│  Mapper  │      │  JWT Service │    │ Auth Manager│
│          │      │              │    │             │
│UsuarioMapper    │ JwtService   │    │   Spring    │
│.java     │      │ .java        │    │  Security   │
│          │      │              │    │             │
│Convierte │      │Genera/valida │    │  Valida     │
│Usuario   │      │   tokens     │    │ credenciales│
│a DTO     │      │              │    │             │
└──────────┘      └──────────────┘    └─────┬───────┘
                                             │
                                             ▼
                                    ┌────────────────┐
                                    │UserDetailsService│
                                    │                │
                                    │CustomUserDetails│
                                    │Service.java    │
                                    │                │
                                    │Carga usuario   │
                                    │desde BD        │
                                    └────────┬───────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CAPA DE REPOSITORIOS (Acceso a BD)                    │
│  📄 UsuarioRepository.java                                               │
│  - findByEmail()                                                         │
│  - findByEmailWithRolesAndPermissions()                                  │
│  - existsByEmail()                                                       │
│                                                                          │
│  📄 RolRepository.java           📄 PermisoRepository.java               │
│  - findByName()                  - findByName()                          │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │
                          │ JPA/Hibernate
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           BASE DE DATOS MySQL                            │
│  📊 usuarios  ←→  📊 user_roles  ←→  📊 roles                           │
│                                        ↕                                 │
│                             📊 role_permissions                          │
│                                        ↕                                 │
│                                   📊 permisos                            │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 GUÍA ARCHIVO POR ARCHIVO

### 🔷 NIVEL 1: BASE DE DATOS (Entidades)

#### 1️⃣ Usuario.java 👤
**Ubicación:** `models/Usuario.java`

**¿Qué es?**  
Representa un usuario del sistema (cliente, empleado, admin).

**Campos importantes:**
```java
- id: Identificador único
- email: Para login (único)
- password: Encriptado con BCrypt
- name: Nombre del usuario
- roles: Set<Rol> - Roles asignados (ManyToMany)
```

**Relaciones:**
- **Usuario ←→ Rol** (ManyToMany): Un usuario puede tener varios roles

**Se conecta con:**
- `UsuarioRepository` → Para guardar/buscar usuarios
- `CustomUserDetailsService` → Para cargar usuarios en Spring Security
- `AuthenticationService` → Para crear/autenticar usuarios

**Ejemplo:**
```java
Usuario admin = Usuario.builder()
    .email("admin@barberia.com")
    .password("$2a$10$...")  // BCrypt
    .roles(Set.of(rolAdmin))
    .build();
```

---

#### 2️⃣ Rol.java 🎭
**Ubicación:** `models/Rol.java`

**¿Qué es?**  
Representa un rol del sistema (ADMIN, USER, MANAGER).

**Campos importantes:**
```java
- id: Identificador único
- name: Nombre del rol (ADMIN, USER, MANAGER)
- permissions: Set<Permiso> - Permisos del rol (ManyToMany)
```

**Relaciones:**
- **Rol ←→ Usuario** (ManyToMany): Un rol puede tener muchos usuarios
- **Rol ←→ Permiso** (ManyToMany): Un rol puede tener muchos permisos

**Se conecta con:**
- `Usuario` → A través de la relación ManyToMany
- `Permiso` → A través de la relación ManyToMany
- `RolRepository` → Para buscar roles
- `CustomUserDetailsService` → Para extraer authorities

**Ejemplo:**
```java
Rol admin = Rol.builder()
    .name("ADMIN")
    .description("Administrador del sistema")
    .permissions(Set.of(readClients, createClients, deleteUsers))
    .build();
```

---

#### 3️⃣ Permiso.java 🔑
**Ubicación:** `models/Permiso.java`

**¿Qué es?**  
Representa un permiso específico (READ_CLIENTS, CREATE_BOOKING).

**Campos importantes:**
```java
- id: Identificador único
- name: Nombre del permiso (READ_CLIENTS)
- description: Descripción legible
```

**Relaciones:**
- **Permiso ←→ Rol** (ManyToMany): Un permiso puede estar en varios roles

**Se conecta con:**
- `Rol` → A través de la relación ManyToMany
- `PermisoRepository` → Para buscar permisos
- `CustomUserDetailsService` → Para extraer authorities

**Ejemplo:**
```java
Permiso readClients = Permiso.builder()
    .name("READ_CLIENTS")
    .description("Ver lista de clientes")
    .build();
```

---

### 🔷 NIVEL 2: ACCESO A DATOS (Repositorios)

#### 4️⃣ UsuarioRepository.java 💾
**Ubicación:** `repositories/UsuarioRepository.java`

**¿Qué hace?**  
Proporciona métodos para consultar usuarios en MySQL.

**Métodos principales:**
```java
findByEmail(String email)
// SELECT * FROM usuarios WHERE email = ?

existsByEmail(String email)
// SELECT COUNT(*) > 0 FROM usuarios WHERE email = ?

findByEmailWithRolesAndPermissions(String email)
// SELECT u, r, p FROM usuarios u
// LEFT JOIN FETCH u.roles r
// LEFT JOIN FETCH r.permissions p
// WHERE u.email = ?
```

**¿Por qué JOIN FETCH?**  
Evita el problema N+1 (múltiples queries). Carga todo en una sola consulta.

**Usado por:**
- `CustomUserDetailsService` → Para cargar usuarios en login
- `AuthenticationService` → Para verificar/crear usuarios

---

#### 5️⃣ RolRepository.java & PermisoRepository.java 💾
**Ubicación:** `repositories/RolRepository.java`, `repositories/PermisoRepository.java`

**¿Qué hacen?**  
Buscan roles y permisos por nombre.

**Usado por:**
- `DataInitializer` → Para crear datos de prueba
- `AuthenticationService` → Para asignar rol USER al registrar

---

### 🔷 NIVEL 3: MAPPERS (Conversión)

#### 6️⃣ UsuarioMapper.java 🔄
**Ubicación:** `mappers/UsuarioMapper.java`

**¿Qué hace?**  
Convierte entre entidades (Usuario) y DTOs (AuthResponse).

**Métodos principales:**
```java
toAuthResponse(Usuario usuario, String token)
// Usuario + JWT → AuthResponse
// Extrae roles y permisos del usuario
// Construye el DTO para el cliente

extractRoleNames(Usuario usuario)
// Usuario → Set<String> de roles

extractPermissionNames(Usuario usuario)
// Usuario → Set<String> de permisos
```

**¿Por qué usar mappers?**
- ✅ Separa la capa de presentación del dominio
- ✅ Controla qué información se expone al cliente
- ✅ Facilita cambios sin afectar la BD
- ✅ Evita exponer entidades JPA directamente

**Usado por:**
- `AuthenticationService` → Para construir AuthResponse

**Ejemplo:**
```java
Usuario usuario = /* desde BD */;
String token = jwtService.generateToken(...);

AuthResponse response = usuarioMapper.toAuthResponse(usuario, token);
// {
//   "token": "eyJhbG...",
//   "email": "admin@barberia.com",
//   "roles": ["ADMIN"],
//   "permissions": ["READ_CLIENTS", "CREATE_BOOKING"]
// }
```

---

### 🔷 NIVEL 4: DTOs (Data Transfer Objects)

#### 7️⃣ LoginRequest.java 📥
**Ubicación:** `dto/LoginRequest.java`

**¿Qué es?**  
Objeto que recibe las credenciales del cliente.

**Campos:**
```java
@NotBlank @Email
String email;

@NotBlank
String password;
```

**Flujo:**
```
Cliente → POST /api/auth/login
Body: { "email": "admin@...", "password": "admin123" }
    ↓
AuthController recibe LoginRequest
    ↓
AuthenticationService valida
```

---

#### 8️⃣ RegisterRequest.java 📥
**Ubicación:** `dto/RegisterRequest.java`

**¿Qué es?**  
Objeto que recibe datos para registrar un usuario.

**Campos:**
```java
@NotBlank @Size(min=2, max=50)
String name;

@NotBlank @Email
String email;

@NotBlank @Size(min=6)
String password;
```

---

#### 9️⃣ AuthResponse.java 📤
**Ubicación:** `dto/AuthResponse.java`

**¿Qué es?**  
Objeto que se retorna al cliente después de login/registro.

**Campos:**
```java
String token;          // JWT
String type = "Bearer";
String email;
String name;
Set<String> roles;
Set<String> permissions;
```

**Ejemplo:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@barberia.com",
  "name": "Admin",
  "roles": ["ADMIN"],
  "permissions": ["READ_CLIENTS", "CREATE_CLIENTS", "DELETE_USERS"]
}
```

---

### 🔷 NIVEL 5: SEGURIDAD (Core de Spring Security)

#### 🔟 JwtService.java 🔐
**Ubicación:** `security/JwtService.java`

**¿Qué hace?**  
Genera y valida tokens JWT.

**Métodos principales:**

**1. generateToken(UserDetails userDetails)**
```java
// Crea JWT con:
// - Subject: email del usuario
// - Claims: roles y permisos
// - Expiration: 24 horas
// - Firma: HMAC-SHA256 con clave secreta

String token = jwtService.generateToken(userDetails);
// "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1..."
```

**2. extractUsername(String token)**
```java
// Extrae el email del token
String email = jwtService.extractUsername(token);
// "admin@barberia.com"
```

**3. isTokenValid(String token, UserDetails userDetails)**
```java
// Valida:
// - La firma es correcta
// - No ha expirado
// - El email coincide
boolean valid = jwtService.isTokenValid(token, userDetails);
```

**Usado por:**
- `AuthenticationService` → Para generar tokens al login
- `JwtAuthenticationFilter` → Para validar tokens en cada request

---

#### 1️⃣1️⃣ JwtAuthenticationFilter.java 🚧
**Ubicación:** `security/JwtAuthenticationFilter.java`

**¿Qué hace?**  
Intercepta CADA request HTTP y valida el JWT.

**Flujo completo:**
```java
1. Request llega → doFilterInternal()
2. Extrae header "Authorization: Bearer <token>"
3. Extrae el token (quita "Bearer ")
4. Extrae email del token → jwtService.extractUsername()
5. Carga usuario desde BD → userDetailsService.loadUserByUsername()
6. Valida token → jwtService.isTokenValid()
7. Si válido:
   - Crea Authentication
   - Establece en SecurityContext
8. El request continúa → Controller
```

**¿Por qué es importante?**  
Sin este filtro, Spring Security no sabría quién es el usuario autenticado.

**Se ejecuta:**
- En CADA request (excepto los públicos)
- ANTES de llegar al controller
- ANTES de verificar @PreAuthorize

---

#### 1️⃣2️⃣ CustomUserDetailsService.java 👥
**Ubicación:** `security/CustomUserDetailsService.java`

**¿Qué hace?**  
Carga usuarios desde la base de datos para Spring Security.

**Método principal:**
```java
loadUserByUsername(String email) {
    // 1. Busca usuario en BD
    Usuario usuario = usuarioRepository.findByEmailWithRolesAndPermissions(email);
    
    // 2. Extrae authorities (roles + permisos)
    // - Roles: ROLE_ADMIN, ROLE_USER
    // - Permisos: READ_CLIENTS, CREATE_BOOKING
    
    // 3. Retorna UserDetails
    return User.builder()
        .username(usuario.getEmail())
        .password(usuario.getPassword())
        .authorities(authorities)
        .build();
}
```

**¿Cuándo se usa?**
- Al hacer login (AuthenticationManager lo llama)
- Al validar JWT (JwtAuthenticationFilter lo llama)

**Se conecta con:**
- `UsuarioRepository` → Para buscar usuarios
- `AuthenticationManager` → Spring Security lo usa automáticamente

---

### 🔷 NIVEL 6: CONFIGURACIÓN

#### 1️⃣3️⃣ SecurityConfig.java ⚙️
**Ubicación:** `config/SecurityConfig.java`

**¿Qué hace?**  
**LA CONFIGURACIÓN MÁS IMPORTANTE** - Define cómo funciona la seguridad.

**Componentes:**

**1. securityFilterChain()**
```java
http
    .csrf(disable)  // Deshabilitado para JWT
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()  // Público
        .anyRequest().authenticated()  // Resto protegido
    )
    .sessionManagement(STATELESS)  // Sin sesiones
    .addFilterBefore(jwtAuthFilter, ...)  // Registra filtro JWT
```

**2. authenticationProvider()**
```java
// Define cómo autenticar:
// UserDetailsService + PasswordEncoder
```

**3. passwordEncoder()**
```java
// BCrypt para encriptar passwords
```

**Anotaciones importantes:**
- `@EnableWebSecurity` → Habilita Spring Security
- `@EnableMethodSecurity` → Habilita @PreAuthorize

---

#### 1️⃣4️⃣ GlobalExceptionHandler.java ⚠️
**Ubicación:** `config/GlobalExceptionHandler.java`

**¿Qué hace?**  
Maneja errores globalmente con respuestas consistentes.

**Excepciones manejadas:**
```java
BadCredentialsException → 401 Unauthorized
MethodArgumentNotValidException → 400 Bad Request
UsernameNotFoundException → 404 Not Found
RuntimeException → 400 Bad Request
Exception → 500 Internal Server Error
```

---

#### 1️⃣5️⃣ DevSecurityConfig.java 🧪
**Ubicación:** `config/DevSecurityConfig.java`

**¿Qué hace?**  
Configuración alternativa para desarrollo SIN seguridad.

**Activar:**
```properties
spring.profiles.active=dev
```

**⚠️ SOLO PARA DESARROLLO**

---

### 🔷 NIVEL 7: SERVICIOS (Lógica de Negocio)

#### 1️⃣6️⃣ AuthenticationService.java 🎯
**Ubicación:** `services/AuthenticationService.java`

**¿Qué hace?**  
Lógica de login y registro.

**Método register():**
```
1. Valida email único → usuarioRepository.existsByEmail()
2. Encripta password → passwordEncoder.encode()
3. Busca rol USER → rolRepository.findByName("USER")
4. Crea usuario → Usuario.builder()
5. Guarda en BD → usuarioRepository.save()
6. Genera JWT → jwtService.generateToken()
7. Construye respuesta → usuarioMapper.toAuthResponse()
8. Retorna AuthResponse
```

**Método login():**
```
1. Valida credenciales → authenticationManager.authenticate()
2. Extrae UserDetails → authentication.getPrincipal()
3. Genera JWT → jwtService.generateToken()
4. Carga usuario → usuarioRepository.findByEmail...()
5. Construye respuesta → usuarioMapper.toAuthResponse()
6. Retorna AuthResponse
```

**Se conecta con:**
- `UsuarioRepository` → Guardar/buscar usuarios
- `RolRepository` → Buscar roles
- `AuthenticationManager` → Validar credenciales
- `JwtService` → Generar tokens
- `UsuarioMapper` → Construir respuesta
- `PasswordEncoder` → Encriptar passwords

---

### 🔷 NIVEL 8: CONTROLLERS (API REST)

#### 1️⃣7️⃣ AuthController.java 🌐
**Ubicación:** `controllers/AuthController.java`

**¿Qué hace?**  
Expone endpoints públicos de autenticación.

**Endpoints:**
```java
POST /api/auth/register
// Registra nuevo usuario

POST /api/auth/login
// Autentica usuario

GET /api/auth/public
// Endpoint público de prueba
```

**Flujo:**
```
Cliente → AuthController → AuthenticationService → Respuesta
```

---

#### 1️⃣8️⃣ DemoController.java 🧪
**Ubicación:** `controllers/DemoController.java`

**¿Qué hace?**  
Ejemplos de endpoints protegidos con @PreAuthorize.

**Endpoints:**
```java
GET /api/demo/protected
// Requiere: Autenticación

GET /api/demo/admin
@PreAuthorize("hasRole('ADMIN')")
// Requiere: Rol ADMIN

GET /api/demo/read-clients
@PreAuthorize("hasAuthority('READ_CLIENTS')")
// Requiere: Permiso READ_CLIENTS

DELETE /api/demo/delete-user/{id}
@PreAuthorize("hasRole('ADMIN') and hasAuthority('DELETE_USERS')")
// Requiere: ADMIN + permiso
```

---

### 🔷 NIVEL 9: UTILIDADES

#### 1️⃣9️⃣ DataInitializer.java 🌱
**Ubicación:** `utils/DataInitializer.java`

**¿Qué hace?**  
Crea datos de prueba al iniciar la aplicación.

**Crea:**
- 9 permisos
- 3 roles (ADMIN, MANAGER, USER)
- 4 usuarios de prueba

**Se ejecuta:**
- Al iniciar la aplicación (CommandLineRunner)
- Solo si no hay usuarios en BD

---

## 🔄 FLUJOS COMPLETOS

### 🟢 FLUJO 1: REGISTRO DE USUARIO

```
1. Cliente → POST /api/auth/register
   Body: { name, email, password }
        ↓
2. AuthController.register()
   - Valida con @Valid
        ↓
3. AuthenticationService.register()
   - usuarioRepository.existsByEmail() → Valida email único
   - rolRepository.findByName("USER") → Busca rol
   - passwordEncoder.encode() → Encripta password
   - Usuario.builder() → Crea usuario
   - usuarioRepository.save() → Guarda en BD
   - jwtService.generateToken() → Genera JWT
   - usuarioMapper.toAuthResponse() → Construye respuesta
        ↓
4. AuthController retorna AuthResponse
        ↓
5. Cliente recibe token JWT
```

---

### 🔵 FLUJO 2: LOGIN

```
1. Cliente → POST /api/auth/login
   Body: { email, password }
        ↓
2. AuthController.login()
        ↓
3. AuthenticationService.login()
        ↓
4. authenticationManager.authenticate()
        ↓
5. Spring Security llama CustomUserDetailsService.loadUserByUsername()
        ↓
6. usuarioRepository.findByEmailWithRolesAndPermissions()
        ↓
7. MySQL retorna Usuario con roles y permisos
        ↓
8. CustomUserDetailsService extrae authorities
   - Roles: ROLE_ADMIN
   - Permisos: READ_CLIENTS, CREATE_BOOKING, etc.
        ↓
9. AuthenticationManager valida password
   - passwordEncoder.matches()
        ↓
10. Si válido, retorna Authentication
        ↓
11. jwtService.generateToken() → Genera JWT
        ↓
12. usuarioMapper.toAuthResponse() → Construye respuesta
        ↓
13. Cliente recibe token JWT con roles y permisos
```

---

### 🟣 FLUJO 3: REQUEST AUTENTICADO

```
1. Cliente → GET /api/demo/admin
   Header: Authorization: Bearer <token>
        ↓
2. Spring Security Filter Chain
        ↓
3. JwtAuthenticationFilter.doFilterInternal()
   - Extrae header Authorization
   - Extrae token (quita "Bearer ")
   - jwtService.extractUsername() → Obtiene email
        ↓
4. userDetailsService.loadUserByUsername(email)
        ↓
5. usuarioRepository.findByEmailWithRolesAndPermissions()
        ↓
6. MySQL retorna Usuario con roles
        ↓
7. CustomUserDetailsService retorna UserDetails con authorities
        ↓
8. jwtService.isTokenValid() → Valida firma y expiración
        ↓
9. Si válido:
   - Crea Authentication con UserDetails y authorities
   - SecurityContextHolder.setAuthentication()
        ↓
10. @PreAuthorize("hasRole('ADMIN')")
    - Spring Security consulta SecurityContext
    - Busca "ROLE_ADMIN" en authorities
    - Si lo tiene → Permite acceso
    - Si no → 403 Forbidden
        ↓
11. DemoController.adminEndpoint() → Ejecuta método
        ↓
12. Cliente recibe respuesta
```

---

## 📊 TABLA DE CONEXIONES

| Archivo | Usa (Inyecta) | Es Usado Por |
|---------|---------------|--------------|
| **Usuario.java** | - | UsuarioRepository, CustomUserDetailsService |
| **Rol.java** | Permiso | Usuario, RolRepository |
| **Permiso.java** | - | Rol, PermisoRepository |
| **UsuarioRepository** | Usuario | AuthenticationService, CustomUserDetailsService |
| **RolRepository** | Rol | AuthenticationService, DataInitializer |
| **UsuarioMapper** | Usuario, Rol, Permiso | AuthenticationService |
| **JwtService** | - | AuthenticationService, JwtAuthenticationFilter |
| **CustomUserDetailsService** | UsuarioRepository | AuthenticationManager, JwtAuthenticationFilter |
| **JwtAuthenticationFilter** | JwtService, UserDetailsService | SecurityConfig |
| **SecurityConfig** | JwtAuthenticationFilter, UserDetailsService, PasswordEncoder | Spring Security |
| **AuthenticationService** | UsuarioRepository, RolRepository, JwtService, UsuarioMapper, PasswordEncoder, AuthenticationManager | AuthController |
| **AuthController** | AuthenticationService | Cliente HTTP |
| **DemoController** | - | Cliente HTTP |

---

## 🎯 ORDEN DE ESTUDIO RECOMENDADO

### Día 1: Base de Datos
1. **Usuario.java** - Entiende la entidad
2. **Rol.java** - Entiende la relación con Usuario
3. **Permiso.java** - Entiende la relación con Rol
4. Revisa las tablas en MySQL

### Día 2: Repositorios y Mappers
5. **UsuarioRepository** - Queries y JOIN FETCH
6. **RolRepository** y **PermisoRepository**
7. **UsuarioMapper** - Conversión entidad → DTO

### Día 3: JWT
8. **JwtService** - Generación y validación
9. Decodifica un JWT en jwt.io
10. Entiende claims y firma

### Día 4: Spring Security Core
11. **CustomUserDetailsService** - Carga usuarios
12. **JwtAuthenticationFilter** - Intercepta requests
13. **SecurityConfig** - Configuración principal

### Día 5: Servicios y Controllers
14. **AuthenticationService** - Lógica de login
15. **AuthController** - Endpoints
16. **DemoController** - @PreAuthorize

### Día 6: Flujos Completos
17. Sigue el flujo de registro completo
18. Sigue el flujo de login completo
19. Sigue el flujo de request autenticado

### Día 7: Práctica y Teoría
20. Prueba todos los endpoints
21. Lee SECURITY_GUIDE.md
22. Prepara respuestas de entrevista

---

**¡Ahora tienes el mapa completo del sistema!** 🗺️

**Siguiente paso:** Empieza con [Usuario.java](src/main/java/com/barberia/models/Usuario.java) y avanza en orden.
