package com.ropastore.exception;

public class ProductoDuplicadoException extends RuntimeException {

    private final String codigo;

    public ProductoDuplicadoException(String codigo) {
        super("Ya existe un producto registrado con el código: " + codigo);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
