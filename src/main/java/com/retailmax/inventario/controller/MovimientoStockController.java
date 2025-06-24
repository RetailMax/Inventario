package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.service.MovimientoStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "MovimientoStock", description = "Operations related to stock movement history")
@RequiredArgsConstructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    /**
     * RF15: Allows querying the stock history of a product given its SKU.
     * GET /api/inventario/movimientos/{sku}
     */
    @GetMapping("/movimientos/{sku}")
    @Operation(summary = "Get stock movement history by SKU",
            description = "Queries the stock movement history for a product given its SKU.")
    public ResponseEntity<List<MovimientoStockDTO>> obtenerHistorialMovimientos(@PathVariable String sku) {
        List<MovimientoStockDTO> movimientos = movimientoStockService.obtenerHistorialMovimientos(sku, null, null);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * POST /api/inventario/movimientos
     * Allows registering a new stock movement.
     */
    @PostMapping("/movimientos")
    @Operation(summary = "Register new stock movement",
            description = "Allows registering an inbound or outbound stock movement for an existing product.")
    public ResponseEntity<?> registrarMovimiento(@Valid @RequestBody MovimientoStock movimiento) {
        try {
            MovimientoStockDTO nuevoMovimiento = movimientoStockService.registrarMovimiento(movimiento);
            return new ResponseEntity<>(nuevoMovimiento, HttpStatus.CREATED);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto no encontrado: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al registrar el movimiento: " + e.getMessage());
        }
    }
}
