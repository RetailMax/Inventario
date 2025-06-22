package com.retailmax.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AjusteStockManualRequestDTO {

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotBlank(message = "El tipo de ajuste es obligatorio (ENTRADA o SALIDA)")
    private String tipoMovimiento;

    private String motivo;

    // Getters
    public String getSku() {
        return sku;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public String getMotivo() {
        return motivo;
    }

    // Setters
    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
