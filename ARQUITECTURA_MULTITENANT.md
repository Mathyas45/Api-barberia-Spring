# 📊 Arquitectura Multi-Tenant - Modelo por Columna

## 🎯 Concepto

**Multi-tenant por columna** significa que múltiples "negocios" (barberías) comparten la misma base de datos, pero sus datos están separados lógicamente mediante una columna discriminadora: `negocio_id`.

```
┌─────────────────────────────────────────────────────────────┐
│                    BASE DE DATOS ÚNICA                       │
│                      barberia_db                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Tabla: usuarios                                             │
│  ┌────┬──────┬───────────────────┬──────────────────────┐   │
│  │ id │ name │ email             │ negocio_id           │   │
│  ├────┼──────┼───────────────────┼──────────────────────┤   │
│  │ 1  │ Juan │ juan@example.com  │ 1 (Barbería A)       │   │
│  │ 2  │ Ana  │ ana@example.com   │ 1 (Barbería A)       │   │
│  │ 3  │ Luis │ luis@example.com  │ 2 (Barbería B)       │   │
│  │ 4  │ Marta│ marta@example.com │ 2 (Barbería B)       │   │
│  └────┴──────┴───────────────────┴──────────────────────┘   │
│                                                              │
│  Negocio 1: Barbería "El Estilo" → usuarios 1 y 2           │
│  Negocio 2: Barbería "Corte Moderno" → usuarios 3 y 4       │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Estructura de Tablas

### 1. Tabla `negocios` (nueva)

```sql
CREATE TABLE negocios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,              -- "Barbería El Estilo"
    ruc VARCHAR(20) UNIQUE,                    -- Identificación fiscal
    direccion VARCHAR(200),                    -- Dirección física
    telefono VARCHAR(20),                      -- Teléfono de contacto
    email VARCHAR(100),                        -- Email del negocio
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',  -- ACTIVO, SUSPENDIDO, INACTIVO
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Datos de ejemplo
INSERT INTO negocios (nombre, ruc, direccion, telefono, email, estado) VALUES
('Barbería El Estilo', '20123456789', 'Av. Principal 123', '987654321', 'contacto@elestilo.com', 'ACTIVO'),
('Barbería Corte Moderno', '20987654321', 'Jr. Libertad 456', '912345678', 'info@cortemoderno.com', 'ACTIVO'),
('Barbería Look Premium', '20555666777', 'Calle Los Olivos 789', '998877665', 'admin@lookpremium.com', 'ACTIVO');
```

### 2. Tabla `usuarios` (modificada)

```sql
ALTER TABLE usuarios 
ADD COLUMN negocio_id BIGINT NOT NULL AFTER password,
ADD CONSTRAINT fk_usuarios_negocio 
    FOREIGN KEY (negocio_id) REFERENCES negocios(id) ON DELETE RESTRICT;

-- Índice para optimizar consultas por negocio
CREATE INDEX idx_usuarios_negocio ON usuarios(negocio_id);

-- Para usuarios existentes (si los hay), asigna negocio por defecto
UPDATE usuarios SET negocio_id = 1 WHERE negocio_id IS NULL;
```

### 3. Otras tablas con multi-tenant

```sql
-- Citas
ALTER TABLE citas 
ADD COLUMN negocio_id BIGINT NOT NULL,
ADD CONSTRAINT fk_citas_negocio 
    FOREIGN KEY (negocio_id) REFERENCES negocios(id);

-- Servicios
ALTER TABLE servicios 
ADD COLUMN negocio_id BIGINT NOT NULL,
ADD CONSTRAINT fk_servicios_negocio 
    FOREIGN KEY (negocio_id) REFERENCES negocios(id);

-- Clientes
ALTER TABLE clientes 
ADD COLUMN negocio_id BIGINT NOT NULL,
ADD CONSTRAINT fk_clientes_negocio 
    FOREIGN KEY (negocio_id) REFERENCES negocios(id);
```

---

## 🔐 Seguridad Multi-Tenant

### Reglas de Aislamiento

1. **Usuarios**
   - Un usuario pertenece a UN solo negocio
   - No puede ver ni gestionar usuarios de otros negocios
   - El `negocio_id` se valida en cada operación

2. **Consultas**
   - TODAS las consultas incluyen `WHERE negocio_id = ?`
   - Nunca consultar sin filtro de negocio (excepto login)
   - El `negocio_id` viene del token JWT del usuario autenticado

3. **Validaciones**
   ```java
   // ❌ MAL - Sin filtro de negocio
   List<Usuario> usuarios = usuarioRepository.findAll();
   
   // ✅ BIEN - Con filtro de negocio
   List<Usuario> usuarios = usuarioRepository.findAllByNegocioId(negocioId);
   ```

### Flujo de Seguridad

```
1. Usuario hace login
   ↓
2. Sistema valida credenciales (sin filtro de negocio)
   ↓
3. Genera JWT con negocio_id del usuario
   {
     "sub": "juan@example.com",
     "negocioId": 1,
     "roles": ["ADMIN"],
     ...
   }
   ↓
4. Usuario hace request con token
   ↓
5. JwtAuthenticationFilter extrae negocio_id del token
   ↓
