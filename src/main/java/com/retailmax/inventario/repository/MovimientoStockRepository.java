package com.retailmax.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retailmax.inventario.model.MovimientoStock;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findBySku(String sku);
    // Método para buscar movimientos por ID de producto y rango de fechas, ordenados por fecha descendente
    List<MovimientoStock> findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(Long productoInventarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Método para buscar todos los movimientos de un producto, ordenados por fecha descendente
    List<MovimientoStock> findByProductoInventarioIdOrderByFechaMovimientoDesc(Long productoInventarioId);
}