package com.retailmax.inventario.controller;

import com.retailmax.inventario.model.Stock;
import com.retailmax.inventario.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping
    public List<Stock> getAll() {
        return stockService.getAll();
    }

    @GetMapping("/{id}")
    public Stock getById(@PathVariable Long id) {
        return stockService.getById(id).orElseThrow(() -> new RuntimeException("Stock no encontrado"));
    }

    @PostMapping
    public Stock create(@RequestBody Stock stock) {
        return stockService.create(stock);
    }

    @PutMapping("/{id}")
    public Stock update(@PathVariable Long id, @RequestBody Stock stock) {
        return stockService.update(id, stock);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        stockService.delete(id);
    }
}
