package com.retailmax.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.AjusteStockManualRequestDTO;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.EstadoStock;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoInventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoInventarioRepository productoInventarioRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PRODUCTOS_BASE_URL = "/api/inventario/productos";

    @BeforeEach
    void setup() {
        movimientoStockRepository.deleteAll();
        productoInventarioRepository.deleteAll();
    }

    @Test
    void testCrearProductoInventario() throws Exception {
        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO(
                "TESTSKU123",
                15,
                "Z1",
                5,
                "BASE_SKU_TEST",
                null,
                null,
                EstadoStock.DISPONIBLE
        );

        mockMvc.perform(post(PRODUCTOS_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku", is("TESTSKU123")));
    }

    @Test
    void testBuscarProductoPorSku() throws Exception {
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU321");
        producto.setCantidadDisponible(30);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(5);
        producto.setCantidadEnTransito(0);
        producto.setStock(30);
        producto.setUbicacionAlmacen("A2");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(producto);

        mockMvc.perform(get(PRODUCTOS_BASE_URL + "/SKU321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadDisponible", is(30)));
    }

    @Test
    void testEliminarProducto() throws Exception {
        String skuToDelete = "ELIMINARSKU";
        ProductoInventario producto = new ProductoInventario();
        producto.setSku(skuToDelete);
        producto.setCantidadDisponible(5);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(1);
        producto.setCantidadEnTransito(0);
        producto.setStock(5);
        producto.setUbicacionAlmacen("B3");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(producto);

        mockMvc.perform(delete(PRODUCTOS_BASE_URL + "/" + skuToDelete))
                .andExpect(status().isNoContent());

        Optional<ProductoInventario> check = productoInventarioRepository.findBySku(skuToDelete);
        assertFalse(check.isPresent());
    }

    @Test
    void testConsultarTodosLosProductos_Success() throws Exception {
        // Setup: Add some products
        ProductoInventario p1 = new ProductoInventario();
        p1.setSku("PROD-001");
        p1.setCantidadDisponible(100);
        p1.setCantidadEnTransito(0);
        p1.setStock(100);
        p1.setCantidadReservada(0);
        p1.setCantidadMinimaStock(10);
        p1.setUbicacionAlmacen("A1");
        p1.setActivo(true);
        p1.setFechaCreacion(LocalDateTime.now());
        p1.setFechaUltimaActualizacion(LocalDateTime.now());
        p1.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(p1);

        ProductoInventario p2 = new ProductoInventario();
        p2.setSku("PROD-002");
        p2.setCantidadDisponible(50);
        p2.setCantidadEnTransito(0);
        p2.setStock(50);
        p2.setCantidadReservada(0);
        p2.setCantidadMinimaStock(5);
        p2.setUbicacionAlmacen("A2");
        p2.setActivo(true);
        p2.setFechaCreacion(LocalDateTime.now());
        p2.setFechaUltimaActualizacion(LocalDateTime.now());
        p2.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(p2);

        mockMvc.perform(get(PRODUCTOS_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productoInventarioDTOList.length()", is(2)))
                .andExpect(jsonPath("$._embedded.productoInventarioDTOList[0].sku", is("PROD-001")))
                .andExpect(jsonPath("$._embedded.productoInventarioDTOList[1].sku", is("PROD-002")));
    }

    @Test
    void testVerificarYNotificarStockBajo_Success() throws Exception {
        // Setup: Create products, one with low stock
        ProductoInventario pLowStock = new ProductoInventario();
        pLowStock.setSku("LOW-SKU");
        pLowStock.setCantidadDisponible(8); // Below threshold of 10
        pLowStock.setCantidadEnTransito(0);
        pLowStock.setStock(8);
        pLowStock.setCantidadReservada(0);
        pLowStock.setCantidadMinimaStock(10); // Threshold
        pLowStock.setUbicacionAlmacen("B1");
        pLowStock.setActivo(true);
        pLowStock.setFechaCreacion(LocalDateTime.now());
        pLowStock.setFechaUltimaActualizacion(LocalDateTime.now());
        pLowStock.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(pLowStock);

        ProductoInventario pNormalStock = new ProductoInventario();
        pNormalStock.setSku("NORMAL-SKU");
        pNormalStock.setCantidadDisponible(20); // Above threshold
        pNormalStock.setCantidadEnTransito(0);
        pNormalStock.setStock(20);
        pNormalStock.setCantidadReservada(0);
        pNormalStock.setCantidadMinimaStock(10);
        pNormalStock.setUbicacionAlmacen("B2");
        pNormalStock.setActivo(true);
        pNormalStock.setFechaCreacion(LocalDateTime.now());
        pNormalStock.setFechaUltimaActualizacion(LocalDateTime.now());
        pNormalStock.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(pNormalStock);

        int umbral = 10;
        mockMvc.perform(get(PRODUCTOS_BASE_URL + "/bajo-stock/" + umbral)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productoInventarioDTOList.length()", is(1)))
                .andExpect(jsonPath("$._embedded.productoInventarioDTOList[0].sku", is("LOW-SKU")));
    }

    @Test
    void testVerificarYNotificarStockExcesivo_Success() throws Exception {
        // Setup: Create products, one with excessive stock
        ProductoInventario pExcessStock = new ProductoInventario();
        pExcessStock.setSku("EXCESS-SKU");
        pExcessStock.setCantidadEnTransito(0);
        pExcessStock.setCantidadDisponible(150); // Above threshold of 100
        pExcessStock.setStock(150);
        pExcessStock.setCantidadReservada(0);
        pExcessStock.setCantidadMinimaStock(10);
        pExcessStock.setUbicacionAlmacen("C1");
        pExcessStock.setActivo(true);
        pExcessStock.setFechaCreacion(LocalDateTime.now());
        pExcessStock.setFechaUltimaActualizacion(LocalDateTime.now());
        pExcessStock.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(pExcessStock);

        ProductoInventario pNormalStock = new ProductoInventario();
        pNormalStock.setSku("NORMAL-SKU-2");
        pNormalStock.setCantidadDisponible(80); // Below threshold
        pNormalStock.setCantidadEnTransito(0);
        pNormalStock.setStock(80);
        pNormalStock.setCantidadReservada(0);
        pNormalStock.setCantidadMinimaStock(10);
        pNormalStock.setUbicacionAlmacen("C2");
        pNormalStock.setActivo(true);
        pNormalStock.setFechaCreacion(LocalDateTime.now());
        pNormalStock.setFechaUltimaActualizacion(LocalDateTime.now());
        pNormalStock.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(pNormalStock);

        int umbral = 100;
        mockMvc.perform(get(PRODUCTOS_BASE_URL + "/exceso-stock/" + umbral)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productoInventarioDTOList.length()", is(1)))
                .andExpect(jsonPath("$._embedded.productoInventarioDTOList[0].sku", is("EXCESS-SKU")));
    }

    @Test
    void testAjustarStockManual_EntradaExitosa() throws Exception {
        // 1. Setup: Crear un producto existente
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("AJUSTE-SKU");
        producto.setCantidadDisponible(50);
        producto.setCantidadEnTransito(0);
        producto.setStock(50);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(5);
        producto.setCantidadEnTransito(0);
        producto.setUbicacionAlmacen("C1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(producto);

        // 2. Preparar el DTO de la solicitud
        AjusteStockManualRequestDTO ajusteDTO = new AjusteStockManualRequestDTO();
        ajusteDTO.setSku("AJUSTE-SKU");
        ajusteDTO.setCantidad(20);
        ajusteDTO.setTipoMovimiento("ENTRADA");
        ajusteDTO.setMotivo("Ajuste manual de prueba");

        // 3. Ejecutar y verificar
        mockMvc.perform(post(PRODUCTOS_BASE_URL + "/stock/ajuste-manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ajusteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("AJUSTE-SKU")))
                .andExpect(jsonPath("$.cantidadDisponible", is(70))); // 50 (inicial) + 20 (entrada) = 70
    }

    @Test
    void testAjustarStockManual_TipoMovimientoInvalido() throws Exception {
        // Preparar el DTO de la solicitud con tipo inválido
        AjusteStockManualRequestDTO ajusteDTO = new AjusteStockManualRequestDTO();
        ajusteDTO.setSku("AJUSTE-SKU");
        ajusteDTO.setCantidad(10);
        ajusteDTO.setTipoMovimiento("TIPO_INVALIDO");
        ajusteDTO.setMotivo("Prueba con tipo inválido");

        // Ejecutar y verificar que se recibe un Bad Request
        mockMvc.perform(post(PRODUCTOS_BASE_URL + "/stock/ajuste-manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ajusteDTO)))
                .andExpect(status().isBadRequest());
    }
}
