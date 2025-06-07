package com.retailmax.inventario.repository;

import com.retailmax.inventario.model.UmbralAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Importa Optional si vas a usarlo para métodos de búsqueda

@Repository // Anotación opcional, pero buena práctica para indicar que es un componente de repositorio
public interface UmbralAlertaRepository extends JpaRepository<UmbralAlerta, Long> {

    // Método para encontrar un UmbralAlerta por su SKU
    // Spring Data JPA generará automáticamente la implementación de este método.
    // Usamos Optional<UmbralAlerta> para manejar casos donde el SKU no exista.
    Optional<UmbralAlerta> findBySku(String sku);
}