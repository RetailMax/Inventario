package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.service.MovimientoStockService; // Cambiado a MovimientoStockService
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/inventario") // Mismo prefijo base para agrupar bajo la API de inventario
@Tag(name = "MovimientoStock", description = "Operaciones relacionadas con el historial de movimientos de stock")
@RequiredArgsConstructor // Para inyección de dependencias vía constructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService; // Cambiado a MovimientoStockService

    /**
     * RF15: Permite la consulta del historial de stock de un producto dado su SKU.
     * GET /api/inventario/movimientos/{sku}
     * @param sku SKU del producto cuyo historial de movimientos se desea consultar.
     * @return ResponseEntity con una lista de MovimientoStockDTO y estado HTTP 200 OK.
     */
    @GetMapping("/movimientos/{sku}")
    @Operation(summary = "Obtener historial de movimientos de stock por SKU",
               description = "Consulta el historial de movimientos de stock para un producto dado su SKU.")
    public ResponseEntity<List<MovimientoStockDTO>> obtenerHistorialMovimientos(@PathVariable String sku) {
        List<MovimientoStockDTO> movimientos = movimientoStockService.obtenerHistorialMovimientos(sku, null, null); // Usando movimientoStockService
        return ResponseEntity.ok(movimientos);
    }

    // Podrías añadir más endpoints aquí si hubiera otras operaciones específicas de MovimientoStock
    // que no encajen en InventarioController (ej. consultar movimientos por rango de fechas, por tipo, etc.).
}