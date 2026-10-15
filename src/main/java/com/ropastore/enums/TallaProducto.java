package com.ropastore.enums;

public enum TallaProducto {
    XS("Extra Pequeña (XS)"),
    S("Pequeña (S)"),
    M("Mediana (M)"),
    L("Grande (L)"),
    XL("Extra Grande (XL)"),
    XXL("Doble Extra Grande (XXL)"),
    UNICA("Talla Única");

    private final String label;

    TallaProducto(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
