package com.retailmax.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovimientoStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoInventarioRepository productoInventarioRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        movimientoStockRepository.deleteAll();
        productoInventarioRepository.deleteAll();

        ProductoInventario producto = new ProductoInventario();
        producto.setSku("MOV-SKU-001");
        producto.setCantidadDisponible(50);
        producto.setCantidadReservada(0);
        producto.setCantidadMinimaStock(10);
        producto.setCantidadEnTransito(0);
        producto.setStock(50);  // total
        producto.setUbicacionAlmacen("BODEGA-1");
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());

        productoInventarioRepository.save(producto);
    }

    @Test
    void testRegistrarMovimientoEntrada() throws Exception {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setSku("MOV-SKU-001");
        movimiento.setCantidadMovida(10);
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        mockMvc.perform(post("/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movimiento)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku", is("MOV-SKU-001")))
                .andExpect(jsonPath("$.tipoMovimiento", is("ENTRADA")));
    }

    @Test
    void testRegistrarMovimientoProductoInexistente() throws Exception {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setSku("INEXISTENTE");
        movimiento.setCantidadMovida(10);
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        mockMvc.perform(post("/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movimiento)))
                .andExpect(status().isBadRequest());
    }
}
