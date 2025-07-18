package com.retailmax.inventario.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CatalogoProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void setup() {
        // Si hay un repositorio para poblar el catálogo, se puede limpiar/agregar aquí.
        // De lo contrario, depende del estado de la BD.
    }

    @Test
    void testObtenerCatalogo_ListaVacia() throws Exception {
        // Si la BD está vacía, debe devolver una lista vacía
        mockMvc.perform(get("/api/catalogo/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testObtenerCatalogo_ConProductos() throws Exception {
        // Este test asume que hay una forma de poblar el catálogo.
        // Si existe un servicio/repo, se debería poblar aquí. Ejemplo:
        // catalogoProductoRepository.save(new ProductoCatalogo(...));
        //
        // Como ejemplo, solo se verifica que el endpoint responde OK.
        mockMvc.perform(get("/api/catalogo/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        // Si se quiere, se puede deserializar la respuesta para validaciones más avanzadas:
        // String json = mockMvc.perform(...).andReturn().getResponse().getContentAsString();
        // List<ProductoCatalogoDTO> productos = objectMapper.readValue(json, new TypeReference<>() {});
    }
}