6. Todas las operaciones usan ese negocio_id como filtro
```

---

## 📝 Ejemplos de Uso

### 1. Registrar Usuario (con negocioId)

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Juan Barbero",
  "email": "juan@barberia.com",
  "password": "123456",
  "roleName": "BARBERO",
  "negocioId": 1
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "juan@barberia.com",
  "name": "Juan Barbero",
  "roles": ["BARBERO"],
  "permissions": ["citas:leer", "citas:actualizar"]
}
```

### 2. Login (sin negocioId, pero token lo incluye)

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "juan@barberia.com",
  "password": "123456"
}
```

**Response incluye el negocio del usuario:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "juan@barberia.com",
  "name": "Juan Barbero",
  "negocioId": 1,  ← Incluido en el token
  "roles": ["BARBERO"]
}
```

### 3. Listar Usuarios de MI Negocio

```java
// En el controller, extraemos negocio_id del usuario autenticado
Long negocioId = usuarioAutenticado.getNegocioId();

// Solo trae usuarios de ESE negocio
List<Usuario> usuarios = usuarioRepository.findAllByNegocioId(negocioId);
```

---

## ⚙️ Configuración en Código

### 1. Entidad Usuario

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String password;
    
    // MULTI-TENANT
    @Column(name = "negocio_id", nullable = false)
    private Long negocioId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", insertable = false, updatable = false)
    private Negocio negocio;
    
    // ... resto de campos
}
```

### 2. Repository con Filtros

```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Login (sin filtro de negocio)
    Optional<Usuario> findByEmail(String email);
    
    // MULTI-TENANT: Solo usuarios de un negocio
    List<Usuario> findAllByNegocioId(Long negocioId);
    
    // MULTI-TENANT: Contar usuarios de un negocio
    long countByNegocioId(Long negocioId);
    
    // MULTI-TENANT: Buscar por ID y negocio (seguridad)
    Optional<Usuario> findByIdAndNegocioId(Long id, Long negocioId);
}
```

### 3. Service con Validación

```java
@Service
public class UsuarioService {
    
    public List<Usuario> listarUsuarios(Long negocioId) {
        // Solo retorna usuarios del negocio especificado
        return usuarioRepository.findAllByNegocioId(negocioId);
    }
    
    public Usuario obtenerUsuario(Long id, Long negocioId) {
        // Valida que el usuario pertenezca al negocio
        return usuarioRepository.findByIdAndNegocioId(id, negocioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado o no pertenece a tu negocio"));
    }
}
```

---

## 🎯 Ventajas y Desventajas

### ✅ Ventajas

1. **Simplicidad**
   - Una sola base de datos
   - Fácil de mantener y hacer backups

2. **Costo**
   - No requiere infraestructura adicional
   - Económico para múltiples negocios

3. **Escalabilidad vertical**
   - Más negocios = solo más filas en las tablas
   - No requiere nuevas instancias de BD

4. **Flexibilidad**
   - Agregar negocios es solo un INSERT
   - No requiere desplegar nueva infraestructura

### ⚠️ Desventajas

1. **Aislamiento limitado**
   - Un error en el código puede exponer datos entre negocios
   - Requiere disciplina estricta en las queries

2. **Performance**
   - Todos los negocios compiten por los mismos recursos
   - Un negocio con mucha carga afecta a los demás

3. **Backups**
   - No se puede hacer backup de un solo negocio
   - Recuperación individual más compleja

---

## 🚀 Próximos Pasos

1. ✅ Crear tabla `negocios`
2. ✅ Agregar `negocio_id` a tabla `usuarios`
3. ✅ Actualizar entidad `Usuario` con campo `negocioId`
4. ✅ Actualizar `RegisterRequest` con validación `@NotNull`
5. ✅ Actualizar `AuthenticationService` para asignar `negocioId`
6. 🔜 Agregar `negocioId` al JWT token
7. 🔜 Crear filtro para extraer `negocioId` del usuario autenticado
8. 🔜 Aplicar multi-tenant a Citas, Servicios, Clientes
9. 🔜 Crear endpoints de gestión de Negocios (CRUD)

---

## 🧪 Testing Multi-Tenant

### Prueba 1: Crear 2 negocios
```sql
INSERT INTO negocios (nombre, ruc) VALUES 
('Barbería A', '20111111111'),
('Barbería B', '20222222222');
```

### Prueba 2: Crear usuarios en cada negocio
```json
// Usuario en negocio 1
POST /api/auth/register
{
  "name": "Admin Barbería A",
  "email": "admin@barberia-a.com",
  "password": "123456",
  "roleName": "ADMIN",
  "negocioId": 1
}

// Usuario en negocio 2
POST /api/auth/register
{
  "name": "Admin Barbería B",
  "email": "admin@barberia-b.com",
  "password": "123456",
  "roleName": "ADMIN",
  "negocioId": 2
}
```

### Prueba 3: Validar aislamiento
- Login con usuario de negocio 1
- Intentar acceder a datos de negocio 2
- Debe retornar error o lista vacía

---

**¿Listo para implementar? El sistema ahora soporta múltiples barberías en una sola base de datos.**
