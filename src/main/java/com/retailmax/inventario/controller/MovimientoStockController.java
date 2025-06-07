package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.model.Stock;
import com.retailmax.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimientos") // Endpoint claro y sin colisión
@RequiredArgsConstructor
public class MovimientoStockController {

    private final InventarioService inventarioService;

    /**
     * RF6: Registra una entrada o salida de stock.
     * POST /api/movimientos
     */
    @PostMapping
    public ResponseEntity<Stock> registrarMovimiento(@Valid @RequestBody MovimientoStockDTO movimientoDTO) {
        Stock resultado = inventarioService.registrarMovimientoDesdeDTO(movimientoDTO);
        return ResponseEntity.ok(resultado);
    }

    /**
     * RF7: Consulta la cantidad de stock disponible por SKU y ubicación.
     * GET /api/movimientos/consulta?sku=SKU123&ubicacion=B1
     */
    @GetMapping("/consulta")
    public ResponseEntity<Stock> consultarStock(
            @RequestParam String sku,
            @RequestParam String ubicacion) {

        Stock stock = inventarioService.consultarStock(sku, ubicacion);
        return ResponseEntity.ok(stock);
    }
}
