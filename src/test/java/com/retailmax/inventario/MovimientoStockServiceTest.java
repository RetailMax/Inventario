package com.retailmax.inventario;

import static org.mockito.Mockito.*; // Importa todas las estáticas de Mockito
import static org.junit.jupiter.api.Assertions.*;

import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.service.MovimientoStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Importación clave: MockBean

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest // Carga el contexto completo de Spring Boot para la aplicación principal
public class MovimientoStockServiceTest {

    @Autowired // El servicio real, que será inyectado con los mocks de los repositorios
    private MovimientoStockService movimientoStockService;

    // Usamos @MockBean para crear mocks de los repositorios y que Spring los inyecte.
    // Esto reemplaza cualquier bean existente y resuelve el conflicto.
    @MockBean
    private MovimientoStockRepository movimientoStockRepository;

    @MockBean
    private ProductoInventarioRepository productoInventarioRepository;

    private ProductoInventario testProducto;

    // *** ELIMINAMOS LA CLASE DE CONFIGURACIÓN ANIDADA QUE CAUSABA EL CONFLICTO ***
    // @TestConfiguration
    // static class MovimientoStockServiceTestConfiguration {
    //     @Bean
    //     public MovimientoStockRepository movimientoStockRepository() {
    //         return mock(MovimientoStockRepository.class);
    //     }
    //     @Bean
    //     public ProductoInventarioRepository productoInventarioRepository() {
    //         return mock(ProductoInventarioRepository.class);
    //     }
    // }

    @BeforeEach
    void setUp() {
        // Reiniciar los mocks antes de cada prueba para asegurar un estado limpio
        // Esto es necesario porque @MockBean crea un singleton mock por contexto
        reset(movimientoStockRepository);
        reset(productoInventarioRepository);

        // Crear un producto de prueba que será usado en varios tests
        testProducto = crearProductoInventario("SKU001", 100, "Bodega A", 10);
    }

    /**
     * Prueba para verificar la búsqueda de movimientos por SKU.
     */
    @Test
    void testBuscarPorSku_Success() {
        String sku = "SKU001";
        MovimientoStock mov1 = crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 50, "Compra inicial");
        MovimientoStock mov2 = crearMovimientoStock(testProducto, TipoMovimiento.SALIDA, 20, "Venta");

        when(movimientoStockRepository.findBySku(sku)).thenReturn(Arrays.asList(mov1, mov2));

        List<MovimientoStock> movimientos = movimientoStockService.buscarPorSku(sku);

