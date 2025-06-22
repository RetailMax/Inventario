package com.retailmax.inventario.dto;

import com.retailmax.inventario.model.enums.EstadoStock;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarProductoInventarioRequestDTO {

    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    @NotNull(message = "La cantidad inicial no puede ser nula")
    @Min(value = 0, message = "La cantidad inicial no puede ser negativa")
    private Integer cantidadInicial;

    @NotBlank(message = "La ubicación de almacén no puede estar vacía")
    private String ubicacionAlmacen;

    @Min(value = 0, message = "La cantidad mínima de stock no puede ser negativa")
    private Integer cantidadMinimaStock;

    // Nuevos campos para RF3: Gestión de Variaciones
    @NotBlank(message = "El SKU del producto base no puede estar vacío si es una variante")
    private String productoBaseSku;

    private String talla;

    private String color;

    // NUEVO CAMPO para RF5: Estado de Stock
    private EstadoStock estado;
}
