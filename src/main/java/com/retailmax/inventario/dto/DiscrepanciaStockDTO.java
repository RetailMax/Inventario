package com.retailmax.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // ⬅️ Esta es la clave
public class DiscrepanciaStockDTO {

    private String sku;
    private String descripcionProducto;
    private Integer stockSistema;
    private Integer stockFisico;
    private Integer diferencia;
    private String motivo;
}
