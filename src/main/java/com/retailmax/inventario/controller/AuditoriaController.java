package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.DiscrepanciaStockDTO;
import com.retailmax.inventario.service.AuditoriaInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoría de Inventario", description = "Detección de diferencias entre stock físico y digital")
public class AuditoriaController {

    private final AuditoriaInventarioService auditoriaInventarioService;

    @PostMapping("/comparar")
    @Operation(
        summary = "Comparar stock físico con stock en sistema",
        description = "Recibe un mapa de SKU y cantidades físicas, y retorna discrepancias detectadas"
    )
    public ResponseEntity<List<DiscrepanciaStockDTO>> compararStockFisico(
            @Valid @RequestBody Map<String, Integer> stockFisico) {

        List<DiscrepanciaStockDTO> discrepancias = auditoriaInventarioService.compararConStockFisico(stockFisico);
        return ResponseEntity.ok(discrepancias);
    }
}
