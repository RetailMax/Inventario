package com.retailmax.inventario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.exception.StockInsuficienteException;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;

import lombok.RequiredArgsConstructor;


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

    @Transactional
    public MovimientoStockDTO registrarMovimiento(MovimientoStock movimientoInput) {
        if (movimientoInput.getSku() == null || movimientoInput.getSku().isBlank()) {
            throw new IllegalArgumentException("El SKU es obligatorio para registrar un movimiento.");
        }
        if (movimientoInput.getCantidadMovida() == null || movimientoInput.getCantidadMovida() <= 0) {
            throw new IllegalArgumentException("La cantidad movida debe ser un entero positivo.");
        }
        if (movimientoInput.getTipoMovimiento() == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio.");
        }

       ProductoInventario producto = productoInventarioRepository.findBySku(movimientoInput.getSku())
        .orElseThrow(() -> new IllegalArgumentException("Producto con SKU " + movimientoInput.getSku() + " no existe."));

        int cantidadActual = producto.getCantidadDisponible();
        int cantidadMovida = movimientoInput.getCantidadMovida();
        TipoMovimiento tipo = movimientoInput.getTipoMovimiento();

        switch (tipo) {
            case ENTRADA:
                producto.setCantidadDisponible(cantidadActual + cantidadMovida);
                break;
            case SALIDA:
                if (cantidadActual < cantidadMovida) {
                    throw new StockInsuficienteException("No hay suficiente stock para el SKU " + producto.getSku() + ". Disponible: " + cantidadActual + ", Solicitado: " + cantidadMovida);
                }
                producto.setCantidadDisponible(cantidadActual - cantidadMovida);
                break;
            default:
                // Otros tipos de movimiento como AJUSTE, RESERVA, etc., podrían necesitar lógica más específica
                // o no ser manejados por este endpoint genérico de "registrarMovimiento".
                // Por ahora, solo soportamos ENTRADA y SALIDA explícitamente aquí.
                throw new IllegalArgumentException("Tipo de movimiento '" + tipo + "' no soportado directamente por esta operación. Considere usar un endpoint específico si es necesario.");
        }
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        productoInventarioRepository.save(producto);

        MovimientoStock nuevoRegistroMovimiento = new MovimientoStock();
        nuevoRegistroMovimiento.setProductoInventario(producto);
        nuevoRegistroMovimiento.setSku(producto.getSku());
        nuevoRegistroMovimiento.setTipoMovimiento(tipo);
        nuevoRegistroMovimiento.setCantidadMovida(cantidadMovida);
        nuevoRegistroMovimiento.setStockFinalDespuesMovimiento(producto.getCantidadDisponible());
        nuevoRegistroMovimiento.setReferenciaExterna(movimientoInput.getReferenciaExterna());
        nuevoRegistroMovimiento.setMotivo(movimientoInput.getMotivo() != null ? movimientoInput.getMotivo() : tipo.getDescripcion());
        nuevoRegistroMovimiento.setFechaMovimiento(movimientoInput.getFechaMovimiento() != null ? movimientoInput.getFechaMovimiento() : LocalDateTime.now());

        MovimientoStock savedMovimiento = movimientoStockRepository.save(nuevoRegistroMovimiento);
        return mapToMovimientoStockDTO(savedMovimiento);
    }

    public MovimientoStockDTO mapToMovimientoStockDTO(MovimientoStock movimientoStock) {
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