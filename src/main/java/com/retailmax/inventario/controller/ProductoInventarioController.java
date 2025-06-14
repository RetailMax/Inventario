package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.*;
import com.retailmax.inventario.service.ProductoInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Gestión de productos en el inventario")
public class ProductoInventarioController {

    private final ProductoInventarioService productoInventarioService;

    @Operation(summary = "Agregar nuevo producto al inventario")
    @PostMapping("/productos")
    public ResponseEntity<ProductoInventarioDTO> agregarProducto(
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar stock de un producto")
    @PutMapping("/stock")
    public ResponseEntity<ProductoInventarioDTO> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO);
        return ResponseEntity.ok(updatedProducto);
    }

    @Operation(summary = "Actualizar ubicación del producto por SKU")
    @PutMapping("/ubicacion")
    public ResponseEntity<ProductoInventarioDTO> actualizarUbicacion(
            @Valid @RequestBody ActualizarUbicacionDTO requestDTO) {
        ProductoInventarioDTO actualizado = productoInventarioService.actualizarUbicacionPorSku(requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Consultar stock por SKU")
    @GetMapping("/stock/{sku}")
    public ResponseEntity<ProductoInventarioDTO> consultarStockPorSku(
            @Parameter(description = "SKU del producto a consultar") @PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarStockPorSku(sku);
        return ResponseEntity.ok(producto);
    }

    @Operation(summary = "Consultar el stock completo del inventario")
    @GetMapping("/stock")
    public ResponseEntity<List<ProductoInventarioDTO>> consultarTodoElStock() {
        List<ProductoInventarioDTO> stockList = productoInventarioService.consultarTodoElStock();
        return ResponseEntity.ok(stockList);
    }

    @Operation(summary = "Eliminar un producto del inventario por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto a eliminar") @PathVariable Long id) {
        productoInventarioService.eliminarProductoPorId(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar productos por ubicación en el almacén")
    @GetMapping("/ubicacion/{ubicacion}")
    public ResponseEntity<List<ProductoInventarioDTO>> consultarPorUbicacion(
            @Parameter(description = "Ubicación en el almacén") @PathVariable String ubicacion) {
        List<ProductoInventarioDTO> productos = productoInventarioService.buscarPorUbicacion(ubicacion);
        return ResponseEntity.ok(productos);
    }
}