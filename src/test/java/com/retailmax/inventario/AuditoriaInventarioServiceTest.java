package com.retailmax.inventario;

import com.retailmax.inventario.dto.DiscrepanciaStockDTO;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.service.AuditoriaInventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditoriaInventarioServiceTest {

    @Mock
    private ProductoInventarioRepository productoInventarioRepository;

    @InjectMocks
    private AuditoriaInventarioService auditoriaInventarioService;

    private ProductoInventario testProducto;

    @BeforeEach
    void setUp() {
        testProducto = new ProductoInventario();
        testProducto.setSku("SKU001");
        testProducto.setCantidadDisponible(100);
        testProducto.setProductoBaseSku("Producto Base Test");
    }

    @Test
    void compararConStockFisico_EmptyMap_ReturnsEmptyList() {
        Map<String, Integer> stockFisico = Collections.emptyMap();

        List<DiscrepanciaStockDTO> result = auditoriaInventarioService.compararConStockFisico(stockFisico);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productoInventarioRepository, never()).findBySku(anyString());
    }

    @Test
    void compararConStockFisico_ProductFound_StockMatches_ReturnsEmptyList() {
        Map<String, Integer> stockFisico = new HashMap<>();
        stockFisico.put("SKU001", 100);

        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));

        List<DiscrepanciaStockDTO> result = auditoriaInventarioService.compararConStockFisico(stockFisico);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productoInventarioRepository, times(1)).findBySku("SKU001");
    }

    @Test
    void compararConStockFisico_ProductFound_StockMismatch_ReturnsDiscrepancy() {
        Map<String, Integer> stockFisico = new HashMap<>();
        stockFisico.put("SKU001", 90); // Stock físico diferente al del sistema (100)

        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(testProducto));

        List<DiscrepanciaStockDTO> result = auditoriaInventarioService.compararConStockFisico(stockFisico);

        assertNotNull(result);
        assertEquals(1, result.size());
        DiscrepanciaStockDTO discrepancy = result.get(0);
        assertEquals("SKU001", discrepancy.getSku());
        assertEquals(100, discrepancy.getStockSistema());
        assertEquals(90, discrepancy.getStockFisico());
        assertEquals(-10, discrepancy.getDiferencia());
        assertEquals("Diferencia entre stock físico y sistema", discrepancy.getMotivo());
        verify(productoInventarioRepository, times(1)).findBySku("SKU001");
    }

    @Test
    void compararConStockFisico_ProductNotFound_ReturnsDiscrepancy() {
        Map<String, Integer> stockFisico = new HashMap<>();
        stockFisico.put("SKU404", 50); // Producto no existe en el sistema

        when(productoInventarioRepository.findBySku("SKU404")).thenReturn(Optional.empty());

        List<DiscrepanciaStockDTO> result = auditoriaInventarioService.compararConStockFisico(stockFisico);

        assertNotNull(result);
        assertEquals(1, result.size());
        DiscrepanciaStockDTO discrepancy = result.get(0);
        assertEquals("SKU404", discrepancy.getSku());
        assertEquals(0, discrepancy.getStockSistema());
        assertEquals(50, discrepancy.getStockFisico());
        assertEquals(50, discrepancy.getDiferencia());
        assertEquals("Producto no registrado en sistema", discrepancy.getMotivo());
        verify(productoInventarioRepository, times(1)).findBySku("SKU404");
    }

    // Tests para el método privado stockSistemaEquals (probado indirectamente a través de compararConStockFisico)
    // Sin embargo, para asegurar cobertura explícita, se puede añadir un test si el método fuera package-private.
    // Dado que es private, su cobertura se garantiza por los tests de compararConStockFisico.
    // Para fines de JaCoCo, los casos de stockSistemaEquals ya están cubiertos por los tests anteriores:
    // - stockSistemaEquals(100, 100) -> true (cubierto por ProductFound_StockMatches)
    // - stockSistemaEquals(100, 90) -> false (cubierto por ProductFound_StockMismatch)
    // - stockSistemaEquals(0, 50) -> false (cubierto por ProductNotFound)
    // - stockSistemaEquals(null, 50) -> false (no es posible si stockSistema viene de int)
    // - stockSistemaEquals(100, null) -> false (no es posible si fisico viene de Integer)



    @Test
    void compararConStockFisico_ProductFound_SistemaStockIsNull_ReturnsDiscrepancy() {
        Map<String, Integer> stockFisico = new HashMap<>();
        stockFisico.put("SKU001", 90); // Stock físico

        // Mock the product, but make its cantidadDisponible return null
        ProductoInventario productoWithNullStock = mock(ProductoInventario.class);
        when(productoWithNullStock.getCantidadDisponible()).thenReturn(null); // Esto hará que stockSistema sea null
        when(productoWithNullStock.getProductoBaseSku()).thenReturn("Producto Base Test"); // Mock si se usa en el DTO

        when(productoInventarioRepository.findBySku("SKU001")).thenReturn(Optional.of(productoWithNullStock));

        List<DiscrepanciaStockDTO> result = auditoriaInventarioService.compararConStockFisico(stockFisico);

        assertNotNull(result);
        assertEquals(1, result.size());
        DiscrepanciaStockDTO discrepancy = result.get(0);
        assertEquals("SKU001", discrepancy.getSku());
        assertNull(discrepancy.getStockSistema()); // Esperamos null
        assertEquals(90, discrepancy.getStockFisico());
        assertEquals(90, discrepancy.getDiferencia()); // 90 - 0 = 90 (por el cambio en el servicio)
        assertEquals("Diferencia entre stock físico y sistema", discrepancy.getMotivo());
        verify(productoInventarioRepository, times(1)).findBySku("SKU001");
    }

}