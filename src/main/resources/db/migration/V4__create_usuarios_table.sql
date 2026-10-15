-- ===================================================================
-- V4__create_usuarios_table.sql
-- Creación de tabla de usuarios para autenticación
-- ===================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'ADMIN'
);

CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

INSERT INTO usuarios (username, password, nombre, rol)
VALUES ('Admin', '1234', 'Administrador', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

