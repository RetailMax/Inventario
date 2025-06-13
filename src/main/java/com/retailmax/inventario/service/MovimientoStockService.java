package com.retailmax.inventario.service;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoInventarioRepository productoInventarioRepository;

    public List<MovimientoStock> buscarPorSku(String sku) {
        return movimientoStockRepository.findBySku(sku);
    }

    // ✅ MÉTODO QUE FALTABA: para corregir los tests
    public MovimientoStock registrarMovimiento(MovimientoStock movimiento) {
        Optional<ProductoInventario> optProducto = productoInventarioRepository.findBySku(movimiento.getSku());

        if (optProducto.isEmpty()) {
            throw new IllegalArgumentException("Producto con SKU " + movimiento.getSku() + " no encontrado");
        }

        ProductoInventario producto = optProducto.get();

        // Ajustar stock según tipo de movimiento
        if (movimiento.getTipoMovimiento() == TipoMovimiento.ENTRADA) {
            producto.setCantidadDisponible(producto.getCantidadDisponible() + movimiento.getCantidadMovida());
        } else if (movimiento.getTipoMovimiento() == TipoMovimiento.SALIDA) {
            if (producto.getCantidadDisponible() < movimiento.getCantidadMovida()) {
                throw new IllegalArgumentException("Stock insuficiente para salida");
            }
            producto.setCantidadDisponible(producto.getCantidadDisponible() - movimiento.getCantidadMovida());
        }

        productoInventarioRepository.save(producto);

        movimiento.setProductoInventario(producto);
        movimiento.setStockFinalDespuesMovimiento(producto.getCantidadDisponible());

        if (movimiento.getFechaMovimiento() == null) {
            movimiento.setFechaMovimiento(LocalDateTime.now());
        }

        return movimientoStockRepository.save(movimiento);
    }
}
