# 🚀 Guía de Revisión Paso a Paso - Spring Security JWT

## 📋 Índice de Revisión

Esta guía te llevará archivo por archivo, explicando su propósito y cómo se conecta con los demás.

---

## PASO 1: Configurar la Base de Datos

### 📄 `application.properties`

**Ubicación:** `src/main/resources/application.properties`

**¿Qué hace?**
Configura la conexión a MySQL y parámetros de JWT.

**Puntos importantes:**
```properties
# Conexión a MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/barberia_db

# Clave secreta JWT (cambiar en producción)
jwt.secret.key=404E635266556A...

# Expiración: 24 horas
jwt.expiration=86400000
```

**Asegúrate de:**
1. ✅ Tener MySQL corriendo en puerto 3306
2. ✅ Cambiar la contraseña de MySQL si no es vacía
3. ✅ La base de datos se creará automáticamente

---

## PASO 2: Entidades (Modelos de Base de Datos)

### 📄 `Usuario.java`

**Ubicación:** `src/main/java/com/barberia/models/Usuario.java`

**¿Qué hace?**
Representa la tabla `usuarios` en la base de datos.

**Campos principales:**
- `email` (único) - Para login
- `password` (encriptado) - BCrypt
- `roles` - Relación ManyToMany con Rol

**Conexiones:**
```
Usuario ←→ Rol (ManyToMany)
   ↓
UsuarioRepository
   ↓
CustomUserDetailsService (carga usuarios para Spring Security)
```

**Revisa:**
1. ✅ Relación `@ManyToMany` con Rol
2. ✅ `FetchType.EAGER` en roles (necesario para Spring Security)
3. ✅ `@PreUpdate` para actualizar `updatedAt`

---

### 📄 `Rol.java`

**Ubicación:** `src/main/java/com/barberia/models/Rol.java`

**¿Qué hace?**
Representa roles del sistema (ADMIN, USER, MANAGER).

**Campos principales:**
- `name` (único) - Nombre del rol
- `permissions` - Relación ManyToMany con Permiso

**Conexiones:**
```
Usuario ←→ Rol ←→ Permiso
```

**Revisa:**
1. ✅ Relación con Usuario (bidireccional)
2. ✅ Relación con Permiso
3. ✅ `FetchType.EAGER` en permisos

---

### 📄 `Permiso.java`

**Ubicación:** `src/main/java/com/barberia/models/Permiso.java`

**¿Qué hace?**
Representa permisos específicos (READ_CLIENTS, CREATE_BOOKING, etc.).

**Campos principales:**
- `name` (único) - Nombre del permiso
- `description` - Descripción legible

**Revisa:**
1. ✅ `mappedBy = "permissions"` apunta al campo correcto en Rol

---

## PASO 3: Repositorios (Acceso a Datos)

### 📄 `UsuarioRepository.java`

**Ubicación:** `src/main/java/com/barberia/repositories/UsuarioRepository.java`

**¿Qué hace?**
Proporciona métodos para consultar usuarios en la base de datos.

**Métodos principales:**
- `findByEmail()` - Busca usuario por email
- `existsByEmail()` - Verifica si existe un email
- `findByEmailWithRolesAndPermissions()` - Query optimizada con JOIN FETCH

**Usado por:**
- `CustomUserDetailsService` - Para cargar usuarios
- `AuthenticationService` - Para login y registro

**Revisa:**
1. ✅ Query personalizada con `@Query`
2. ✅ `JOIN FETCH` para evitar N+1 queries

---

### 📄 `RolRepository.java` y `PermisoRepository.java`

**¿Qué hacen?**
Consultan roles y permisos.

**Usados por:**
- `DataInitializer` - Para crear datos de prueba
- `AuthenticationService` - Para asignar roles

---

## PASO 4: DTOs (Data Transfer Objects)

### 📄 `LoginRequest.java`

**Ubicación:** `src/main/java/com/barberia/dto/LoginRequest.java`

**¿Qué hace?**
Recibe el email y password del cliente en el login.

**Validaciones:**
- `@NotBlank` - No puede ser vacío
- `@Email` - Debe ser email válido

**Usado por:**
- `AuthController.login()` - Recibe el request

---

### 📄 `RegisterRequest.java`

**¿Qué hace?**
Recibe datos para registrar un nuevo usuario.

