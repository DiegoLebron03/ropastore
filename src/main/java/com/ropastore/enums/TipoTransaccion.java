package com.ropastore.enums;

public enum TipoTransaccion {
    // Entradas
    COMPRA("Entrada por Compra", CategoriaMovimiento.ENTRADA),
    DEVOLUCION_CLIENTE("Devolución de Cliente", CategoriaMovimiento.ENTRADA),
    AJUSTE_POSITIVO("Ajuste de Inventario (+)", CategoriaMovimiento.ENTRADA),

    // Salidas
    VENTA("Salida por Venta", CategoriaMovimiento.SALIDA),
    DEVOLUCION_PROVEEDOR("Devolución a Proveedor", CategoriaMovimiento.SALIDA),
    PRODUCTO_DEFECTUOSO("Salida por Daño / Defecto", CategoriaMovimiento.SALIDA),
    AJUSTE_NEGATIVO("Ajuste de Inventario (-)", CategoriaMovimiento.SALIDA);

    public enum CategoriaMovimiento {
        ENTRADA,
        SALIDA
    }

    private final String descripcion;
    private final CategoriaMovimiento categoria;

    TipoTransaccion(String descripcion, CategoriaMovimiento categoria) {
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public CategoriaMovimiento getCategoria() {
        return categoria;
    }

    public boolean isEntrada() {
        return this.categoria == CategoriaMovimiento.ENTRADA;
    }

    public boolean isSalida() {
        return this.categoria == CategoriaMovimiento.SALIDA;
    }
}
