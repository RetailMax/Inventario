package com.retailmax.inventario.model.enums;


/*Clase para escoger el tipo de movimiento dentro de MS*/
public enum TipoMovimiento {
    ENTRADA("Entrada de Stock"),
    SALIDA("Salida de Stock"),
    AJUSTE("Ajuste de Inventario"),
    RESERVA("Reserva de Stock"),
    LIBERACION("Liberación de Stock Reservado"),
    DEVOLUCION_CLIENTE("Devolución de Cliente"),
    DEVOLUCION_PROVEEDOR("Devolución a Proveedor"),
    AJUSTE_NEGATIVO("Ajuste Negativo de Inventario");
    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Método estático para obtener una constante TipoMovimiento a partir de su descripción (insensible a mayúsculas/minúsculas).
     * @param descripcion La descripción del tipo de movimiento (ej. "Entrada de Stock").
     * @return La constante TipoMovimiento correspondiente.
     * @throws IllegalArgumentException si la descripción no coincide con ningún tipo de movimiento.
     */
    public static TipoMovimiento fromDescripcion(String descripcion) {
        for (TipoMovimiento tipo : TipoMovimiento.values()) {
            if (tipo.getDescripcion().equalsIgnoreCase(descripcion)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("No existe un TipoMovimiento con la descripción: " + descripcion);
    }

    /**
     * Método estático para obtener una constante TipoMovimiento a partir de su nombre (insensible a mayúsculas/minúsculas).
     * Esto es útil para convertir un String (ej. "ENTRADA") a la constante Enum.
     * @param name El nombre de la constante del Enum (ej. "ENTRADA").
     * @return La constante TipoMovimiento correspondiente.
     * @throws IllegalArgumentException si el nombre no coincide con ninguna constante del Enum.
     */
    public static TipoMovimiento fromName(String name) {
        return TipoMovimiento.valueOf(name.toUpperCase());
    }
}