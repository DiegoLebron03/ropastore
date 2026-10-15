-- ===================================================================
-- V3__add_imagen_url_to_productos.sql
-- Adición de soporte para imágenes de productos en catálogo
-- ===================================================================

ALTER TABLE productos ADD COLUMN IF NOT EXISTS imagen_url VARCHAR(500);

-- Asignación de imágenes iniciales a productos semilla
UPDATE productos SET imagen_url = '/images/productos/camisa-001.jpg' WHERE codigo = 'CAM-001' AND imagen_url IS NULL;
UPDATE productos SET imagen_url = '/images/productos/jeans-002.jpg' WHERE codigo = 'JEA-002' AND imagen_url IS NULL;
UPDATE productos SET imagen_url = '/images/productos/chaqueta-003.jpg' WHERE codigo = 'CHA-003' AND imagen_url IS NULL;
UPDATE productos SET imagen_url = '/images/productos/polo-004.jpg' WHERE codigo = 'GOR-004' AND imagen_url IS NULL;
UPDATE productos SET imagen_url = '/images/productos/accesorios-005.jpg' WHERE codigo = 'ZAP-005' AND imagen_url IS NULL;
