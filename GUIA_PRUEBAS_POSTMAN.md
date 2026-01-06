# 🧪 Guía de Pruebas en Postman - Sistema Multi-Tenant

## 📋 Contexto del Sistema

### Arquitectura de Usuarios (MULTI-TENANT)

```
┌─────────────────────────────────────────────────────────────┐
│              MODELO MULTI-TENANT POR COLUMNA                 │
│                                                              │
│  🏢 Negocio 1: Barbería "El Estilo"                         │
│     └─ ADMIN → Juan (negocio_id=1)                          │
│     └─ BARBERO → Pedro (negocio_id=1)                       │
│                                                              │
│  🏢 Negocio 2: Barbería "Corte Moderno"                     │
│     └─ ADMIN → Ana (negocio_id=2)                           │
│     └─ BARBERO → Luis (negocio_id=2)                        │
│                                                              │
│  AISLAMIENTO:                                                │
│  - Juan solo ve datos de Negocio 1                          │
│  - Ana solo ve datos de Negocio 2                           │
│  - Los datos están en la MISMA base de datos                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    USUARIOS DEL SISTEMA                      │
│  (Acceden al panel administrativo)                           │
│                                                              │
│  • ADMIN → Crea y gestiona usuarios, configuración total    │
│  • BARBERO → Gestiona citas, servicios                      │
│  • RECEPCIONISTA → Gestiona citas, clientes                 │
│                                                              │
│  Endpoint: POST /api/auth/register                           │
│  Acceso: ADMIN (requiere token JWT en producción)           │
│  ⚠️ El rol ADMIN y el negocio deben existir en BD           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                         CLIENTES                             │
│  (Reservan citas desde la app/web)                           │
│                                                              │
│  • CLIENTE → Reserva citas, ve servicios                    │
│                                                              │
│  Endpoint: POST /api/clientes/register (futuro)              │
│  Acceso: Público                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Configuración Inicial

### 1. Crear estructura multi-tenant en la base de datos

**PASO 1: Crear tabla negocios**

```sql
CREATE TABLE negocios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    email VARCHAR(100),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insertar negocios de ejemplo
INSERT INTO negocios (nombre, ruc, direccion, telefono, email, estado) VALUES
('Barbería El Estilo', '20123456789', 'Av. Principal 123', '987654321', 'contacto@elestilo.com', 'ACTIVO'),
('Barbería Corte Moderno', '20987654321', 'Jr. Libertad 456', '912345678', 'info@cortemoderno.com', 'ACTIVO');
```

**PASO 2: Agregar negocio_id a tabla usuarios**

```sql
-- Agregar columna negocio_id
ALTER TABLE usuarios 
ADD COLUMN negocio_id BIGINT NOT NULL DEFAULT 1 AFTER password;

-- Agregar foreign key
ALTER TABLE usuarios
ADD CONSTRAINT fk_usuarios_negocio 
    FOREIGN KEY (negocio_id) REFERENCES negocios(id) ON DELETE RESTRICT;

-- Índice para optimizar consultas
CREATE INDEX idx_usuarios_negocio ON usuarios(negocio_id);
```

**PASO 3: Crear roles y permisos (igual que antes)**

```sql
-- 1. Crear el rol ADMIN
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrador del sistema');

-- 2. Crear permisos completos
INSERT INTO permisos (name, description) VALUES 
('usuarios:crear', 'Crear usuarios'),
('usuarios:leer', 'Ver usuarios'),
('usuarios:actualizar', 'Actualizar usuarios'),
('usuarios:eliminar', 'Eliminar usuarios'),
('citas:crear', 'Crear citas'),
('citas:leer', 'Ver citas'),
('citas:actualizar', 'Actualizar citas'),
('citas:eliminar', 'Eliminar citas'),
('servicios:crear', 'Crear servicios'),
('servicios:leer', 'Ver servicios'),
('servicios:actualizar', 'Actualizar servicios'),
('servicios:eliminar', 'Eliminar servicios'),
('reportes:generar', 'Generar reportes');

