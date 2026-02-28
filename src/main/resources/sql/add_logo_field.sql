-- =====================================================
-- MIGRACIÓN: Agregar campo logo_url a tabla negocios
-- =====================================================
-- Fecha: 2026-02-16
-- Descripción: Añade soporte para almacenar la URL del logo del negocio
-- =====================================================

USE barberia_db;

-- Agregar columna logo_url si no existe
ALTER TABLE negocios 
ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500) NULL
COMMENT 'URL o ruta del logo del negocio (local o cloud)';

-- Verificar la columna
DESCRIBE negocios;

-- Consulta para ver los negocios con sus logos
SELECT id, nombre, logo_url, color_principal, estado 
FROM negocios;
