package com.retailmax.inventario.controller;

import com.retailmax.inventario.assemblers.MovimientoStockModelAssembler;
import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.service.MovimientoStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/inventario/movimientos")
@Tag(name = "MovimientoStock", description = "Operaciones relacionadas con el historial de movimientos de stock")
@RequiredArgsConstructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;
    private final MovimientoStockModelAssembler assembler;

    /**
     * RF15: Permite la consulta del historial de stock de un producto dado su SKU.
     * GET /api/inventario/movimientos/{sku}
     */
    @GetMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener historial de movimientos de stock por SKU",
            description = "Consulta el historial de movimientos de stock para un producto dado su SKU.")
    public ResponseEntity<CollectionModel<EntityModel<MovimientoStockDTO>>> obtenerHistorialMovimientos(@PathVariable String sku) {
        List<EntityModel<MovimientoStockDTO>> movimientos = movimientoStockService.obtenerHistorialMovimientos(sku, null, null).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(movimientos,
                        linkTo(methodOn(MovimientoStockController.class).obtenerHistorialMovimientos(sku)).withSelfRel()));
    }

    /**
     * POST /api/inventario/movimientos
     * Permite registrar un nuevo movimiento de stock.
     */
    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Registrar nuevo movimiento de stock",
            description = "Permite registrar un movimiento de entrada o salida de stock para un producto existente.")
    public ResponseEntity<EntityModel<MovimientoStockDTO>> registrarMovimiento(@Valid @RequestBody MovimientoStock movimiento) {
        MovimientoStockDTO nuevoMovimiento = movimientoStockService.registrarMovimiento(movimiento);
        EntityModel<MovimientoStockDTO> entityModel = assembler.toModel(nuevoMovimiento);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping(value = "/id/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar un movimiento de stock por ID",
            description = "Permite consultar un movimiento de stock específico utilizando su ID.")
    public ResponseEntity<EntityModel<MovimientoStockDTO>> getMovimientoById(@PathVariable Long id) {
        MovimientoStockDTO movimiento = movimientoStockService.consultarMovimientoPorId(id);
        return ResponseEntity.ok(assembler.toModel(movimiento));
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Consultar movimientos de stock",
            description = "Proporciona un punto de entrada raíz para los recursos de movimientos de stock.")
    public ResponseEntity<CollectionModel<EntityModel<MovimientoStockDTO>>> getMovimientosRoot() {
        List<EntityModel<MovimientoStockDTO>> emptyList = Collections.emptyList();
        CollectionModel<EntityModel<MovimientoStockDTO>> collectionModel = CollectionModel.of(emptyList,
                linkTo(methodOn(MovimientoStockController.class).getMovimientosRoot()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }
}
