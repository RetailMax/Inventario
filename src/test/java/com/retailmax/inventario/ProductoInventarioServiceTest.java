package com.retailmax.inventario;

import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.repository.MovimientoStockRepository;
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

public class ProductoInventarioServiceTest {

    @Mock
    private ProductoInventarioRepository productoInventarioRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @InjectMocks
    private ProductoInventarioService productoInventarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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
    void consultarProductoPorSku_DeberiaRetornarProductoExistente() {
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU123");
        producto.setCantidadDisponible(5);

        when(productoInventarioRepository.findBySku("SKU123")).thenReturn(Optional.of(producto));

        ProductoInventarioDTO dto = productoInventarioService.consultarProductoPorSku("SKU123");

        assertNotNull(dto);
        assertEquals("SKU123", dto.getSku());
    }

    @Test
    void consultarProductoPorSku_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoInventarioRepository.findBySku("NO_EXISTE")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                productoInventarioService.consultarProductoPorSku("NO_EXISTE"));
    }
}
