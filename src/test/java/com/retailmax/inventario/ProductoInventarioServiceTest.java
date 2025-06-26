package com.retailmax.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.retailmax.inventario.model.enums.EstadoStock;
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
}
