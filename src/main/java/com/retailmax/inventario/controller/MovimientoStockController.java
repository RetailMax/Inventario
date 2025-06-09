package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.service.ProductoInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/inventario") // Mismo prefijo base para agrupar bajo la API de inventario
@RequiredArgsConstructor // Para inyección de dependencias vía constructor
public class MovimientoStockController {

    private final ProductoInventarioService productoInventarioService; // Inyección del servicio

    /**
     * RF15: Permite la consulta del historial de stock de un producto dado su SKU.
     * GET /api/inventario/movimientos/{sku}
     * @param sku SKU del producto cuyo historial de movimientos se desea consultar.
     * @return ResponseEntity con una lista de MovimientoStockDTO y estado HTTP 200 OK.
     */
    @GetMapping("/movimientos/{sku}")
    public ResponseEntity<List<MovimientoStockDTO>> obtenerHistorialMovimientos(@PathVariable String sku) {
        List<MovimientoStockDTO> movimientos = productoInventarioService.obtenerHistorialMovimientos(sku, null, null);
        return ResponseEntity.ok(movimientos);
    }

    // Podrías añadir más endpoints aquí si hubiera otras operaciones específicas de MovimientoStock
    // que no encajen en InventarioController (ej. consultar movimientos por rango de fechas, por tipo, etc.).
}