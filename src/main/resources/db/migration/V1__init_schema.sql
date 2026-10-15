-- ===================================================================
-- V1__init_schema.sql
-- Creación de tablas de Productos y Transacciones de RopaStore
-- ===================================================================

CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    costo NUMERIC(12, 2) NOT NULL CHECK (costo >= 0),
    precio NUMERIC(12, 2) NOT NULL CHECK (precio >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    categoria VARCHAR(30) NOT NULL,
    marca VARCHAR(80),
    talla VARCHAR(20),
    color VARCHAR(50),
    stock_minimo INTEGER NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0),
    stock_actual INTEGER NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos(codigo);
CREATE INDEX IF NOT EXISTS idx_productos_estado ON productos(estado);
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria);

CREATE TABLE IF NOT EXISTS transacciones (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    fecha TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(12, 2) NOT NULL CHECK (costo_unitario >= 0),
    balance_resultante INTEGER NOT NULL CHECK (balance_resultante >= 0),
    observacion VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transacciones_producto ON transacciones(producto_id);
CREATE INDEX IF NOT EXISTS idx_transacciones_fecha ON transacciones(fecha);
CREATE INDEX IF NOT EXISTS idx_transacciones_tipo ON transacciones(tipo);
