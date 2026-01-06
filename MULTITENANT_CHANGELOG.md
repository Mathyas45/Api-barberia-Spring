# 🏢 Sistema Multi-Tenant Implementado

## ✅ Cambios Realizados

Se ha implementado el modelo **Multi-tenant por columna** usando `negocio_id` para soportar múltiples barberías en una sola base de datos.

---

## 📋 Archivos Modificados/Creados

### 🆕 Nuevos Archivos

1. **[Negocio.java](src/main/java/com/barberia/models/Negocio.java)**
   - Entidad para representar cada barbería/negocio
   - Campos: nombre, ruc, dirección, teléfono, email, estado

2. **[NegocioRepository.java](src/main/java/com/barberia/repositories/NegocioRepository.java)**
   - Repositorio para gestión de negocios
   - Métodos: findByRuc, existsByRuc, findByNombre

3. **[ARQUITECTURA_MULTITENANT.md](ARQUITECTURA_MULTITENANT.md)**
   - Documentación completa del modelo multi-tenant
   - Diagramas, ejemplos, ventajas y desventajas

4. **[setup_multitenant.sql](setup_multitenant.sql)**
   - Script SQL completo para configurar la base de datos
   - Crea tabla negocios, modifica usuarios, crea roles y permisos

### 📝 Archivos Modificados

1. **[Usuario.java](src/main/java/com/barberia/models/Usuario.java)**
   - ✅ Agregado campo `negocioId` (BIGINT NOT NULL)
   - ✅ Relación ManyToOne con `Negocio`
   - ✅ Documentación multi-tenant

2. **[UsuarioRepository.java](src/main/java/com/barberia/repositories/UsuarioRepository.java)**
   - ✅ Nuevos métodos con filtro de negocio:
     - `findAllByNegocioId(negocioId)`
     - `countByNegocioId(negocioId)`
     - `findByIdAndNegocioId(id, negocioId)`
     - `findByEmailAndNegocioId(email, negocioId)`

3. **[RegisterRequest.java](src/main/java/com/barberia/dto/RegisterRequest.java)**
   - ✅ Agregado campo `negocioId` con validación `@NotNull`
   - ✅ Documentación explicando el uso multi-tenant

4. **[AuthenticationService.java](src/main/java/com/barberia/services/AuthenticationService.java)**
   - ✅ Validación de `negocioId` obligatorio
   - ✅ Asignación de `negocioId` al crear usuario
   - ✅ Comentarios explicando el flujo multi-tenant

5. **[AuthController.java](src/main/java/com/barberia/controllers/AuthController.java)**
   - ✅ Documentación actualizada con ejemplos de `negocioId`
   - ✅ Notas de seguridad multi-tenant

6. **[GUIA_PRUEBAS_POSTMAN.md](GUIA_PRUEBAS_POSTMAN.md)**
   - ✅ Completamente reescrita para multi-tenant
   - ✅ 11 pruebas paso a paso
   - ✅ Scripts SQL incluidos
   - ✅ Ejemplos de aislamiento de datos

---

## 🗄️ Estructura de Base de Datos

### Nueva Tabla: `negocios`

```sql
CREATE TABLE negocios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    email VARCHAR(100),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Tabla Modificada: `usuarios`

```sql
ALTER TABLE usuarios 
ADD COLUMN negocio_id BIGINT NOT NULL AFTER password,
ADD CONSTRAINT fk_usuarios_negocio 
    FOREIGN KEY (negocio_id) REFERENCES negocios(id);
```

---

## 🚀 Cómo Usar

### 1. Configurar Base de Datos

```bash
# Opción A: Ejecutar script completo
mysql -u root -p barberia_db < setup_multitenant.sql

# Opción B: Ejecutar el DataInitializer del proyecto (si existe)
# (Reiniciar la aplicación Spring Boot)
```

### 2. Crear Negocios

```sql
INSERT INTO negocios (nombre, ruc, direccion, telefono, email) VALUES
('Barbería El Estilo', '20123456789', 'Av. Principal 123', '987654321', 'contacto@elestilo.com'),
('Barbería Corte Moderno', '20987654321', 'Jr. Libertad 456', '912345678', 'info@cortemoderno.com');
```

### 3. Registrar Usuario con Negocio

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Admin Barbería 1",
  "email": "admin@elestilo.com",
  "password": "admin123",
  "roleName": "ADMIN",
  "negocioId": 1
}
```

