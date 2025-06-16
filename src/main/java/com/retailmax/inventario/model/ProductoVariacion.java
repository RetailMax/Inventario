package com.retailmax.inventario.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una variación de producto (por talla, color, etc.).
 */
@Entity
@Table(name = "producto_variacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoVariacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String skuProductoBase;

    @Column(nullable = false)
    private String talla;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer stock;

    @Column
    private String descripcion;

    @Column
    private String ubicacion;

    @Column
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaUltimaActualizacion;
}
