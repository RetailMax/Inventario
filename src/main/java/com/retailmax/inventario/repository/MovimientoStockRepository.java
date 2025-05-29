package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findBySku(String sku);
}
