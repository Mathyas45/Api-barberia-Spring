-- ========================================
-- SCRIPT: Configuración de Permisos para Clientes
-- ========================================
-- Descripción: Crea permisos específicos para el módulo de clientes
--              y los asigna a los roles correspondientes
-- Uso: Ejecutar después de crear roles base (ADMIN, BARBERO, RECEPCIONISTA)

-- ========================================
-- 1. CREAR PERMISOS DE CLIENTES
-- ========================================
INSERT INTO permisos (name, description, created_at) VALUES
('READ_CLIENTS', 'Permite ver la lista y detalles de clientes', NOW()),
('CREATE_CLIENTS', 'Permite crear nuevos clientes', NOW()),
('UPDATE_CLIENTS', 'Permite actualizar información de clientes', NOW()),
('DELETE_CLIENTS', 'Permite eliminar clientes (solo ADMIN)', NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ========================================
-- 2. ASIGNAR TODOS LOS PERMISOS A ADMIN
-- ========================================
-- ADMIN tiene todos los permisos de clientes
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    r.id,
    p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.name = 'ADMIN'
  AND p.name IN ('READ_CLIENTS', 'CREATE_CLIENTS', 'UPDATE_CLIENTS', 'DELETE_CLIENTS')
ON DUPLICATE KEY UPDATE rol_id = rol_id;

-- ========================================
-- 3. ASIGNAR PERMISOS A BARBERO
-- ========================================
-- BARBERO puede ver y crear clientes
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    r.id,
    p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.name = 'BARBERO'
  AND p.name IN ('READ_CLIENTS', 'CREATE_CLIENTS')
ON DUPLICATE KEY UPDATE rol_id = rol_id;

-- ========================================
-- 4. ASIGNAR PERMISOS A RECEPCIONISTA
-- ========================================
-- RECEPCIONISTA puede ver, crear y actualizar clientes
INSERT INTO role_permissions (rol_id, permiso_id)
SELECT 
    r.id,
    p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.name = 'RECEPCIONISTA'
  AND p.name IN ('READ_CLIENTS', 'CREATE_CLIENTS', 'UPDATE_CLIENTS')
ON DUPLICATE KEY UPDATE rol_id = rol_id;

-- ========================================
-- 5. VERIFICAR CONFIGURACIÓN
-- ========================================
-- Query para ver qué permisos tiene cada rol
SELECT 
    r.name AS 'Rol',
    GROUP_CONCAT(p.name SEPARATOR ', ') AS 'Permisos'
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.rol_id
LEFT JOIN permisos p ON rp.permiso_id = p.id
WHERE p.name LIKE '%CLIENT%'
GROUP BY r.id, r.name
ORDER BY r.name;

-- ========================================
-- 6. VERIFICAR PERMISOS DE UN USUARIO ESPECÍFICO
-- ========================================
-- Descomenta y cambia el email para verificar un usuario
/*
SELECT 
    u.email AS 'Usuario',
    r.name AS 'Rol',
    GROUP_CONCAT(p.name SEPARATOR ', ') AS 'Permisos'
FROM usuarios u
JOIN user_roles ur ON u.id = ur.usuario_id
JOIN roles r ON ur.rol_id = r.id
LEFT JOIN role_permissions rp ON r.id = rp.rol_id
LEFT JOIN permisos p ON rp.permiso_id = p.id
WHERE u.email = 'admin@elestilo.com'
  AND (p.name LIKE '%CLIENT%' OR p.name IS NULL)
GROUP BY u.id, u.email, r.id, r.name;
*/

-- ========================================
-- RESULTADO ESPERADO
-- ========================================
-- +----------------+------------------------------------------------------+
-- | Rol            | Permisos                                             |
-- +----------------+------------------------------------------------------+
-- | ADMIN          | CREATE_CLIENTS, DELETE_CLIENTS, READ_CLIENTS, ...   |
-- | BARBERO        | CREATE_CLIENTS, READ_CLIENTS                         |
-- | RECEPCIONISTA  | CREATE_CLIENTS, READ_CLIENTS, UPDATE_CLIENTS         |
-- +----------------+------------------------------------------------------+
