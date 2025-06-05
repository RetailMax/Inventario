package com.retailmax.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.retailmax.inventario.model.Inventario;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

   // Usando JPQL
        @Query("SELECT p FROM Inventario p WHERE p.ubicacion = :ubicacion")
        List<Inventario> buscarPorUbicacion(@Param("ubicacion") String ubicacion);

        // Usando SQL nativo
        @Query(value = "SELECT * FROM inventarios WHERE ubicacion = :ubicacion", nativeQuery = true)
        Inventario buscarPorUbicacionNativo(@Param("ubicacion") String ubicacion);

        
        
        /*
        TODOS LOS MÉTODOS QUE HEREDA DE JpaRepository:
        findAll()	                Listar todos los inventarios
        findById(Long id)	        Buscar un inventario por ID
        save(Inventario inventario)	Guardar (crear o actualizar) un inventario
        deleteById(Long id)	        Eliminar un inventario por ID
        existsById(Long id)	        Saber si existe un inventario por ese ID
        count()	                    Saber cuántos registros hay

        */

}
