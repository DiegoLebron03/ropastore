package com.ropastore.enums;

public enum CategoriaProducto {
    CAMISAS("Camisas"),
    PANTALONES("Pantalones"),
    JEANS("Jeans"),
    CHAQUETAS("Chaquetas"),
    ZAPATOS("Calzado / Sneakers"),
    GORRAS("Gorras & Accesorios"),
    ACCESORIOS("Accesorios");

    private final String nombre;

    CategoriaProducto(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
