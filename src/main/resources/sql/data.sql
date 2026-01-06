-- ============================================
-- SCRIPT DE DATOS INICIALES
-- ============================================
-- Este archivo inserta los datos de prueba
--
-- USO:
-- 1. Opción automática: Spring Boot lo ejecuta si habilitas en application.yml
-- 2. Opción manual: Ejecuta este script en MySQL Workbench
-- 3. Opción Java: Usa DataInitializer.java (recomendado)
--
-- ADVERTENCIA: Este script usa INSERT IGNORE para evitar duplicados
-- ============================================

-- ============================================
-- 1. INSERTAR PERMISOS
-- ============================================
INSERT IGNORE INTO permisos (name, description) VALUES
('READ_CLIENTS', 'Ver lista de clientes'),
('CREATE_CLIENTS', 'Crear nuevos clientes'),
('UPDATE_CLIENTS', 'Actualizar clientes existentes'),
('DELETE_CLIENTS', 'Eliminar clientes'),
('READ_BOOKINGS', 'Ver citas/reservas'),
('CREATE_BOOKING', 'Crear nuevas citas'),
('UPDATE_BOOKING', 'Actualizar citas existentes'),
('DELETE_BOOKING', 'Cancelar citas'),
('DELETE_USERS', 'Eliminar usuarios del sistema');

-- ============================================
-- 2. INSERTAR ROLES
-- ============================================
INSERT IGNORE INTO roles (name, description) VALUES
('ADMIN', 'Administrador con acceso total'),
('MANAGER', 'Gerente con permisos de gestión'),
('USER', 'Usuario básico');

-- ============================================
-- 3. ASIGNAR PERMISOS A ROLES
-- ============================================

-- ADMIN: Todos los permisos
INSERT IGNORE INTO role_permissions (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.name = 'ADMIN';

-- MANAGER: Gestión de clientes y citas (sin eliminar usuarios)
INSERT IGNORE INTO role_permissions (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.name = 'MANAGER'
AND p.name IN (
    'READ_CLIENTS',
    'CREATE_CLIENTS',
    'UPDATE_CLIENTS',
    'READ_BOOKINGS',
    'CREATE_BOOKING',
    'UPDATE_BOOKING',
    'DELETE_BOOKING'
);

-- USER: Solo lectura
INSERT IGNORE INTO role_permissions (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.name = 'USER'
AND p.name IN ('READ_CLIENTS', 'READ_BOOKINGS');

-- ============================================
-- 4. INSERTAR USUARIOS DE PRUEBA
-- ============================================
-- IMPORTANTE: Los passwords están encriptados con BCrypt
-- Password original para todos: "password" (sin comillas)
--
-- Para generar nuevos passwords BCrypt:
-- - Online: https://bcrypt-generator.com/
-- - Java: new BCryptPasswordEncoder().encode("tu_password")
-- - PowerShell: (requiere módulo)

-- Usuario ADMIN
-- Email: admin@barberia.com
-- Password: admin123
INSERT IGNORE INTO usuarios (name, email, password, reg_estado) VALUES
('Admin', 'admin@barberia.com', '$2a$10$8YPNqIQbKFWQzZS1JxAyaeZsJPMFHrMNIf3vCcBhBzqkYEAZp5XKS', 1);

-- Usuario MANAGER
-- Email: manager@barberia.com
-- Password: manager123
INSERT IGNORE INTO usuarios (name, email, password, reg_estado) VALUES
('Manager', 'manager@barberia.com', '$2a$10$8YPNqIQbKFWQzZS1JxAyaeZsJPMFHrMNIf3vCcBhBzqkYEAZp5XKS', 1);

-- Usuario USER
-- Email: user@barberia.com
-- Password: user123
INSERT IGNORE INTO usuarios (name, email, password, reg_estado) VALUES
('User Normal', 'user@barberia.com', '$2a$10$8YPNqIQbKFWQzZS1JxAyaeZsJPMFHrMNIf3vCcBhBzqkYEAZp5XKS', 1);

-- Usuario SUPER (tiene ADMIN + MANAGER)
-- Email: super@barberia.com
-- Password: super123
INSERT IGNORE INTO usuarios (name, email, password, reg_estado) VALUES
('Super Usuario', 'super@barberia.com', '$2a$10$8YPNqIQbKFWQzZS1JxAyaeZsJPMFHrMNIf3vCcBhBzqkYEAZp5XKS', 1);

-- ============================================
-- 5. ASIGNAR ROLES A USUARIOS
-- ============================================

-- Admin → ADMIN
INSERT IGNORE INTO user_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
CROSS JOIN roles r
WHERE u.email = 'admin@barberia.com'
AND r.name = 'ADMIN';

-- Manager → MANAGER
INSERT IGNORE INTO user_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
CROSS JOIN roles r
WHERE u.email = 'manager@barberia.com'
AND r.name = 'MANAGER';

-- User → USER
INSERT IGNORE INTO user_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
CROSS JOIN roles r
WHERE u.email = 'user@barberia.com'
AND r.name = 'USER';

-- Super → ADMIN + MANAGER
INSERT IGNORE INTO user_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
CROSS JOIN roles r
WHERE u.email = 'super@barberia.com'
AND r.name IN ('ADMIN', 'MANAGER');

-- ============================================
-- VERIFICACIÓN: Consultas útiles
-- ============================================

-- Ver todos los usuarios con sus roles:
-- SELECT u.name, u.email, GROUP_CONCAT(r.name) as roles
-- FROM usuarios u
-- LEFT JOIN user_roles ur ON u.id = ur.usuario_id
-- LEFT JOIN roles r ON ur.rol_id = r.id
-- GROUP BY u.id;

-- Ver todos los roles con sus permisos:
-- SELECT r.name as rol, GROUP_CONCAT(p.name) as permisos
-- FROM roles r
-- LEFT JOIN role_permissions rp ON r.id = rp.rol_id
-- LEFT JOIN permisos p ON rp.permiso_id = p.id
-- GROUP BY r.id;
