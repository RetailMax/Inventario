package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Producto findByCodigo(String codigo);
}
