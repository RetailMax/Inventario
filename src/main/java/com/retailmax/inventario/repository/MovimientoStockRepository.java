package com.retailmax.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.retailmax.inventario.model.MovimientoStock;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    
    // Método necesario para corregir el error
    List<MovimientoStock> findBySku(String sku);
    // Método para buscar movimientos por ID de producto y rango de fechas, ordenados por fecha descendente
    List<MovimientoStock> findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(Long productoInventarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Método para buscar todos los movimientos de un producto, ordenados por fecha descendente
    List<MovimientoStock> findByProductoInventarioIdOrderByFechaMovimientoDesc(Long productoInventarioId);
}