-- 3. Asignar todos los permisos al rol ADMIN
INSERT INTO role_permissions (rol_id, permiso_id) 
SELECT 
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    id
FROM permisos;
```

**Alternativa:** Ejecuta el DataInitializer que viene con el proyecto.

### 2. Verificar archivo .env

```env
# Modo desarrollo (sin JWT para pruebas)
SPRING_PROFILES_ACTIVE=dev

# Base de datos
DB_HOST=localhost
DB_PORT=3306
DB_NAME=barberia_db
DB_USERNAME=root
DB_PASSWORD=root

# JWT
JWT_SECRET_KEY=GkN7OtiVoyX+jK5anr1MU6cIOhby1nDu9DxtEN51/lhjgWf2Sqv8hT7Z0C0xpcUnpM90g7sqRXnXYyxFIJ8jyw==
JWT_EXPIRATION=86400000
```

### 3. Iniciar la aplicación

```powershell
cd c:\Barberia_proyecto\api-barberia
.\mvnw.cmd spring-boot:run
```

**Salida esperada:**
```
Perfil activo: dev
MODO DESARROLLO: Seguridad JWT DESHABILITADA
Inicializando datos...
✓ Permisos creados
✓ Roles creados con permisos
✓ Usuario admin@barberia.com creado
Tomcat started on port 8080
```

---

## 📝 Pruebas en Postman

### PRUEBA 1: Registrar Usuario ADMIN en Negocio 1

**Requisitos previos:**
- El rol ADMIN debe existir en BD
- El negocio con id=1 debe existir en BD

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Admin Barbería El Estilo",
  "email": "admin@elestilo.com",
  "password": "admin123",
  "rolId": 1,
  "negocioId": 1
}
```

**Notas:**
- ✅ Los campos `rolId` y `negocioId` son OBLIGATORIOS
- ✅ El rol debe existir en la tabla `roles` (1=ADMIN, 2=MANAGER, 3=USER)
- ✅ El negocio debe existir en la tabla `negocios`
- ✅ En modo dev no requiere token
- ✅ Este usuario solo podrá gestionar datos del negocio 1

**Response esperada (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@elestilo.com",
  "name": "Admin Barbería El Estilo",
  "roles": ["ADMIN"],
  "permissions": [
    "usuarios:crear",
    "usuarios:leer",
    "usuarios:actualizar",
    "usuarios:eliminar",
    "citas:crear",
    "citas:leer",
    "citas:actualizar",
    "citas:eliminar",
    "servicios:crear",
    "servicios:leer",
    "servicios:actualizar",
    "servicios:eliminar",
    "reportes:generar"
  ]
}
```

✅ **Guarda el token para las siguientes pruebas**

---

### PRUEBA 2: Registrar Usuario ADMIN en Negocio 2 (Multi-tenant)

**Crear usuario para otra barbería**

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Admin Barbería Corte Moderno",
  "email": "admin@cortemoderno.com",
  "password": "admin123",
  "roleName": "ADMIN",
  "negocioId": 2
}
```

**Notas:**
- ✅ Mismo rol (ADMIN) pero diferente negocio
- ✅ Este usuario solo gestionará datos del negocio 2
- ✅ No podrá ver datos del negocio 1

---

### PRUEBA 3: Login con ADMIN del Negocio 1

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@elestilo.com",
  "password": "admin123"
}
```

**Response esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@elestilo.com",
  "name": "Admin Barbería El Estilo",
  "roles": ["ADMIN"],
  "permissions": [ ... ]
}
```

---

### PRUEBA 4: ADMIN crea un BARBERO en su mismo negocio

**IMPORTANTE: El campo `negocioId` debe coincidir con el del ADMIN que crea**

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Juan Barbero",
  "email": "juan@elestilo.com",
  "password": "123456",
  "roleName": "BARBERO",
  "negocioId": 1
}
```

**Nota:** Primero crear el rol BARBERO en la BD:
```sql
-- Crear rol
INSERT INTO roles (name, description) VALUES ('BARBERO', 'Barbero profesional');

-- Asignar permisos
INSERT INTO role_permissions (rol_id, permiso_id) 
SELECT 
    (SELECT id FROM roles WHERE name = 'BARBERO'),
    id
