package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.ProductoCatalogoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;

@Service
public class CatalogoProductoService {

    public List<ProductoCatalogoDTO> obtenerCatalogo() {
        // Simulación de integración con servicio externo
        return Arrays.asList(
            new ProductoCatalogoDTO("SKU123", "Producto de Prueba", "Categoria A"),
            new ProductoCatalogoDTO("SKU456", "Otro Producto", "Categoria B")
        );
    }
}
