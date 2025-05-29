package com.retailmax.inventario.controller;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.Stock;
import com.retailmax.inventario.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @PostMapping("/movimiento")
    public Stock registrar(@RequestBody MovimientoStock movimiento) {
        return inventarioService.registrarMovimiento(movimiento);
    }

    @GetMapping("/disponibilidad")
    public Stock consultar(@RequestParam String sku, @RequestParam String ubicacion) {
        return inventarioService.consultarStock(sku, ubicacion);
    }
}
