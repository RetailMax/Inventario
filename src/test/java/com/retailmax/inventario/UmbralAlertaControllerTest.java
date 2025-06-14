package com.retailmax.inventario; // Asegúrate de que este sea el paquete correcto

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

    // Definimos la URL base del controlador para evitar repeticiones y errores
    private static final String BASE_URL = "/api/inventario/umbrales";

    @BeforeEach
    void setup() {
        // Asegúrate de que este repositorio no sea un mock si la intención es que los tests de controlador
        // interactúen con una BD real (como Oracle, o H2 en memoria para tests).
        // Si este repositorio debe ser un mock, entonces este test es más un @WebMvcTest
        // y el setup debería ser para resetear el mock.
        // Pero asumiendo que es un test de integración, deleteAll() es correcto.
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

        // Ruta corregida: BASE_URL en lugar de /api/umbral-alerta
        mockMvc.perform(post(BASE_URL)
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

        // Primero creamos el recurso para poder consultarlo
        mockMvc.perform(post(BASE_URL) // Ruta corregida
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Ruta corregida: BASE_URL + /{sku}
        mockMvc.perform(get(BASE_URL + "/SKU-CONSULTA"))
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

        // Primero creamos el recurso para poder eliminarlo
        mockMvc.perform(post(BASE_URL) // Ruta corregida
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Ruta corregida: BASE_URL + /{sku}
        mockMvc.perform(delete(BASE_URL + "/SKU-ELIMINAR"))
                .andExpect(status().isNoContent());

        // Ruta corregida: BASE_URL + /{sku}
        mockMvc.perform(get(BASE_URL + "/SKU-ELIMINAR"))
                .andExpect(status().isNotFound());
    }
}
