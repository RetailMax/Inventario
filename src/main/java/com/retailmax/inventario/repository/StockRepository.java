package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySkuAndUbicacion(String sku, String ubicacion);
}
