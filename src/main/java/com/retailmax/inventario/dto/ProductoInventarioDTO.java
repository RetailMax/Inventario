package com.retailmax.inventario.dto;

import java.time.LocalDateTime;

import com.retailmax.inventario.model.enums.EstadoStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoInventarioDTO {

    private Long id;
    private String sku;
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
    private Integer cantidadTotal;
    private String ubicacionAlmacen;
    private Integer cantidadMinimaStock;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;

    // Nuevos campos para RF3
    private String productoBaseSku;
    private String talla;
    private String color;

    // NUEVO campo para RF5
    private EstadoStock estado;
}