FROM permisos 
WHERE name IN ('citas:leer', 'citas:actualizar');
```

**⚠️ En modo PROD (con JWT activo):**
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "name": "Juan Barbero",
  "email": "juan@barberia.com",
  "password": "123456",
  "roleName": "BARBERO"
}
```

**Response esperada (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "juan@barberia.com",
  "name": "Juan Barbero",
  "roles": ["BARBERO"],
  "permissions": [
    "citas:leer",
    "citas:actualizar"
  ]
}
```

---

### PRUEBA 4: Intentar registrar sin roleName (debería fallar)

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "María Sin Rol",
  "email": "maria@test.com",
  "password": "123456",
  "negocioId": 1
}
```

**Response esperada (400 Bad Request):**
```json
{
  "error": "El campo 'roleName' es obligatorio. Valores válidos: ADMIN, BARBERO, RECEPCIONISTA, etc."
}
```

---

### PRUEBA 8: Login con BARBERO y verificar su negocio
### PRUEBA 7: Intentar registrar sin roleName (debería fallar)

### PRUEBA 5: ADMIN crea un RECEPCIONISTA

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "María Recepción",
  "email": "maria@elestilo.com",
  "password": "123456",
  "roleName": "RECEPCIONISTA",
  "negocioId": 1
}
```

**Si el rol RECEPCIONISTA no existe, crearlo primero en BD:**
```sql
-- Crear rol
INSERT INTO roles (name, description) VALUES ('RECEPCIONISTA', 'Recepcionista de barbería');

-- Asignar permisos
INSERT INTO role_permissions (rol_id, permiso_id) 
SELECT 
    (SELECT id FROM roles WHERE name = 'RECEPCIONISTA'),
    id
FROM permisos 
WHERE name IN ('citas:crear', 'citas:leer', 'citas:actualizar');
```

**Response esperada (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "maria@elestilo.com",
  "name": "María Recepción",
  "roles": ["RECEPCIONISTA"],
  "permissions": [
    "citas:crear",
    "citas:leer",
    "citas:actualizar"
  ]
}
```

---

### PRUEBA 10: Intentar crear usuario con rol inexistente

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Test Usuario",
  "email": "test@barberia.com",
  "password": "123456",
  "roleName": "ROL_INVENTADO"
}
```

**Response esperada (400 Bad Request o 500):**
```json
{
  "error": "Rol 'ROL_INVENTADO' no encontrado. Roles disponibles: ADMIN, BARBERO, USER, RECEPCIONISTA"
}
```

---

## 🔄 Cambiar a Modo Producción (con JWT)

### 1. Actualizar .env

```env
SPRING_PROFILES_ACTIVE=prod
```

### 2. Reiniciar aplicación

```powershell
# Detener con Ctrl+C
# Iniciar de nuevo
.\mvnw.cmd springtest.com",
  "password": "123456",
  "roleName": "ROL_INVENTADO",
  "negocioId": 1
}
```

**Response esperada (400 Bad Request o 500):**
```json
{
  "error": "Rol 'ROL_INVENTADO' no encontrado. Roles disponibles: ADMIN, BARBERO, USER, RECEPCIONISTA"
}
```

---

### PRUEBA 11: Intentar crear usuario con negocio inexistente

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Test Usuario",
  "email": "test2@test.com",
  "password": "123456",
  "roleName": "BARBERO",
  "negocioId": 999
}
```

**Response esperada (400 Bad Request o 500):**
```json
{
  "error": "Negocio con id 999 no encontrado
### 3. Ahora TODOS los endpoints requieren JWT (excepto login)

```http
POST http://localhost:8080/api/auth/register
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "Nuevo Usuario",
  "email": "nuevo@barberia.com",
  "password": "123456",
  "roleName": "BARBERO"
}
```

---

## 📊 Resumen de Roles y Permisos

### Roles Disponibles (por defecto)

| Rol | Permisos | Uso |
|-----|----------|-----|
| **ADMIN** | usuarios:*, citas:*, servicios:*, reportes:* | Panel administrativo completo |
| **BARBERO** | citas:leer, citas:actualizar | Gestionar sus citas |
| **USER** | citas:crear, citas:leer | Usuario básico (deprecado para sistema) |
| **RECEPCIONISTA** | citas:crear, citas:leer, citas:actualizar | Gestionar citas y clientes |

### Agregar Más Roles

**1. Crear el rol en BD:**
```sql
INSERT INTO roles (name, description) 
VALUES ('GERENTE', 'Gerente de operaciones');
```

**2. Asignar permisos:**
```sql
INSERT INTO role_permissions (rol_id, permiso_id) 
SELECT 
    (SELECT id FROM roles WHERE name = 'GERENTE'),
    p.id
