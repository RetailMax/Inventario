package com.retailmax.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.exception.StockInsuficienteException;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.model.enums.EstadoStock;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.service.ProductoInventarioService;

@ExtendWith(MockitoExtension.class)
public class ProductoInventarioServiceTest {

    @Mock
    private ProductoInventarioRepository productoInventarioRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @InjectMocks
    private ProductoInventarioService productoInventarioService;

    @Test
    void agregarProductoInventario_DeberiaAgregarProductoCorrectamente() {
        AgregarProductoInventarioRequestDTO request = new AgregarProductoInventarioRequestDTO();
        request.setSku("SKU001");
        request.setCantidadInicial(10);
        request.setCantidadMinimaStock(2);
        request.setUbicacionAlmacen("A1");

        when(productoInventarioRepository.existsBySku("SKU001")).thenReturn(false);
        when(productoInventarioRepository.save(any(ProductoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductoInventarioDTO result = productoInventarioService.agregarProductoInventario(request);

        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        assertEquals(10, result.getCantidadDisponible());
        verify(productoInventarioRepository, times(1)).save(any(ProductoInventario.class));
    }

    @Test
    void agregarProductoInventario_DeberiaLanzarExcepcionSiYaExiste() {
        AgregarProductoInventarioRequestDTO request = new AgregarProductoInventarioRequestDTO();
        request.setSku("SKU_EXISTENTE");

        when(productoInventarioRepository.existsBySku("SKU_EXISTENTE")).thenReturn(true);

        assertThrows(ProductoExistenteException.class, () ->
                productoInventarioService.agregarProductoInventario(request));
    }

    @Test
    void consultarProductoPorSku_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoInventarioRepository.findBySku("NO_EXISTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.consultarProductoPorSku("NO_EXISTE"));
    }
    @Test
    void consultarProductoPorSku_DeberiaRetornarProductoExistente() {
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU123");
        producto.setEstado(EstadoStock.DISPONIBLE);
        producto.setCantidadDisponible(5);
        producto.setCantidadReservada(0); // <-- FIX agregado

        when(productoInventarioRepository.findBySku("SKU123")).thenReturn(Optional.of(producto));

        ProductoInventarioDTO dto = productoInventarioService.consultarProductoPorSku("SKU123");

        assertNotNull(dto);
        assertEquals("SKU123", dto.getSku());
    }

    // --- Tests para actualizarProducto para aumentar cobertura de JaCoCo ---

    @Test
    void actualizarProducto_DeberiaActualizarProductoCorrectamente() {
        // Caso: Actualización exitosa de un producto existente.
        // Esperado: El producto se actualiza con los nuevos datos y se devuelve el DTO actualizado.
        String sku = "SKU_ACTUALIZAR";
        ProductoInventario productoExistente = new ProductoInventario();
        productoExistente.setSku(sku);
        productoExistente.setCantidadDisponible(10);
        productoExistente.setCantidadReservada(0);
        productoExistente.setCantidadMinimaStock(5);
        productoExistente.setUbicacionAlmacen("OLD_LOC");
        productoExistente.setEstado(EstadoStock.DISPONIBLE);

        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO();
        requestDTO.setCantidadMinimaStock(10);
        requestDTO.setUbicacionAlmacen("NEW_LOC");
        requestDTO.setProductoBaseSku("NEW_BASE_SKU");
        requestDTO.setTalla("M");
        requestDTO.setColor("Blue");

        when(productoInventarioRepository.findBySku(sku)).thenReturn(Optional.of(productoExistente));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoInventarioDTO result = productoInventarioService.actualizarProducto(sku, requestDTO);

        assertNotNull(result);
        assertEquals(sku, result.getSku());
        assertEquals(10, result.getCantidadMinimaStock());
        assertEquals("NEW_LOC", result.getUbicacionAlmacen());
        assertEquals("NEW_BASE_SKU", result.getProductoBaseSku());
        assertEquals("M", result.getTalla());
        assertEquals("Blue", result.getColor());
        verify(productoInventarioRepository, times(1)).findBySku(sku);
        verify(productoInventarioRepository, times(1)).save(productoExistente);
    }

    @Test
    void actualizarProducto_DeberiaLanzarExcepcionSiProductoNoExiste() {
        // Caso: Se intenta actualizar un producto con un SKU que no existe.
        // Esperado: Lanza RecursoNoEncontradoException.
        String skuInexistente = "SKU_INEXISTENTE_UPDATE";
        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO();
        when(productoInventarioRepository.findBySku(skuInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.actualizarProducto(skuInexistente, requestDTO));
        verify(productoInventarioRepository, never()).save(any());
    }

    // --- Tests para actualizarStock para aumentar cobertura de JaCoCo ---

    @Test
    void actualizarStock_ProductoNoEncontrado_LanzaExcepcion() {
        // Caso: Se intenta actualizar el stock de un SKU que no existe.
        // Esperado: Lanza RecursoNoEncontradoException.
        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO("SKU_INEXISTENTE", 10, "ENTRADA", "ref", "motivo");
        when(productoInventarioRepository.findBySku("SKU_INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.actualizarStock(request));
        verify(productoInventarioRepository, never()).save(any());
    }

    @Test
    void actualizarStock_TipoEntrada_SumaStock() {
        // Caso: Movimiento de ENTRADA.
        // Esperado: El stock se incrementa correctamente.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU001");
        producto.setCantidadDisponible(100);
        producto.setCantidadReservada(0);
        producto.setEstado(EstadoStock.DISPONIBLE);
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO("SKU001", 20, "ENTRADA", "ref", "motivo");

        ProductoInventarioDTO result = productoInventarioService.actualizarStock(request);

        assertNotNull(result);
        assertEquals(120, result.getCantidadDisponible());
        verify(productoInventarioRepository, times(1)).save(any(ProductoInventario.class));
        verify(movimientoStockRepository, times(1)).save(any(com.retailmax.inventario.model.MovimientoStock.class));
    }

    @Test
    void actualizarStock_TipoSalida_RestaStock() {
        // Caso: Movimiento de SALIDA con stock suficiente.
        // Esperado: El stock se decrementa correctamente.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU001");
        producto.setCantidadDisponible(100);
        producto.setCantidadReservada(0);
        producto.setEstado(EstadoStock.DISPONIBLE);
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO("SKU001", 30, "SALIDA", "ref", "motivo");

        ProductoInventarioDTO result = productoInventarioService.actualizarStock(request);

        assertNotNull(result);
        assertEquals(70, result.getCantidadDisponible());
    }

    @Test
    void actualizarStock_TipoSalida_StockInsuficiente_LanzaExcepcion() {
        // Caso: Movimiento de SALIDA con stock insuficiente.
        // Esperado: Lanza StockInsuficienteException.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU001");
        producto.setCantidadDisponible(10);
        producto.setCantidadReservada(0);
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO("SKU001", 20, "SALIDA", "ref", "motivo");

        assertThrows(StockInsuficienteException.class, () ->
                productoInventarioService.actualizarStock(request));
    }

    @Test
    void actualizarStock_TipoAjuste_ReemplazaStock() {
        // Caso: Movimiento de AJUSTE.
        // Esperado: El stock se reemplaza con el nuevo valor.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU001");
        producto.setCantidadDisponible(100);
        producto.setCantidadReservada(0);
        producto.setEstado(EstadoStock.DISPONIBLE);
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));
        when(productoInventarioRepository.save(any(ProductoInventario.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO("SKU001", 55, "AJUSTE", "ref", "motivo");

        ProductoInventarioDTO result = productoInventarioService.actualizarStock(request);

        assertNotNull(result);
        assertEquals(55, result.getCantidadDisponible());
    }

    @Test
    void actualizarStock_TipoMovimientoNoSoportado_LanzaExcepcion() {
        // Caso: Se usa un TipoMovimiento válido pero no soportado por el switch (ej. RESERVA).
        // Esperado: Lanza IllegalArgumentException desde el caso 'default' del switch.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU001");
        producto.setCantidadDisponible(100);
        producto.setCantidadReservada(0);
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO("SKU001", 10, "RESERVA", "ref", "motivo");

        assertThrows(IllegalArgumentException.class, () ->
                productoInventarioService.actualizarStock(request));
    }

    // --- Tests para liberarStockReservado para aumentar cobertura de JaCoCo ---

    @Test
    void liberarStockReservado_ProductoNoEncontrado_LanzaExcepcion() {
        // Caso: Se intenta liberar stock de un SKU que no existe.
        // Esperado: Lanza RecursoNoEncontradoException.
        String skuInexistente = "SKU_NO_EXISTE";
        when(productoInventarioRepository.findBySku(skuInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.liberarStockReservado(skuInexistente, 10, "Motivo cualquiera"));

        verify(productoInventarioRepository, never()).save(any());
        verify(movimientoStockRepository, never()).save(any());
    }

    @Test
    void liberarStockReservado_StockReservadoInsuficiente_LanzaExcepcion() {
        // Caso: Se intenta liberar más stock del que está reservado.
        // Esperado: Lanza IllegalArgumentException.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU001");
        producto.setCantidadReservada(5); // Solo 5 reservados
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class, () ->
                productoInventarioService.liberarStockReservado("SKU001", 10, "Intentando liberar más de lo reservado")); // Se intentan liberar 10

        verify(productoInventarioRepository, never()).save(any());
    }

    @Test
    void liberarStockReservado_Exitoso_ConMotivoProporcionado() {
        // Caso: Liberación exitosa con un motivo específico.
        // Esperado: El stock se actualiza y se registra el movimiento con el motivo dado.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU001");
        producto.setCantidadDisponible(90);
        producto.setCantidadReservada(20);
        producto.setEstado(EstadoStock.DISPONIBLE);
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));

        productoInventarioService.liberarStockReservado("SKU001", 15, "Venta cancelada #123");

        assertEquals(5, producto.getCantidadReservada()); // 20 - 15
        assertEquals(105, producto.getCantidadDisponible()); // 90 + 15
        verify(productoInventarioRepository, times(1)).save(producto);
        verify(movimientoStockRepository, times(1)).save(any(com.retailmax.inventario.model.MovimientoStock.class));
    }

    @Test
    void liberarStockReservado_Exitoso_ConMotivoPorDefecto() {
        // Caso: Liberación exitosa sin un motivo específico (debe usar el default).
        // Esperado: El stock se actualiza y se registra el movimiento con el motivo por defecto.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU002");
        producto.setCantidadDisponible(50);
        producto.setCantidadReservada(10);
        producto.setEstado(EstadoStock.DISPONIBLE);
        when(productoInventarioRepository.findBySku("SKU002")).thenReturn(Optional.of(producto));

        productoInventarioService.liberarStockReservado("SKU002", 10, null); // Motivo es null

        verify(productoInventarioRepository, times(1)).save(producto);
        verify(movimientoStockRepository, times(1)).save(any(com.retailmax.inventario.model.MovimientoStock.class));
    }

    @Test
    void mapToDTO_ShouldReturnNullForNullInput() {
        // Prueba para cubrir la rama de 'null' en el método mapToDTO
        ProductoInventarioDTO result = productoInventarioService.mapToDTO(null);
        assertNull(result);
    }

    // --- Tests para mapMovimientoToDTO para aumentar cobertura de JaCoCo ---

    @Test
    void mapMovimientoToDTO_ShouldReturnNullForNullInput() {
        // Prueba para cubrir la rama de 'null' en el método mapMovimientoToDTO
        MovimientoStockDTO result = productoInventarioService.mapMovimientoToDTO(null);
        assertNull(result);
    }

    @Test
    void mapMovimientoToDTO_ShouldHandleNullProductoInventario() {
        // Prueba para cubrir la rama donde productoInventario es null dentro de MovimientoStock
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(1L);
        movimiento.setSku("TESTSKU");
        movimiento.setProductoInventario(null); // Simula que el producto asociado es null
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimiento.setCantidadMovida(10);

        MovimientoStockDTO result = productoInventarioService.mapMovimientoToDTO(movimiento);
        assertNotNull(result);
        assertNull(result.getProductoInventarioId()); // Verifica que el ID del producto sea null
    }

    // --- Tests para obtenerHistorialMovimientos para aumentar cobertura de JaCoCo ---

    @Test
    void obtenerHistorialMovimientos_ProductoNoEncontrado_LanzaExcepcion() {
        // Caso: Se intenta obtener el historial de un SKU que no existe.
        // Esperado: Lanza RecursoNoEncontradoException.
        String skuInexistente = "SKU_NO_EXISTE";
        when(productoInventarioRepository.findBySku(skuInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.obtenerHistorialMovimientos(skuInexistente, null, null));

        verify(movimientoStockRepository, never()).findByProductoInventarioIdOrderByFechaMovimientoDesc(anyLong());
        verify(movimientoStockRepository, never()).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(anyLong(), any(), any());
    }

    @Test
    void obtenerHistorialMovimientos_SinRangoDeFechas_RetornaHistorialCompleto() {
        // Caso: Se solicita el historial sin especificar fechas.
        // Esperado: Se llama al método del repositorio que no filtra por fecha y se retorna el historial.
        ProductoInventario producto = new ProductoInventario();
        producto.setId(1L);
        producto.setSku("SKU_HISTORIAL");
        producto.setEstado(EstadoStock.DISPONIBLE);

        MovimientoStock m1 = new MovimientoStock();
        m1.setProductoInventario(producto);
        m1.setTipoMovimiento(TipoMovimiento.ENTRADA);
        MovimientoStock m2 = new MovimientoStock();
        m2.setProductoInventario(producto);
        m2.setTipoMovimiento(TipoMovimiento.SALIDA);
        List<MovimientoStock> movimientos = Arrays.asList(m1, m2);

        when(productoInventarioRepository.findBySku("SKU_HISTORIAL")).thenReturn(Optional.of(producto));
        when(movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(1L)).thenReturn(movimientos);

        List<MovimientoStockDTO> result = productoInventarioService.obtenerHistorialMovimientos("SKU_HISTORIAL", null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdOrderByFechaMovimientoDesc(1L);
        verify(movimientoStockRepository, never()).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(anyLong(), any(), any());
    }

    @Test
    void obtenerHistorialMovimientos_ConRangoDeFechas_RetornaHistorialFiltrado() {
        // Caso: Se solicita el historial especificando un rango de fechas.
        // Esperado: Se llama al método del repositorio que filtra por fecha y se retorna el historial.
        ProductoInventario producto = new ProductoInventario();
        producto.setId(1L);
        producto.setSku("SKU_HISTORIAL_FECHA");
        producto.setEstado(EstadoStock.DISPONIBLE);

        MovimientoStock m1 = new MovimientoStock();
        m1.setProductoInventario(producto);
        m1.setTipoMovimiento(TipoMovimiento.ENTRADA);
        List<MovimientoStock> movimientos = Arrays.asList(m1);

        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(productoInventarioRepository.findBySku("SKU_HISTORIAL_FECHA")).thenReturn(Optional.of(producto));
        when(movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(1L, fechaInicio, fechaFin)).thenReturn(movimientos);

        List<MovimientoStockDTO> result = productoInventarioService.obtenerHistorialMovimientos("SKU_HISTORIAL_FECHA", fechaInicio, fechaFin);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movimientoStockRepository, never()).findByProductoInventarioIdOrderByFechaMovimientoDesc(anyLong());
        verify(movimientoStockRepository, times(1)).findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(1L, fechaInicio, fechaFin);
    }

    // --- Tests para actualizarEstado para aumentar cobertura de JaCoCo ---

    @Test
    void actualizarEstado_ProductoNoEncontrado_LanzaExcepcion() {
        // Caso: Se intenta actualizar el estado de un producto con un ID que no existe.
        // Esperado: Lanza RecursoNoEncontradoException.
        Long idInexistente = 999L;
        when(productoInventarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.actualizarEstado(idInexistente, EstadoStock.DESCONTINUADO));

        verify(productoInventarioRepository, times(1)).findById(idInexistente);
        verify(productoInventarioRepository, never()).save(any());
    }

    @Test
    void actualizarEstado_Exitoso() {
        // Caso: Se actualiza el estado de un producto existente.
        // Esperado: El estado del producto se actualiza y se guarda.
        Long idExistente = 1L;
        ProductoInventario producto = new ProductoInventario();
        producto.setId(idExistente);
        producto.setEstado(EstadoStock.DISPONIBLE);

        when(productoInventarioRepository.findById(idExistente)).thenReturn(Optional.of(producto));

        productoInventarioService.actualizarEstado(idExistente, EstadoStock.VENDIDO);

        verify(productoInventarioRepository, times(1)).findById(idExistente);
        verify(productoInventarioRepository, times(1)).save(producto);
        assertEquals(EstadoStock.VENDIDO, producto.getEstado());
        assertNotNull(producto.getFechaUltimaActualizacion());
    }

    // --- Tests para validarDisponibilidad para aumentar cobertura de JaCoCo ---

    @Test
    void validarDisponibilidad_ProductoNoEncontrado_LanzaExcepcion() {
        // Caso: Se valida la disponibilidad de un SKU que no existe.
        // Esperado: Lanza RecursoNoEncontradoException.
        String skuInexistente = "SKU_NO_EXISTE_VALIDACION";
        when(productoInventarioRepository.findBySku(skuInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.validarDisponibilidad(skuInexistente, 10));

        verify(productoInventarioRepository, times(1)).findBySku(skuInexistente);
    }

    @Test
    void validarDisponibilidad_StockSuficiente_RetornaTrue() {
        // Caso: La cantidad solicitada es menor o igual a la disponible.
        // Esperado: Retorna true.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU_DISPONIBLE");
        producto.setCantidadDisponible(20);

        when(productoInventarioRepository.findBySku("SKU_DISPONIBLE")).thenReturn(Optional.of(producto));

        boolean resultado = productoInventarioService.validarDisponibilidad("SKU_DISPONIBLE", 15);
        assertTrue(resultado);

        // También probar el caso límite
        boolean resultadoLimite = productoInventarioService.validarDisponibilidad("SKU_DISPONIBLE", 20);
        assertTrue(resultadoLimite);

        verify(productoInventarioRepository, times(2)).findBySku("SKU_DISPONIBLE");
    }

    @Test
    void validarDisponibilidad_StockInsuficiente_RetornaFalse() {
        // Caso: La cantidad solicitada es mayor a la disponible.
        // Esperado: Retorna false.
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU_INSUFICIENTE");
        producto.setCantidadDisponible(5);

        when(productoInventarioRepository.findBySku("SKU_INSUFICIENTE")).thenReturn(Optional.of(producto));

        boolean resultado = productoInventarioService.validarDisponibilidad("SKU_INSUFICIENTE", 10);

        assertFalse(resultado);
        verify(productoInventarioRepository, times(1)).findBySku("SKU_INSUFICIENTE");
    }

    // --- Tests para eliminarProducto para aumentar cobertura de JaCoCo ---

    @Test
    void eliminarProducto_ProductoNoEncontrado_LanzaExcepcion() {
        // Caso: Se intenta eliminar un producto con un SKU que no existe.
        // Esperado: Lanza RecursoNoEncontradoException (cubre el lambda).
        String skuInexistente = "SKU_NO_EXISTE_DELETE";
        when(productoInventarioRepository.findBySku(skuInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.eliminarProducto(skuInexistente));

        verify(productoInventarioRepository, times(1)).findBySku(skuInexistente);
        verify(productoInventarioRepository, never()).delete(any());
    }

    @Test
    void eliminarProducto_Exitoso() {
        // Caso: Se elimina un producto existente.
        // Esperado: El método delete del repositorio es invocado.
        String skuExistente = "SKU_EXISTE_DELETE";
        ProductoInventario producto = new ProductoInventario();
        producto.setSku(skuExistente);

        when(productoInventarioRepository.findBySku(skuExistente)).thenReturn(Optional.of(producto));
        doNothing().when(productoInventarioRepository).delete(producto);

        productoInventarioService.eliminarProducto(skuExistente);

        verify(productoInventarioRepository, times(1)).findBySku(skuExistente);
        verify(productoInventarioRepository, times(1)).delete(producto);
    }

    // --- Tests para consultarMovimientoPorId para aumentar cobertura de JaCoCo ---

    @Test
    void consultarMovimientoPorId_MovimientoNoEncontrado_LanzaExcepcion() {
        // Caso: Se consulta un movimiento con un ID que no existe.
        // Esperado: Lanza RecursoNoEncontradoException (cubre el lambda).
        Long idInexistente = 999L;
        when(movimientoStockRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                productoInventarioService.consultarMovimientoPorId(idInexistente));

        verify(movimientoStockRepository, times(1)).findById(idInexistente);
    }

    @Test
    void consultarMovimientoPorId_MovimientoEncontrado_RetornaDTO() {
        // Caso: Se consulta un movimiento existente.
        // Esperado: Retorna el DTO del movimiento.
        Long idExistente = 1L;
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(idExistente);
        movimiento.setSku("SKU-MOV");
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);

        when(movimientoStockRepository.findById(idExistente)).thenReturn(Optional.of(movimiento));

        MovimientoStockDTO result = productoInventarioService.consultarMovimientoPorId(idExistente);

        assertNotNull(result);
        assertEquals(idExistente, result.getId());
        assertEquals("SKU-MOV", result.getSku());
        verify(movimientoStockRepository, times(1)).findById(idExistente);
    }

    // --- Tests para buscarPorEstado para aumentar cobertura de JaCoCo ---

    @Test
    void buscarPorEstado_RetornaProductosFiltrados() {
        // Caso: Se buscan productos por un estado específico.
        // Esperado: Retorna una lista de DTOs de los productos que coinciden con el estado.
        ProductoInventario p1 = new ProductoInventario();
        p1.setSku("SKU-VENDIDO-1");
        p1.setEstado(EstadoStock.VENDIDO);
        p1.setCantidadDisponible(0);
        p1.setCantidadReservada(0);

        List<ProductoInventario> productosVendidos = Arrays.asList(p1);

        when(productoInventarioRepository.findByEstado(EstadoStock.VENDIDO)).thenReturn(productosVendidos);

        List<ProductoInventarioDTO> result = productoInventarioService.buscarPorEstado(EstadoStock.VENDIDO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SKU-VENDIDO-1", result.get(0).getSku());
        verify(productoInventarioRepository, times(1)).findByEstado(EstadoStock.VENDIDO);
    }
}
