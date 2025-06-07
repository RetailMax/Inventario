package com.retailmax.inventario.service;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.Stock;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventarioService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    /**
     * Registra un movimiento de entrada o salida de stock.
     * @param movimiento MovimientoStock recibido desde el controller.
     * @return Stock actualizado.
     */
    public Stock registrarMovimiento(MovimientoStock movimiento) {
        Optional<Stock> optionalStock = stockRepository.findBySkuAndUbicacion(
                movimiento.getSku(), movimiento.getUbicacion());

        Stock stock;
        if (optionalStock.isPresent()) {
            stock = optionalStock.get();
            stock.setCantidad(stock.getCantidad() + movimiento.getCantidad());
        } else {
            stock = Stock.builder()
                    .sku(movimiento.getSku())
                    .ubicacion(movimiento.getUbicacion())
                    .cantidad(movimiento.getCantidad())
                    .build();
        }

        // Guardar el movimiento en el historial
        movimientoStockRepository.save(movimiento);
        return stockRepository.save(stock);
    }

    /**
     * Consulta la cantidad disponible de stock por SKU y ubicación.
     * @param sku SKU del producto.
     * @param ubicacion Ubicación en bodega.
     * @return Stock actual o null si no existe.
     */
    public Stock consultarStock(String sku, String ubicacion) {
        return stockRepository.findBySkuAndUbicacion(sku, ubicacion)
                .orElse(null);
    }
}
