package com.retailmax.inventario.controller;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.service.MovimientoStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    @GetMapping("/{sku}")
    public List<MovimientoStock> obtenerPorSku(@PathVariable String sku) {
        return movimientoStockService.buscarPorSku(sku);
    }
}