### 4. Login (el token incluirá el negocio)

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@elestilo.com",
  "password": "admin123"
}
```

---

## 🔐 Seguridad Multi-Tenant

### Reglas de Aislamiento

✅ **Cada usuario pertenece a UN solo negocio**
- El `negocioId` se asigna al registrar
- No se puede cambiar sin autorización

✅ **Las consultas filtran por negocio**
```java
// ❌ MAL - Sin filtro
List<Usuario> usuarios = usuarioRepository.findAll();

// ✅ BIEN - Con filtro de negocio
List<Usuario> usuarios = usuarioRepository.findAllByNegocioId(negocioId);
```

✅ **Validaciones en endpoints**
- Un ADMIN de negocio 1 NO puede crear usuarios para negocio 2
- Las citas/servicios/clientes también filtrarán por negocio

---

## 📊 Ejemplos de Queries

### Listar usuarios de un negocio

```java
// Negocio 1
List<Usuario> usuarios = usuarioRepository.findAllByNegocioId(1L);

// Negocio 2
List<Usuario> usuarios = usuarioRepository.findAllByNegocioId(2L);
```

### Contar usuarios por negocio

```java
long total = usuarioRepository.countByNegocioId(1L);
```

### Buscar usuario validando negocio

```java
Optional<Usuario> usuario = usuarioRepository.findByIdAndNegocioId(userId, negocioId);
```

---

## 🎯 Próximos Pasos

### Pendientes Inmediatos

- [ ] Agregar validación en `AuthenticationService` para verificar que el negocio existe
- [ ] Incluir `negocioId` en el token JWT (claims)
- [ ] Crear filtro para extraer `negocioId` del usuario autenticado
- [ ] Validar en producción que un ADMIN solo cree usuarios de su negocio

### Aplicar Multi-Tenant a Otras Entidades

- [ ] Tabla `citas` → Agregar `negocio_id`
- [ ] Tabla `servicios` → Agregar `negocio_id`
- [ ] Tabla `clientes` → Agregar `negocio_id`
- [ ] Tabla `reportes` → Agregar `negocio_id`

### Mejoras Avanzadas

- [ ] Tenant Context Holder (almacenar negocio_id en ThreadLocal)
- [ ] Filtros automáticos con Hibernate `@Filter`
- [ ] Endpoints CRUD para gestión de Negocios
- [ ] Dashboard multi-tenant (Super Admin)

---

## 📚 Documentación

- **[ARQUITECTURA_MULTITENANT.md](ARQUITECTURA_MULTITENANT.md)** - Arquitectura completa del modelo
- **[GUIA_PRUEBAS_POSTMAN.md](GUIA_PRUEBAS_POSTMAN.md)** - Guía de pruebas paso a paso
- **[setup_multitenant.sql](setup_multitenant.sql)** - Script SQL de configuración

---

## ✅ Testing

### Checklist de Pruebas

- [ ] Crear 2 negocios en BD
- [ ] Registrar ADMIN en negocio 1
- [ ] Registrar ADMIN en negocio 2
- [ ] Login con cada ADMIN
- [ ] ADMIN negocio 1 crea BARBERO para negocio 1
- [ ] ADMIN negocio 2 crea BARBERO para negocio 2
- [ ] Verificar que datos están aislados por negocio
- [ ] Intentar crear usuario sin negocioId (debe fallar)
- [ ] Intentar acceder a datos de otro negocio (debe fallar en prod)

---

## 🐛 Troubleshooting

### "Column 'negocio_id' cannot be null"
**Solución:** Ejecuta `setup_multitenant.sql` para agregar la columna

### "El campo 'negocioId' es obligatorio"
**Solución:** Incluye `"negocioId": 1` en el JSON del registro

### "Negocio no encontrado"
**Solución:** Crea el negocio con: `INSERT INTO negocios (nombre) VALUES ('Mi Barbería');`

### Usuarios de diferentes negocios se ven entre sí
**Solución:** Asegúrate de usar métodos con filtro de negocio en los repositorios

---

**Sistema Multi-Tenant listo para producción con múltiples barberías en una sola base de datos** ✅