        assertNotNull(movimientos);
        assertEquals(2, movimientos.size());
        assertEquals(sku, movimientos.get(0).getSku());
        assertEquals(sku, movimientos.get(1).getSku());
        verify(movimientoStockRepository, times(1)).findBySku(sku);
    }

    /**
     * Prueba para verificar la obtención del historial de movimientos sin filtros de fecha.
     */
    @Test
    void testObtenerHistorialMovimientos_NoDateFilter() {
        String sku = "SKU001";
        MovimientoStock mov1 = crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 50, "Compra inicial");
        MovimientoStock mov2 = crearMovimientoStock(testProducto, TipoMovimiento.SALIDA, 20, "Venta");

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(testProducto));
        // Aquí mockeamos findByProductoInventarioIdOrderByFechaMovimientoDesc ya que es el que se llama sin fechas
        when(movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId())).thenReturn(Arrays.asList(mov2, mov1));

        List<MovimientoStockDTO> historial = movimientoStockService.obtenerHistorialMovimientos(sku, null, null);

        assertNotNull(historial);
        assertEquals(2, historial.size());
        assertEquals(TipoMovimiento.SALIDA.name(), historial.get(0).getTipoMovimiento());
        assertEquals(TipoMovimiento.ENTRADA.name(), historial.get(1).getTipoMovimiento());
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId());
        verify(movimientoStockRepository, never()).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    /**
     * Prueba para verificar la obtención del historial de movimientos con filtros de fecha.
     */
    @Test
    void testObtenerHistorialMovimientos_WithDateFilter() {
        String sku = "SKU001";
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(5);
        LocalDateTime fechaFin = LocalDateTime.now();

        MovimientoStock mov1 = crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 50, "Compra inicial");
        mov1.setFechaMovimiento(LocalDateTime.now().minusDays(3));
        MovimientoStock mov2 = crearMovimientoStock(testProducto, TipoMovimiento.SALIDA, 20, "Venta");
        mov2.setFechaMovimiento(LocalDateTime.now().minusDays(1));

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(testProducto));
        // Aquí mockeamos findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc
        when(movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                eq(testProducto.getId()), eq(fechaInicio), eq(fechaFin))).thenReturn(Arrays.asList(mov2, mov1));

        List<MovimientoStockDTO> historial = movimientoStockService.obtenerHistorialMovimientos(sku, fechaInicio, fechaFin);

        assertNotNull(historial);
        assertEquals(2, historial.size());
        assertEquals(TipoMovimiento.SALIDA.name(), historial.get(0).getTipoMovimiento());
        assertEquals(TipoMovimiento.ENTRADA.name(), historial.get(1).getTipoMovimiento());
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                eq(testProducto.getId()), eq(fechaInicio), eq(fechaFin));
        verify(movimientoStockRepository, never()).findByProductoInventarioIdOrderByFechaMovimientoDesc(anyLong());
    }

    /**
     * Prueba para verificar que se lanza una excepción si el producto no es encontrado
     * al intentar obtener su historial de movimientos.
     */
    @Test
    void testObtenerHistorialMovimientos_ProductoNotFound() {
        String sku = "SKU001";
        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> movimientoStockService.obtenerHistorialMovimientos(sku, null, null));
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(movimientoStockRepository, never()).findByProductoInventarioIdOrderByFechaMovimientoDesc(anyLong());
    }

    /**
     * Prueba para verificar la consulta de un movimiento por ID cuando es encontrado.
     */
    @Test
    void testConsultarMovimientoPorId_Found() {
        Long id = 1L;
        MovimientoStock movimiento = crearMovimientoStock(testProducto, TipoMovimiento.AJUSTE, 10, "Ajuste inventario");
        movimiento.setId(id); // Asegura que el ID del mock sea el que buscamos

        when(movimientoStockRepository.findById(id)).thenReturn(Optional.of(movimiento));

        MovimientoStockDTO found = movimientoStockService.consultarMovimientoPorId(id);

        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals(TipoMovimiento.AJUSTE.name(), found.getTipoMovimiento());
        verify(movimientoStockRepository, times(1)).findById(id);
    }

    /**
     * Prueba para verificar que se lanza una excepción si el movimiento no es encontrado por ID.
     */
    @Test
    void testConsultarMovimientoPorId_NotFound() {
        Long id = 99L;
        when(movimientoStockRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> movimientoStockService.consultarMovimientoPorId(id));
        verify(movimientoStockRepository, times(1)).findById(id);
    }

    /**
     * Método auxiliar para crear una instancia de ProductoInventario para las pruebas.
     */
    private ProductoInventario crearProductoInventario(String sku, Integer cantidadDisponible, String ubicacion, Integer cantidadMinima) {
        ProductoInventario producto = new ProductoInventario();
        producto.setId(1L); // Asigna un ID ficticio para las pruebas
        producto.setSku(sku);
        producto.setStock(cantidadDisponible);
        producto.setCantidadDisponible(cantidadDisponible);
        producto.setCantidadReservada(0);
        producto.setCantidadEnTransito(0);
        producto.setCantidadMinimaStock(cantidadMinima);
        producto.setUbicacionAlmacen(ubicacion);
        producto.setFechaCreacion(LocalDateTime.now().minusDays(10));
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setActivo(true);
        return producto;
    }

    /**
     * Método auxiliar para crear una instancia de MovimientoStock para las pruebas.
     */
    private MovimientoStock crearMovimientoStock(ProductoInventario producto, TipoMovimiento tipo, Integer cantidad, String referencia) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(Long.valueOf((int)(Math.random() * 1000))); // Genera un ID aleatorio para cada movimiento
        movimiento.setProductoInventario(producto);
        movimiento.setSku(producto.getSku());
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidadMovida(cantidad);
        movimiento.setStockFinalDespuesMovimiento(producto.getCantidadDisponible() + (tipo == TipoMovimiento.ENTRADA ? cantidad : -cantidad)); // Ajusta según el tipo de movimiento
        movimiento.setReferenciaExterna(referencia);
        movimiento.setMotivo(tipo.getDescripcion());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        return movimiento;
    }
}
