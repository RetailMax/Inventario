package com.retailmax.inventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.AjusteStockManualRequestDTO;
import com.retailmax.inventario.dto.ReservaStockRequestDTO;
import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.LiberarStockRequestDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testActualizarProducto_Success() throws Exception {
        // 1. Setup: Crear un producto existente para actualizar
        String skuToUpdate = "UPDATE-SKU";
        ProductoInventario productoExistente = new ProductoInventario();
        productoExistente.setSku(skuToUpdate);
        productoExistente.setCantidadDisponible(100);
        productoExistente.setCantidadReservada(0);
        productoExistente.setCantidadMinimaStock(10);
        productoExistente.setCantidadEnTransito(0); // Campo NOT NULL que faltaba
        productoExistente.setStock(100); // Campo NOT NULL que también faltaba
        productoExistente.setUbicacionAlmacen("OLD-LOC");
        productoExistente.setProductoBaseSku("OLD-BASE");
        productoExistente.setTalla("M");
        productoExistente.setColor("Red");
        productoExistente.setActivo(true);
        productoExistente.setFechaCreacion(LocalDateTime.now().minusDays(5));
        productoExistente.setFechaUltimaActualizacion(LocalDateTime.now().minusDays(5));
        productoExistente.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(productoExistente);

        // 2. Preparar el DTO con los datos actualizados
        AgregarProductoInventarioRequestDTO updateRequestDTO = new AgregarProductoInventarioRequestDTO(
                skuToUpdate, // SKU no se actualiza, pero se envía para coherencia
                productoExistente.getCantidadDisponible(), // Cantidad disponible no se actualiza por este endpoint
                "NEW-LOC",
                20, // Nueva cantidad mínima
                "NEW-BASE",
                "L",
                "Blue",
                EstadoStock.DISPONIBLE
        );

        // 3. Ejecutar la solicitud PUT y verificar
        mockMvc.perform(put(PRODUCTOS_BASE_URL + "/" + skuToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ubicacionAlmacen", is("NEW-LOC")))
                .andExpect(jsonPath("$.cantidadMinimaStock", is(20)))
                .andExpect(jsonPath("$.productoBaseSku", is("NEW-BASE")))
                .andExpect(jsonPath("$.talla", is("L")))
                .andExpect(jsonPath("$.color", is("Blue")));
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

    // --- Tests para validarDisponibilidadStock para aumentar cobertura de JaCoCo ---

    @Test
    void testValidarDisponibilidadStock_Disponible_ReturnsOk() throws Exception {
        // 1. Setup: Crear un producto con stock suficiente
        String sku = "VALID-SKU";
        ProductoInventario producto = new ProductoInventario();
        producto.setSku(sku);
        producto.setCantidadDisponible(20);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(5);
        producto.setCantidadEnTransito(0);
        producto.setStock(20);
        producto.setUbicacionAlmacen("A1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(producto);

        // 2. Preparar el DTO de la solicitud
        ReservaStockRequestDTO requestDTO = new ReservaStockRequestDTO();
        requestDTO.setSku(sku);
        requestDTO.setCantidad(10);

        // 3. Ejecutar y verificar
        mockMvc.perform(post(PRODUCTOS_BASE_URL + "/stock/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock disponible para reserva"));
    }

    @Test
    void testValidarDisponibilidadStock_Insuficiente_ReturnsConflict() throws Exception {
        // 1. Setup: Crear un producto con stock insuficiente
        String sku = "INSUF-SKU";
        ProductoInventario producto = new ProductoInventario();
        producto.setSku(sku);
        producto.setCantidadDisponible(5);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(5);
        producto.setCantidadEnTransito(0);
        producto.setStock(5);
        producto.setUbicacionAlmacen("A1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(producto);

        // 2. Preparar el DTO de la solicitud
        ReservaStockRequestDTO requestDTO = new ReservaStockRequestDTO();
        requestDTO.setSku(sku);
        requestDTO.setCantidad(10);

        // 3. Ejecutar y verificar
        mockMvc.perform(post(PRODUCTOS_BASE_URL + "/stock/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Stock insuficiente"));
    }

    @Test
    void testValidarDisponibilidadStock_ProductoNoEncontrado_ReturnsNotFound() throws Exception {
        // 1. Preparar el DTO para un SKU que no existe
        ReservaStockRequestDTO requestDTO = new ReservaStockRequestDTO();
        requestDTO.setSku("NON-EXISTENT-SKU");
        requestDTO.setCantidad(10);

        // 2. Ejecutar y verificar
        mockMvc.perform(post(PRODUCTOS_BASE_URL + "/stock/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    // --- Tests para actualizarStock y liberarStock para aumentar cobertura de JaCoCo ---

    @Test
    void testActualizarStock_Success() throws Exception {
        // 1. Setup: Crear un producto existente
        String sku = "STOCK-UPDATE-SKU";
        ProductoInventario producto = new ProductoInventario();
        producto.setSku(sku);
        producto.setCantidadDisponible(50);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(10);
        producto.setCantidadEnTransito(0);
        producto.setStock(50);
        producto.setUbicacionAlmacen("D1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setEstado(EstadoStock.DISPONIBLE);
        productoInventarioRepository.save(producto);

        // 2. Preparar el DTO de la solicitud de actualización
        ActualizarStockRequestDTO requestDTO = new ActualizarStockRequestDTO(
                sku,
                25, // Cantidad a agregar
                "ENTRADA",
                "COMPRA-PROV-XYZ",
                "Recepción de nueva mercancía"
        );

        // 3. Ejecutar la solicitud PUT y verificar
        mockMvc.perform(put(PRODUCTOS_BASE_URL + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is(sku)))
                .andExpect(jsonPath("$.cantidadDisponible", is(75))); // 50 + 25
    }

    @Test
    void testLiberarStock_Success() throws Exception {
        // 1. Setup: Crear un producto con stock reservado
        String sku = "LIBERAR-SKU";
        ProductoInventario producto = new ProductoInventario();
        producto.setSku(sku);
        producto.setCantidadDisponible(80);
        producto.setCantidadReservada(20); // Stock reservado
        producto.setCantidadMinimaStock(10);
        producto.setCantidadEnTransito(0);
        producto.setStock(100); // Stock total
        producto.setUbicacionAlmacen("E1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setEstado(EstadoStock.RESERVADO);
        productoInventarioRepository.save(producto);

        // 2. Preparar el DTO de la solicitud de liberación
        LiberarStockRequestDTO requestDTO = new LiberarStockRequestDTO(sku, 15, "Venta cancelada por cliente");

        // 3. Ejecutar la solicitud POST y verificar
        mockMvc.perform(post(PRODUCTOS_BASE_URL + "/stock/liberar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock liberado exitosamente."));

        // 4. Verificar el estado final en la base de datos
        ProductoInventario productoActualizado = productoInventarioRepository.findBySku(sku).orElseThrow();
        assertEquals(95, productoActualizado.getCantidadDisponible()); // 80 + 15
        assertEquals(5, productoActualizado.getCantidadReservada());   // 20 - 15
    }

    @Test
    void testLiberarStock_FailsWhenNotEnoughReserved() throws Exception {
        // 1. Setup: Reutilizamos el test anterior, pero intentamos liberar más de lo reservado
        testLiberarStock_Success(); // Asegura que el producto "LIBERAR-SKU" exista
        LiberarStockRequestDTO requestDTO = new LiberarStockRequestDTO("LIBERAR-SKU", 30, "Intento de liberar demasiado");

        // 2. Ejecutar y verificar que falla con Bad Request
        mockMvc.perform(post(PRODUCTOS_BASE_URL + "/stock/liberar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }
}
