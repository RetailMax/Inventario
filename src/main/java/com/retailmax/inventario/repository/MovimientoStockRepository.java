package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    
    // Método necesario para corregir el error
    List<MovimientoStock> findBySku(String sku);
}