**Validaciones:**
- `@NotBlank` en todos los campos
- `@Size(min=6)` en password

---

### 📄 `AuthResponse.java`

**¿Qué hace?**
Retorna el JWT al cliente después de login/registro exitoso.

**Contiene:**
- `token` - JWT firmado
- `email`, `name` - Datos del usuario
- `roles`, `permissions` - Autoridades

---

## PASO 5: Seguridad (Core de Spring Security)

### 📄 `JwtService.java` ⭐ IMPORTANTE

**Ubicación:** `src/main/java/com/barberia/security/JwtService.java`

**¿Qué hace?**
Genera y valida tokens JWT.

**Métodos principales:**

1. **`generateToken(UserDetails)`**
   - Crea un JWT con roles y permisos
   - Firma con la clave secreta
   - Establece expiración

2. **`extractUsername(token)`**
   - Extrae el email del token

3. **`isTokenValid(token, userDetails)`**
   - Valida firma y expiración

**Conexiones:**
```
AuthenticationService → generateToken()
   ↓
JwtAuthenticationFilter → extractUsername(), isTokenValid()
```

**Revisa:**
1. ✅ `secretKey` - Debe ser segura en producción
2. ✅ `jwtExpiration` - 24 horas por defecto
3. ✅ Algoritmo HMAC-SHA256

---

### 📄 `JwtAuthenticationFilter.java` ⭐ IMPORTANTE

**Ubicación:** `src/main/java/com/barberia/security/JwtAuthenticationFilter.java`

**¿Qué hace?**
Intercepta CADA request HTTP y valida el JWT.

**Flujo:**
```
1. Extrae header "Authorization: Bearer <token>"
2. Extrae el token
3. Extrae el email del token
4. Carga el usuario desde BD
5. Valida el token
6. Si es válido, establece Authentication en SecurityContext
7. El request continúa al controller
```

**Conexiones:**
```
Cada Request HTTP
   ↓
JwtAuthenticationFilter (este archivo)
   ├─> JwtService (validar token)
   ├─> UserDetailsService (cargar usuario)
   └─> SecurityContextHolder (establecer autenticación)
```

**Revisa:**
1. ✅ `doFilterInternal()` - Método principal
2. ✅ `filterChain.doFilter()` - Continúa la cadena
3. ✅ Manejo de errores con try-catch

---

### 📄 `CustomUserDetailsService.java` ⭐ IMPORTANTE

**Ubicación:** `src/main/java/com/barberia/security/CustomUserDetailsService.java`

**¿Qué hace?**
Carga usuarios desde la base de datos para Spring Security.

**Método principal:**
```java
loadUserByUsername(String email) {
    // 1. Busca usuario en BD
    // 2. Extrae roles y permisos
    // 3. Convierte a GrantedAuthority
    // 4. Retorna UserDetails
}
```

**Conexiones:**
```
JwtAuthenticationFilter → loadUserByUsername()
   ↓
UsuarioRepository.findByEmailWithRolesAndPermissions()
   ↓
Retorna UserDetails con authorities
```

**Revisa:**
1. ✅ Prefijo "ROLE_" en roles
2. ✅ Permisos sin prefijo
3. ✅ Flags de UserDetails (enabled, locked, etc.)

---

## PASO 6: Configuración

### 📄 `SecurityConfig.java` ⭐ MUY IMPORTANTE

**Ubicación:** `src/main/java/com/barberia/config/SecurityConfig.java`

**¿Qué hace?**
Configuración PRINCIPAL de Spring Security.

**Componentes:**

1. **`securityFilterChain()`**
   - Define endpoints públicos: `/api/auth/**`
   - Define endpoints protegidos: Todo lo demás
   - Configura STATELESS (sin sesiones)
   - Registra el filtro JWT

2. **`authenticationProvider()`**
   - Define cómo autenticar (UserDetailsService + PasswordEncoder)

3. **`passwordEncoder()`**
   - BCrypt para encriptar passwords

**Conexiones:**
```
Spring Security inicia
   ↓
SecurityConfig
   ├─> Registra JwtAuthenticationFilter
   ├─> Configura AuthenticationProvider
   │   ├─> UserDetailsService
   │   └─> PasswordEncoder
   └─> Define reglas de autorización
```

