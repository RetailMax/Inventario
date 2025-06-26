package com.retailmax.inventario;


import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.exception.StockInsuficienteException;
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
    void setUp() { // This method ensures testProducto is reset for each test
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

    // Helper method to create a MovimientoStock for input
    private MovimientoStock crearMovimientoStockInput(String sku, Integer cantidad, TipoMovimiento tipo, String referencia, String motivo, LocalDateTime fecha) {
        MovimientoStock m = new MovimientoStock();
        m.setSku(sku);
        m.setCantidadMovida(cantidad);
        m.setTipoMovimiento(tipo);
        m.setReferenciaExterna(referencia);
        m.setMotivo(motivo);
        m.setFechaMovimiento(fecha);
        return m;
    }

    // --- Tests para registrarMovimiento para aumentar cobertura de JaCoCo ---

    @Test
    void registrarMovimiento_SkuNull_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput(null, 10, TipoMovimiento.ENTRADA, "REF1", "Motivo", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).findBySku(anyString());
    }

    @Test
    void registrarMovimiento_SkuBlank_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput(" ", 10, TipoMovimiento.ENTRADA, "REF1", "Motivo", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).findBySku(anyString());
    }

    @Test
    void registrarMovimiento_CantidadMovidaNull_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", null, TipoMovimiento.ENTRADA, "REF1", "Motivo", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).findBySku(anyString());
    }

    @Test
    void registrarMovimiento_CantidadMovidaZero_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 0, TipoMovimiento.ENTRADA, "REF1", "Motivo", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).findBySku(anyString());
    }

    @Test
    void registrarMovimiento_CantidadMovidaNegative_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", -5, TipoMovimiento.ENTRADA, "REF1", "Motivo", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).findBySku(anyString());
    }

    @Test
    void registrarMovimiento_TipoMovimientoNull_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 10, null, "REF1", "Motivo", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).findBySku(anyString());
    }

    @Test
    void registrarMovimiento_ProductoNotFound_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU404", 10, TipoMovimiento.ENTRADA, "REF1", "Motivo", LocalDateTime.now());
        when(productoInventarioRepository.findBySku("SKU404")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, times(1)).findBySku("SKU404");
        verify(productoInventarioRepository, never()).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, never()).save(any(MovimientoStock.class));
    }

    @Test
    void registrarMovimiento_Entrada_Success() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 20, TipoMovimiento.ENTRADA, "REF1", "Motivo", LocalDateTime.now());
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        movimientoStockService.registrarMovimiento(movimientoInput);

        assertEquals(120, testProducto.getCantidadDisponible()); // Initial 100 + 20
        verify(productoInventarioRepository, times(1)).save(testProducto);
        verify(movimientoStockRepository, times(1)).save(any(MovimientoStock.class));
    }

    @Test
    void registrarMovimiento_Salida_Success() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 30, TipoMovimiento.SALIDA, "REF2", "Motivo", LocalDateTime.now());
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        movimientoStockService.registrarMovimiento(movimientoInput);

        assertEquals(70, testProducto.getCantidadDisponible()); // Initial 100 - 30
        verify(productoInventarioRepository, times(1)).save(testProducto);
        verify(movimientoStockRepository, times(1)).save(any(MovimientoStock.class));
    }

    @Test
    void registrarMovimiento_Salida_InsufficientStock_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 120, TipoMovimiento.SALIDA, "REF3", "Motivo", LocalDateTime.now());
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));

        assertThrows(StockInsuficienteException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, never()).save(any(MovimientoStock.class));
    }

    @Test
    void registrarMovimiento_UnsupportedTipoMovimiento_ThrowsException() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 10, TipoMovimiento.RESERVA, "REF4", "Motivo", LocalDateTime.now()); // RESERVA is not handled in switch
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));

        assertThrows(IllegalArgumentException.class, () -> movimientoStockService.registrarMovimiento(movimientoInput));
        verify(productoInventarioRepository, never()).save(any(ProductoInventario.class)); // Should not save product
        verify(movimientoStockRepository, never()).save(any(MovimientoStock.class)); // Should not save movement
    }

    // These tests cover the ternary operators for motivo and fechaMovimiento
    @Test
    void registrarMovimiento_MotivoNull_UsesDefaultDescription() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 10, TipoMovimiento.ENTRADA, "REF5", null, LocalDateTime.now());
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        movimientoStockService.registrarMovimiento(movimientoInput);

        verify(movimientoStockRepository, times(1)).save(argThat(mov -> mov.getMotivo().equals(TipoMovimiento.ENTRADA.getDescripcion())));
    }

    @Test
    void registrarMovimiento_FechaMovimientoNull_UsesCurrentDateTime() {
        MovimientoStock movimientoInput = crearMovimientoStockInput("SKU001", 10, TipoMovimiento.ENTRADA, "REF6", "Motivo", null);
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        movimientoStockService.registrarMovimiento(movimientoInput);

        verify(movimientoStockRepository, times(1)).save(argThat(mov -> mov.getFechaMovimiento() != null && mov.getFechaMovimiento().isAfter(LocalDateTime.now().minusSeconds(5))));
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
