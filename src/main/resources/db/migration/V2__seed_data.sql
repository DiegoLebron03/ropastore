-- ===================================================================
-- V2__seed_data.sql
-- Datos iniciales realistas para RopaStore
-- ===================================================================

-- 1. Insertar Productos
INSERT INTO productos (codigo, descripcion, costo, precio, estado, categoria, marca, talla, color, stock_minimo, stock_actual, created_at)
VALUES 
('CAM-001', 'Camisa Oversize Básica', 850.00, 1450.00, 'ACTIVO', 'CAMISAS', 'Zara Studio', 'M', 'Blanco Ópalo', 10, 30, NOW() - INTERVAL '15 days'),
('JEA-002', 'Jean Slim Fit Denim', 1200.00, 2100.00, 'ACTIVO', 'JEANS', 'Levi''s', 'L', 'Azul Índigo', 8, 35, NOW() - INTERVAL '14 days'),
('CHA-003', 'Chaqueta Denim Clásica', 1800.00, 3200.00, 'ACTIVO', 'CHAQUETAS', 'Pull & Bear', 'L', 'Negro Deslavado', 5, 3, NOW() - INTERVAL '12 days'),
('GOR-004', 'Gorra Classic Vintage', 350.00, 650.00, 'ACTIVO', 'GORRAS', 'New Era', 'UNICA', 'Verde Bosque', 12, 25, NOW() - INTERVAL '10 days'),
('ZAP-005', 'Sneakers Urban Casual', 2200.00, 3900.00, 'ACTIVO', 'ZAPATOS', 'Adidas Neo', 'M', 'Blanco / Verde', 6, 4, NOW() - INTERVAL '8 days');

-- 2. Insertar Histórico de Transacciones
-- CAM-001 (Inicial: +50 compra, -20 venta = 30)
INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '14 days', 'COMPRA', 50, 850.00, 50, 'Lote inicial de apertura de tienda', NOW() - INTERVAL '14 days'
FROM productos WHERE codigo = 'CAM-001';

INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '5 days', 'VENTA', 20, 850.00, 30, 'Ventas en tienda física primera quincena', NOW() - INTERVAL '5 days'
FROM productos WHERE codigo = 'CAM-001';

-- JEA-002 (Inicial: +40 compra, -15 venta, +10 ajuste = 35)
INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '13 days', 'COMPRA', 40, 1200.00, 40, 'Recepción proveedor Denim Corp', NOW() - INTERVAL '13 days'
FROM productos WHERE codigo = 'JEA-002';

INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '6 days', 'VENTA', 15, 1200.00, 25, 'Ventas fin de semana', NOW() - INTERVAL '6 days'
FROM productos WHERE codigo = 'JEA-002';

INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '2 days', 'AJUSTE_POSITIVO', 10, 1200.00, 35, 'Conteo físico de auditoría interna', NOW() - INTERVAL '2 days'
FROM productos WHERE codigo = 'JEA-002';

-- CHA-003 (Inicial: +15 compra, -12 venta = 3 -> Bajo stock)
INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '11 days', 'COMPRA', 15, 1800.00, 15, 'Colección Otoño', NOW() - INTERVAL '11 days'
FROM productos WHERE codigo = 'CHA-003';

INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '3 days', 'VENTA', 12, 1800.00, 3, 'Liquidación promocional', NOW() - INTERVAL '3 days'
FROM productos WHERE codigo = 'CHA-003';

-- GOR-004 (Inicial: +60 compra, -35 venta = 25)
INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '9 days', 'COMPRA', 60, 350.00, 60, 'Surtido de accesorios', NOW() - INTERVAL '9 days'
FROM productos WHERE codigo = 'GOR-004';

INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '1 days', 'VENTA', 35, 350.00, 25, 'Venta masiva fin de mes', NOW() - INTERVAL '1 days'
FROM productos WHERE codigo = 'GOR-004';

-- ZAP-005 (Inicial: +20 compra, -16 venta = 4 -> Bajo stock)
INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '7 days', 'COMPRA', 20, 2200.00, 20, 'Lote calzado deportivo', NOW() - INTERVAL '7 days'
FROM productos WHERE codigo = 'ZAP-005';

INSERT INTO transacciones (producto_id, fecha, tipo, cantidad, costo_unitario, balance_resultante, observacion, created_at)
SELECT id, NOW() - INTERVAL '1 days', 'VENTA', 16, 2200.00, 4, 'Venta especial calzado', NOW() - INTERVAL '1 days'
FROM productos WHERE codigo = 'ZAP-005';
