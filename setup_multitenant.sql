-- ════════════════════════════════════════════════════════════════════════════════
-- SCRIPT DE CONFIGURACIÓN MULTI-TENANT
-- Sistema de Barbería - Modelo Multi-tenant por Columna
-- ════════════════════════════════════════════════════════════════════════════════
--
-- PROPÓSITO:
-- Este script configura la base de datos para soportar múltiples negocios (barberías)
-- en una sola base de datos, usando el patrón Multi-tenant por columna (negocio_id)
--
-- EJECUCIÓN:
-- 1. Crear la base de datos: CREATE DATABASE barberia_db;
-- 2. Usar la base de datos: USE barberia_db;
-- 3. Ejecutar este script completo
--
-- ════════════════════════════════════════════════════════════════════════════════

-- ────────────────────────────────────────────────────────────────────────────────
-- PASO 1: CREAR TABLA NEGOCIOS (Multi-tenant)
-- ────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS negocios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre comercial de la barbería',
    ruc VARCHAR(20) UNIQUE COMMENT 'RUC o identificación fiscal',
    direccion VARCHAR(200) COMMENT 'Dirección física del negocio',
    telefono VARCHAR(20) COMMENT 'Teléfono de contacto',
    email VARCHAR(100) COMMENT 'Email de contacto',
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' COMMENT 'ACTIVO, SUSPENDIDO, INACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_negocios_estado (estado),
    INDEX idx_negocios_ruc (ruc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar negocios de ejemplo
INSERT INTO negocios (nombre, ruc, direccion, telefono, email, estado) VALUES
('Barbería El Estilo', '20123456789', 'Av. Principal 123, Lima', '987654321', 'contacto@elestilo.com', 'ACTIVO'),
('Barbería Corte Moderno', '20987654321', 'Jr. Libertad 456, Lima', '912345678', 'info@cortemoderno.com', 'ACTIVO'),
('Barbería Look Premium', '20555666777', 'Calle Los Olivos 789, Lima', '998877665', 'admin@lookpremium.com', 'ACTIVO');

-- ────────────────────────────────────────────────────────────────────────────────
-- PASO 2: MODIFICAR TABLA USUARIOS (Agregar Multi-tenant)
-- ────────────────────────────────────────────────────────────────────────────────

-- Verificar si la tabla usuarios existe
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    reg_estado INT NOT NULL DEFAULT 1 COMMENT '1=Activo, 0=Inactivo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    INDEX idx_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agregar columna negocio_id si no existe
-- NOTA: Ajusta el DEFAULT 1 según el primer negocio que quieras asignar por defecto
SET @dbname = DATABASE();
SET @tablename = 'usuarios';
SET @columnname = 'negocio_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1', -- La columna ya existe
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT NOT NULL DEFAULT 1 AFTER password')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar foreign key solo si no existe
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE
      constraint_schema = @dbname
      AND table_name = @tablename
      AND constraint_name = 'fk_usuarios_negocio'
  ) > 0,
  'SELECT 1', -- La foreign key ya existe
  CONCAT('ALTER TABLE ', @tablename, ' ADD CONSTRAINT fk_usuarios_negocio FOREIGN KEY (negocio_id) REFERENCES negocios(id) ON DELETE RESTRICT')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Crear índice para optimizar consultas por negocio
CREATE INDEX IF NOT EXISTS idx_usuarios_negocio ON usuarios(negocio_id);

-- ────────────────────────────────────────────────────────────────────────────────
-- PASO 3: CREAR TABLA DE ROLES
-- ────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'ADMIN, BARBERO, RECEPCIONISTA, etc.',
    description VARCHAR(200) COMMENT 'Descripción del rol',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar roles del sistema
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrador con todos los permisos'),
('BARBERO', 'Barbero profesional que gestiona sus citas'),
('RECEPCIONISTA', 'Personal que gestiona citas y clientes'),
('GERENTE', 'Gerente con acceso a reportes y estadísticas'),
('USER', 'Usuario básico del sistema')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ────────────────────────────────────────────────────────────────────────────────
-- PASO 4: CREAR TABLA DE PERMISOS
-- ────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'usuarios:crear, citas:leer, etc.',
    description VARCHAR(200) COMMENT 'Descripción del permiso',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_permisos_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar permisos del sistema
INSERT INTO permisos (name, description) VALUES
-- Permisos de Usuarios
('usuarios:crear', 'Crear nuevos usuarios del sistema'),
('usuarios:leer', 'Ver usuarios del sistema'),
('usuarios:actualizar', 'Actualizar usuarios del sistema'),
('usuarios:eliminar', 'Eliminar usuarios del sistema'),

-- Permisos de Citas
('citas:crear', 'Crear nuevas citas'),
('citas:leer', 'Ver citas'),
('citas:actualizar', 'Actualizar citas existentes'),
('citas:eliminar', 'Eliminar citas'),
('citas:confirmar', 'Confirmar asistencia a citas'),

