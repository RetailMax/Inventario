package com.retailmax.inventario.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "inventarios")
@Entity
public class Inventario {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //DEFINICIÓN DE @Columna
    /* @Column: se usan para mapear los atributos de la clase Java a columnas
     de una tabla en la base de datos. Sirven para especificar detalles sobre 
     cómo se debe almacenar cada campo en la base de datos.*/

    @Column(nullable = false, length = 100)
    private String sku; // SKU único por variante de producto

    @Column(nullable = false)
    private Integer cantidadDisponible; // stock actual

    @Column(length = 100)
    private String ubicacion; // bodega o tienda (opcional)

    @Column(nullable = false)
    private LocalDateTime fechaUltimaActualizacion; // auditoría

    @Column(nullable = false)
    private Boolean activo;
}