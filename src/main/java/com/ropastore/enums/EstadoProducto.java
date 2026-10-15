package com.ropastore.enums;

public enum EstadoProducto {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoProducto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
