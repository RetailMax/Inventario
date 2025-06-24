package com.retailmax.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para validar si se puede reservar stock de un producto específico.
 */
@Data
public class ReservaStockRequestDTO {

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
}
