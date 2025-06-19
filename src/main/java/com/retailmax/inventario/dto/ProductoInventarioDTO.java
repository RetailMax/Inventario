package com.retailmax.inventario.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder; // Usamos @Builder aquí para facilitar el mapeo desde la entidad
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Útil para construir el DTO en el servicio
public class ProductoInventarioDTO {

    private Long id;
    private String sku;
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
    private Integer cantidadTotal; // Suma de disponible y reservado
    private String ubicacionAlmacen;
    private Integer cantidadMinimaStock;
    // Añadir campos de auditoría si se van a exponer en el DTO
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;

    // Nuevos campos para RF3: Gestión de Variaciones
    private String productoBaseSku;
    private String talla;
    private String color;
}