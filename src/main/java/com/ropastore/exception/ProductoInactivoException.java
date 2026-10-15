package com.ropastore.exception;

public class ProductoInactivoException extends RuntimeException {

    public ProductoInactivoException(String codigo) {
        super("El producto (" + codigo + ") está inactivo. No se permiten operaciones de inventario sobre artículos inactivos.");
    }
}