FROM permisos p
WHERE p.name IN ('reportes:generar', 'reportes:exportar', 'citas:leer', 'servicios:leer');
```

**3. Usar en el registro:**
```json
{
  "name": "Carlos Gerente",
  "email": "carlos@barberia.com",
  "password": "123456",
  "roleName": "GERENTE"
}
```

¡Y ya funciona! Sin cambiar código.

---

## ✅ Checklist de Pruebas

- [ ] Crear rol ADMIN en la BD con script SQL
- [ ] Registrar primer usuario ADMIN (con roleName)
- [ ] Login con ADMIN
- [ ] Crear rol BARBERO en la BD
- [ ] ADMIN crea un BARBERO (con roleName)
- [ ] Crear rol RECEPCIONISTA en la BD
- [ ] ADMIN crea un RECEPCIONISTA (con roleName)
- [ ] Intentar registrar sin roleName (debe fallar con validación)
- [ ] Login con BARBERO
- [ ] Login con RECEPCIONISTA
- [ ] Verificar permisos en cada token
- [ ] Cambiar a modo prod y probar con JWT
- [ ] Intentar registrar sin token en prod (debe fallar)
multi-tenant con `negocio_id` implementado
2. ✅ Tabla `negocios` creada
3. ✅ Tabla `usuarios` con columna `negocio_id`
4. ✅ Entidad `Negocio` y repositorio creados
5. ✅ `RegisterRequest` con validación de `negocioId`
6. ✅ `AuthenticationService` asigna `negocioId` al crear usuario
7. 🔜 Agregar validación de negocio existente en `AuthenticationService`
8. 🔜 Agregar `negocioId` al JWT token (claims)
9. 🔜 Crear filtro para extraer `negocioId` del usuario autenticado
10. 🔜 Validar que un ADMIN solo pueda crear usuarios de su propio negocio
11. 🔜 Aplicar multi-tenant a: Citas, Servicios, Clientes, Reportes
12. 🔜 Crear endpoints CRUD de Negocios (solo Super Admin)
13. 🔜 Implementar tenant context holder
14. 🔜 Agregar filtros automáticos con Spring Data JPA (@Filter)

---

**¿Listo para probar el multi-tenant? Empieza con la PRUEBA 1 y sigue en orden.**

**📚 Lee también:** [ARQUITECTURA_MULTITENANT.md](ARQUITECTURA_MULTITENANT.md) para entender el modelo completo.

### Error: "El campo 'roleName' es obligatorio"
**Causa:** Intentando crear usuario sin el campo roleName  
**Solución:** Agregar `"roleName": "ADMIN"` al request

### Error: "Rol 'XXX' no encontrado"
**Causa:** El rol no existe en la BD  
**Solución:** Crear el rol en la tabla `roles` con SQL

### Error: Validación "El roleName es obligatorio"
**Causa:** El campo roleName está null o vacío  
**Solución:** Es un campo obligatorio, siempre debe enviarse

---

## 🎯 Próximos Pasos

1. ✅ Sistema de usuarios administrativos funcionando
2. 🔜 Crear endpoint separado para registro de CLIENTES
3. 🔜 Agregar más roles según necesites (GERENTE, SUPERVISOR, etc.)
4. 🔜 Implementar endpoints CRUD de usuarios (solo ADMIN)
5. 🔜 Implementar gestión de citas, servicios, etc.

---

**¿Listo para probar? Empieza con la PRUEBA 1 y sigue en orden.**
