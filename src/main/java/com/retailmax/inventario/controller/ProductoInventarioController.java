package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.service.ProductoInventarioService;
import jakarta.validation.Valid; // Para validar los DTOs de entrada
import lombok.RequiredArgsConstructor; // Para inyección de dependencias vía constructor
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/inventario") // Prefijo para todas las rutas en este controlador
@RequiredArgsConstructor // Genera un constructor con los campos 'final' para inyección
public class ProductoInventarioController {

    private final ProductoInventarioService productoInventarioService; // Inyección del servicio

    @PostMapping("/productos")
    public ResponseEntity<ProductoInventarioDTO> agregarProducto(
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }
    @GetMapping("/productos")
    public ResponseEntity<List<ProductoInventarioDTO>> consultarTodosLosProductos() {
        List<ProductoInventarioDTO> productosList = productoInventarioService.consultarTodosLosProductos(); // Asumiendo que tienes este método en tu servicio
        return ResponseEntity.ok(productosList);
    }

    @GetMapping("/productos/{sku}")
    public ResponseEntity<ProductoInventarioDTO> consultarProductoPorSku(@PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarProductoPorSku(sku); // Necesitarás este método en tu servicio
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/productos/{sku}")
    public ResponseEntity<ProductoInventarioDTO> actualizarProducto(
            @PathVariable String sku,
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) { // Podrías usar el mismo DTO de creación o uno específico para actualización
        ProductoInventarioDTO productoActualizado = productoInventarioService.actualizarProducto(sku, requestDTO); // Necesitarás este método
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/productos/{sku}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String sku) {
        productoInventarioService.eliminarProducto(sku); // Necesitarás este método en tu servicio
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stock")
    public ResponseEntity<ProductoInventarioDTO> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO); // Necesitarás este método en tu servicio
        return ResponseEntity.ok(updatedProducto);
    }


}