**Revisa:**
1. ✅ `@EnableMethodSecurity` - Para usar @PreAuthorize
2. ✅ `.csrf(AbstractHttpConfigurer::disable)` - Deshabilitado para JWT
3. ✅ `.sessionManagement(STATELESS)` - Sin sesiones
4. ✅ `.addFilterBefore()` - JWT antes de UsernamePasswordAuthenticationFilter

**Endpoints públicos:**
- `/api/auth/login`
- `/api/auth/register`
- `/api/public/**`

**Endpoints protegidos:**
- Todo lo demás (`.anyRequest().authenticated()`)

---

### 📄 `GlobalExceptionHandler.java`

**Ubicación:** `src/main/java/com/barberia/config/GlobalExceptionHandler.java`

**¿Qué hace?**
Maneja errores globalmente con respuestas consistentes.

**Excepciones manejadas:**
- `BadCredentialsException` - Credenciales incorrectas → 401
- `MethodArgumentNotValidException` - Validación fallida → 400
- `UsernameNotFoundException` - Usuario no encontrado → 404
- `RuntimeException` - Errores generales → 400
- `Exception` - Error inesperado → 500

---

## PASO 7: Servicios (Lógica de Negocio)

### 📄 `AuthenticationService.java` ⭐ IMPORTANTE

**Ubicación:** `src/main/java/com/barberia/services/AuthenticationService.java`

**¿Qué hace?**
Lógica de login y registro.

**Métodos:**

1. **`register(RegisterRequest)`**
   ```
   1. Valida que email no exista
   2. Encripta password
   3. Asigna rol USER
   4. Guarda en BD
   5. Genera JWT
   6. Retorna AuthResponse
   ```

2. **`login(LoginRequest)`**
   ```
   1. AuthenticationManager valida credenciales
   2. Si válidas, genera JWT
   3. Extrae roles y permisos
   4. Retorna AuthResponse
   ```

**Conexiones:**
```
AuthController
   ↓
AuthenticationService
   ├─> AuthenticationManager (validar credenciales)
   ├─> JwtService (generar token)
   ├─> UsuarioRepository (buscar/guardar usuarios)
   └─> PasswordEncoder (encriptar passwords)
```

**Revisa:**
1. ✅ `@Transactional` en métodos de BD
2. ✅ Manejo de errores (email duplicado, rol no encontrado)
3. ✅ Generación de token después de login exitoso

---

## PASO 8: Controllers (Endpoints REST)

### 📄 `AuthController.java`

**Ubicación:** `src/main/java/com/barberia/controllers/AuthController.java`

**¿Qué hace?**
Expone endpoints públicos de autenticación.

**Endpoints:**

1. **POST `/api/auth/register`**
   - Crea nuevo usuario
   - Retorna JWT

2. **POST `/api/auth/login`**
   - Autentica usuario
   - Retorna JWT

3. **GET `/api/auth/public`**
   - Endpoint público de prueba

**Revisa:**
1. ✅ `@RestController` - Retorna JSON
2. ✅ `@RequestMapping("/api/auth")` - Ruta base
3. ✅ `@Valid` - Valida requests
4. ✅ `@CrossOrigin` - Permite CORS (cambiar en producción)

---

### 📄 `DemoController.java`

**Ubicación:** `src/main/java/com/barberia/controllers/DemoController.java`

**¿Qué hace?**
Ejemplos de endpoints protegidos con diferentes niveles de acceso.

**Endpoints:**

| Endpoint | Requiere | @PreAuthorize |
|----------|----------|---------------|
| `/api/demo/protected` | Autenticación | Ninguno |
| `/api/demo/admin` | Rol ADMIN | `hasRole('ADMIN')` |
| `/api/demo/user` | Rol USER | `hasRole('USER')` |
| `/api/demo/read-clients` | Permiso READ_CLIENTS | `hasAuthority('READ_CLIENTS')` |
| `/api/demo/create-booking` | Permiso CREATE_BOOKING | `hasAnyAuthority(...)` |
| `/api/demo/delete-user/{id}` | ADMIN + DELETE_USERS | `hasRole() and hasAuthority()` |

**Revisa:**
1. ✅ Diferentes usos de `@PreAuthorize`
2. ✅ Combinaciones con `and`, `or`
3. ✅ `SecurityContextHolder` para acceder al usuario actual

