package com.retailmax.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.exception.StockInsuficienteException;
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

        assertThrows(RuntimeException.class, () ->
                productoInventarioService.consultarProductoPorSku("NO_EXISTE"));
    }
    @Test
    void consultarProductoPorSku_DeberiaRetornarProductoExistente() {
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU123");
        producto.setCantidadDisponible(5);
        producto.setCantidadReservada(0); // <-- FIX agregado

        when(productoInventarioRepository.findBySku("SKU123")).thenReturn(Optional.of(producto));

        ProductoInventarioDTO dto = productoInventarioService.consultarProductoPorSku("SKU123");

        assertNotNull(dto);
        assertEquals("SKU123", dto.getSku());
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
        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(producto));

        ActualizarStockRequestDTO request = new ActualizarStockRequestDTO("SKU001", 10, "RESERVA", "ref", "motivo");

        assertThrows(IllegalArgumentException.class, () ->
                productoInventarioService.actualizarStock(request));
    }
}
