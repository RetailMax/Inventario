package com.retailmax.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.repository.MovimientoStockRepository; // Import MovimientoStockRepository
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
    private MovimientoStockRepository movimientoStockRepository; // Inject MovimientoStockRepository

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PRODUCTOS_BASE_URL = "/api/inventario/productos";

    @BeforeEach
    void setup() {
        // IMPORTANT: Delete child records (MovimientoStock) first to avoid foreign key violations
        movimientoStockRepository.deleteAll(); // <--- ADDED THIS LINE
        productoInventarioRepository.deleteAll();
    }

    @Test
    void testCrearProductoInventario() throws Exception {
        // Actualizar la creación del DTO para incluir los nuevos campos de variaciones
        // (productoBaseSku, talla, color).
        // Se asume que para esta prueba, un producto base no tiene variaciones propias,
        // por lo que se pueden pasar valores nulos o vacíos si la lógica lo permite,
        // o un SKU base si este producto es en sí mismo una variante.
        AgregarProductoInventarioRequestDTO requestDTO = new AgregarProductoInventarioRequestDTO(
                "TESTSKU123", 15, "Z1", 5, "BASE_SKU_TEST", null, null);

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
        productoInventarioRepository.save(producto);

        mockMvc.perform(delete(PRODUCTOS_BASE_URL + "/" + skuToDelete))
                .andExpect(status().isNoContent());

        Optional<ProductoInventario> check = productoInventarioRepository.findBySku(skuToDelete);
        assertFalse(check.isPresent());
    }
}
