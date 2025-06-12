package com.retailmax.inventario.model.enums;

public enum TipoAlerta {
    BAJO_STOCK("Bajo Stock"),
    EXCESO_STOCK("Exceso de Stock"),
    SIN_MOVIMIENTO("Sin Movimiento");

    private final String descripcion;

    TipoAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoAlerta fromDescripcion(String descripcion) {
        for (TipoAlerta tipo : TipoAlerta.values()) {
            if (tipo.getDescripcion().equalsIgnoreCase(descripcion)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("No existe un TipoAlerta con la descripción: " + descripcion);
    }

    public static TipoAlerta fromName(String name) {
        return TipoAlerta.valueOf(name.toUpperCase());
    }
}