package com.retailmax.inventario.controller;

import com.retailmax.inventario.assemblers.UmbralAlertaModelAssembler;
import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.service.UmbralAlertaService;
import com.retailmax.inventario.model.enums.TipoAlerta;
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
 * Controlador RESTful para la gestión de Umbrales de Alerta,
 * implementando una versión V2 con soporte HATEOAS.
 * Utiliza UmbralAlertaDTO para la comunicación con el cliente.
 */
@RestController
@RequestMapping("/api/v2/inventario/umbrales")
@RequiredArgsConstructor
public class UmbralAlertaControllerV2 {

    private final UmbralAlertaService umbralAlertaService;
    private final UmbralAlertaModelAssembler assembler;

    /**
     * GET /api/v2/inventario/umbrales
     * Consulta todos los umbrales de alerta configurados con enlaces HATEOAS.
     *
     * @return CollectionModel de EntityModel de UmbralAlertaDTO
     */
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<UmbralAlertaDTO>>> getAllUmbrales() {
        List<EntityModel<UmbralAlertaDTO>> umbrales = umbralAlertaService.consultarTodosLosUmbrales().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        // Retorna la colección de umbrales con un enlace a sí misma
        return ResponseEntity.ok(
                CollectionModel.of(umbrales,
                        linkTo(methodOn(UmbralAlertaControllerV2.class).getAllUmbrales()).withSelfRel()));
    }

    /**
     * GET /api/v2/inventario/umbrales/{sku}
     * Consulta un umbral de alerta por SKU con enlaces HATEOAS.
     *
     * @param sku El SKU del umbral a consultar.
     * @return EntityModel de UmbralAlertaDTO.
     */
    @GetMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<UmbralAlertaDTO>> getUmbralBySku(@PathVariable String sku) {
        UmbralAlertaDTO umbral = umbralAlertaService.consultarUmbralAlertaPorSku(sku);
        return ResponseEntity.ok(assembler.toModel(umbral));
    }

    /**
     * POST /api/v2/inventario/umbrales
     * Crea un nuevo umbral de alerta para un SKU, retornando el recurso creado con enlaces HATEOAS.
     *
     * @param requestDTO El DTO con los datos del umbral a crear.
     * @return ResponseEntity con el EntityModel del umbral creado y el código de estado 201 Created.
     */
    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<UmbralAlertaDTO>> createUmbral(@Valid @RequestBody UmbralAlertaDTO requestDTO) {
        UmbralAlertaDTO nuevoUmbral = umbralAlertaService.crearUmbralAlerta(requestDTO);
        // Retorna 201 Created con el enlace a la ubicación del nuevo recurso
        return ResponseEntity
                .created(linkTo(methodOn(UmbralAlertaControllerV2.class).getUmbralBySku(nuevoUmbral.getSku())).toUri())
                .body(assembler.toModel(nuevoUmbral));
    }

    /**
     * PUT /api/v2/inventario/umbrales/{sku}
     * Actualiza un umbral de alerta existente por SKU, retornando el recurso actualizado con enlaces HATEOAS.
     *
     * @param sku El SKU del umbral a actualizar.
     * @param requestDTO El DTO con los datos a actualizar.
     * @return ResponseEntity con el EntityModel del umbral actualizado y el código de estado 200 OK.
     */
    @PutMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<UmbralAlertaDTO>> updateUmbral(
            @PathVariable String sku,
            @Valid @RequestBody UmbralAlertaDTO requestDTO) {
        UmbralAlertaDTO updatedUmbral = umbralAlertaService.actualizarUmbralAlerta(sku, requestDTO);
        return ResponseEntity.ok(assembler.toModel(updatedUmbral));
    }

    /**
     * DELETE /api/v2/inventario/umbrales/{sku}
     * Elimina un umbral de alerta por SKU, retornando un código de estado 204 No Content.
     *
     * @param sku El SKU del umbral a eliminar.
     * @return ResponseEntity con el código de estado 204 No Content.
     */
    @DeleteMapping(value = "/{sku}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> deleteUmbral(@PathVariable String sku) {
        umbralAlertaService.eliminarUmbralAlerta(sku);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v2/inventario/umbrales/tipo/{tipoAlerta}
     * Consulta umbrales activos por tipo de alerta con enlaces HATEOAS.
     *
     * @param tipoAlerta El tipo de alerta (ej. BAJO_STOCK, EXCESO_STOCK).
     * @return CollectionModel de EntityModel de UmbralAlertaDTO.
     */
    @GetMapping(value = "/tipo/{tipoAlerta}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<UmbralAlertaDTO>>> getUmbralesActivosPorTipo(@PathVariable String tipoAlerta) {
        TipoAlerta alertType;
        try {
            alertType = TipoAlerta.fromName(tipoAlerta); // Convertir String del path a Enum
        } catch (IllegalArgumentException e) {
            // Manejo de error si el tipo de alerta es inválido
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<EntityModel<UmbralAlertaDTO>> umbrales = umbralAlertaService.consultarUmbralesActivosPorTipo(alertType).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        // Retorna la colección de umbrales filtrados con un enlace a sí misma
        return ResponseEntity.ok(
                CollectionModel.of(umbrales,
                        linkTo(methodOn(UmbralAlertaControllerV2.class).getUmbralesActivosPorTipo(tipoAlerta)).withSelfRel()));
    }
}
