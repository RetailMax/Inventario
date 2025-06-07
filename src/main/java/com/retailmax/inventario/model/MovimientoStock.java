package com.retailmax.inventario.model;

import com.retailmax.inventario.model.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;

    private Integer cantidadMovida;

    private Integer stockFinalDespuesMovimiento;

    private String referenciaExterna;

    private String motivo;

    private String origen;

    private String ubicacion; // <--- Esto faltaba y debe existir

    private LocalDateTime fechaMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_inventario_id")
    private ProductoInventario productoInventario;

    // Método auxiliar para el builder, por claridad
    public static MovimientoStockBuilder builder() {
        return new MovimientoStockBuilder();
    }

    public static class MovimientoStockBuilder {
        private String sku;
        private TipoMovimiento tipoMovimiento;
        private Integer cantidadMovida;
        private Integer stockFinalDespuesMovimiento;
        private String referenciaExterna;
        private String motivo;
        private String origen;
        private String ubicacion;
        private LocalDateTime fechaMovimiento;
        private ProductoInventario productoInventario;

        public MovimientoStockBuilder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public MovimientoStockBuilder tipoMovimiento(TipoMovimiento tipoMovimiento) {
            this.tipoMovimiento = tipoMovimiento;
            return this;
        }

        public MovimientoStockBuilder cantidadMovida(Integer cantidadMovida) {
            this.cantidadMovida = cantidadMovida;
            return this;
        }

        public MovimientoStockBuilder stockFinalDespuesMovimiento(Integer cantidad) {
            this.stockFinalDespuesMovimiento = cantidad;
            return this;
        }

        public MovimientoStockBuilder referenciaExterna(String ref) {
            this.referenciaExterna = ref;
            return this;
        }

        public MovimientoStockBuilder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public MovimientoStockBuilder origen(String origen) {
            this.origen = origen;
            return this;
        }

        public MovimientoStockBuilder ubicacion(String ubicacion) {
            this.ubicacion = ubicacion;
            return this;
        }

        public MovimientoStockBuilder productoInventario(ProductoInventario productoInventario) {
            this.productoInventario = productoInventario;
            return this;
        }

        public MovimientoStock build() {
            MovimientoStock m = new MovimientoStock();
            m.setSku(this.sku);
            m.setTipoMovimiento(this.tipoMovimiento);
            m.setCantidadMovida(this.cantidadMovida);
            m.setStockFinalDespuesMovimiento(this.stockFinalDespuesMovimiento);
            m.setReferenciaExterna(this.referenciaExterna);
            m.setMotivo(this.motivo);
            m.setOrigen(this.origen);
            m.setUbicacion(this.ubicacion);
            m.setProductoInventario(this.productoInventario);
            m.setFechaMovimiento(LocalDateTime.now());
            return m;
        }
    }
}
