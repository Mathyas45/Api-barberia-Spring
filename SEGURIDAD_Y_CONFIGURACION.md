# 🔐 GUÍA DE SEGURIDAD Y CONFIGURACIÓN

## 📋 ÍNDICE

1. [Variables de Entorno](#1-variables-de-entorno)
2. [Entidades ManyToMany Explícitas](#2-entidades-manytomany-explícitas)
3. [SecurityConfig Unificado](#3-securityconfig-unificado)
4. [Scripts SQL vs DataInitializer](#4-scripts-sql-vs-datainitializer)
5. [Despliegue en Producción](#5-despliegue-en-producción)

---

## 1️⃣ VARIABLES DE ENTORNO

### 🎯 ¿Por qué usar variables de entorno?

**PROBLEMA:**
```java
// ❌ MAL - Datos sensibles en el código
jwt.secret.key=mi_clave_super_secreta_123
datasource.password=root
```

**RIESGOS:**
- ✗ Se suben a GitHub → Cualquiera puede verlas
- ✗ Difícil cambiar en producción
- ✗ Misma clave en desarrollo y producción
- ✗ Vulnerabilidad de seguridad crítica

**SOLUCIÓN:**
```yaml
# ✅ BIEN - Variables de entorno
jwt:
  secret:
    key: ${JWT_SECRET_KEY:valor_por_defecto}
```

### 📁 Archivos creados

#### 1. `.env.example`
```bash
# Plantilla para el .env real
# NO contiene datos reales
# SE SUBE a Git como documentación
```

#### 2. `.env`
```bash
# Contiene los datos REALES
# NO se sube a Git
# Cada desarrollador tiene el suyo
```

#### 3. `.gitignore`
```
.env              # ← Ignorar archivo con secrets
application-local.yml
*.key
```

### 🔧 Cómo usar variables de entorno

#### **Opción 1: Archivo .env (Desarrollo)**

1. **Copia la plantilla:**
```powershell
cp .env.example .env
```

2. **Edita .env con tus datos:**
```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=barberia_db
DB_USERNAME=root
DB_PASSWORD=tu_password_real

JWT_SECRET_KEY=clave_super_secreta_generada
JWT_EXPIRATION=86400000

SPRING_PROFILES_ACTIVE=dev
```

3. **Spring Boot las cargará automáticamente** (con plugin de IDE)

#### **Opción 2: Variables del Sistema (Producción)**

**Windows:**
```powershell
# Temporal (solo sesión actual)
$env:JWT_SECRET_KEY="clave_real_produccion"
$env:DB_PASSWORD="password_real"

# Permanente
[System.Environment]::SetEnvironmentVariable("JWT_SECRET_KEY", "valor", "User")
```

**Linux/Mac:**
```bash
# Temporal
export JWT_SECRET_KEY="clave_real_produccion"
export DB_PASSWORD="password_real"

# Permanente (agregar a ~/.bashrc o ~/.zshrc)
echo 'export JWT_SECRET_KEY="valor"' >> ~/.bashrc
```

#### **Opción 3: Parámetros al ejecutar**

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--JWT_SECRET_KEY=valor --DB_PASSWORD=valor"
```

#### **Opción 4: Docker**

```dockerfile
# docker-compose.yml
services:
  api:
    environment:
      - JWT_SECRET_KEY=${JWT_SECRET_KEY}
      - DB_PASSWORD=${DB_PASSWORD}
    env_file:
      - .env
```

### 🔐 Generar claves secretas seguras

#### **Opción 1: Node.js**
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

#### **Opción 2: PowerShell**
```powershell
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

#### **Opción 3: Java**
```java
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
String base64Key = Encoders.BASE64.encode(key.getEncoded());
System.out.println(base64Key);
```

#### **Opción 4: Online (⚠️ solo para desarrollo)**
- https://generate-secret.vercel.app/64
- https://randomkeygen.com/

---

## 2️⃣ ENTIDADES MANYTOMANY EXPLÍCITAS

### 🎯 ¿Por qué crear entidades intermedias?

#### **ANTES (Implícito):**
```java
@Entity
public class Usuario {
    @ManyToMany
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles;
}
```

**PROBLEMA:**
- ❌ La tabla `user_roles` es invisible
- ❌ No puedes agregar campos adicionales
- ❌ Difícil de entender para principiantes
- ❌ No puedes hacer queries personalizadas

#### **AHORA (Explícito):**
```java
@Entity
@Table(name = "user_roles")
public class UserRole {
    @Id
    private Long id;
    
    @ManyToOne
    private Usuario usuario;
    
    @ManyToOne
    private Rol rol;
    
    // ✅ PUEDES AGREGAR MÁS CAMPOS
    private LocalDateTime fechaAsignacion;
    private String asignadoPor;
}
```

### 📊 Comparación Visual

```
┌─────────────────────────────────────────────────────────────┐
│                    IMPLÍCITO (Antes)                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Usuario ←──────────────────────────────→ Rol              │
│                  (tabla invisible)                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    EXPLÍCITO (Ahora)                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Usuario ←──── UserRole ────→ Rol                          │
│                   ↑                                         │
│                   │                                         │
│            Entidad visible                                  │
│         - id                                                │
│         - fechaAsignacion                                   │
│         - asignadoPor                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 🚀 Beneficios

1. **Claridad:** Ves la tabla directamente en el código
2. **Extensibilidad:** Puedes agregar campos en el futuro
3. **Consultas:** Puedes hacer queries sobre la tabla intermedia
4. **Auditoría:** Saber cuándo y quién asignó el rol
5. **Aprendizaje:** Entiendes mejor las relaciones

### 📝 Ejemplo de uso futuro

```java
@Entity
public class UserRole {
    @Id
    private Long id;
    
    @ManyToOne
    private Usuario usuario;
    
    @ManyToOne
    private Rol rol;
    
    // ✨ CAMPOS ADICIONALES
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaExpiracion;  // Rol temporal
    private String asignadoPor;             // Auditoría
    private Boolean activo = true;          // Activar/desactivar
}

// Query personalizada
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    // Buscar roles activos de un usuario
    List<UserRole> findByUsuarioAndActivoTrue(Usuario usuario);
    
    // Buscar roles que expiran pronto
    @Query("SELECT ur FROM UserRole ur WHERE ur.fechaExpiracion < :fecha")
    List<UserRole> findExpiringRoles(LocalDateTime fecha);
}
```

---

## 3️⃣ SECURITYCONFIG UNIFICADO

### 🎯 Problema: Dos archivos de configuración

**ANTES:**
- `SecurityConfig.java` → Producción (con JWT)
- `DevSecurityConfig.java` → Desarrollo (sin JWT)

**PROBLEMA:**
- ❌ Duplicación de código
- ❌ Difícil mantener sincronizados
- ❌ Confusión sobre cuál se está usando

### ✅ Solución: Un solo archivo con perfiles

**AHORA:**
- `SecurityConfig.java` → Configuración unificada con `@Profile`

```java
@Configuration
public class SecurityConfig {
    
    // ✅ PRODUCCIÓN (cuando NO es dev)
    @Bean
    @Profile("!dev")  // Se activa si NO es "dev"
    public SecurityFilterChain securityFilterChainProduction(HttpSecurity http) {
        // Configuración con JWT
    }
    
    // ✅ DESARROLLO (cuando es dev)
    @Bean
    @Profile("dev")  // Se activa solo si es "dev"
    public SecurityFilterChain securityFilterChainDevelopment(HttpSecurity http) {
        // Configuración sin JWT
    }
}
```

### 🔄 Cómo cambiar entre modos

#### **Opción 1: application.yml**
```yaml
spring:
  profiles:
    active: dev  # o prod
```

#### **Opción 2: Variable de entorno**
```powershell
# Windows
$env:SPRING_PROFILES_ACTIVE="dev"

# Linux/Mac
export SPRING_PROFILES_ACTIVE=dev
```

#### **Opción 3: Al ejecutar**
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### **Opción 4: En IDE (IntelliJ/VS Code)**
```
Run Configuration → Environment Variables:
SPRING_PROFILES_ACTIVE=dev
```

### 📊 Comparación de perfiles

| Aspecto | Perfil DEV | Perfil PROD |
|---------|-----------|-------------|
| **JWT** | ❌ No requerido | ✅ Requerido |
| **Endpoints** | ✅ Todos públicos | 🔒 Protegidos |
| **Uso** | Testing con Postman | Frontend React |
| **Filtro JWT** | ❌ No activo | ✅ Activo |
| **@PreAuthorize** | ❌ Ignorado | ✅ Validado |

### 🧪 Testing con ambos perfiles

**Desarrollo (sin JWT):**
```powershell
# Activar modo dev
$env:SPRING_PROFILES_ACTIVE="dev"

# Ejecutar
mvn spring-boot:run

# Probar sin token
curl http://localhost:8080/api/demo/admin
# ✅ Funciona sin Authorization header
```

**Producción (con JWT):**
```powershell
# Activar modo prod
$env:SPRING_PROFILES_ACTIVE="prod"

# Ejecutar
mvn spring-boot:run

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@barberia.com","password":"admin123"}'

# Usar token
curl http://localhost:8080/api/demo/admin \
  -H "Authorization: Bearer eyJhbG..."
```

---

## 4️⃣ SCRIPTS SQL VS DATAINITIALIZER

### 📊 Comparación

| Aspecto | Scripts SQL | DataInitializer.java |
|---------|-------------|---------------------|
| **Lenguaje** | SQL puro | Java |
| **Control** | Mayor control | Más flexible |
| **Reutilización** | Portable | Dependiente de Spring |
| **Lógica compleja** | ❌ Difícil | ✅ Fácil (código Java) |
| **Recomendado para** | Producción inicial | Desarrollo/Testing |

### 📁 Scripts SQL creados

#### **1. schema.sql** - Estructura de la BD
```sql
-- Crea las tablas
CREATE TABLE usuarios (...);
CREATE TABLE roles (...);
CREATE TABLE user_roles (...);
```

#### **2. data.sql** - Datos iniciales
```sql
-- Inserta datos de prueba
INSERT INTO usuarios VALUES (...);
INSERT INTO roles VALUES (...);
```

### 🔧 Cómo usar los scripts SQL

#### **Opción 1: Ejecución automática por Spring Boot**

```yaml
# application.yml
spring:
  sql:
    init:
      mode: always  # o never, o embedded
      schema-locations: classpath:sql/schema.sql
      data-locations: classpath:sql/data.sql
```

**Modos disponibles:**
- `always` → Ejecuta SIEMPRE al iniciar
- `never` → NUNCA ejecuta
- `embedded` → Solo para H2/HSQLDB (BD en memoria)

**⚠️ CUIDADO:** Con `always` y `data.sql`, los datos se insertan cada vez. Usa `INSERT IGNORE` en MySQL.

#### **Opción 2: Ejecución manual (MySQL Workbench)**

1. Abre MySQL Workbench
2. Conecta a tu BD
3. File → Open SQL Script → `schema.sql`
4. Ejecuta (⚡ botón)
5. Repite con `data.sql`

#### **Opción 3: PowerShell/CMD**

```powershell
# Ejecutar schema.sql
mysql -u root -p barberia_db < src/main/resources/sql/schema.sql

# Ejecutar data.sql
mysql -u root -p barberia_db < src/main/resources/sql/data.sql
```

### 🆚 ¿Cuál usar?

#### **Usa Scripts SQL cuando:**
- ✅ Necesitas portabilidad (usar en otros proyectos)
- ✅ Tienes DBAs que prefieren SQL
- ✅ Quieres control total sobre el esquema
- ✅ Despliegue inicial en producción
- ✅ Migraciones complejas

#### **Usa DataInitializer cuando:**
- ✅ Desarrollo y testing local
- ✅ Necesitas lógica condicional compleja
- ✅ Quieres generar datos dinámicos
- ✅ Integración con otros servicios Java
- ✅ Testing automatizado

### 🎯 Recomendación para tu proyecto

```
DESARROLLO:
├── DataInitializer.java  ✅ Activo
└── Scripts SQL          ❌ Desactivados

PRODUCCIÓN:
├── DataInitializer.java  ❌ Desactivado
└── Scripts SQL          ✅ Ejecutar manualmente
```

### 🔀 Alternar entre métodos

**Deshabilitar DataInitializer:**
```java
@Component
@Profile("!prod")  // No ejecutar en producción
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

**Habilitar Scripts SQL:**
```yaml
spring:
  sql:
    init:
      mode: always
```

---

## 5️⃣ DESPLIEGUE EN PRODUCCIÓN

### 📋 Checklist antes de producción

```
✅ Variables de entorno configuradas (no hardcoded)
✅ JWT_SECRET_KEY generada con clave fuerte
✅ spring.profiles.active=prod
✅ ddl-auto=validate (NO update)
✅ show-sql=false
✅ HTTPS habilitado
✅ .env NO está en Git
✅ Passwords de usuarios actualizadas
✅ DataInitializer deshabilitado
✅ Logs configurados correctamente
✅ Backups automáticos configurados
```

### 🚀 Configuración recomendada para producción

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate  # ⚠️ NUNCA update en producción
    show-sql: false
  
jwt:
  secret:
    key: ${JWT_SECRET_KEY}  # ⚠️ DEBE ser variable de entorno
  expiration: 3600000  # 1 hora (más seguro que 24h)

logging:
  level:
    com.barberia: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
```

### 🌐 Plataformas de despliegue

#### **Heroku**
```bash
# Configurar variables de entorno
heroku config:set JWT_SECRET_KEY=tu_clave
heroku config:set DB_HOST=host
heroku config:set SPRING_PROFILES_ACTIVE=prod
```

#### **AWS Elastic Beanstalk**
```
Configuration → Software → Environment properties:
JWT_SECRET_KEY=valor
DB_PASSWORD=valor
```

#### **Docker**
```dockerfile
ENV JWT_SECRET_KEY=${JWT_SECRET_KEY}
ENV SPRING_PROFILES_ACTIVE=prod
```

---

## 📚 Resumen Rápido

### 🔐 Seguridad
- ✅ Claves en variables de entorno
- ✅ `.env` en `.gitignore`
- ✅ Usar `.env.example` como plantilla

### 🗂️ Entidades ManyToMany
- ✅ Creadas `UserRole` y `RolePermission`
- ✅ Más claras y extensibles
- ✅ Preparadas para campos adicionales

### ⚙️ SecurityConfig
- ✅ Unificado en un solo archivo
- ✅ `@Profile("dev")` para testing sin JWT
- ✅ `@Profile("!dev")` para producción con JWT
- ✅ Cambiar con `spring.profiles.active`

### 💾 Datos Iniciales
- ✅ Scripts SQL creados (`schema.sql`, `data.sql`)
- ✅ DataInitializer.java sigue disponible
- ✅ Elegir según necesidad

---

## 🎓 Siguiente Paso

1. **Crea tu `.env`:**
```powershell
cp .env.example .env
# Edita .env con tus datos
```

2. **Prueba modo dev (sin JWT):**
```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```

3. **Prueba modo prod (con JWT):**
```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
mvn spring-boot:run
```

4. **Revisa las entidades nuevas:**
- [UserRole.java](src/main/java/com/barberia/models/UserRole.java)
- [RolePermission.java](src/main/java/com/barberia/models/RolePermission.java)

¡Ahora tienes un sistema más seguro y profesional! 🚀
