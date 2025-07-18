package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.ProductoCatalogoDTO;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CatalogoProductoServiceTest {

    @Test
    public void testObtenerCatalogo() {
        CatalogoProductoService service = new CatalogoProductoService();
        List<ProductoCatalogoDTO> productos = service.obtenerCatalogo();
        assertFalse(productos.isEmpty());
    }
}
