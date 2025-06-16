package com.retailmax.inventario.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductoVariacionDTO {

    private Long id;
    private String sku;
    private String skuProductoBase;
    private String talla;
    private String color;
    private Integer cantidadDisponible;
    private String ubicacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;
}
