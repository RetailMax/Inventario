package com.retailmax.inventario.dto;

public class ProductoCatalogoDTO {
    private String sku;
    private String nombre;
    private String categoria;

    public ProductoCatalogoDTO() {}

    public ProductoCatalogoDTO(String sku, String nombre, String categoria) {
        this.sku = sku;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
