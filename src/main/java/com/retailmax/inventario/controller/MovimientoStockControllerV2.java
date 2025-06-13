package com.retailmax.inventario.controller;

import com.retailmax.inventario.assemblers.MovimientoStockModelAssembler;
import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.service.MovimientoStockService; // Ahora usando MovimientoStockService
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Controlador RESTful para la gestión de Movimientos de Stock,
 * implementando una versión V2 con soporte HATEOAS.
 * Utiliza MovimientoStockDTO para la comunicación con el cliente.
 */
@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/v2/inventario/movimientos") // Prefijo para todas las rutas en este controlador
@Tag(name = "MovimientoStock", description = "Operaciones relacionadas con el historial de movimientos de stock")
@RequiredArgsConstructor // Genera un constructor con los campos 'final' para inyección
public class MovimientoStockControllerV2 {

    private final MovimientoStockService movimientoStockService; // Inyección del servicio
    private final MovimientoStockModelAssembler assembler;

    /**
     * RF15: Permite la consulta del historial de stock de un producto dado su SKU.
     * GET /api/v2/inventario/movimientos/{sku}
     *
     * @param sku SKU del producto cuyo historial de movimientos se desea consultar.
     * @return ResponseEntity con una CollectionModel de EntityModel de MovimientoStockDTO y estado HTTP 200 OK.
     */
    
    @GetMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener historial de movimientos de stock por SKU",
               description = "Consulta el historial de movimientos de stock para un producto dado su SKU.")
     public ResponseEntity<CollectionModel<EntityModel<MovimientoStockDTO>>> obtenerHistorialMovimientos(@PathVariable String sku) {
        List<EntityModel<MovimientoStockDTO>> movimientos = movimientoStockService.obtenerHistorialMovimientos(sku, null, null).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        // Retorna la colección de movimientos con un enlace a sí misma
        return ResponseEntity.ok(
                CollectionModel.of(movimientos,
                        linkTo(methodOn(MovimientoStockControllerV2.class).obtenerHistorialMovimientos(sku)).withSelfRel()));
    }

    /**
     * GET /api/v2/inventario/movimientos/id/{id}
     * Consulta un movimiento de stock individual por su ID con enlaces HATEOAS.
     * Este endpoint se añade para soportar el enlace 'self' en los EntityModel de MovimientoStockDTO.
     *
     * @param id El ID del movimiento de stock a consultar.
     * @return EntityModel de MovimientoStockDTO.
     */
    @GetMapping(value = "/id/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar un movimiento de stock por ID",
               description = "Permite consultar un movimiento de stock específico utilizando su ID.")
    public ResponseEntity<EntityModel<MovimientoStockDTO>> getMovimientoById(@PathVariable Long id) {
        MovimientoStockDTO movimiento = movimientoStockService.consultarMovimientoPorId(id);
        return ResponseEntity.ok(assembler.toModel(movimiento));
    }

    // Se podrían añadir más endpoints para filtrar por fechas, tipo de movimiento, etc.,
    // si las funcionalidades lo requieren, siguiendo el mismo patrón HATEOAS.

/**
     * GET /api/v2/inventario/movimientos
     * Proporciona un punto de entrada raíz para los recursos de movimientos.
     * Puede devolver una lista vacía o enlaces a otras operaciones,
     * ya que obtener todos los movimientos de todos los productos podría ser
     * intensivo en recursos sin paginación y filtrado adecuados.
     */
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar movimientos de stock",
               description = "Proporciona un punto de entrada raíz para los recursos de movimientos de stock.")
    public ResponseEntity<CollectionModel<EntityModel<MovimientoStockDTO>>> getMovimientosRoot() {
        // Devuelve una lista vacía con un enlace 'self'.
        // Esto indica que el recurso existe pero puede requerir consultas más específicas.
        List<EntityModel<MovimientoStockDTO>> emptyList = Collections.emptyList();
        CollectionModel<EntityModel<MovimientoStockDTO>> collectionModel = CollectionModel.of(emptyList,
                linkTo(methodOn(MovimientoStockControllerV2.class).getMovimientosRoot()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

}
