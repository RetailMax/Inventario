package com.retailmax.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiberarStockRequestDTO {

    @NotBlank(message = "El SKU no puede estar vacío.")
    private String sku;

    @NotNull(message = "La cantidad a liberar es obligatoria.")
    @Min(value = 1, message = "Debe liberar al menos una unidad.")
    private Integer cantidadLiberar;

    private String motivo;  // Opcional
}
