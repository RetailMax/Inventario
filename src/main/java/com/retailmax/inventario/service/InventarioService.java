package com.retailmax.inventario.service;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.Stock;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InventarioService {

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private MovimientoStockRepository movimientoRepo;

    public Stock registrarMovimiento(MovimientoStock movimiento) {
        Stock stock = stockRepo.findBySkuAndUbicacion(movimiento.getSku(), movimiento.getOrigen())
                .orElse(new Stock(null, movimiento.getSku(), movimiento.getOrigen(), 0));

        switch (movimiento.getTipo().toUpperCase()) {
            case "ENTRADA":
                stock.setCantidad(stock.getCantidad() + movimiento.getCantidad());
                break;
            case "SALIDA":
            case "RESERVA":
                if (stock.getCantidad() < movimiento.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para " + movimiento.getTipo());
                }
                stock.setCantidad(stock.getCantidad() - movimiento.getCantidad());
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento no reconocido");
        }

        movimiento.setFecha(LocalDateTime.now());
        movimientoRepo.save(movimiento);
        return stockRepo.save(stock);
    }

    public Stock consultarStock(String sku, String ubicacion) {
        return stockRepo.findBySkuAndUbicacion(sku, ubicacion)
                .orElseThrow(() -> new RuntimeException("No se encontró stock para " + sku + " en " + ubicacion));
    }
}
