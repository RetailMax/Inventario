package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.DiscrepanciaStockDTO;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditoriaInventarioService {

    private final ProductoInventarioRepository productoInventarioRepository;

    public List<DiscrepanciaStockDTO> compararConStockFisico(Map<String, Integer> stockFisico) {
        List<DiscrepanciaStockDTO> discrepancias = new ArrayList<>();

        for (Map.Entry<String, Integer> entrada : stockFisico.entrySet()) {
            String sku = entrada.getKey();
            Integer stockFisicoValor = entrada.getValue();

            ProductoInventario producto = productoInventarioRepository.findBySku(sku).orElse(null);

            if (producto != null) {
                int stockSistema = producto.getCantidadDisponible();
                if (!stockSistemaEquals(stockSistema, stockFisicoValor)) {
                    discrepancias.add(DiscrepanciaStockDTO.builder()
                            .sku(sku)
                            .descripcionProducto(producto.getProductoBaseSku())
                            .stockSistema(stockSistema)
                            .stockFisico(stockFisicoValor)
                            .diferencia(stockFisicoValor - stockSistema)
                            .motivo("Diferencia entre stock físico y sistema")
                            .build());
                }
            } else {
                discrepancias.add(DiscrepanciaStockDTO.builder()
                        .sku(sku)
                        .descripcionProducto("Producto no encontrado en sistema")
                        .stockSistema(0)
                        .stockFisico(stockFisicoValor)
                        .diferencia(stockFisicoValor)
                        .motivo("Producto no registrado en sistema")
                        .build());
            }
        }

        return discrepancias;
    }

    private boolean stockSistemaEquals(Integer sistema, Integer fisico) {
        return sistema != null && sistema.equals(fisico);
    }
}
