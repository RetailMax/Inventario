package com.retailmax.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.repository.UmbralAlertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UmbralAlertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UmbralAlertaRepository umbralAlertaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        umbralAlertaRepository.deleteAll();
    }

    @Test
    void testRegistrarUmbralAlerta() throws Exception {
        UmbralAlertaDTO dto = UmbralAlertaDTO.builder()
                .sku("SKU-123")
                .tipoAlerta("BAJO_STOCK")
                .umbralCantidad(10)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaUltimaActualizacion(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/umbral-alerta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku", is("SKU-123")))
                .andExpect(jsonPath("$.tipoAlerta", is("BAJO_STOCK")))
                .andExpect(jsonPath("$.umbralCantidad", is(10)))
                .andExpect(jsonPath("$.activo", is(true)));
    }

    @Test
    void testConsultarUmbralAlertaPorSku() throws Exception {
        UmbralAlertaDTO dto = UmbralAlertaDTO.builder()
                .sku("SKU-CONSULTA")
                .tipoAlerta("BAJO_STOCK")
                .umbralCantidad(20)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaUltimaActualizacion(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/umbral-alerta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/umbral-alerta/SKU-CONSULTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("SKU-CONSULTA")))
                .andExpect(jsonPath("$.umbralCantidad", is(20)));
    }

    @Test
    void testEliminarUmbralAlerta() throws Exception {
        UmbralAlertaDTO dto = UmbralAlertaDTO.builder()
                .sku("SKU-ELIMINAR")
                .tipoAlerta("BAJO_STOCK")
                .umbralCantidad(5)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaUltimaActualizacion(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/umbral-alerta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/umbral-alerta/SKU-ELIMINAR"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/umbral-alerta/SKU-ELIMINAR"))
                .andExpect(status().isNotFound());
    }
}
