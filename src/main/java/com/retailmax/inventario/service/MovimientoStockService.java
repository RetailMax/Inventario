package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoInventarioRepository productoInventarioRepository; // Añadido

    @Transactional(readOnly = true)
    public List<MovimientoStock> buscarPorSku(String sku) {
        return movimientoStockRepository.findBySku(sku);
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> obtenerHistorialMovimientos(String sku, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado para obtener su historial de movimientos."));

        List<MovimientoStock> movimientos;
        if (fechaInicio != null && fechaFin != null) {
            movimientos = movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                productoInventario.getId(), fechaInicio, fechaFin
            );
        } else {
            movimientos = movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(productoInventario.getId());
        }

        return movimientos.stream()
                .map(this::mapToMovimientoStockDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovimientoStockDTO consultarMovimientoPorId(Long id) {
        MovimientoStock movimientoStock = movimientoStockRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Movimiento de stock con ID " + id + " no encontrado."));
        return mapToMovimientoStockDTO(movimientoStock);
    }

    private MovimientoStockDTO mapToMovimientoStockDTO(MovimientoStock movimientoStock) {
        return MovimientoStockDTO.builder()
                .id(movimientoStock.getId())
                .productoInventarioId(movimientoStock.getProductoInventario() != null ? movimientoStock.getProductoInventario().getId() : null)
                .sku(movimientoStock.getSku())
                .tipoMovimiento(movimientoStock.getTipoMovimiento().name())
                .cantidadMovida(movimientoStock.getCantidadMovida())
                .stockFinalDespuesMovimiento(movimientoStock.getStockFinalDespuesMovimiento())
                .referenciaExterna(movimientoStock.getReferenciaExterna())
                .motivo(movimientoStock.getMotivo())
                .fechaMovimiento(movimientoStock.getFechaMovimiento())
                .build();
    }
}