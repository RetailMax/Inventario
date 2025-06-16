package com.retailmax.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearProductoVariacionRequestDTO {

    @NotBlank
    private String skuProductoBase;

    @NotBlank
    private String talla;

    @NotBlank
    private String color;

    @NotNull
    private Integer stock;

    private String descripcion;

    private String ubicacion;
}
