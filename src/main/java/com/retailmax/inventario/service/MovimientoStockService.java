package com.retailmax.inventario.service;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;

    public List<MovimientoStock> buscarPorSku(String sku) {
        return movimientoStockRepository.findBySku(sku);
    }
}
