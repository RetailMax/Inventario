package com.retailmax.inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor; // Añadir esta importación
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.retailmax.inventario.model.enums.TipoMovimiento; // Asegúrate de que este Enum esté definido correctamente
@Entity
@Table(name = "movimientos_stock")
@Data 
@NoArgsConstructor
@AllArgsConstructor // Genera un constructor con todos los argumentos (id, productoInventario, sku, etc.)


/*Clase para registrar el movimiento de stock en el MS */


public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_inventario_id", nullable = false)
    private ProductoInventario productoInventario;

    @Column(nullable = false)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipoMovimiento;

    // Aquí iría el Enum anidado si elegiste esa opción:
    /*
    public enum TipoMovimiento {
        // ... contenido del Enum ...
    }
    */

    @Column(nullable = false)
    private Integer cantidadMovida;

    private Integer stockFinalDespuesMovimiento;

    private String referenciaExterna;

    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    // ¡EL CONSTRUCTOR MANUAL HA SIDO ELIMINADO!
    // Ahora dependes de @NoArgsConstructor o @AllArgsConstructor.
    // La fechaMovimiento se establecerá en el servicio o usando el setter.
}
