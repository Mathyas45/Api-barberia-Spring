# 🔐 Guía Completa de Spring Security con JWT

## 📚 Tabla de Contenidos

1. [Flujo Completo de Autenticación](#flujo-completo-de-autenticación)
2. [Mapa del Proyecto](#mapa-del-proyecto)
3. [Glosario de Términos](#glosario-de-términos)
4. [Cómo Spring Security Valida un Request](#cómo-spring-security-valida-un-request)
5. [Buenas Prácticas](#buenas-prácticas)
6. [Preguntas de Entrevista](#preguntas-de-entrevista)
7. [Comandos de Testing](#comandos-de-testing)

---

## 🔄 Flujo Completo de Autenticación

### 1️⃣ REGISTRO DE USUARIO

```
┌──────────┐                 ┌────────────────────┐
│  Cliente │                 │  AuthController    │
└─────┬────┘                 └──────────┬─────────┘
      │                                 │
      │ POST /api/auth/register        │
      ├────────────────────────────────>│
      │ {                               │
      │   "name": "Juan",               │
      │   "email": "juan@mail.com",     │         ┌──────────────────────┐
      │   "password": "123456"          │         │ AuthenticationService│
      │ }                               │         └──────────┬───────────┘
      │                                 │                    │
      │                                 │ register()         │
      │                                 ├───────────────────>│
      │                                 │                    │
      │                                 │                    │ 1. Valida email único
      │                                 │                    │ 2. Encripta password (BCrypt)
      │                                 │                    │ 3. Asigna rol USER
      │                                 │                    │ 4. Guarda en BD
      │                                 │                    │
      │                                 │                    │ generateToken()
      │                                 │                    ├──────────────┐
      │                                 │                    │              │
      │                                 │                    │<─────────────┘
      │                                 │                    │
      │                                 │ AuthResponse       │
      │                                 │<───────────────────┤
      │ {                               │                    │
      │   "token": "eyJhbG...",         │
      │   "type": "Bearer",             │
      │   "email": "juan@mail.com",     │
      │   "roles": ["USER"]             │
      │ }                               │
      │<────────────────────────────────┤
      │                                 │
```

### 2️⃣ LOGIN (AUTENTICACIÓN)

```
┌──────────┐                 ┌────────────────────┐
│  Cliente │                 │  AuthController    │
└─────┬────┘                 └──────────┬─────────┘
      │                                 │
      │ POST /api/auth/login           │
      ├────────────────────────────────>│
      │ {                               │
      │   "email": "juan@mail.com",     │         ┌──────────────────────┐
      │   "password": "123456"          │         │ AuthenticationService│
      │ }                               │         └──────────┬───────────┘
      │                                 │                    │
      │                                 │ login()            │
      │                                 ├───────────────────>│
      │                                 │                    │
      │                                 │                    │ authenticate()
      │                                 │                    ├──────────────┐
      │                                 │                    │              │
      │                                 │                    ├──────────────┤
      │                                 │                    │              │
      │                                 │                    │ AuthenticationManager
      │                                 │                    │      │
      │                                 │                    │      v
      │                                 │                    │ UserDetailsService.loadUserByUsername()
      │                                 │                    │      │
      │                                 │                    │      v
      │                                 │                    │ Busca usuario en BD
      │                                 │                    │      │
      │                                 │                    │      v
      │                                 │                    │ PasswordEncoder.matches()
      │                                 │                    │      │
      │                                 │                    │      v
      │                                 │                    │ ✅ Credenciales válidas
      │                                 │                    │
      │                                 │                    │ generateToken()
      │                                 │                    ├──────────────┐
      │                                 │                    │              │
      │                                 │                    │<─────────────┘
      │                                 │                    │
      │                                 │ AuthResponse       │
      │                                 │<───────────────────┤
      │ {                               │                    │
      │   "token": "eyJhbG...",         │
      │   "email": "juan@mail.com",     │
      │   "roles": ["USER"],            │
      │   "permissions": [...]          │
      │ }                               │
      │<────────────────────────────────┤
      │                                 │
```

### 3️⃣ REQUEST CON JWT (AUTORIZACIÓN)

```
┌──────────┐                                          ┌────────────────────────┐
│  Cliente │                                          │ JwtAuthenticationFilter│
└─────┬────┘                                          └───────────┬────────────┘
      │                                                           │
      │ GET /api/demo/admin                                      │
      │ Authorization: Bearer eyJhbGci...                        │
      ├─────────────────────────────────────────────────────────>│
      │                                                           │
      │                                                           │ 1. Extrae header Authorization
      │                                                           │ 2. Extrae token (quita "Bearer ")
      │                                                           │ 3. Extrae email del token
      │                                                           │ 4. Valida token
      │                                                           │
      │                                                           │ loadUserByUsername()
      │                                                           ├──────────────┐
      │                                                           │              │
      │                                                           │ Busca usuario│
      │                                                           │ en BD        │
      │                                                           │<─────────────┘
      │                                                           │
      │                                                           │ isTokenValid()
      │                                                           ├──────────────┐
      │                                                           │              │
      │                                                           │ ✅ Válido   │
      │                                                           │<─────────────┘
      │                                                           │
      │                                                           │ 5. Crea Authentication
      │                                                           │ 6. Establece en SecurityContext
      │                                                           │
      │                                                           v
      │                                                  ┌────────────────┐
      │                                                  │ SecurityConfig │
      │                                                  └────────┬───────┘
      │                                                           │
      │                                                           │ Usuario autenticado ✅
      │                                                           │
      │                                                           v
      │                                                  ┌────────────────┐
      │                                                  │ @PreAuthorize  │
      │                                                  └────────┬───────┘
      │                                                           │
      │                                                           │ hasRole('ADMIN')?
      │                                                           │
      │                                                           │ ✅ SÍ
      │                                                           │
      │                                                           v
      │                                                  ┌────────────────┐
      │                                                  │  Controller    │
      │                                                  └────────┬───────┘
      │                                                           │
      │ {                                                         │
      │   "message": "Solo ADMIN"                                 │
      │ }                                                         │
      │<──────────────────────────────────────────────────────────┤
      │                                                           │
```

---

## 🗺️ Mapa del Proyecto

### 📁 Estructura de Directorios

```
src/main/java/com/barberia/
│
├── 📦 models/                    # Entidades JPA (Base de Datos)
│   ├── Usuario.java             # Usuario con email, password, roles
│   ├── Rol.java                 # Roles (ADMIN, USER, MANAGER)
│   └── Permiso.java             # Permisos (READ_CLIENTS, CREATE_BOOKING, etc.)
│
├── 📦 repositories/              # Acceso a datos (Spring Data JPA)
│   ├── UsuarioRepository.java   # Queries para usuarios
│   ├── RolRepository.java       # Queries para roles
│   └── PermisoRepository.java   # Queries para permisos
│
├── 📦 dto/                       # Data Transfer Objects (Request/Response)
│   ├── LoginRequest.java        # Email + password para login
│   ├── RegisterRequest.java     # Datos para registro
│   └── AuthResponse.java        # Respuesta con token JWT
│
├── 📦 security/                  # Componentes de seguridad
│   ├── JwtService.java          # Generación y validación de JWT
│   ├── JwtAuthenticationFilter.java  # Filtro que intercepta requests
│   └── CustomUserDetailsService.java # Carga usuarios desde BD
│
├── 📦 config/                    # Configuraciones
│   ├── SecurityConfig.java      # Configuración principal de seguridad
│   └── GlobalExceptionHandler.java # Manejo global de errores
│
├── 📦 services/                  # Lógica de negocio
│   └── AuthenticationService.java # Login y registro
│
├── 📦 controllers/               # Endpoints REST
│   ├── AuthController.java      # /api/auth/** (login, register)
│   └── DemoController.java      # /api/demo/** (ejemplos protegidos)
│
└── 📦 utils/                     # Utilidades
    └── DataInitializer.java     # Crea datos de prueba al iniciar
```

---

### 🔗 Flujo de Conexiones entre Archivos

#### 🟢 1. INICIO DE LA APLICACIÓN

```
DemoApplication.java (main)
        │
        ├─> DataInitializer.java
        │   └─> Crea usuarios, roles y permisos de prueba
        │
        └─> SecurityConfig.java
            ├─> Registra JwtAuthenticationFilter
            ├─> Configura endpoints públicos/privados
            ├─> Define PasswordEncoder (BCrypt)
            └─> Define AuthenticationProvider
```

#### 🔵 2. FLUJO DE LOGIN

```
1. AuthController.login()
        │
        v
2. AuthenticationService.login()
        │
        ├─> AuthenticationManager.authenticate()
        │   │
        │   └─> CustomUserDetailsService.loadUserByUsername()
        │       │
        │       └─> UsuarioRepository.findByEmailWithRolesAndPermissions()
        │           └─> MySQL (usuarios, roles, permisos)
        │
        ├─> JwtService.generateToken()
        │   └─> Crea JWT firmado
        │
        └─> Retorna AuthResponse con token
```

#### 🟣 3. FLUJO DE REQUEST AUTENTICADO

```
1. Request HTTP con JWT
        │
        v
2. JwtAuthenticationFilter.doFilterInternal()
        │
        ├─> JwtService.extractUsername()
        │   └─> Lee el token y extrae el email
        │
        ├─> CustomUserDetailsService.loadUserByUsername()
        │   └─> Carga usuario desde BD
        │
        ├─> JwtService.isTokenValid()
        │   └─> Valida firma y expiración
        │
        └─> SecurityContextHolder.setAuthentication()
            └─> Usuario autenticado en el contexto
```

#### 🟡 4. FLUJO DE AUTORIZACIÓN (@PreAuthorize)

```
1. Request llega al Controller
        │
        v
2. @PreAuthorize("hasRole('ADMIN')")
        │
        v
3. Spring Security consulta SecurityContext
        │
        ├─> Usuario está autenticado? ✅
        ├─> Usuario tiene rol ADMIN? ✅
        │
        v
4. Ejecuta el método del Controller
        │
        v
5. Retorna respuesta al cliente
```

---

## 📖 Glosario de Términos

### 🔐 Conceptos de Spring Security

#### **Authentication (Autenticación)**
- **Qué es:** Proceso de verificar **QUIÉN eres**
- **Cómo:** Email + password → Token JWT
- **En Spring Security:** Objeto `Authentication` que contiene:
  - `Principal`: Identidad del usuario (email)
  - `Credentials`: Contraseña (se limpia después de autenticar)
  - `Authorities`: Roles y permisos

#### **Authorization (Autorización)**
- **Qué es:** Proceso de verificar **QUÉ PUEDES HACER**
- **Cómo:** Spring Security consulta los roles/permisos del usuario
- **En Spring Security:** Se usa `@PreAuthorize`, `@Secured`, o configuración de URLs

#### **Principal**
- **Qué es:** El usuario autenticado actualmente
- **Contenido:** Generalmente el email o username
- **Acceso:** `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`

#### **GrantedAuthority**
- **Qué es:** Una "capacidad" del usuario
- **Tipos:**
  - **Roles:** ROLE_ADMIN, ROLE_USER (prefijo ROLE_)
  - **Permisos:** READ_CLIENTS, CREATE_BOOKING (sin prefijo)
- **Uso:** Spring Security los consulta para autorizar

#### **SecurityContext**
- **Qué es:** Contenedor que almacena la autenticación del usuario actual
- **Alcance:** Thread-local (cada request tiene su propio contexto)
- **Acceso:** `SecurityContextHolder.getContext()`

#### **SecurityContextHolder**
- **Qué es:** Clase estática que da acceso al SecurityContext
- **Uso:**
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String email = auth.getName();
```

#### **Filter (Filtro)**
- **Qué es:** Componente que intercepta requests HTTP antes del controller
- **Cadena de filtros:** Spring Security tiene ~10 filtros predeterminados
- **Nuestro filtro:** `JwtAuthenticationFilter` (valida el JWT)

#### **UserDetails**
- **Qué es:** Interfaz que representa un usuario en Spring Security
- **Contiene:**
  - Username
  - Password
  - Authorities (roles + permisos)
  - Flags (enabled, locked, expired, etc.)
- **Implementación:** `User.builder()` o clase personalizada

#### **UserDetailsService**
- **Qué es:** Interfaz para cargar usuarios desde la base de datos
- **Método:** `loadUserByUsername(String username)`
- **Nuestra implementación:** `CustomUserDetailsService`

#### **AuthenticationManager**
- **Qué es:** Gestor principal de autenticación
- **Responsabilidad:** Coordina el proceso de login
- **Usa:** `AuthenticationProvider` para validar credenciales

#### **AuthenticationProvider**
- **Qué es:** Define CÓMO autenticar
- **DaoAuthenticationProvider:**
  - Usa `UserDetailsService` para cargar usuario
  - Usa `PasswordEncoder` para validar password
  - Si coincide → autenticación exitosa

#### **PasswordEncoder**
- **Qué es:** Encripta y valida passwords
- **BCryptPasswordEncoder:**
  - Algoritmo BCrypt (one-way hash)
  - Salt automático (mismo password → hashes diferentes)
  - Adaptive (se puede ajustar la complejidad)

---

### 🔑 Conceptos de JWT

#### **JWT (JSON Web Token)**
- **Qué es:** Token firmado que contiene información del usuario
- **Estructura:** `header.payload.signature`
  - **Header:** Tipo de token y algoritmo de firma
  - **Payload:** Claims (datos del usuario)
  - **Signature:** Firma digital para verificar integridad
- **Ventaja:** Stateless (no requiere sesión en el servidor)

#### **Claims**
- **Qué es:** Datos almacenados en el payload del JWT
- **Claims estándar:**
  - `sub` (subject): Email del usuario
  - `iat` (issued at): Fecha de creación
  - `exp` (expiration): Fecha de expiración
- **Claims personalizados:**
  - `roles`: ["ADMIN", "USER"]
  - `permissions`: ["READ_CLIENTS"]

#### **Secret Key**
- **Qué es:** Clave secreta para firmar el JWT
- **Importancia:** SOLO el servidor la conoce
- **Seguridad:** Si alguien la obtiene, puede crear tokens falsos
- **Recomendación:** Mínimo 256 bits, almacenar en variable de entorno

#### **Stateless**
- **Qué es:** El servidor NO guarda sesión del usuario
- **Cómo funciona:** Cada request trae su JWT
- **Ventajas:**
  - Escalabilidad (no hay sesiones compartidas)
  - Microservicios (cada servicio valida el JWT)
- **Desventajas:**
  - No se puede "invalidar" un token antes de que expire

---

### 📋 Conceptos de @PreAuthorize

#### **@PreAuthorize**
- **Qué es:** Anotación para proteger métodos con SpEL
- **Ubicación:** Sobre métodos de @Controller o @Service
- **Requiere:** `@EnableMethodSecurity` en SecurityConfig

#### **hasRole('ADMIN')**
- **Qué hace:** Verifica que el usuario tenga el rol ADMIN
- **Busca:** "ROLE_ADMIN" en las authorities
- **Ejemplo:**
```java
@PreAuthorize("hasRole('ADMIN')")
public String adminOnly() { ... }
```

#### **hasAuthority('READ_CLIENTS')**
- **Qué hace:** Verifica que el usuario tenga el permiso exacto
- **Busca:** "READ_CLIENTS" (sin prefijo)
- **Ejemplo:**
```java
@PreAuthorize("hasAuthority('READ_CLIENTS')")
public List<Client> getClients() { ... }
```

#### **hasAnyRole('ADMIN', 'MANAGER')**
- **Qué hace:** Verifica que tenga AL MENOS uno de los roles
- **Ejemplo:**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public String managerArea() { ... }
```

#### **Expresiones SpEL complejas**
```java
// AND: Requiere ambas condiciones
@PreAuthorize("hasRole('ADMIN') and hasAuthority('DELETE_USERS')")

// OR: Requiere al menos una
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")

// Combinaciones
@PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and hasAuthority('SPECIAL_ACCESS'))")
```

---

## ⚙️ Cómo Spring Security Valida un Request

### Flujo Detallado Paso a Paso

```
┌───────────────────────────────────────────────────────────────────┐
│                    1. REQUEST LLEGA AL SERVIDOR                   │
│                                                                    │
│  GET /api/demo/admin                                              │
│  Headers:                                                         │
│    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCIOiJKV1QiLCJz │
└───────────────────────────────────────────────────────────────────┘
                               │
                               v
┌───────────────────────────────────────────────────────────────────┐
│            2. CADENA DE FILTROS DE SPRING SECURITY                │
│                                                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  🔹 SecurityContextPersistenceFilter                        │ │
│  │     └─> Crea el SecurityContext para este request           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                               │                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  🔹 JwtAuthenticationFilter (NUESTRO FILTRO)                │ │
│  │     ├─> Extrae header: "Bearer eyJhbGc..."                  │ │
│  │     ├─> Extrae token: "eyJhbGc..."                          │ │
│  │     ├─> Extrae email del token                              │ │
│  │     ├─> Carga usuario desde BD                              │ │
│  │     ├─> Valida token (firma + expiración)                   │ │
│  │     └─> Establece Authentication en SecurityContext         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                               │                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  🔹 AuthorizationFilter                                     │ │
│  │     └─> Verifica si el endpoint requiere autenticación      │ │
│  └─────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────┘
                               │
                               v
┌───────────────────────────────────────────────────────────────────┐
│                  3. SPRING SECURITY VERIFICA                      │
│                                                                    │
│  SecurityContextHolder.getContext().getAuthentication()           │
│  ├─> ¿Está autenticado? ✅ SÍ (hay un Authentication)            │
│  └─> ¿Tiene el rol requerido? (consulta @PreAuthorize)           │
└───────────────────────────────────────────────────────────────────┘
                               │
                               v
┌───────────────────────────────────────────────────────────────────┐
│                  4. @PreAuthorize("hasRole('ADMIN')")             │
│                                                                    │
│  Spring Security evalúa la expresión:                             │
│  ├─> Obtiene las authorities del Authentication                   │
│  ├─> Busca "ROLE_ADMIN"                                          │
│  └─> ✅ ENCONTRADO → Permite acceso                              │
│                                                                    │
│  Si NO lo encuentra:                                              │
│  └─> ❌ 403 FORBIDDEN → Acceso denegado                          │
└───────────────────────────────────────────────────────────────────┘
                               │
                               v
┌───────────────────────────────────────────────────────────────────┐
│                     5. EJECUTA EL CONTROLLER                      │
│                                                                    │
│  @GetMapping("/admin")                                            │
│  @PreAuthorize("hasRole('ADMIN')")                                │
│  public ResponseEntity<String> adminEndpoint() {                  │
│      return ResponseEntity.ok("Solo ADMIN");                      │
│  }                                                                 │
└───────────────────────────────────────────────────────────────────┘
                               │
                               v
┌───────────────────────────────────────────────────────────────────┐
│                     6. RESPUESTA AL CLIENTE                       │
│                                                                    │
│  HTTP 200 OK                                                      │
│  {                                                                 │
│    "message": "Solo ADMIN"                                        │
│  }                                                                 │
└───────────────────────────────────────────────────────────────────┘
```

### Escenarios de Error

#### ❌ Sin JWT
```
Request sin header Authorization
    │
    v
JwtAuthenticationFilter → No hay token → No autentica
    │
    v
Spring Security → No hay Authentication → ❌ 401 UNAUTHORIZED
```

#### ❌ JWT Inválido
```
Request con JWT malformado o expirado
    │
    v
JwtAuthenticationFilter → Token inválido → No autentica
    │
    v
Spring Security → No hay Authentication → ❌ 401 UNAUTHORIZED
```

#### ❌ Sin Permisos
```
Request con JWT válido pero sin el rol requerido
    │
    v
JwtAuthenticationFilter → Token válido → Autentica
    │
    v
@PreAuthorize("hasRole('ADMIN')") → Usuario es USER → ❌ 403 FORBIDDEN
```

---

## ✅ Buenas Prácticas

### 🔒 Seguridad

1. **Nunca expongas la clave secreta del JWT**
   ```properties
   # ❌ MAL: Hardcodeada
   jwt.secret.key=mi_clave_super_secreta
   
   # ✅ BIEN: Variable de entorno
   jwt.secret.key=${JWT_SECRET_KEY}
   ```

2. **Usa HTTPS en producción**
   - JWT en HTTP puede ser interceptado
   - Configura certificados SSL/TLS

3. **Expiración del token adecuada**
   - APIs públicas: 15-60 minutos
   - APIs internas: 1-4 horas
   - Implementa refresh tokens para sesiones largas

4. **Valida SIEMPRE los inputs**
   ```java
   @PostMapping("/login")
   public AuthResponse login(@Valid @RequestBody LoginRequest request) {
       // @Valid valida automáticamente
   }
   ```

5. **No guardes información sensible en el JWT**
   - ❌ Password, tarjetas de crédito, etc.
   - ✅ Email, roles, permisos

6. **Encripta passwords con BCrypt**
   - Nunca guardes passwords en texto plano
   - BCrypt es adaptativo (puedes aumentar la complejidad)

### 🏗️ Arquitectura

1. **Separa responsabilidades**
   - Controllers: Solo reciben/retornan datos
   - Services: Lógica de negocio
   - Repositories: Acceso a datos
   - Config: Configuraciones

2. **Usa DTOs para requests/responses**
   - No expongas entidades JPA directamente
   - Evita exponer campos sensibles

3. **Manejo global de excepciones**
   - Usa `@RestControllerAdvice`
   - Respuestas consistentes

4. **Logging apropiado**
   ```java
   log.info("Usuario {} autenticado correctamente", email);
   log.error("Error al autenticar usuario: {}", e.getMessage());
   ```

5. **Auditoría**
   - Registra intentos de login (exitosos y fallidos)
   - Registra cambios en permisos
   - Usa `@CreatedBy`, `@LastModifiedBy` de JPA

### 📊 Base de Datos

1. **Usa índices en campos de búsqueda frecuente**
   ```java
   @Column(unique = true, nullable = false)
   @Index(name = "idx_email")
   private String email;
   ```

2. **Lazy loading para relaciones**
   - Usa `EAGER` solo cuando sea necesario
   - Evita el problema N+1 con `JOIN FETCH`

3. **Migraciones de BD**
   - Usa Flyway o Liquibase en producción
   - No uses `ddl-auto=update` en producción

### 🧪 Testing

1. **Tests unitarios para servicios**
   ```java
   @Test
   void shouldAuthenticateUser() {
       // given
       LoginRequest request = new LoginRequest("user@test.com", "pass");
       // when
       AuthResponse response = authService.login(request);
       // then
       assertNotNull(response.getToken());
   }
   ```

2. **Tests de integración para controllers**
   ```java
   @SpringBootTest
   @AutoConfigureMockMvc
   class AuthControllerTest {
       @Test
       void shouldLogin() throws Exception {
           mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(json))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.token").exists());
       }
   }
   ```

---

## 💼 Preguntas de Entrevista

### Básicas

**1. ¿Qué es Spring Security?**
> Framework de seguridad para aplicaciones Spring. Maneja autenticación (quién eres) y autorización (qué puedes hacer). Protege endpoints, valida usuarios, y gestiona sesiones.

**2. ¿Diferencia entre autenticación y autorización?**
> - **Autenticación:** Verificar la identidad (login con email/password)
> - **Autorización:** Verificar permisos (¿puede este usuario eliminar clientes?)

**3. ¿Qué es un JWT y por qué usarlo?**
> JSON Web Token: Token firmado que contiene información del usuario. Es stateless (no requiere sesión en el servidor), escalable, y perfecto para APIs y microservicios.

**4. ¿Cómo funciona JWT?**
> 1. Usuario hace login → servidor valida credenciales
> 2. Servidor genera JWT firmado con clave secreta
> 3. Cliente guarda el JWT
> 4. Cliente envía JWT en cada request (header Authorization)
> 5. Servidor valida la firma y extrae los datos

**5. ¿Qué es un filtro en Spring Security?**
> Componente que intercepta requests HTTP antes del controller. Permite validar, modificar o rechazar requests. JwtAuthenticationFilter valida el JWT en cada request.

### Intermedias

**6. ¿Cómo funciona el proceso de login en tu implementación?**
> 1. Usuario envía email + password
> 2. AuthenticationManager usa UserDetailsService para cargar el usuario
> 3. PasswordEncoder valida el password
> 4. Si es correcto, JwtService genera un token
> 5. Token se retorna al cliente con roles y permisos

**7. ¿Diferencia entre hasRole y hasAuthority?**
> - **hasRole('ADMIN'):** Busca "ROLE_ADMIN" (Spring agrega el prefijo automáticamente)
> - **hasAuthority('READ_CLIENTS'):** Busca exactamente "READ_CLIENTS" (sin prefijo)
> Los roles son más generales, los authorities son más granulares.

**8. ¿Qué es UserDetailsService y para qué sirve?**
> Interfaz que define cómo cargar un usuario desde la base de datos. Spring Security la usa para autenticación. Retorna un UserDetails con username, password, y authorities.

**9. ¿Cómo se valida un JWT?**
> 1. Se extrae la firma del token
> 2. Se recalcula la firma usando la clave secreta
> 3. Si las firmas coinciden, el token no fue modificado
> 4. Se verifica la fecha de expiración
> 5. Si todo es válido, se acepta el token

**10. ¿Qué es el SecurityContext?**
> Contenedor que almacena la autenticación del usuario actual en un request. Es thread-local (cada request tiene el suyo). Se accede con SecurityContextHolder.

### Avanzadas

**11. ¿Cómo manejas la expiración de tokens?**
> - Tokens de corta duración (15-60 min)
> - Implementar refresh tokens (tokens de larga duración solo para renovar)
> - Cliente detecta 401 → solicita nuevo token con refresh token
> - Alternativamente, usar token deslizante (sliding session)

**12. ¿Cómo invalidar un JWT antes de que expire?**
> JWT es stateless, no se puede invalidar directamente. Soluciones:
> 1. **Lista negra (blacklist):** Guardar tokens invalidados en Redis
> 2. **Versión de token:** Agregar versión en JWT, cambiarla al invalidar
> 3. **Tokens de corta duración:** Minimiza el riesgo
> 4. **Event-driven:** Publicar evento de logout, servicios escuchan

**13. ¿Diferencia entre STATEFUL y STATELESS?**
> - **STATEFUL:** Servidor guarda sesión (JSESSIONID en cookie)
>   - Pros: Control total (invalidar sesión inmediatamente)
>   - Contras: No escala bien, sesiones compartidas entre servidores
> 
> - **STATELESS:** Servidor no guarda nada (JWT en cada request)
>   - Pros: Escalabilidad, microservicios, no hay sesiones compartidas
>   - Contras: No se puede invalidar inmediatamente

**14. ¿Cómo proteges contra ataques comunes?**
> - **CSRF:** Deshabilitado para JWT (no usa cookies)
> - **XSS:** Sanitizar inputs, usar HttpOnly cookies si guardas JWT ahí
> - **Brute force:** Rate limiting (Spring Cloud Gateway, Redis)
> - **SQL Injection:** Usar JPA con prepared statements
> - **Token theft:** HTTPS, tokens de corta duración, refresh tokens

**15. ¿Cómo escalar esta solución en microservicios?**
> 1. **API Gateway:** Valida JWT una vez, propaga identidad
> 2. **Clave compartida:** Todos los servicios usan la misma clave para validar JWT
> 3. **Servicio de autenticación centralizado:** OAuth2/Keycloak
> 4. **Información en JWT:** Roles y permisos en el token, cada servicio valida localmente
> 5. **Event-driven:** Cambios de permisos → evento → servicios actualizan cache

---

## 🧪 Comandos de Testing

### 1️⃣ Registro de Usuario

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan@test.com",
    "password": "123456"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "juan@test.com",
  "name": "Juan Pérez",
  "roles": ["USER"],
  "permissions": ["READ_CLIENTS", "READ_BOOKING", "CREATE_BOOKING"]
}
```

---

### 2️⃣ Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@barberia.com",
    "password": "admin123"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@barberia.com",
  "name": "Admin",
  "roles": ["ADMIN"],
  "permissions": [
    "READ_CLIENTS",
    "CREATE_CLIENTS",
    "UPDATE_CLIENTS",
    "DELETE_CLIENTS",
    "READ_BOOKING",
    "CREATE_BOOKING",
    "MANAGE_BOOKING",
    "DELETE_USERS",
    "SPECIAL_ACCESS"
  ]
}
```

---

### 3️⃣ Acceso con JWT

**Guarda el token en una variable:**
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Request autenticado:**
```bash
curl -X GET http://localhost:8080/api/demo/protected \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
{
  "message": "Acceso concedido a endpoint protegido",
  "user": "admin@barberia.com",
  "authorities": [
    {"authority": "ROLE_ADMIN"},
    {"authority": "READ_CLIENTS"},
    {"authority": "CREATE_CLIENTS"},
    ...
  ]
}
```

---

### 4️⃣ Endpoint Solo ADMIN

```bash
curl -X GET http://localhost:8080/api/demo/admin \
  -H "Authorization: Bearer $TOKEN"
```

**Si eres ADMIN:**
```
"Solo los ADMIN pueden ver este contenido"
```

**Si NO eres ADMIN:**
```json
{
  "timestamp": "2024-12-28T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

### 5️⃣ Endpoint con Permiso

```bash
curl -X GET http://localhost:8080/api/demo/read-clients \
  -H "Authorization: Bearer $TOKEN"
```

**Si tienes el permiso READ_CLIENTS:**
```
"Aquí estarían los clientes (requiere permiso READ_CLIENTS)"
```

---

### 6️⃣ Usuarios de Prueba

La aplicación crea automáticamente estos usuarios:

| Email | Password | Rol | Descripción |
|-------|----------|-----|-------------|
| `admin@barberia.com` | `admin123` | ADMIN | Todos los permisos |
| `manager@barberia.com` | `manager123` | MANAGER | Permisos de gestión |
| `user@barberia.com` | `user123` | USER | Permisos básicos |
| `super@barberia.com` | `super123` | ADMIN + MANAGER | Múltiples roles |

---

## 🎯 Resumen Final

### Lo que has aprendido:

✅ **Autenticación JWT stateless**  
✅ **Roles y permisos granulares**  
✅ **@PreAuthorize para proteger endpoints**  
✅ **Flujo completo de login → token → autorización**  
✅ **Arquitectura limpia y separación de responsabilidades**  
✅ **Buenas prácticas de la industria**  

### Próximos pasos:

1. **Implementa refresh tokens**
2. **Agrega rate limiting (prevenir brute force)**
3. **Implementa OAuth2 (login con Google/GitHub)**
4. **Agrega auditoría (quién hizo qué y cuándo)**
5. **Tests unitarios e integración**
6. **Documenta tu API con Swagger/OpenAPI**

---

**¡Felicidades! Ahora tienes una implementación profesional de Spring Security con JWT que puedes usar en tu portafolio y entrevistas.** 🚀
