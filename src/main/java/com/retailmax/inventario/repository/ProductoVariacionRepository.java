package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.ProductoVariacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para manejar operaciones CRUD y consultas específicas
 * sobre la entidad ProductoVariacion.
 */
@Repository
public interface ProductoVariacionRepository extends JpaRepository<ProductoVariacion, Long> {

    // Buscar una variación por SKU exacto
    Optional<ProductoVariacion> findBySku(String sku);

    // Obtener todas las variaciones asociadas a un SKU de producto base
    List<ProductoVariacion> findBySkuProductoBase(String skuProductoBase);

    // Buscar variaciones por talla y color (útil para filtros)
    List<ProductoVariacion> findByTallaAndColor(String talla, String color);

    // Verificar existencia de una variación con un SKU dado
    boolean existsBySku(String sku);
}
