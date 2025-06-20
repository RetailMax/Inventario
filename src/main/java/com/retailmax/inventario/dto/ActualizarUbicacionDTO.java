package com.retailmax.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar la ubicación de un producto por SKU.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarUbicacionDTO {

    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    @NotBlank(message = "La nueva ubicación no puede estar vacía")
    private String nuevaUbicacion;
}