-- Permisos de Servicios
('servicios:crear', 'Crear nuevos servicios'),
('servicios:leer', 'Ver servicios'),
('servicios:actualizar', 'Actualizar servicios existentes'),
('servicios:eliminar', 'Eliminar servicios'),

-- Permisos de Clientes
('clientes:crear', 'Registrar nuevos clientes'),
('clientes:leer', 'Ver información de clientes'),
('clientes:actualizar', 'Actualizar datos de clientes'),
('clientes:eliminar', 'Eliminar clientes'),

-- Permisos de Reportes
('reportes:generar', 'Generar reportes del negocio'),
('reportes:exportar', 'Exportar reportes a PDF/Excel'),
('reportes:estadisticas', 'Ver estadísticas del negocio'),

-- Permisos de Configuración
('config:leer', 'Ver configuración del negocio'),
('config:actualizar', 'Modificar configuración del negocio')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ────────────────────────────────────────────────────────────────────────────────
-- PASO 5: CREAR TABLA INTERMEDIA ROLE_PERMISSIONS (Many-to-Many)
-- ────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS role_permissions (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE,
    INDEX idx_role_permissions_rol (rol_id),
    INDEX idx_role_permissions_permiso (permiso_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Asignar permisos al rol ADMIN (todos los permisos)
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    id
FROM permisos
ON DUPLICATE KEY UPDATE rol_id=VALUES(rol_id);

-- Asignar permisos al rol BARBERO
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'BARBERO'),
    id
FROM permisos
WHERE name IN (
    'citas:leer',
    'citas:actualizar',
    'citas:confirmar',
    'servicios:leer',
    'clientes:leer'
)
ON DUPLICATE KEY UPDATE rol_id=VALUES(rol_id);

-- Asignar permisos al rol RECEPCIONISTA
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'RECEPCIONISTA'),
    id
FROM permisos
WHERE name IN (
    'citas:crear',
    'citas:leer',
    'citas:actualizar',
    'citas:confirmar',
    'servicios:leer',
    'clientes:crear',
    'clientes:leer',
    'clientes:actualizar'
)
ON DUPLICATE KEY UPDATE rol_id=VALUES(rol_id);

-- Asignar permisos al rol GERENTE
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'GERENTE'),
    id
FROM permisos
WHERE name IN (
    'citas:leer',
    'servicios:leer',
    'clientes:leer',
    'reportes:generar',
    'reportes:exportar',
    'reportes:estadisticas',
    'config:leer'
)
ON DUPLICATE KEY UPDATE rol_id=VALUES(rol_id);

-- Asignar permisos al rol USER
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'USER'),
    id
FROM permisos
WHERE name IN (
    'citas:crear',
    'citas:leer',
    'servicios:leer'
)
ON DUPLICATE KEY UPDATE rol_id=VALUES(rol_id);

-- ────────────────────────────────────────────────────────────────────────────────
-- PASO 6: CREAR TABLA INTERMEDIA USER_ROLES (Many-to-Many)
-- ────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_user_roles_user (user_id),
    INDEX idx_user_roles_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ════════════════════════════════════════════════════════════════════════════════
-- RESUMEN DE LA CONFIGURACIÓN
-- ════════════════════════════════════════════════════════════════════════════════
--
-- ✅ Tabla 'negocios' creada con 3 barberías de ejemplo
-- ✅ Tabla 'usuarios' con columna 'negocio_id' (Multi-tenant)
-- ✅ Tabla 'roles' con 5 roles: ADMIN, BARBERO, RECEPCIONISTA, GERENTE, USER
-- ✅ Tabla 'permisos' con 22 permisos granulares
-- ✅ Tabla 'role_permissions' con asignaciones por rol
-- ✅ Tabla 'user_roles' lista para asignar roles a usuarios
--
-- PRÓXIMOS PASOS:
-- ────────────────────────────────────────────────────────────────────────────────
-- 1. Registrar usuarios con el endpoint POST /api/auth/register
--    Ejemplo:
--    {
--      "name": "Admin Barbería 1",
--      "email": "admin@elestilo.com",
--      "password": "admin123",
--      "roleName": "ADMIN",
--      "negocioId": 1
--    }
--
-- 2. Los usuarios creados solo podrán gestionar datos de su negocio (negocio_id)
--
-- 3. Ver guía completa en: GUIA_PRUEBAS_POSTMAN.md
--
-- ════════════════════════════════════════════════════════════════════════════════

SELECT 'Base de datos configurada exitosamente para Multi-Tenant' AS mensaje;
SELECT CONCAT('Negocios creados: ', COUNT(*)) AS resultado FROM negocios;
SELECT CONCAT('Roles creados: ', COUNT(*)) AS resultado FROM roles;
SELECT CONCAT('Permisos creados: ', COUNT(*)) AS resultado FROM permisos;
SELECT 'Listo para crear usuarios con negocioId' AS siguiente_paso;
