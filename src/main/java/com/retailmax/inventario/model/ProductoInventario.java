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

/*Clase base para empezar a mover productos dentro del MS Inventario */


public class ProductoInventario {

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
    private Integer stock; // stock actual

    @Column(nullable = false)
    private Integer cantidadReservada; //Reservera para stock que se va a comprar

    @Column(nullable = false)
    private Integer cantidadDisponible; // Stock disponible para venta
    
    @Column(nullable = false)
    private Integer cantidadEnTransito; // Movimiento de stock entre almacenes

    @Column(nullable = false)
    private Integer cantidadMinimaStock ; //Umbral para alertas de bajo stock

    @Column(length = 100)
    private String ubicacionAlmacen; // bodega o tienda (opcional)

    @Column(nullable = false)
    private LocalDateTime fechaUltimaActualizacion; // AUDITORÍA

    @Column(nullable = false)
    private LocalDateTime fechaCreacion; // AUDITORÍA

    @Column(nullable = false)
    private Boolean activo;

    // Nuevos campos para RF3: Gestión de Variaciones
    @Column(name = "producto_base_sku", length = 100) // SKU del producto "padre" o agrupador
    private String productoBaseSku;

    @Column(name = "talla", length = 50)
    private String talla; // Ejemplo: S, M, L, XL, 32, 34, etc.

    @Column(name = "color", length = 50)
    private String color; // Ejemplo: Azul, Rojo, Verde, etc.
}
