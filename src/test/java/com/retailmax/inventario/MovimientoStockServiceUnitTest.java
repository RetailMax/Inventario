package com.retailmax.inventario;


import com.retailmax.inventario.dto.MovimientoStockDTO;
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
    void testObtenerHistorialMovimientos_ConRangoDeFechas_CoversIfBranch() {
        // Caso: Se solicita el historial con un rango de fechas.
        // Esperado: Se invoca el método del repositorio que filtra por fecha.
        String sku = "SKU001";
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(5);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(testProducto));

        MovimientoStock m1 = crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 15, "Compra con fecha");
        List<MovimientoStock> movimientosFiltrados = Collections.singletonList(m1);
        when(movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                testProducto.getId(), fechaInicio, fechaFin
        )).thenReturn(movimientosFiltrados);

        List<MovimientoStockDTO> result = movimientoStockService.obtenerHistorialMovimientos(sku, fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(movimientoStockRepository, never()).findByProductoInventarioIdOrderByFechaMovimientoDesc(anyLong());
    }

    @Test
    void testMapToMovimientoStockDTO_ProductoInventarioNotNull() {
        // Caso: Se mapea un movimiento cuyo producto asociado NO es nulo.
        // Esperado: El DTO resultante tiene el productoInventarioId correcto.
        MovimientoStock movimientoConProducto = crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 10, "Movimiento con producto");
        movimientoConProducto.setId(100L); // Asignar un ID para el movimiento
        
        // No necesitamos mockear el repositorio aquí, solo probar el método de mapeo
        MovimientoStockDTO result = movimientoStockService.mapToMovimientoStockDTO(movimientoConProducto);

        assertNotNull(result);
        assertEquals(movimientoConProducto.getId(), result.getId());
        assertEquals(testProducto.getId(), result.getProductoInventarioId()); // Verifica que el ID del producto no sea null
        assertEquals(movimientoConProducto.getSku(), result.getSku());
    }

    @Test
    void testMapToMovimientoStockDTO_conProductoInventarioNulo() {
        // Caso: Se mapea un movimiento cuyo producto asociado es nulo.
        // Esperado: El DTO resultante tiene un productoInventarioId nulo (cubre la rama 'null' del ternario).
        MovimientoStock movimientoSinProducto = crearMovimientoStock(testProducto, TipoMovimiento.AJUSTE, 5, "Ajuste sin producto");
        movimientoSinProducto.setProductoInventario(null); // Forzar el caso nulo
        movimientoSinProducto.setId(101L);

        // Llamada directa al método de mapeo
        MovimientoStockDTO result = movimientoStockService.mapToMovimientoStockDTO(movimientoSinProducto);

        assertNotNull(result);
        assertNull(result.getProductoInventarioId());
        assertEquals(movimientoSinProducto.getSku(), result.getSku());
    }

    @Test
    void testObtenerHistorialMovimientos_SinRangoDeFechas_RetornaHistorialCompleto() {
        // Caso: Se solicita el historial sin especificar fechas (cubre la rama 'else').
        // Esperado: Se invoca el método del repositorio que no filtra por fecha.
        String sku = "SKU001";
        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(testProducto));

        List<MovimientoStock> movimientos = Arrays.asList(crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 10, "Compra 1"));
        when(movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId())).thenReturn(movimientos);

        List<MovimientoStockDTO> result = movimientoStockService.obtenerHistorialMovimientos(sku, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId());
        verify(movimientoStockRepository, never()).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(anyLong(), any(), any());
    }

    @Test
    void testObtenerHistorialMovimientos_FechaParcial_RetornaHistorialCompleto() {
        // Caso: Se solicita el historial con solo una de las dos fechas (cubre la rama del '&&' que faltaba).
        // Esperado: Se ignora la fecha parcial y se invoca el método que no filtra por fecha (comportamiento del 'else').
        String sku = "SKU001";
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(5);

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(testProducto));

        List<MovimientoStock> movimientos = Arrays.asList(crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 10, "Compra 1"));
        when(movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId())).thenReturn(movimientos);

        // Probar con fecha de inicio pero sin fecha de fin
        List<MovimientoStockDTO> result = movimientoStockService.obtenerHistorialMovimientos(sku, fechaInicio, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId());
        verify(movimientoStockRepository, never()).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(anyLong(), any(), any());
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

    // --- Tests for TipoMovimiento Enum to increase coverage ---

    @Test
    void testTipoMovimiento_fromDescripcion_Success() {
        // This test covers the successful path of fromDescripcion, including case-insensitivity.
        assertEquals(TipoMovimiento.ENTRADA, TipoMovimiento.fromDescripcion("Entrada de Stock"));
        assertEquals(TipoMovimiento.SALIDA, TipoMovimiento.fromDescripcion("salida de stock"));
        assertEquals(TipoMovimiento.AJUSTE, TipoMovimiento.fromDescripcion("Ajuste de Inventario"));
    }

    @Test
    void testTipoMovimiento_fromDescripcion_NotFound_ThrowsException() {
        // This test covers the failure path of fromDescripcion, ensuring an exception is thrown for invalid input.
        assertThrows(IllegalArgumentException.class, () -> TipoMovimiento.fromDescripcion("Invalid Description"));
    }
}
