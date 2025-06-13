package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.service.ProductoInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class MovimientoStockController {

    private final ProductoInventarioService productoInventarioService;

    @GetMapping("/movimientos/{sku}")
    public ResponseEntity<List<MovimientoStockDTO>> obtenerHistorialMovimientos(@PathVariable String sku) {
        List<MovimientoStockDTO> movimientos = productoInventarioService.obtenerHistorialMovimientos(sku, null, null);
        return ResponseEntity.ok(movimientos);
    }
}
