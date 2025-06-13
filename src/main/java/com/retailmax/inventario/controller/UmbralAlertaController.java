package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.model.enums.TipoAlerta;
import com.retailmax.inventario.service.UmbralAlertaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario/umbrales")
@RequiredArgsConstructor
public class UmbralAlertaController {

    private final UmbralAlertaService umbralAlertaService;

    @PostMapping
    public ResponseEntity<UmbralAlertaDTO> crearUmbralAlerta(
            @Valid @RequestBody UmbralAlertaDTO requestDTO) {
        UmbralAlertaDTO nuevoUmbral = umbralAlertaService.crearUmbralAlerta(requestDTO);
        return new ResponseEntity<>(nuevoUmbral, HttpStatus.CREATED);
    }

    @PutMapping("/{sku}")
    public ResponseEntity<UmbralAlertaDTO> actualizarUmbralAlerta(
            @PathVariable String sku,
            @Valid @RequestBody UmbralAlertaDTO requestDTO) {
        UmbralAlertaDTO updated = umbralAlertaService.actualizarUmbralAlerta(sku, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<UmbralAlertaDTO> consultarUmbralAlertaPorSku(@PathVariable String sku) {
        UmbralAlertaDTO umbral = umbralAlertaService.consultarUmbralAlertaPorSku(sku);
        return ResponseEntity.ok(umbral);
    }

    @GetMapping
    public ResponseEntity<List<UmbralAlertaDTO>> consultarTodosLosUmbrales() {
        List<UmbralAlertaDTO> lista = umbralAlertaService.consultarTodosLosUmbrales();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/tipo/{tipoAlerta}")
    public ResponseEntity<List<UmbralAlertaDTO>> consultarPorTipo(@PathVariable String tipoAlerta) {
        try {
            TipoAlerta tipo = TipoAlerta.fromName(tipoAlerta);
            return ResponseEntity.ok(umbralAlertaService.consultarUmbralesActivosPorTipo(tipo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> eliminarUmbralAlerta(@PathVariable String sku) {
        umbralAlertaService.eliminarUmbralAlerta(sku);
        return ResponseEntity.noContent().build();
    }
}
