package com.retailmax.inventario;


import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.service.MovimientoStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class MovimientoStockServiceUnitTest {

    @InjectMocks
    private MovimientoStockService movimientoStockService;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @Mock
    private ProductoInventarioRepository productoInventarioRepository;

    private ProductoInventario testProducto;

    @BeforeEach
    void setUp() {
        testProducto = crearProductoInventario("SKU001", 100, "Bodega A", 10);
    }

    @Test
    void testBuscarPorSku_Success() {
        String sku = "SKU001";
        MovimientoStock m1 = crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 20, "Compra");
        MovimientoStock m2 = crearMovimientoStock(testProducto, TipoMovimiento.SALIDA, 10, "Venta");

        when(movimientoStockRepository.findBySku(sku)).thenReturn(Arrays.asList(m1, m2));

        List<MovimientoStock> result = movimientoStockService.buscarPorSku(sku);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(movimientoStockRepository).findBySku(sku);
    }

    @Test
    void testObtenerHistorialMovimientos_ProductoNotFound() {
        when(productoInventarioRepository.findBySku("SKU404")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            movimientoStockService.obtenerHistorialMovimientos("SKU404", null, null);
        });

        verify(productoInventarioRepository).findBySku("SKU404");
    }

    @Test
    void testConsultarMovimientoPorId_NotFound() {
        when(movimientoStockRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            movimientoStockService.consultarMovimientoPorId(999L);
        });

        verify(movimientoStockRepository).findById(999L);
    }

    private ProductoInventario crearProductoInventario(String sku, Integer stock, String ubicacion, Integer min) {
        ProductoInventario p = new ProductoInventario();
        p.setId(1L);
        p.setSku(sku);
        p.setStock(stock);
        p.setCantidadDisponible(stock);
        p.setCantidadMinimaStock(min);
        p.setUbicacionAlmacen(ubicacion);
        p.setActivo(true);
        p.setFechaCreacion(LocalDateTime.now().minusDays(10));
        p.setFechaUltimaActualizacion(LocalDateTime.now());
        return p;
    }

    private MovimientoStock crearMovimientoStock(ProductoInventario producto, TipoMovimiento tipo, int cantidad, String ref) {
        MovimientoStock m = new MovimientoStock();
        m.setId(1L);
        m.setProductoInventario(producto);
        m.setSku(producto.getSku());
        m.setTipoMovimiento(tipo);
        m.setCantidadMovida(cantidad);
        m.setReferenciaExterna(ref);
        m.setMotivo(tipo.getDescripcion());
        m.setFechaMovimiento(LocalDateTime.now());
        m.setStockFinalDespuesMovimiento(producto.getCantidadDisponible());
        return m;
    }
}
