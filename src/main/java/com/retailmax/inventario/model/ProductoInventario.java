package com.retailmax.inventario.model;

import java.time.LocalDateTime;

import com.retailmax.inventario.model.enums.EstadoStock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer cantidadReservada;

    @Column(nullable = false)
    private Integer cantidadDisponible;

    @Column(nullable = false)
    private Integer cantidadEnTransito;

    @Column(nullable = false)
    private Integer cantidadMinimaStock;

    @Column(length = 100)
    private String ubicacionAlmacen;

    @Column(nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "producto_base_sku", length = 100)
    private String productoBaseSku;

    @Column(name = "talla", length = 50)
    private String talla;

    @Column(name = "color", length = 50)
    private String color;

    // NUEVO CAMPO PARA RF5
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoStock estado = EstadoStock.DISPONIBLE;
}
