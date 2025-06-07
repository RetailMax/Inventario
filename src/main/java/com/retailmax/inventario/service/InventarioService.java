package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.Stock;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoInventarioService productoInventarioService;
    private final StockRepository stockRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    public Stock registrarMovimientoDesdeDTO(MovimientoStockDTO dto) {
        ProductoInventario producto = productoInventarioService.obtenerProductoPorSku(dto.getSku());

        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado con SKU: " + dto.getSku());
        }

        MovimientoStock movimiento = MovimientoStock.builder()
                .sku(dto.getSku())
                .tipoMovimiento(dto.getTipoMovimiento())
                .cantidadMovida(dto.getCantidadMovida())
                .referenciaExterna(dto.getReferenciaExterna())
                .motivo(dto.getMotivo())
                .origen(dto.getOrigen())
                .ubicacion(dto.getUbicacionAlmacen())
                .productoInventario(producto)
                .fechaMovimiento(LocalDateTime.now())
                .build();

        return registrarMovimiento(movimiento);
    }

    public Stock registrarMovimiento(MovimientoStock movimiento) {
        String sku = movimiento.getSku();
        String ubicacion = movimiento.getUbicacion();

        Stock stock = stockRepository.findBySkuAndUbicacion(sku, ubicacion)
                .map(s -> {
                    s.setCantidad(s.getCantidad() + movimiento.getCantidadMovida());
                    return s;
                })
                .orElse(Stock.builder()
                        .sku(sku)
                        .ubicacion(ubicacion)
                        .cantidad(movimiento.getCantidadMovida())
                        .build());

        movimiento.setStockFinalDespuesMovimiento(stock.getCantidad());
        movimientoStockRepository.save(movimiento);
        return stockRepository.save(stock);
    }

    public Stock consultarStock(String sku, String ubicacion) {
        return stockRepository.findBySkuAndUbicacion(sku, ubicacion)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado para SKU: " + sku + " en ubicación: " + ubicacion));
    }
}
