package com.retailmax.inventario;

import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.service.ProductoInventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoInventarioServiceTest {

    @Mock
    private ProductoInventarioRepository productoInventarioRepository;

    @InjectMocks
    private ProductoInventarioService productoInventarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuscarPorSkuExistente() {
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("ABC123");

        when(productoInventarioRepository.findBySku("ABC123"))
                .thenReturn(Optional.of(producto));

        Optional<ProductoInventario> result = productoInventarioService.buscarPorSku("ABC123");

        assertTrue(result.isPresent());
        assertEquals("ABC123", result.get().getSku());
    }

    @Test
    void testBuscarPorSkuNoExistente() {
        when(productoInventarioRepository.findBySku("XYZ999"))
                .thenReturn(Optional.empty());

        Optional<ProductoInventario> result = productoInventarioService.buscarPorSku("XYZ999");

        assertFalse(result.isPresent());
    }
}
