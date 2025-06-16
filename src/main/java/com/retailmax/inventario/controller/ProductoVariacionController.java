package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.CrearProductoVariacionRequestDTO;
import com.retailmax.inventario.dto.ProductoVariacionDTO;
import com.retailmax.inventario.service.ProductoVariacionService;
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
@RequestMapping("/api/variaciones")
@RequiredArgsConstructor
@Tag(name = "Variaciones de Producto", description = "Gestión de variantes como talla, color, presentación")
public class ProductoVariacionController {

    private final ProductoVariacionService variacionService;

    @Operation(summary = "Registrar una nueva variación para un producto")
    @PostMapping
    public ResponseEntity<ProductoVariacionDTO> registrarVariacion(
            @Valid @RequestBody CrearProductoVariacionRequestDTO requestDTO) {
        ProductoVariacionDTO creada = variacionService.registrarVariacion(requestDTO);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar una variación de producto por SKU")
    @GetMapping("/{sku}")
    public ResponseEntity<ProductoVariacionDTO> obtenerVariacionPorSku(
            @Parameter(description = "SKU de la variación") @PathVariable String sku) {
        ProductoVariacionDTO dto = variacionService.obtenerPorSku(sku);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Consultar todas las variaciones asociadas a un producto base")
    @GetMapping("/producto/{skuProductoBase}")
    public ResponseEntity<List<ProductoVariacionDTO>> obtenerVariacionesPorProductoBase(
            @Parameter(description = "SKU del producto base") @PathVariable String skuProductoBase) {
        List<ProductoVariacionDTO> lista = variacionService.obtenerPorProductoBase(skuProductoBase);
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Eliminar una variación de producto por SKU")
    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> eliminarVariacion(
            @Parameter(description = "SKU de la variación a eliminar") @PathVariable String sku) {
        ProductoVariacionDTO dto = variacionService.obtenerPorSku(sku); // obtener ID
        variacionService.eliminarVariacion(dto.getId()); // eliminar por ID
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar stock de una variación")
    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductoVariacionDTO> actualizarStock(
            @Parameter(description = "ID de la variación") @PathVariable Long id,
            @Parameter(description = "Cantidad a ajustar (positiva o negativa)") @RequestParam int cantidad) {
        ProductoVariacionDTO actualizada = variacionService.ajustarStock(id, cantidad);
        return ResponseEntity.ok(actualizada);
    }
}