---

## PASO 9: Utilidades

### 📄 `DataInitializer.java`

**Ubicación:** `src/main/java/com/barberia/utils/DataInitializer.java`

**¿Qué hace?**
Crea datos de prueba al iniciar la aplicación.

**Crea:**
- 9 permisos
- 3 roles (ADMIN, MANAGER, USER) con sus permisos
- 4 usuarios de prueba

**Usuarios creados:**
| Email | Password | Rol | Permisos |
|-------|----------|-----|----------|
| admin@barberia.com | admin123 | ADMIN | Todos |
| manager@barberia.com | manager123 | MANAGER | Gestión |
| user@barberia.com | user123 | USER | Básicos |
| super@barberia.com | super123 | ADMIN + MANAGER | Todos |

**Revisa:**
1. ✅ Solo se ejecuta si no hay usuarios (`count() > 0`)
2. ✅ Usa `PasswordEncoder` para encriptar passwords
3. ✅ Logs informativos con los usuarios creados

---

## PASO 10: Conexión entre Archivos

### Flujo Completo de Login

```
1. Cliente HTTP Request
   POST /api/auth/login
   Body: { email, password }
        ↓
2. AuthController.login()
   - Recibe LoginRequest
   - Valida con @Valid
        ↓
3. AuthenticationService.login()
   - Llama AuthenticationManager
        ↓
4. AuthenticationManager
   - Usa AuthenticationProvider
        ↓
5. DaoAuthenticationProvider
   - Llama UserDetailsService
        ↓
6. CustomUserDetailsService.loadUserByUsername()
   - Llama UsuarioRepository
        ↓
7. UsuarioRepository.findByEmailWithRolesAndPermissions()
   - Query a MySQL con JOIN FETCH
   - Retorna Usuario con roles y permisos
        ↓
8. CustomUserDetailsService
   - Convierte Usuario a UserDetails
   - Extrae authorities (roles + permisos)
   - Retorna UserDetails
        ↓
9. DaoAuthenticationProvider
   - Compara passwords con PasswordEncoder
   - Si coincide, retorna Authentication
        ↓
10. AuthenticationService
    - Llama JwtService.generateToken()
        ↓
11. JwtService.generateToken()
    - Crea JWT con claims (email, roles, permisos)
    - Firma con secretKey
    - Retorna token
        ↓
12. AuthenticationService
    - Construye AuthResponse
    - Retorna al controller
        ↓
13. AuthController
    - Retorna AuthResponse al cliente
        ↓
14. Cliente HTTP Response
    { token, email, roles, permissions }
```

---

### Flujo Completo de Request Autenticado

```
1. Cliente HTTP Request
   GET /api/demo/admin
   Header: Authorization: Bearer <token>
        ↓
2. Spring Security Filter Chain
        ↓
3. JwtAuthenticationFilter.doFilterInternal()
   - Extrae header "Authorization"
   - Extrae token (quita "Bearer ")
   - Llama JwtService.extractUsername(token)
        ↓
4. JwtService.extractUsername()
   - Parsea el JWT
   - Extrae claim "sub" (email)
   - Retorna email
        ↓
5. JwtAuthenticationFilter
   - Llama UserDetailsService.loadUserByUsername(email)
        ↓
6. CustomUserDetailsService.loadUserByUsername()
   - Carga usuario desde BD
   - Retorna UserDetails con authorities
        ↓
7. JwtAuthenticationFilter
   - Llama JwtService.isTokenValid(token, userDetails)
        ↓
8. JwtService.isTokenValid()
   - Verifica firma
   - Verifica expiración
   - Retorna true/false
        ↓
9. JwtAuthenticationFilter
   - Si válido, crea Authentication
   - Establece en SecurityContext
        ↓
10. Spring Security continúa
    - AuthorizationFilter verifica @PreAuthorize
        ↓
11. @PreAuthorize("hasRole('ADMIN')")
    - Consulta SecurityContext
    - Busca "ROLE_ADMIN" en authorities
    - Si lo tiene, permite acceso
        ↓
12. DemoController.adminEndpoint()
    - Ejecuta el método
    - Retorna respuesta
        ↓
13. Cliente HTTP Response
    "Solo los ADMIN pueden ver este contenido"
```

---

## PASO 11: Testing Paso a Paso

