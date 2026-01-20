-- =====================================================
-- SCRIPT DE DATOS DE PRUEBA PARA TESTING
-- =====================================================
-- Este script inicializa datos completos para probar el sistema
-- Incluye: negocios, usuarios, roles, permisos, profesionales,
-- servicios, horarios, configuraciones y reservas de prueba
-- =====================================================

USE barberia_db;

-- =====================================================
-- 1. LIMPIAR DATOS EXISTENTES (OPCIONAL - COMENTAR SI NO QUIERES BORRAR)
-- =====================================================
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE reservas;
-- TRUNCATE TABLE horarios_profesionales;
-- TRUNCATE TABLE profesionales;
-- TRUNCATE TABLE servicios;
-- TRUNCATE TABLE categorias;
-- TRUNCATE TABLE horarios_negocios;
-- TRUNCATE TABLE configuraciones_reservas;
-- TRUNCATE TABLE user_roles;
-- TRUNCATE TABLE role_permissions;
-- TRUNCATE TABLE usuarios;
-- TRUNCATE TABLE roles;
-- TRUNCATE TABLE permisos;
-- TRUNCATE TABLE negocios;
-- TRUNCATE TABLE clientes;
-- SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 2. INSERTAR PERMISOS
-- =====================================================
INSERT IGNORE INTO permisos (id, name, description, reg_estado) VALUES
(1, 'READ_CLIENTS', 'Ver lista de clientes', 1),
(2, 'CREATE_CLIENTS', 'Crear nuevos clientes', 1),
(3, 'UPDATE_CLIENTS', 'Actualizar clientes existentes', 1),
(4, 'DELETE_CLIENTS', 'Eliminar clientes', 1),
(5, 'READ_BOOKINGS', 'Ver citas/reservas', 1),
(6, 'CREATE_BOOKING', 'Crear nuevas citas', 1),
(7, 'UPDATE_BOOKING', 'Actualizar citas existentes', 1),
(8, 'DELETE_BOOKING', 'Cancelar citas', 1),
(9, 'READ_AVAILABILITIES', 'Ver disponibilidad de profesionales', 1),
(10, 'MANAGE_PROFESSIONALS', 'Gestionar profesionales', 1),
(11, 'MANAGE_SERVICES', 'Gestionar servicios', 1),
(12, 'MANAGE_BUSINESS', 'Gestionar negocios', 1),
(13, 'DELETE_USERS', 'Eliminar usuarios del sistema', 1);

-- =====================================================
-- 3. INSERTAR ROLES
-- =====================================================
INSERT IGNORE INTO roles (id, name, description, reg_estado) VALUES
(1, 'ADMIN', 'Administrador con acceso total', 1),
(2, 'MANAGER', 'Gerente con permisos de gestión', 1),
(3, 'BARBER', 'Barbero/Profesional', 1),
(4, 'USER', 'Usuario/Cliente básico', 1);

-- =====================================================
-- 4. ASIGNAR PERMISOS A ROLES
-- =====================================================

-- ADMIN: Todos los permisos
INSERT IGNORE INTO role_permissions (rol_id, permiso_id)
SELECT 1, id FROM permisos;

