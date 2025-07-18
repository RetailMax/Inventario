package com.retailmax.inventario.controller;

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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductoInventarioRepository productoInventarioRepository;

    @BeforeEach
    void setup() {
        productoInventarioRepository.deleteAll();
    }

    @Test
    void testCompararStockFisico_SinDiscrepancias() throws Exception {
        // Crear producto con stock 10
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU-TEST");
        producto.setCantidadDisponible(10);
        producto.setCantidadMinimaStock(1);
        producto.setCantidadReservada(0);
        producto.setCantidadEnTransito(0);
        producto.setStock(10);
        producto.setUbicacionAlmacen("A1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        productoInventarioRepository.save(producto);

        Map<String, Integer> stockFisico = new HashMap<>();
        stockFisico.put("SKU-TEST", 10);

        mockMvc.perform(post("/api/auditoria/comparar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockFisico)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCompararStockFisico_ConDiscrepancia() throws Exception {
        // Crear producto con stock 10
        ProductoInventario producto = new ProductoInventario();
        producto.setSku("SKU-DESC");
        producto.setCantidadDisponible(10);
        producto.setCantidadMinimaStock(1);
        producto.setCantidadReservada(0);
        producto.setCantidadEnTransito(0);
        producto.setStock(10);
        producto.setUbicacionAlmacen("A1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        productoInventarioRepository.save(producto);

        Map<String, Integer> stockFisico = new HashMap<>();
        stockFisico.put("SKU-DESC", 7); // Discrepancia: 10 en sistema, 7 físico

        mockMvc.perform(post("/api/auditoria/comparar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockFisico)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku", is("SKU-DESC")))
                .andExpect(jsonPath("$[0].stockSistema", is(10)))
                .andExpect(jsonPath("$[0].stockFisico", is(7)))
                .andExpect(jsonPath("$[0].diferencia", is(-3)))
                .andExpect(jsonPath("$[0].motivo", containsString("Diferencia")));
    }

    @Test
    void testCompararStockFisico_ProductoNoExiste() throws Exception {
        // No creamos el producto en sistema
        Map<String, Integer> stockFisico = new HashMap<>();
        stockFisico.put("SKU-NOEXISTE", 5);

        mockMvc.perform(post("/api/auditoria/comparar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockFisico)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku", is("SKU-NOEXISTE")))
                .andExpect(jsonPath("$[0].stockSistema", is(0)))
                .andExpect(jsonPath("$[0].stockFisico", is(5)))
                .andExpect(jsonPath("$[0].diferencia", is(5)))
                .andExpect(jsonPath("$[0].motivo", containsString("no registrado")));
    }

    @Test
    void testCompararStockFisico_MapaVacio() throws Exception {
        Map<String, Integer> stockFisico = new HashMap<>();
        mockMvc.perform(post("/api/auditoria/comparar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockFisico)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
