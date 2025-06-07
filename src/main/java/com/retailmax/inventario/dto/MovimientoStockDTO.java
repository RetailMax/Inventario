package com.retailmax.inventario.dto;

import com.retailmax.inventario.model.enums.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStockDTO {

    // Campos para respuesta
    private Long id;

    // ✅ Campo faltante, requerido en mapToMovimientoStockDTO
    private Long productoInventarioId;

    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    @NotBlank(message = "La ubicación no puede estar vacía")
    private String ubicacionAlmacen;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad movida es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidadMovida;

    private Integer stockFinalDespuesMovimiento;

    private String referenciaExterna;
    private String motivo;
    private String origen;

    // Solo para respuesta
    private LocalDateTime fechaMovimiento;
}
