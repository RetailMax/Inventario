package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.ProductoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Indica a Spring que esta interfaz es un componente de repositorio
public interface ProductoInventarioRepository extends JpaRepository<ProductoInventario, Long> {

    Optional<ProductoInventario> findBySku(String sku);
    boolean existsBySku(String sku);
    List<ProductoInventario> findByCantidadDisponibleLessThan(Integer cantidad);
    List<ProductoInventario> findByCantidadDisponibleGreaterThan(Integer cantidad);
    List<ProductoInventario> findByUbicacionAlmacen(String ubicacionAlmacen);

    // NOTA:
    // JpaRepository ya proporciona métodos como:
    // - save(S entity): Guarda una entidad en la base de datos (para crear o actualizar).
    // - findById(ID id): Busca una entidad por su ID (devuelve Optional).
    // - findAll(): Recupera todas las entidades.
    // - deleteById(ID id): Elimina una entidad por su ID.
    // - count(): Devuelve el número total de entidades.
    // ... y muchos más, incluyendo variantes para paginación y ordenamiento.
    // Por lo tanto, no es necesario declararlos explícitamente aquí a menos que
    // quieras sobreescribir su comportamiento predeterminado con una @Query personalizada.
}