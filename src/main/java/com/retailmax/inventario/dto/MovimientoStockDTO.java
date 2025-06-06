package com.retailmax.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; // Usamos @Builder aquí para facilitar el mapeo desde la entidad
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Útil para construir el DTO en el servicio
public class MovimientoStockDTO {

    private Long id;
    private Long productoInventarioId; // El ID de la entidad ProductoInventario relacionada
    private String sku;
    private String tipoMovimiento; // El nombre del Enum como String
    private Integer cantidadMovida;
    private Integer stockFinalDespuesMovimiento; // Stock disponible después de este movimiento
    private String referenciaExterna;
    private String motivo;
    private LocalDateTime fechaMovimiento;
}