package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.AjusteStockManualRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.service.ProductoInventarioService;
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
@Tag(name = "ProductoInventario", description = "Operaciones relacionadas con la gestión de productos de inventario")
@RequiredArgsConstructor
public class ProductoInventarioController {

    private final ProductoInventarioService productoInventarioService;

    @PostMapping("/productos")
    @Operation(summary = "Agregar un nuevo producto al inventario",
            description = "Permite agregar un nuevo producto al inventario con los detalles proporcionados.")
    public ResponseEntity<ProductoInventarioDTO> agregarProducto(
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @GetMapping("/productos")
    @Operation(summary = "Consultar todos los productos del inventario",
            description = "Permite consultar todos los productos actualmente registrados en el inventario.")
    public ResponseEntity<List<ProductoInventarioDTO>> consultarTodosLosProductos() {
        List<ProductoInventarioDTO> productosList = productoInventarioService.consultarTodosLosProductos();
        return ResponseEntity.ok(productosList);
    }

    @GetMapping("/productos/{sku}")
    @Operation(summary = "Consultar un producto por SKU",
            description = "Permite consultar un producto específico del inventario utilizando su SKU.")
    public ResponseEntity<ProductoInventarioDTO> consultarProductoPorSku(@PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarProductoPorSku(sku);
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/productos/{sku}")
    @Operation(summary = "Actualizar un producto existente en el inventario",
            description = "Permite actualizar los detalles de un producto existente en el inventario utilizando su SKU.")
    public ResponseEntity<ProductoInventarioDTO> actualizarProducto(
            @PathVariable String sku,
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO productoActualizado = productoInventarioService.actualizarProducto(sku, requestDTO);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/productos/{sku}")
    @Operation(summary = "Eliminar un producto del inventario",
            description = "Permite eliminar un producto del inventario utilizando su SKU.")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String sku) {
        productoInventarioService.eliminarProducto(sku);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stock")
    @Operation(summary = "Actualizar el stock de un producto",
            description = "Permite actualizar el stock de un producto específico en el inventario.")
    public ResponseEntity<ProductoInventarioDTO> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO);
        return ResponseEntity.ok(updatedProducto);
    }

    // 🔥 RF8 - Ajuste Manual de Stock
    @PostMapping("/stock/ajuste-manual")
    @Operation(
            summary = "Realizar un ajuste manual de stock",
            description = "Permite ajustar manualmente el stock de un producto, especificando si es una ENTRADA o SALIDA."
    )
    public ResponseEntity<ProductoInventarioDTO> ajustarStockManual(
            @Valid @RequestBody AjusteStockManualRequestDTO dto) {

        TipoMovimiento tipoMovimiento;
        try {
            tipoMovimiento = TipoMovimiento.valueOf(dto.getTipoMovimiento().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Tipo inválido
        }

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO(
                dto.getSku(),
                dto.getCantidad(),
                tipoMovimiento.name(),
                "AJUSTE_MANUAL",
                null,
                dto.getMotivo()
        );

        ProductoInventarioDTO actualizado = productoInventarioService.actualizarStock(request);
        return ResponseEntity.ok(actualizado);
    }
}
