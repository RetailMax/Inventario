package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.EstadoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoInventarioRepository extends JpaRepository<ProductoInventario, Long> {

    Optional<ProductoInventario> findBySku(String sku);
    boolean existsBySku(String sku);
    List<ProductoInventario> findByCantidadDisponibleLessThan(Integer cantidad);
    List<ProductoInventario> findByCantidadDisponibleGreaterThan(Integer cantidad);
    List<ProductoInventario> findByUbicacionAlmacen(String ubicacionAlmacen);

    // ✅ Requerido por RF5:
    List<ProductoInventario> findByEstado(EstadoStock estado);
}
