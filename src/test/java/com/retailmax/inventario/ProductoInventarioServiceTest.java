package com.retailmax.inventario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.exception.StockInsuficienteException;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.service.ProductoInventarioService; // Asegurarse de la importación correcta
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class ProductoInventarioServiceTest {

    @Autowired
    private ProductoInventarioService productoInventarioService;

    @MockBean
    private ProductoInventarioRepository productoInventarioRepository;

    @MockBean
    private MovimientoStockRepository movimientoStockRepository;

    private ProductoInventario testProducto; // Declarado a nivel de clase

    @BeforeEach
    void setUp() {
        // Reiniciar los mocks antes de cada prueba para asegurar un estado limpio
        reset(productoInventarioRepository);
        reset(movimientoStockRepository);

        // Inicializar testProducto una vez aquí
        testProducto = crearProductoInventario("SKU001", 100, "Bodega A", 10);
    }

    /**
     * Prueba para verificar la adición exitosa de un producto al inventario.
     */
    @Test
    void testAgregarProductoInventario_Success() {
        // Datos de prueba
        // Actualizar la creación del DTO para incluir los nuevos campos de variaciones
        // (productoBaseSku, talla, color)
        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO(
                "SKU001", 100, "Bodega A", 10, "BASE_SKU", "M", "Azul");
        ProductoInventario productoInventario = crearProductoInventario("SKU001", 100, "Bodega A", 10);

        // Simular el comportamiento del repositorio:
        // 1. existsBySku devuelve false (el producto no existe)
        when(productoInventarioRepository.existsBySku("SKU001")).thenReturn(false);
        // 2. save devuelve el producto que se va a guardar
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenReturn(productoInventario);
        // 3. save de MovimientoStockRepository no hace nada (void method)
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenReturn(new MovimientoStock());


        // Ejecutar el método del servicio
        ProductoInventarioDTO result = productoInventarioService.agregarProductoInventario(requestDTO);

        // Verificar los resultados
        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        assertEquals(100, result.getCantidadDisponible());
        assertEquals("Bodega A", result.getUbicacionAlmacen());
        assertEquals(10, result.getCantidadMinimaStock());
        assertEquals("BASE_SKU", result.getProductoBaseSku());
        assertEquals("M", result.getTalla());
        assertEquals("Azul", result.getColor());

        // Verificar que los métodos del repositorio fueron llamados
        verify(productoInventarioRepository, times(1)).existsBySku("SKU001");
        verify(productoInventarioRepository, times(1)).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, times(1)).save(any(MovimientoStock.class));
    }

    /**
     * Prueba para verificar que se lanza una excepción si el producto ya existe al intentar agregarlo.
     */
    @Test
    void testAgregarProductoInventario_AlreadyExists() {
        // Datos de prueba
        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO(
                "SKU001", 100, "Bodega A", 10, "BASE_SKU", "M", "Azul");

        // Simular el comportamiento del repositorio: existsBySku devuelve true (el producto ya existe)
        when(productoInventarioRepository.existsBySku("SKU001")).thenReturn(true);

        // Verificar que se lanza la excepción correcta
        assertThrows(ProductoExistenteException.class, () -> productoInventarioService.agregarProductoInventario(requestDTO));

        // Verificar que save no fue llamado ya que el producto ya existía
        verify(productoInventarioRepository, times(1)).existsBySku("SKU001");
        verify(productoInventarioRepository, never()).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, never()).save(any(MovimientoStock.class));
    }

    /**
     * Prueba para verificar la actualización de stock para una ENTRADA.
     */
    @Test
    void testActualizarStock_Entrada() {
        // Datos de prueba
        String sku = "SKU001";
        ProductoInventario productoExistente = crearProductoInventario(sku, 50, "Bodega A", 10);
        ActualizarStockRequestDTO requestDTO = new ActualizarStockRequestDTO(sku, 20, "ENTRADA", "AJUSTE_TEST", "Ref Compra 123", "Motivo de prueba entrada");

        // Simular el comportamiento del repositorio
        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(productoExistente));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Devuelve el mismo objeto pasado
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenReturn(new MovimientoStock());

        // Ejecutar el método del servicio
        ProductoInventarioDTO result = productoInventarioService.actualizarStock(requestDTO);

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(70, result.getCantidadDisponible()); // 50 + 20
        verify(productoInventarioRepository, times(1)).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, times(1)).save(any(MovimientoStock.class));
    }

    /**
     * Prueba para verificar la actualización de stock para una SALIDA.
     */
    @Test
    void testActualizarStock_Salida_Success() {
        String sku = "SKU001";
        ProductoInventario productoExistente = crearProductoInventario(sku, 50, "Bodega A", 10);
        ActualizarStockRequestDTO requestDTO = new ActualizarStockRequestDTO(sku, 20, "SALIDA", "AJUSTE_TEST", "Ref Venta 456", "Motivo de prueba salida");

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(productoExistente));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoStockRepository.save(any(MovimientoStock.class))).thenReturn(new MovimientoStock());

        ProductoInventarioDTO result = productoInventarioService.actualizarStock(requestDTO);

        assertNotNull(result);
        assertEquals(30, result.getCantidadDisponible()); // 50 - 20
        verify(productoInventarioRepository, times(1)).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, times(1)).save(any(MovimientoStock.class));
    }

    /**
     * Prueba para verificar que se lanza una excepción si no hay suficiente stock para una SALIDA.
     */
    @Test
    void testActualizarStock_Salida_InsufficientStock() {
        String sku = "SKU001";
        ProductoInventario productoExistente = crearProductoInventario(sku, 10, "Bodega A", 5);
        ActualizarStockRequestDTO requestDTO = new ActualizarStockRequestDTO(sku, 20, "SALIDA", "AJUSTE_TEST", "Ref Venta 456", "Motivo de prueba salida insuficiente");

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(productoExistente));

        assertThrows(StockInsuficienteException.class, () -> productoInventarioService.actualizarStock(requestDTO));
        verify(productoInventarioRepository, never()).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, never()).save(any(MovimientoStock.class));
    }

    /**
     * Prueba para verificar que se consulta correctamente un producto por SKU.
     */
    @Test
    void testConsultarProductoPorSku_Found() {
        String sku = "SKU001";
        ProductoInventario producto = crearProductoInventario(sku, 100, "Bodega A", 10);

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(producto));

        ProductoInventarioDTO found = productoInventarioService.consultarProductoPorSku(sku);

        assertNotNull(found);
        assertEquals(sku, found.getSku());
        assertEquals(100, found.getCantidadDisponible());
        verify(productoInventarioRepository, times(1)).findBySku(sku);
    }

    /**
     * Prueba para verificar que se lanza una excepción si el producto no se encuentra por SKU.
     */
    @Test
    void testConsultarProductoPorSku_NotFound() {
        String sku = "SKU001";
        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoInventarioService.consultarProductoPorSku(sku));
        verify(productoInventarioRepository, times(1)).findBySku(sku);
    }

    /**
     * Prueba para verificar que se eliminó correctamente un producto por SKU.
     */
    @Test
    void testEliminarProducto_Success() {
        String sku = "SKU001";
        ProductoInventario producto = crearProductoInventario(sku, 100, "Bodega A", 10);

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(producto));
        doNothing().when(productoInventarioRepository).delete(producto);

        productoInventarioService.eliminarProducto(sku);

        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(productoInventarioRepository, times(1)).delete(producto);
    }

    /**
     * Prueba para verificar que se lanza una excepción si el producto no se encuentra al intentar eliminar.
     */
    @Test
    void testEliminarProducto_NotFound() {
        String sku = "SKU001";
        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoInventarioService.eliminarProducto(sku));
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(productoInventarioRepository, never()).delete(any(ProductoInventario.class));
    }

    /**
     * Prueba para verificar la consulta de todos los productos.
     */
    @Test
    void testConsultarTodosLosProductos() {
        ProductoInventario prod1 = crearProductoInventario("SKU001", 100, "Bodega A", 10);
        ProductoInventario prod2 = crearProductoInventario("SKU002", 50, "Bodega B", 5);
        when(productoInventarioRepository.findAll()).thenReturn(Arrays.asList(prod1, prod2));

        List<ProductoInventarioDTO> products = productoInventarioService.consultarTodosLosProductos();

        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals("SKU001", products.get(0).getSku());
        assertEquals("SKU002", products.get(1).getSku());
        verify(productoInventarioRepository, times(1)).findAll();
    }

    /**
     * Prueba para verificar la actualización de un producto existente.
     */
    @Test
    void testActualizarProducto_Success() {
        String sku = "SKU001";
        ProductoInventario productoExistente = crearProductoInventario(sku, 100, "Bodega A", 10);
        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO(
                sku, 100, "Bodega C", 15, "NEW_BASE", "L", "Rojo"); // Actualizar con variaciones

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(productoExistente));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoInventarioDTO result = productoInventarioService.actualizarProducto(sku, requestDTO);

        assertNotNull(result);
        assertEquals("Bodega C", result.getUbicacionAlmacen());
        assertEquals(15, result.getCantidadMinimaStock());
        assertEquals("NEW_BASE", result.getProductoBaseSku());
        assertEquals("L", result.getTalla());
        assertEquals("Rojo", result.getColor());
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(productoInventarioRepository, times(1)).save(any(ProductoInventario.class));
    }

    /**
     * Prueba para verificar que se lanza una excepción al actualizar un producto no encontrado.
     */
    @Test
    void testActualizarProducto_NotFound() {
        String sku = "SKU001";
        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO(
                sku, 100, "Bodega C", 15, "BASE", "S", "Verde");

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoInventarioService.actualizarProducto(sku, requestDTO));
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(productoInventarioRepository, never()).save(any(ProductoInventario.class));
    }

    /**
     * Prueba para verificar la función de verificar y notificar stock bajo.
     */
    @Test
    void testVerificarYNotificarStockBajo() {
        ProductoInventario prod1 = crearProductoInventario("SKU001", 5, "Bodega A", 10); // Bajo stock
        // ProductoInventario prod2 = crearProductoInventario("SKU002", 15, "Bodega B", 10); // No bajo stock - No se usa
        when(productoInventarioRepository.findByCantidadDisponibleLessThan(10)).thenReturn(List.of(prod1));

        List<ProductoInventarioDTO> bajoStock = productoInventarioService.verificarYNotificarStockBajo(10);

        assertNotNull(bajoStock);
        assertEquals(1, bajoStock.size());
        assertEquals("SKU001", bajoStock.get(0).getSku());
        verify(productoInventarioRepository, times(1)).findByCantidadDisponibleLessThan(10);
    }

    /**
     * Prueba para verificar la función de verificar y notificar stock excesivo.
     */
    @Test
    void testVerificarYNotificarStockExcesivo() {
        ProductoInventario prod1 = crearProductoInventario("SKU001", 150, "Bodega A", 10); // Excesivo
        // ProductoInventario prod2 = crearProductoInventario("SKU002", 50, "Bodega B", 10); // No excesivo - No se usa
        when(productoInventarioRepository.findByCantidadDisponibleGreaterThan(100)).thenReturn(List.of(prod1));

        List<ProductoInventarioDTO> excesoStock = productoInventarioService.verificarYNotificarStockExcesivo(100);

        assertNotNull(excesoStock);
        assertEquals(1, excesoStock.size());
        assertEquals("SKU001", excesoStock.get(0).getSku());
        verify(productoInventarioRepository, times(1)).findByCantidadDisponibleGreaterThan(100);
    }

    /**
     * Prueba para verificar la obtención del historial de movimientos.
     */
    @Test
    void testObtenerHistorialMovimientos() {
        // Usamos el SKU de testProducto directamente para consistencia
        String sku = testProducto.getSku();

        // Creamos movimientos vinculados a testProducto
        MovimientoStock mov1 = crearMovimientoStock(testProducto, TipoMovimiento.ENTRADA, 50, "Compra inicial");
        MovimientoStock mov2 = crearMovimientoStock(testProducto, TipoMovimiento.SALIDA, 20, "Venta");

        // Mockear findBySku para que devuelva testProducto
        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(testProducto));
        // Mockear findByProductoInventarioIdOrderByFechaMovimientoDesc (o el que corresponda) con los movimientos
        // Nota: Asegúrate que el método mocked aquí coincida con el que el servicio llama cuando fechaInicio/fechaFin son null
        when(movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId())).thenReturn(Arrays.asList(mov2, mov1));


        List<MovimientoStockDTO> historial = productoInventarioService.obtenerHistorialMovimientos(sku, null, null);

        assertNotNull(historial);
        assertEquals(2, historial.size());
        assertEquals(TipoMovimiento.SALIDA.name(), historial.get(0).getTipoMovimiento());
        assertEquals(TipoMovimiento.ENTRADA.name(), historial.get(1).getTipoMovimiento());
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        // Verificación ajustada al método mockeado
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdOrderByFechaMovimientoDesc(testProducto.getId());
        verify(movimientoStockRepository, never()).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }


    /**
     * Método auxiliar para crear una instancia de ProductoInventario para las pruebas.
     */
    private ProductoInventario crearProductoInventario(String sku, Integer cantidadDisponible, String ubicacion, Integer cantidadMinima) {
        ProductoInventario producto = new ProductoInventario();
        producto.setId(1L); // Asigna un ID ficticio para las pruebas
        producto.setSku(sku);
        producto.setStock(cantidadDisponible); // Asegúrate que 'stock' se inicialice
        producto.setCantidadDisponible(cantidadDisponible);
        producto.setCantidadReservada(0);
        producto.setCantidadEnTransito(0);
        producto.setCantidadMinimaStock(cantidadMinima);
        producto.setUbicacionAlmacen(ubicacion);
        // Asignar valores por defecto o nulos para los campos de variación en el método helper si no son relevantes para todas las pruebas
        producto.setProductoBaseSku(null); // O un valor por defecto si es necesario
        producto.setTalla(null);
        producto.setColor(null);
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
