package com.retailmax.inventario; // ✅ ruta correcta para test

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.service.MovimientoStockService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MovimientoStockServiceTest {

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @Mock
    private ProductoInventarioRepository productoInventarioRepository;

    @InjectMocks
    private MovimientoStockService movimientoStockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegistrarMovimientoEntrada() {
        ProductoInventario producto = new ProductoInventario();
        producto.setId(1L);
        producto.setCantidadDisponible(10);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setSku("SKU123");
        movimiento.setCantidadMovida(5); // ⚠️ Usa `setCantidadMovida`, no `setCantidad`
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);

        when(productoInventarioRepository.findBySku("SKU123")).thenReturn(Optional.of(producto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenReturn(producto);
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenReturn(movimiento);

        MovimientoStock result = movimientoStockService.registrarMovimiento(movimiento);

        assertNotNull(result);
        assertEquals(TipoMovimiento.ENTRADA, result.getTipoMovimiento());
        verify(productoInventarioRepository, times(1)).save(producto);
    }

    @Test
    void testRegistrarMovimientoProductoNoExiste() {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setSku("INEXISTENTE");
        movimiento.setCantidadMovida(5);
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);

        when(productoInventarioRepository.findBySku("INEXISTENTE")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            movimientoStockService.registrarMovimiento(movimiento);
        });

        assertEquals("Producto con SKU INEXISTENTE no encontrado", exception.getMessage());
    }
}
