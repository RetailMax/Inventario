package com.retailmax.inventario;

import com.retailmax.inventario.dto.CrearProductoVariacionRequestDTO;
import com.retailmax.inventario.dto.ProductoVariacionDTO;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.ProductoVariacion;
import com.retailmax.inventario.repository.ProductoVariacionRepository;
import com.retailmax.inventario.service.ProductoVariacionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;

import java.util.Optional;



public class ProductoVariacionServiceTest {

    @Mock
    private ProductoVariacionRepository productoVariacionRepository;

    @InjectMocks
    private ProductoVariacionService productoVariacionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegistrarVariacion_exitoso() {
        CrearProductoVariacionRequestDTO dto = new CrearProductoVariacionRequestDTO();
        dto.setSkuProductoBase("PROD001");
        dto.setTalla("M");
        dto.setColor("Rojo");
        dto.setStock(10);
        dto.setDescripcion("Variante M Rojo");
        dto.setUbicacion("A1");

        when(productoVariacionRepository.existsBySku("PROD001-M-Rojo")).thenReturn(false);
        when(productoVariacionRepository.save(any(ProductoVariacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductoVariacionDTO resultado = productoVariacionService.registrarVariacion(dto);

        assertEquals("PROD001-M-Rojo", resultado.getSku());
        assertEquals(10, resultado.getCantidadDisponible());
    }

    @Test
    public void testRegistrarVariacion_existente() {
        CrearProductoVariacionRequestDTO dto = new CrearProductoVariacionRequestDTO();
        dto.setSkuProductoBase("PROD001");
        dto.setTalla("L");
        dto.setColor("Azul");
        dto.setStock(5);

        when(productoVariacionRepository.existsBySku("PROD001-L-Azul")).thenReturn(true);

        assertThrows(ProductoExistenteException.class,
                () -> productoVariacionService.registrarVariacion(dto));
    }

    @Test
    public void testObtenerPorSku_exitoso() {
        ProductoVariacion mock = ProductoVariacion.builder()
                .id(1L)
                .sku("SKU-001")
                .skuProductoBase("BASE-001")
                .talla("S")
                .color("Negro")
                .stock(20)
                .ubicacion("B2")
                .build();

        when(productoVariacionRepository.findBySku("SKU-001"))
                .thenReturn(Optional.of(mock));

        ProductoVariacionDTO dto = productoVariacionService.obtenerPorSku("SKU-001");

        assertEquals("SKU-001", dto.getSku());
        assertEquals(20, dto.getCantidadDisponible());
    }

    @Test
    public void testObtenerPorSku_noEncontrado() {
        when(productoVariacionRepository.findBySku("NO-EXISTE"))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoVariacionService.obtenerPorSku("NO-EXISTE"));
    }
}
