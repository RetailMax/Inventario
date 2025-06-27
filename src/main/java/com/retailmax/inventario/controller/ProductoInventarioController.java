package com.retailmax.inventario.controller;

import com.retailmax.inventario.assemblers.ProductoInventarioModelAssembler;
import com.retailmax.inventario.dto.*;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.service.ProductoInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/inventario/productos")
@Tag(name = "ProductoInventario", description = "Operaciones relacionadas con la gestión de productos de inventario")
@RequiredArgsConstructor
public class ProductoInventarioController {

    private final ProductoInventarioService productoInventarioService;
    private final ProductoInventarioModelAssembler assembler;

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Agregar un nuevo producto al inventario",
            description = "Permite agregar un nuevo producto al inventario con los detalles proporcionados.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> agregarProducto(
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        return ResponseEntity
                .created(linkTo(methodOn(ProductoInventarioController.class).consultarProductoPorSku(nuevoProducto.getSku())).toUri())
                .body(assembler.toModel(nuevoProducto));
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar todos los productos del inventario",
            description = "Permite consultar todos los productos actualmente registrados en el inventario.")
    public ResponseEntity<CollectionModel<EntityModel<ProductoInventarioDTO>>> consultarTodosLosProductos() {
        List<EntityModel<ProductoInventarioDTO>> productos = productoInventarioService.consultarTodosLosProductos().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(productos,
                        linkTo(methodOn(ProductoInventarioController.class).consultarTodosLosProductos()).withSelfRel()));
    }

    @GetMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar un producto por SKU",
            description = "Permite consultar un producto específico del inventario utilizando su SKU.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> consultarProductoPorSku(@PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarProductoPorSku(sku);
        return ResponseEntity.ok(assembler.toModel(producto));
    }

    @PutMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Actualizar un producto existente en el inventario",
            description = "Permite actualizar los detalles de un producto existente en el inventario utilizando su SKU.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> actualizarProducto(
            @PathVariable String sku,
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO productoActualizado = productoInventarioService.actualizarProducto(sku, requestDTO);
        return ResponseEntity.ok(assembler.toModel(productoActualizado));
    }

    @DeleteMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Eliminar un producto del inventario",
            description = "Permite eliminar un producto del inventario utilizando su SKU.")
    public ResponseEntity<?> eliminarProducto(@PathVariable String sku) {
        productoInventarioService.eliminarProducto(sku);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/stock", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Actualizar el stock de un producto",
            description = "Permite actualizar el stock de un producto específico en el inventario.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO);
        return ResponseEntity.ok(assembler.toModel(updatedProducto));
    }

    // 🔥 RF8 - Ajuste Manual de Stock
    @PostMapping(value = "/stock/ajuste-manual", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(
            summary = "Realizar un ajuste manual de stock",
            description = "Permite ajustar manualmente el stock de un producto, especificando si es una ENTRADA o SALIDA."
    )
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> ajustarStockManual(
            @Valid @RequestBody AjusteStockManualRequestDTO dto) {

        TipoMovimiento tipoMovimiento;
        try {
            tipoMovimiento = TipoMovimiento.valueOf(dto.getTipoMovimiento().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + dto.getTipoMovimiento());
        }

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO(
                dto.getSku(),
                dto.getCantidad(),
                tipoMovimiento.name(),
                "AJUSTE_MANUAL", // Usamos la referencia externa para identificar el origen
                dto.getMotivo()
        );

        ProductoInventarioDTO actualizado = productoInventarioService.actualizarStock(request);
        return ResponseEntity.ok(assembler.toModel(actualizado));
    }

    // ✅ RF10 - Reserva de Stock
    @PostMapping("/stock/reserva") // Este endpoint es de validación, no devuelve un recurso HATEOAS.
    @Operation(
            summary = "Validar disponibilidad de stock para reserva",
            description = "Verifica si hay suficiente stock disponible para un producto antes de realizar una reserva."
    )
    public ResponseEntity<String> validarDisponibilidadStock(
            @Valid @RequestBody ReservaStockRequestDTO requestDTO) {

        boolean disponible = productoInventarioService.validarDisponibilidad(
                requestDTO.getSku(), requestDTO.getCantidad());

        if (disponible) {
            return ResponseEntity.ok("Stock disponible para reserva");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Stock insuficiente");
        }
    }

    // ✅ RF11 - Liberación de Stock
    @PostMapping("/stock/liberar") // Este endpoint es de acción, no devuelve un recurso HATEOAS.
    @Operation(
            summary = "Liberar stock reservado",
            description = "Libera stock previamente reservado y lo devuelve al stock disponible."
    )
    public ResponseEntity<String> liberarStock(
            @Valid @RequestBody LiberarStockRequestDTO requestDTO) {

        productoInventarioService.liberarStockReservado(
                requestDTO.getSku(),
                requestDTO.getCantidadLiberar(),
                requestDTO.getMotivo()
        );

        return ResponseEntity.ok("Stock liberado exitosamente.");
    }

    @GetMapping(value = "/bajo-stock/{umbralCantidadMinima}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar productos con stock bajo",
            description = "Permite consultar productos cuyo stock está por debajo de un umbral específico.")
    public ResponseEntity<CollectionModel<EntityModel<ProductoInventarioDTO>>> verificarYNotificarStockBajo(@PathVariable Integer umbralCantidadMinima) {
        List<EntityModel<ProductoInventarioDTO>> productosBajoStock = productoInventarioService.verificarYNotificarStockBajo(umbralCantidadMinima).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(productosBajoStock,
                        linkTo(methodOn(ProductoInventarioController.class).verificarYNotificarStockBajo(umbralCantidadMinima)).withSelfRel()));
    }

    @GetMapping(value = "/exceso-stock/{umbralCantidadExcesiva}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar productos con stock excesivo",
            description = "Permite consultar productos cuyo stock está por encima de un umbral específico.")
    public ResponseEntity<CollectionModel<EntityModel<ProductoInventarioDTO>>> verificarYNotificarStockExcesivo(@PathVariable Integer umbralCantidadExcesiva) {
        List<EntityModel<ProductoInventarioDTO>> productosExcesoStock = productoInventarioService.verificarYNotificarStockExcesivo(umbralCantidadExcesiva).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(productosExcesoStock,
                        linkTo(methodOn(ProductoInventarioController.class).verificarYNotificarStockExcesivo(umbralCantidadExcesiva)).withSelfRel()));
    }
}
