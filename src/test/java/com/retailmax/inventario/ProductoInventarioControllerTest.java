package com.retailmax.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmax.inventario.model.ProductoInventario;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        productoInventarioRepository.deleteAll();
    }

    @Test
    void testCrearProductoInventario() throws Exception {
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("TESTSKU123");
        producto.setCantidadDisponible(15);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(5);
        producto.setCantidadEnTransito(0);
        producto.setStock(15);
        producto.setUbicacionAlmacen("Z1");
        producto.setActivo(true); 
        producto.setFechaCreacion(LocalDateTime.now()); 
        producto.setFechaUltimaActualizacion(LocalDateTime.now()); 

        mockMvc.perform(post("/api/inventario/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
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

        mockMvc.perform(get("/api/inventario/stock/SKU321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadDisponible", is(30)));
    }

    @Test
    void testEliminarProducto() throws Exception {
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("ELIMINARSKU");
        producto.setCantidadDisponible(5);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(1);
        producto.setCantidadEnTransito(0);
        producto.setStock(5);
        producto.setUbicacionAlmacen("B3");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario saved = productoInventarioRepository.save(producto);

        // Asegúrate de tener un endpoint DELETE /api/inventario/{id}
        mockMvc.perform(delete("/api/inventario/" + saved.getId()))
                .andExpect(status().isNoContent());

        Optional<ProductoInventario> check = productoInventarioRepository.findById(saved.getId());
        assertFalse(check.isPresent());
    }
}
