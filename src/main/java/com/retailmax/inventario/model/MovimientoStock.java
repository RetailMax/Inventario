package com.retailmax.inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.retailmax.inventario.model.enums.TipoMovimiento;

@Entity
@Table(name = "movimientos_stock")
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_inventario_id", nullable = false)
    private ProductoInventario productoInventario;

    @Column(nullable = false, length = 100)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidadMovida;

    @Column(nullable = false)
    private Integer stockFinalDespuesMovimiento;

    @Column(length = 50)
    private String referenciaExterna;

    @Column(length = 255)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;
}
