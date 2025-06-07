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

    /**
     * RF1: Permite la adición de nuevos productos al inventario.
     * POST /api/inventario/productos
     */
    @PostMapping("/productos")
    public ResponseEntity<ProductoInventarioDTO> agregarProducto(
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    /**
     * RF2, RF4, RF5, RF10, RF16: Actualizar stock según tipo de movimiento.
     * PUT /api/inventario/stock
     */
    @PutMapping("/stock")
    public ResponseEntity<ProductoInventarioDTO> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO);
        return ResponseEntity.ok(updatedProducto);
    }

    /**
     * RF3: Consulta stock por SKU.
     * GET /api/inventario/stock/sku/{sku}
     */
    @GetMapping("/stock/sku/{sku}")
    public ResponseEntity<ProductoInventarioDTO> consultarStockPorSku(@PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarStockPorSku(sku);
        return ResponseEntity.ok(producto);
    }

    /**
     * RF3: Consulta general de stock (todos los productos).
     * GET /api/inventario/stock/todos
     */
    @GetMapping("/stock/todos")
    public ResponseEntity<List<ProductoInventarioDTO>> consultarTodoElStock() {
        List<ProductoInventarioDTO> stockList = productoInventarioService.consultarTodoElStock();
        return ResponseEntity.ok(stockList);
    }
}
