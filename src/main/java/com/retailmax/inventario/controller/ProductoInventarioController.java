package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.service.ProductoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class ProductoInventarioController {

    private final ProductoInventarioService productoInventarioService;

    @PostMapping("/productos")
    public ResponseEntity<ProductoInventarioDTO> agregarProducto(
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @PutMapping("/stock")
    public ResponseEntity<ProductoInventarioDTO> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO);
        return ResponseEntity.ok(updatedProducto);
    }

    @GetMapping("/stock/{sku}")
    public ResponseEntity<ProductoInventarioDTO> consultarStockPorSku(@PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarStockPorSku(sku);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/stock")
    public ResponseEntity<List<ProductoInventarioDTO>> consultarTodoElStock() {
        List<ProductoInventarioDTO> stockList = productoInventarioService.consultarTodoElStock();
        return ResponseEntity.ok(stockList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoInventarioService.eliminarProductoPorId(id);
        return ResponseEntity.noContent().build();
    }
}
