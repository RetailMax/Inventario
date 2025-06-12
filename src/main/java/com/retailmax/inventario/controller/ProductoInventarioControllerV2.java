package com.retailmax.inventario.controller;

import com.retailmax.inventario.assemblers.ProductoInventarioModelAssembler;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.service.ProductoInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Controlador RESTful para la gestión de Productos de Inventario,
 * implementando una versión V2 con soporte HATEOAS.
 * Utiliza ProductoInventarioDTO y otros DTOs de request para la comunicación con el cliente.
 */
@RestController
@RequestMapping("/api/v2/inventario/productos")
@Tag(name = "ProductoInventario", description = "Operaciones relacionadas con la gestión de productos de inventario")
@RequiredArgsConstructor
public class ProductoInventarioControllerV2 {

    private final ProductoInventarioService productoInventarioService;
    private final ProductoInventarioModelAssembler assembler;

    /**
     * GET /api/v2/inventario/productos
     * Consulta todos los productos de inventario configurados con enlaces HATEOAS.
     *
     * @return CollectionModel de EntityModel de ProductoInventarioDTO
     */
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar todos los productos del inventario",
               description = "Permite consultar todos los productos actualmente registrados en el inventario.")
    public ResponseEntity<CollectionModel<EntityModel<ProductoInventarioDTO>>> getAllProductos() {
        List<EntityModel<ProductoInventarioDTO>> productos = productoInventarioService.consultarTodosLosProductos().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        // Retorna la colección de productos con un enlace a sí misma
        return ResponseEntity.ok(
                CollectionModel.of(productos,
                        linkTo(methodOn(ProductoInventarioControllerV2.class).getAllProductos()).withSelfRel()));
    }

    /**
     * GET /api/v2/inventario/productos/{sku}
     * Consulta un producto de inventario por SKU con enlaces HATEOAS.
     *
     * @param sku El SKU del producto a consultar.
     * @return EntityModel de ProductoInventarioDTO.
     */
    @GetMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar un producto por SKU",
               description = "Permite consultar un producto específico del inventario utilizando su SKU.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> getProductoBySku(@PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarProductoPorSku(sku);
        return ResponseEntity.ok(assembler.toModel(producto));
    }

    /**
     * POST /api/v2/inventario/productos
     * Agrega un nuevo producto al inventario, retornando el recurso creado con enlaces HATEOAS.
     *
     * @param requestDTO El DTO con los datos del producto a agregar.
     * @return ResponseEntity con el EntityModel del producto creado y el código de estado 201 Created.
     */
    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Agregar un nuevo producto al inventario",
               description = "Permite agregar un nuevo producto al inventario con los detalles proporcionados.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> agregarProducto(@Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        // Retorna 201 Created con el enlace a la ubicación del nuevo recurso
        return ResponseEntity
                .created(linkTo(methodOn(ProductoInventarioControllerV2.class).getProductoBySku(nuevoProducto.getSku())).toUri())
                .body(assembler.toModel(nuevoProducto));
    }

    /**
     * PUT /api/v2/inventario/productos/{sku}
     * Actualiza un producto de inventario existente por SKU, retornando el recurso actualizado con enlaces HATEOAS.
     *
     * @param sku El SKU del producto a actualizar.
     * @param requestDTO El DTO con los datos a actualizar.
     * @return ResponseEntity con el EntityModel del producto actualizado y el código de estado 200 OK.
     */
    @PutMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Actualizar un producto existente en el inventario",
               description = "Permite actualizar los detalles de un producto existente en el inventario utilizando su SKU.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> actualizarProducto(
            @PathVariable String sku,
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) { // Usamos el mismo DTO de creación para actualización
        ProductoInventarioDTO productoActualizado = productoInventarioService.actualizarProducto(sku, requestDTO);
        return ResponseEntity.ok(assembler.toModel(productoActualizado));
    }

    /**
     * DELETE /api/v2/inventario/productos/{sku}
     * Elimina un producto de inventario por SKU, retornando un código de estado 204 No Content.
     *
     * @param sku El SKU del producto a eliminar.
     * @return ResponseEntity con el código de estado 204 No Content.
     */
    @DeleteMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Eliminar un producto del inventario",
               description = "Permite eliminar un producto del inventario utilizando su SKU.")
    public ResponseEntity<?> eliminarProducto(@PathVariable String sku) {
        productoInventarioService.eliminarProducto(sku);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v2/inventario/productos/stock
     * Actualiza el stock de un producto, retornando el recurso actualizado con enlaces HATEOAS.
     *
     * @param requestDTO El DTO con la información de actualización de stock.
     * @return ResponseEntity con el EntityModel del producto actualizado y el código de estado 200 OK.
     */
    @PutMapping(value = "/stock", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Actualizar el stock de un producto",
               description = "Permite actualizar el stock de un producto específico en el inventario.")
    public ResponseEntity<EntityModel<ProductoInventarioDTO>> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO);
        // Aquí no hay un SKU en la URL para el enlace 'self' de la colección,
        // pero podemos enlazar al producto individual afectado.
        return ResponseEntity.ok(assembler.toModel(updatedProducto));
    }

    /**
     * GET /api/v2/inventario/productos/bajo-stock/{umbralCantidadMinima}
     * Consulta productos con stock por debajo de un umbral específico.
     *
     * @param umbralCantidadMinima El umbral de cantidad mínima.
     * @return CollectionModel de EntityModel de ProductoInventarioDTO.
     */
    @GetMapping(value = "/bajo-stock/{umbralCantidadMinima}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar productos con stock bajo",
               description = "Permite consultar productos cuyo stock está por debajo de un umbral específico.")
    public ResponseEntity<CollectionModel<EntityModel<ProductoInventarioDTO>>> verificarYNotificarStockBajo(@PathVariable Integer umbralCantidadMinima) {
        List<EntityModel<ProductoInventarioDTO>> productosBajoStock = productoInventarioService.verificarYNotificarStockBajo(umbralCantidadMinima).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(productosBajoStock,
                        linkTo(methodOn(ProductoInventarioControllerV2.class).verificarYNotificarStockBajo(umbralCantidadMinima)).withSelfRel()));
    }

    /**
     * GET /api/v2/inventario/productos/exceso-stock/{umbralCantidadExcesiva}
     * Consulta productos con stock por encima de un umbral específico.
     *
     * @param umbralCantidadExcesiva El umbral de cantidad excesiva.
     * @return CollectionModel de EntityModel de ProductoInventarioDTO.
     */
    @GetMapping(value = "/exceso-stock/{umbralCantidadExcesiva}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar productos con stock excesivo",
               description = "Permite consultar productos cuyo stock está por encima de un umbral específico.")
    public ResponseEntity<CollectionModel<EntityModel<ProductoInventarioDTO>>> verificarYNotificarStockExcesivo(@PathVariable Integer umbralCantidadExcesiva) {
        List<EntityModel<ProductoInventarioDTO>> productosExcesoStock = productoInventarioService.verificarYNotificarStockExcesivo(umbralCantidadExcesiva).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(productosExcesoStock,
                        linkTo(methodOn(ProductoInventarioControllerV2.class).verificarYNotificarStockExcesivo(umbralCantidadExcesiva)).withSelfRel()));
    }
}
