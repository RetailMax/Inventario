package com.retailmax.inventario;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MovimientoStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Autowired
    private ProductoInventarioRepository productoInventarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/inventario/movimientos";

    private ProductoInventario producto1;
    private ProductoInventario producto2;

    @BeforeEach
    void setup() {
        movimientoStockRepository.deleteAll();
        productoInventarioRepository.deleteAll();

        // Create products to associate movements with
        producto1 = new ProductoInventario();
        producto1.setSku("PROD-1");
        producto1.setStock(100);
        producto1.setCantidadDisponible(100);
        producto1.setCantidadReservada(0);
        producto1.setCantidadEnTransito(0);
        producto1.setCantidadMinimaStock(10);
        producto1.setActivo(true);
        producto1.setFechaCreacion(LocalDateTime.now());
        producto1.setFechaUltimaActualizacion(LocalDateTime.now());
        productoInventarioRepository.save(producto1);

        producto2 = new ProductoInventario();
        producto2.setSku("PROD-2");
        producto2.setStock(50);
        producto2.setCantidadDisponible(50);
        producto2.setCantidadReservada(0);
        producto2.setCantidadEnTransito(0);
        producto2.setCantidadMinimaStock(5);
        producto2.setActivo(true);
        producto2.setFechaCreacion(LocalDateTime.now());
        producto2.setFechaUltimaActualizacion(LocalDateTime.now());
        productoInventarioRepository.save(producto2);
    }

    private MovimientoStock createAndSaveMovimiento(ProductoInventario producto, TipoMovimiento tipo, int cantidad) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoInventario(producto);
        movimiento.setSku(producto.getSku());
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidadMovida(cantidad);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setMotivo("Test movement");
        // Simular el cálculo del stock final, que es un campo NOT NULL en la BD
        int stockAntes = producto.getStock();
        movimiento.setStockFinalDespuesMovimiento(Math.max(0, stockAntes + cantidad));
        return movimientoStockRepository.save(movimiento);
    }

    @Test
    void testGetMovimientosRoot() throws Exception {
        // Setup: Create some movements
        createAndSaveMovimiento(producto1, TipoMovimiento.ENTRADA, 10);
        createAndSaveMovimiento(producto2, TipoMovimiento.SALIDA, -5);

        // Execute & Verify
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                // El método del controlador getMovimientosRoot() devuelve una lista vacía por diseño.
                // La prueba debe verificar que no hay elementos embebidos, ya que Spring HATEOAS omite la clave "_embedded" para colecciones vacías.
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void testGetMovimientoById_Success() throws Exception {
        // Setup
        MovimientoStock movimiento = createAndSaveMovimiento(producto1, TipoMovimiento.AJUSTE, 2);

        // Execute & Verify
        mockMvc.perform(get(BASE_URL + "/id/" + movimiento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(movimiento.getId().intValue())))
                .andExpect(jsonPath("$.sku", is(producto1.getSku())))
                .andExpect(jsonPath("$.cantidadMovida", is(2)));
    }

    @Test
    void testGetMovimientoById_NotFound() throws Exception {
        // Execute & Verify
        mockMvc.perform(get(BASE_URL + "/id/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerHistorialMovimientos() throws Exception {
        // Setup: Create movements for both products
        createAndSaveMovimiento(producto1, TipoMovimiento.ENTRADA, 20);
        createAndSaveMovimiento(producto1, TipoMovimiento.SALIDA, -5);
        createAndSaveMovimiento(producto2, TipoMovimiento.ENTRADA, 30); // This one should not be in the result

        // Execute & Verify
        // La URL correcta según el controlador es /{sku}, no /historial/{sku}.
        mockMvc.perform(get(BASE_URL + "/" + producto1.getSku()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.movimientoStockDTOList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.movimientoStockDTOList[0].sku", is(producto1.getSku())))
                .andExpect(jsonPath("$._embedded.movimientoStockDTOList[1].sku", is(producto1.getSku())));
    }

    // --- Test para registrarMovimiento para aumentar cobertura de JaCoCo ---

    @Test
    void testRegistrarMovimiento_Success() throws Exception {
        // 1. Setup: We use 'producto1' created in the setup method.
        // The initial stock is 100.

        // 2. Prepare the request body (as a MovimientoStock entity, as per the controller signature)
        MovimientoStock movimientoRequest = new MovimientoStock();
        movimientoRequest.setSku(producto1.getSku());
        movimientoRequest.setCantidadMovida(25);
        movimientoRequest.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimientoRequest.setMotivo("Recepción de mercancía de prueba");
        movimientoRequest.setReferenciaExterna("TEST-PO-001");
        // fechaMovimiento will be set by the service if null

        // 3. Execute & Verify
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoRequest)))
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(jsonPath("$.sku", is(producto1.getSku())))
                .andExpect(jsonPath("$.cantidadMovida", is(25)))
                .andExpect(jsonPath("$.tipoMovimiento", is("ENTRADA")))
                .andExpect(jsonPath("$.stockFinalDespuesMovimiento", is(125))) // 100 initial + 25
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.producto-asociado").exists());
    }
}