-- MANAGER: Gestión completa excepto eliminar usuarios
INSERT IGNORE INTO role_permissions (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE name != 'DELETE_USERS';

-- BARBER: Ver clientes, reservas, disponibilidad y gestionar sus citas
INSERT IGNORE INTO role_permissions (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE name IN (
    'READ_CLIENTS', 'READ_BOOKINGS', 'CREATE_BOOKING', 
    'UPDATE_BOOKING', 'READ_AVAILABILITIES'
);

-- USER: Solo lectura
INSERT IGNORE INTO role_permissions (rol_id, permiso_id)
SELECT 4, id FROM permisos WHERE name IN (
    'READ_CLIENTS', 'READ_BOOKINGS', 'CREATE_BOOKING', 'READ_AVAILABILITIES'
);

-- =====================================================
-- 5. INSERTAR NEGOCIOS
-- =====================================================
INSERT IGNORE INTO negocios (id, nombre, ruc, direccion, telefono, email, estado, reg_estado, fecha_registro) VALUES
(1, 'Barbería El Estilo', '20123456789', 'Av. Principal 123, Lima', '987654321', 'contacto@elestilo.com', 'ACTIVO', 1, NOW()),
(2, 'Barbería Corte Moderno', '20987654321', 'Jr. Los Barberos 456, Miraflores', '987654322', 'info@cortemoderno.com', 'ACTIVO', 1, NOW());

-- =====================================================
-- 6. INSERTAR USUARIOS (Passwords: "admin123", "manager123", etc.)
-- =====================================================
-- Nota: Estos hashes BCrypt corresponden a "password123"
INSERT IGNORE INTO usuarios (id, name, email, password, negocio_id, reg_estado, fecha_registro) VALUES
(1, 'Admin Principal', 'admin@barberia.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, NOW()),
(2, 'Manager Barbería', 'manager@barberia.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, NOW()),
(3, 'Juan Pérez', 'barbero1@barberia.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, NOW()),
(4, 'Carlos Gómez', 'barbero2@barberia.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, NOW()),
(5, 'Usuario Cliente', 'user@barberia.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, NOW());

-- =====================================================
-- 7. ASIGNAR ROLES A USUARIOS
-- =====================================================
INSERT IGNORE INTO user_roles (usuario_id, rol_id) VALUES
(1, 1), -- Admin → ADMIN
(2, 2), -- Manager → MANAGER
(3, 3), -- Barbero 1 → BARBER
(4, 3), -- Barbero 2 → BARBER
(5, 4); -- Usuario → USER

-- =====================================================
-- 8. INSERTAR CONFIGURACIÓN DE RESERVAS
-- =====================================================
INSERT IGNORE INTO configuraciones_reservas (
    id, negocio_id, duracion_turno_minutos, intervalo_turnos_minutos,
    anticipacion_minima_horas, anticipacion_maxima_dias, cancelacion_minima_horas,
    requiere_confirmacion, permite_reserva_multiple, max_reservas_simultaneas,
    reg_estado, usuario_registro, fecha_registro
) VALUES
(1, 1, 30, 15, 2, 30, 2, true, false, 3, 1, 1, NOW()),
(2, 2, 30, 10, 1, 60, 1, false, true, 5, 1, 1, NOW());

-- =====================================================
-- 9. INSERTAR HORARIOS DE NEGOCIOS
-- =====================================================
INSERT IGNORE INTO horarios_negocios (
    id, negocio_id, dia_semana, hora_inicio, hora_fin,
    reg_estado, usuario_registro, fecha_registro
) VALUES
-- Negocio 1: Lunes a Viernes 09:00-18:00
(1, 1, 'LUNES', '09:00:00', '18:00:00', 1, 1, NOW()),
(2, 1, 'MARTES', '09:00:00', '18:00:00', 1, 1, NOW()),
(3, 1, 'MIERCOLES', '09:00:00', '18:00:00', 1, 1, NOW()),
(4, 1, 'JUEVES', '09:00:00', '18:00:00', 1, 1, NOW()),
(5, 1, 'VIERNES', '09:00:00', '18:00:00', 1, 1, NOW()),
-- Negocio 1: Sábado 09:00-14:00
(6, 1, 'SABADO', '09:00:00', '14:00:00', 1, 1, NOW());

-- =====================================================
-- 10. INSERTAR CATEGORÍAS DE SERVICIOS
-- =====================================================
INSERT IGNORE INTO categorias (id, negocio_id, nombre, descripcion, reg_estado, usuario_registro, fecha_registro) VALUES
(1, 1, 'Cortes', 'Cortes de cabello', 1, 1, NOW()),
(2, 1, 'Barba', 'Servicios de barba', 1, 1, NOW()),
(3, 1, 'Tratamientos', 'Tratamientos capilares', 1, 1, NOW());

-- =====================================================
-- 11. INSERTAR SERVICIOS
-- =====================================================
INSERT IGNORE INTO servicios (
    id, negocio_id, categoria_id, nombre, descripcion, 
    precio, duracion_minutos_aprox, reg_estado, usuario_registro, fecha_registro
) VALUES
(1, 1, 1, 'Corte Clásico', 'Corte de cabello tradicional', 25.00, 30, 1, 1, NOW()),
(2, 1, 1, 'Corte Moderno', 'Corte de cabello estilo moderno', 35.00, 45, 1, 1, NOW()),
(3, 1, 2, 'Perfilado de Barba', 'Perfilado y arreglo de barba', 15.00, 20, 1, 1, NOW()),
(4, 1, 2, 'Barba Completa', 'Afeitado y arreglo completo', 25.00, 30, 1, 1, NOW()),
(5, 1, 3, 'Tratamiento Capilar', 'Hidratación y tratamiento', 40.00, 45, 1, 1, NOW());

-- =====================================================
-- 12. INSERTAR PROFESIONALES
-- =====================================================
INSERT IGNORE INTO profesionales (
    id, negocio_id, nombre, especialidad, telefono, email,
    usa_horario_negocio, reg_estado, usuario_registro, fecha_registro
) VALUES
(1, 1, 'Juan Pérez', 'Cortes y barba', '987654321', 'juan@barberia.com', true, 1, 1, NOW()),
(2, 1, 'Carlos Gómez', 'Especialista en cortes modernos', '987654322', 'carlos@barberia.com', false, 1, 1, NOW());

-- =====================================================
-- 13. INSERTAR HORARIOS PERSONALIZADOS DE PROFESIONALES
-- =====================================================
-- Carlos tiene horario personalizado (no usa horario del negocio)
INSERT IGNORE INTO horarios_profesionales (
    id, profesional_id, dia_semana, hora_inicio, hora_fin,
    reg_estado, usuario_registro, fecha_registro
) VALUES
(1, 2, 'LUNES', '10:00:00', '19:00:00', 1, 1, NOW()),
(2, 2, 'MARTES', '10:00:00', '19:00:00', 1, 1, NOW()),
(3, 2, 'MIERCOLES', '10:00:00', '19:00:00', 1, 1, NOW()),
(4, 2, 'JUEVES', '10:00:00', '19:00:00', 1, 1, NOW()),
(5, 2, 'VIERNES', '10:00:00', '19:00:00', 1, 1, NOW());

-- =====================================================
-- 14. INSERTAR CLIENTES
-- =====================================================
INSERT IGNORE INTO clientes (
    id, negocio_id, nombre, apellido, telefono, email, dni,
    reg_estado, usuario_registro, fecha_registro
) VALUES
(1, 1, 'Luis', 'Martínez', '999888777', 'luis@email.com', '12345678', 1, 1, NOW()),
(2, 1, 'María', 'García', '999888666', 'maria@email.com', '87654321', 1, 1, NOW()),
(3, 1, 'Pedro', 'López', '999888555', 'pedro@email.com', '11223344', 1, 1, NOW());

-- =====================================================
-- 15. INSERTAR RESERVAS DE PRUEBA
-- =====================================================
-- Reservas para HOY y próximos días (ajusta las fechas según necesites)
INSERT IGNORE INTO reservas (
    id, negocio_id, cliente_id, profesional_id, servicio_id,
    fecha, hora_inicio, hora_fin, estado, notas,
    reg_estado, usuario_registro, fecha_registro
) VALUES
-- Reserva para hoy - Juan Pérez - 10:00-10:30
(1, 1, 1, 1, 1, CURDATE(), '10:00:00', '10:30:00', 'CONFIRMADA', 'Cliente regular', 1, 1, NOW()),
-- Reserva para hoy - Juan Pérez - 11:00-11:30
(2, 1, 2, 1, 1, CURDATE(), '11:00:00', '11:30:00', 'CONFIRMADA', NULL, 1, 1, NOW()),
-- Reserva para mañana - Carlos Gómez - 14:00-14:45
(3, 1, 3, 2, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '14:45:00', 'PENDIENTE', 'Primera visita', 1, 1, NOW());

-- =====================================================
-- VERIFICACIÓN: CONSULTAS ÚTILES
-- =====================================================

-- Ver usuarios con roles:
-- SELECT u.name, u.email, GROUP_CONCAT(r.name) as roles
-- FROM usuarios u
-- LEFT JOIN user_roles ur ON u.id = ur.usuario_id
-- LEFT JOIN roles r ON ur.rol_id = r.id
-- GROUP BY u.id;

-- Ver roles con permisos:
-- SELECT r.name as rol, GROUP_CONCAT(p.name) as permisos
-- FROM roles r
-- LEFT JOIN role_permissions rp ON r.id = rp.rol_id
-- LEFT JOIN permisos p ON rp.permiso_id = p.id
-- GROUP BY r.id;

-- Ver profesionales con sus horarios:
-- SELECT p.nombre, p.usa_horario_negocio, 
--        COALESCE(hp.dia_semana, hn.dia_semana) as dia,
--        COALESCE(hp.hora_inicio, hn.hora_inicio) as inicio,
--        COALESCE(hp.hora_fin, hn.hora_fin) as fin
-- FROM profesionales p
-- LEFT JOIN horarios_profesionales hp ON p.id = hp.profesional_id
-- LEFT JOIN horarios_negocios hn ON p.negocio_id = hn.negocio_id;

-- Ver reservas del día:
-- SELECT r.fecha, r.hora_inicio, r.hora_fin,
--        c.nombre as cliente, p.nombre as profesional, s.nombre as servicio,
--        r.estado
-- FROM reservas r
-- INNER JOIN clientes c ON r.cliente_id = c.id
-- INNER JOIN profesionales p ON r.profesional_id = p.id
-- INNER JOIN servicios s ON r.servicio_id = s.id
-- WHERE r.fecha = CURDATE() AND r.reg_estado = 1
-- ORDER BY r.hora_inicio;

-- =====================================================
-- SCRIPT COMPLETADO
-- =====================================================
-- 
-- USUARIOS DE PRUEBA:
-- Email: admin@barberia.com | Password: password123 | Rol: ADMIN
-- Email: manager@barberia.com | Password: password123 | Rol: MANAGER
-- Email: barbero1@barberia.com | Password: password123 | Rol: BARBER
-- Email: user@barberia.com | Password: password123 | Rol: USER
--
-- Para probar disponibilidad:
-- - Negocio ID: 1
-- - Profesional ID: 1 (Juan Pérez - usa horario negocio)
-- - Profesional ID: 2 (Carlos Gómez - horario personalizado)
-- - Servicio ID: 1, 2, 3, 4, 5
-- - Fecha: Fecha actual (CURDATE())
-- =====================================================