### 1. Inicia la Aplicación

```bash
mvn spring-boot:run
```

**Verifica los logs:**
```
✅ Datos de prueba inicializados correctamente
📝 Usuarios de prueba:
   - admin@barberia.com / admin123 (ADMIN)
   - manager@barberia.com / manager123 (MANAGER)
   - user@barberia.com / user123 (USER)
   - super@barberia.com / super123 (ADMIN + MANAGER)
```

---

### 2. Prueba el Login

**PowerShell:**
```powershell
$body = @{
    email = "admin@barberia.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

$response.Content | ConvertFrom-Json
```

**Guarda el token:**
```powershell
$auth = $response.Content | ConvertFrom-Json
$token = $auth.token
```

---

### 3. Prueba Endpoint Protegido

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/protected" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }
```

---

### 4. Prueba @PreAuthorize

**Endpoint ADMIN (debería funcionar):**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }
```

**Ahora con usuario USER (debería fallar):**
```powershell
# Login como USER
$body = @{
    email = "user@barberia.com"
    password = "user123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

$userToken = ($response.Content | ConvertFrom-Json).token

# Intenta acceder a endpoint ADMIN
Invoke-WebRequest -Uri "http://localhost:8080/api/demo/admin" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $userToken" }

# ❌ Debería retornar 403 Forbidden
```

---

## PASO 12: Checklist de Revisión

### ✅ Entidades

- [ ] Usuario tiene relación con Rol
- [ ] Rol tiene relación con Permiso
- [ ] FetchType.EAGER donde se necesita
- [ ] @Builder.Default en Sets

### ✅ Repositorios

- [ ] UsuarioRepository tiene findByEmailWithRolesAndPermissions()
- [ ] Query usa JOIN FETCH
- [ ] @Repository en todos

### ✅ Security

- [ ] JwtService genera y valida tokens correctamente
- [ ] JwtAuthenticationFilter intercepta requests
- [ ] CustomUserDetailsService carga usuarios con authorities
- [ ] SecurityConfig tiene endpoints públicos y protegidos
- [ ] @EnableMethodSecurity habilitado

### ✅ DTOs

- [ ] LoginRequest tiene validaciones
- [ ] RegisterRequest tiene validaciones
- [ ] AuthResponse tiene todos los campos necesarios

### ✅ Servicios

- [ ] AuthenticationService usa AuthenticationManager
- [ ] Genera token después de login exitoso
- [ ] Encripta password en registro

### ✅ Controllers

- [ ] AuthController tiene login y register
- [ ] DemoController tiene ejemplos de @PreAuthorize
- [ ] @Valid en requests

### ✅ Configuración

- [ ] application.properties tiene configuración de JWT
- [ ] MySQL configurado correctamente
- [ ] GlobalExceptionHandler maneja errores

### ✅ Testing

- [ ] Puedes hacer login
- [ ] Recibes un token válido
- [ ] Puedes acceder a endpoints protegidos con el token
- [ ] @PreAuthorize funciona correctamente
- [ ] Errores se manejan apropiadamente

---

## 🎯 Orden de Estudio Recomendado

1. **Día 1:** Entidades y Repositorios
   - Entiende las relaciones Usuario-Rol-Permiso
   - Revisa las queries

2. **Día 2:** DTOs y JwtService
   - Entiende qué es un JWT
   - Revisa generateToken() e isTokenValid()

3. **Día 3:** JwtAuthenticationFilter y CustomUserDetailsService
   - Entiende cómo se interceptan los requests
   - Cómo se cargan los usuarios

4. **Día 4:** SecurityConfig
   - Configuración completa de Spring Security
   - Entiende cada bean

5. **Día 5:** AuthenticationService y Controllers
   - Flujo completo de login
   - Prueba con Postman

6. **Día 6:** @PreAuthorize y autorización
   - Diferencia entre roles y permisos
   - Prueba diferentes combinaciones

7. **Día 7:** Revisión completa y SECURITY_GUIDE.md
   - Lee la guía completa
   - Prepara respuestas a preguntas de entrevista

---

## 📚 Recursos Adicionales

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [JWT.io](https://jwt.io/) - Decodifica y verifica JWTs
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - Guía teórica completa

---

**¡Éxito en tu aprendizaje y en tus entrevistas! 🚀**
