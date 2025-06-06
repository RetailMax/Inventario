package com.retailmax.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; // Agregado para el constructor con todos los argumentos
import lombok.Data;
import lombok.NoArgsConstructor; // Agregado para el constructor sin argumentos

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ActualizarStockRequestDTO {

    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotBlank(message = "El tipo de actualización no puede ser nulo")
    private String tipoActualizacion; // Se mantiene como String para la entrada de la API

    private String referenciaExterna; // Por ejemplo, un ID de orden, factura, etc.

    private String motivo; // Un campo adicional para describir la razón del movimiento
}