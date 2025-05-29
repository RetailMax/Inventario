package com.retailmax.inventario.service;

import com.retailmax.inventario.model.Stock;
import com.retailmax.inventario.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    public List<Stock> getAll() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getById(Long id) {
        return stockRepository.findById(id);
    }

    public Stock create(Stock stock) {
        return stockRepository.save(stock);
    }

    public Stock update(Long id, Stock newStock) {
        return stockRepository.findById(id)
            .map(existing -> {
                existing.setSku(newStock.getSku());
                existing.setUbicacion(newStock.getUbicacion());
                existing.setCantidad(newStock.getCantidad());
                return stockRepository.save(existing);
            })
            .orElseThrow(() -> new RuntimeException("Stock no encontrado con ID: " + id));
    }

    public void delete(Long id) {
        stockRepository.deleteById(id);
    }
}
