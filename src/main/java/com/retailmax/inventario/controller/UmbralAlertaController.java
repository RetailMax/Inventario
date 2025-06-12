package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.UmbralAlertaDTO; // Ahora usamos solo este DTO
import com.retailmax.inventario.service.UmbralAlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.retailmax.inventario.model.enums.TipoAlerta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario/umbrales")
@Tag(name = "UmbralAlerta", description = "Operaciones relacionadas con la gestión de umbrales de alerta para productos de inventario")
@RequiredArgsConstructor
public class UmbralAlertaController {

    private final UmbralAlertaService umbralAlertaService;

    /**
     * POST /api/inventario/umbrales
     * Crea un nuevo umbral de alerta para un SKU.
     */
    @PostMapping
    @Operation(summary = "Crear umbral de alerta",
               description = "Permite crear un nuevo umbral de alerta para un producto dado su SKU.")
    public ResponseEntity<UmbralAlertaDTO> crearUmbralAlerta(
            @Valid @RequestBody UmbralAlertaDTO requestDTO) { // Usamos el DTO unificado
        UmbralAlertaDTO nuevoUmbral = umbralAlertaService.crearUmbralAlerta(requestDTO);
        return new ResponseEntity<>(nuevoUmbral, HttpStatus.CREATED);
    }

    /**
     * PUT /api/inventario/umbrales/{sku}
     * Actualiza un umbral de alerta existente por SKU.
     */
    @PutMapping("/{sku}")
    @Operation(summary = "Actualizar umbral de alerta por SKU",
               description = "Permite actualizar un umbral de alerta existente para un producto dado su SKU.")
    public ResponseEntity<UmbralAlertaDTO> actualizarUmbralAlerta(
            @PathVariable String sku,
            @Valid @RequestBody UmbralAlertaDTO requestDTO) { // Usamos el DTO unificado
        // Nota: @Valid en PUT es importante para validar el SKU y cualquier otro campo que tenga anotaciones de validación.
        // Pero para campos opcionales, la validación se moverá al servicio o se asume su flexibilidad.
        UmbralAlertaDTO updatedUmbral = umbralAlertaService.actualizarUmbralAlerta(sku, requestDTO);
        return ResponseEntity.ok(updatedUmbral);
    }

    /**
     * GET /api/inventario/umbrales/{sku}
     * Consulta un umbral de alerta por SKU.
     */
    @GetMapping("/{sku}")
    @Operation(summary = "Consultar umbral de alerta por SKU",
               description = "Permite consultar un umbral de alerta configurado para un producto dado su SKU.")
    public ResponseEntity<UmbralAlertaDTO> consultarUmbralAlertaPorSku(@PathVariable String sku) {
        UmbralAlertaDTO umbral = umbralAlertaService.consultarUmbralAlertaPorSku(sku);
        return ResponseEntity.ok(umbral);
    }

    /**
     * GET /api/inventario/umbrales
     * Consulta todos los umbrales de alerta configurados.
     */
    @GetMapping
    @Operation (summary = "Consultar todos los umbrales de alerta",
               description = "Permite consultar todos los umbrales de alerta configurados en el sistema.")
    public ResponseEntity<List<UmbralAlertaDTO>> consultarTodosLosUmbrales() {
        List<UmbralAlertaDTO> umbrales = umbralAlertaService.consultarTodosLosUmbrales();
        return ResponseEntity.ok(umbrales);
    }

    /**
     * GET /api/inventario/umbrales/tipo/{tipoAlerta}
     * Consulta umbrales activos por tipo de alerta.
     * Ejemplo: /api/inventario/umbrales/tipo/BAJO_STOCK
     */
    @GetMapping("/tipo/{tipoAlerta}")
    @Operation(summary = "Consultar umbrales activos por tipo de alerta",
               description = "Permite consultar los umbrales de alerta activos filtrados por tipo de alerta.")
    public ResponseEntity<List<UmbralAlertaDTO>> consultarUmbralesActivosPorTipo(@PathVariable String tipoAlerta) {
        TipoAlerta alertType;
        try {
            alertType = TipoAlerta.fromName(tipoAlerta); // Convertir String del path a Enum
        } catch (IllegalArgumentException e) {
            // Manejo de error si el tipo de alerta es inválido
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<UmbralAlertaDTO> umbrales = umbralAlertaService.consultarUmbralesActivosPorTipo(alertType);
        return ResponseEntity.ok(umbrales);
    }


    /**
     * DELETE /api/inventario/umbrales/{sku}
     * Elimina un umbral de alerta por SKU.
     */
    @DeleteMapping("/{sku}")
    @Operation(summary = "Eliminar umbral de alerta por SKU",
               description = "Permite eliminar un umbral de alerta configurado para un producto dado su SKU.")
    public ResponseEntity<Void> eliminarUmbralAlerta(@PathVariable String sku) {
        umbralAlertaService.eliminarUmbralAlerta(sku);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}