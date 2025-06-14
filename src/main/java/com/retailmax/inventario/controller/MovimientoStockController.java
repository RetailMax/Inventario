package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.MovimientoStockDTO;
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

@RestController // Indicates that this class is a REST controller
@RequestMapping("/api/inventario") // Same base prefix to group under the inventory API
@Tag(name = "MovimientoStock", description = "Operations related to stock movement history")
@RequiredArgsConstructor // For dependency injection via constructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    /**
     * RF15: Allows querying the stock history of a product given its SKU.
     * GET /api/inventario/movimientos/{sku}
     * @param sku SKU of the product whose movement history is to be queried.
     * @return ResponseEntity with a list of MovimientoStockDTO and HTTP status 200 OK.
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
     * @param movimiento The MovimientoStock to register (from the request body).
     * @return ResponseEntity with the registered MovimientoStockDTO and HTTP status 201 Created.
     */
    @PostMapping("/movimientos") // <--- NEW ENDPOINT ADDED HERE
    @Operation(summary = "Register new stock movement",
               description = "Allows registering an inbound or outbound stock movement for an existing product.")
    public ResponseEntity<MovimientoStockDTO> registrarMovimiento(@Valid @RequestBody MovimientoStock movimiento) {
        MovimientoStockDTO nuevoMovimiento = movimientoStockService.registrarMovimiento(movimiento);
        return new ResponseEntity<>(nuevoMovimiento, HttpStatus.CREATED);
    }
}
