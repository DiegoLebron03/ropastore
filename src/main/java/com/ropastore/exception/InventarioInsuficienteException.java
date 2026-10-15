package com.ropastore.exception;

public class InventarioInsuficienteException extends RuntimeException {

    private final String codigoProducto;
    private final int stockDisponible;
    private final int cantidadSolicitada;

    public InventarioInsuficienteException(String codigoProducto, int stockDisponible, int cantidadSolicitada) {
        super("No hay suficiente inventario disponible para realizar esta operación.");
        this.codigoProducto = codigoProducto;
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public int getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}
