package com.retailmax.inventario;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.UmbralAlerta;
import com.retailmax.inventario.model.enums.TipoAlerta;
import com.retailmax.inventario.repository.UmbralAlertaRepository;
import com.retailmax.inventario.service.UmbralAlertaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Importación clave: MockBean

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class UmbralAlertaServiceTest {

    @Autowired // El servicio real que vamos a probar
    private UmbralAlertaService umbralAlertaService;

    // Usamos @MockBean para crear un mock del repositorio y que Spring lo inyecte.
    // Esto resuelve el conflicto de definición de beans y permite la carga del contexto.
    @MockBean
    private UmbralAlertaRepository umbralAlertaRepository;

    // *** RECUERDA ELIMINAR O COMENTAR LA CLASE DE CONFIGURACIÓN ANIDADA QUE CAUSABA EL CONFLICTO ***
    /*
    @TestConfiguration
    static class UmbralAlertaServiceTestConfiguration {
        @Bean
        public UmbralAlertaRepository umbralAlertaRepository() {
            return mock(UmbralAlertaRepository.class);
        }
    }
    */

    @BeforeEach
    void setUp() {
        // Reiniciar el mock antes de cada prueba para asegurar un estado limpio
        reset(umbralAlertaRepository);
    }

    /**
     * Prueba para verificar la creación exitosa de un umbral de alerta.
     */
    @Test
    void testCrearUmbralAlerta_Success() {
        // Datos de prueba
        UmbralAlertaDTO requestDTO = crearUmbralAlertaDTO("SKU001", "BAJO_STOCK", 10, true);
        UmbralAlerta umbralGuardado = crearUmbralAlerta("SKU001", TipoAlerta.BAJO_STOCK, 10, true);
        umbralGuardado.setId(1L);

        // Simular el comportamiento del repositorio:
        // 1. findBySku devuelve Optional.empty() (no existe un umbral con ese SKU)
        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.empty());
        // 2. save devuelve el umbral que se va a guardar
        when(umbralAlertaRepository.save(any(UmbralAlerta.class))).thenReturn(umbralGuardado);

        // Ejecutar el método del servicio
        UmbralAlertaDTO result = umbralAlertaService.crearUmbralAlerta(requestDTO);

        // Verificar los resultados
        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        assertEquals("BAJO_STOCK", result.getTipoAlerta());
        assertEquals(10, result.getUmbralCantidad());
        assertTrue(result.getActivo());
        assertNotNull(result.getFechaCreacion());
        assertNotNull(result.getFechaUltimaActualizacion());

        // Verificar que los métodos del repositorio fueron llamados
        verify(umbralAlertaRepository, times(1)).findBySku("SKU001");
        verify(umbralAlertaRepository, times(1)).save(any(UmbralAlerta.class));
    }

    /**
     * Prueba para verificar que se lanza una excepción si el umbral ya existe al intentar crearlo.
     */
    @Test
    void testCrearUmbralAlerta_AlreadyExists() {
        // Datos de prueba
        UmbralAlertaDTO requestDTO = crearUmbralAlertaDTO("SKU001", "BAJO_STOCK", 10, true);
        UmbralAlerta umbralExistente = crearUmbralAlerta("SKU001", TipoAlerta.BAJO_STOCK, 10, true);

        // Simular el comportamiento del repositorio: findBySku devuelve un Optional.of() (el umbral ya existe)
        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.of(umbralExistente));

        // Verificar que se lanza la excepción correcta
        assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(requestDTO));

        // Verificar que save no fue llamado ya que el umbral ya existía
        verify(umbralAlertaRepository, times(1)).findBySku("SKU001");
        verify(umbralAlertaRepository, never()).save(any(UmbralAlerta.class));
    }

    /**
     * Prueba para verificar la actualización exitosa de un umbral de alerta.
     */
    @Test
    void testActualizarUmbralAlerta_Success() {
        // Datos de prueba
        String sku = "SKU001";
        UmbralAlerta umbralExistente = crearUmbralAlerta(sku, TipoAlerta.BAJO_STOCK, 10, true);
        umbralExistente.setId(1L);
        UmbralAlertaDTO requestDTO = crearUmbralAlertaDTO(sku, "EXCESO_STOCK", 50, false);

        // Simular el comportamiento del repositorio
        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.of(umbralExistente));
        when(umbralAlertaRepository.save(any(UmbralAlerta.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Devuelve el mismo objeto pasado

        // Ejecutar el método del servicio
        UmbralAlertaDTO result = umbralAlertaService.actualizarUmbralAlerta(sku, requestDTO);

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(sku, result.getSku());
        assertEquals("EXCESO_STOCK", result.getTipoAlerta());
        assertEquals(50, result.getUmbralCantidad());
        assertFalse(result.getActivo());
        verify(umbralAlertaRepository, times(1)).findBySku(sku);
        verify(umbralAlertaRepository, times(1)).save(any(UmbralAlerta.class));
    }

    /**
     * Prueba para verificar que se lanza una excepción si el umbral no es encontrado al intentar actualizar.
     */
    @Test
    void testActualizarUmbralAlerta_NotFound() {
        String sku = "SKU001";
        UmbralAlertaDTO requestDTO = crearUmbralAlertaDTO(sku, "EXCESO_STOCK", 50, false);

        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> umbralAlertaService.actualizarUmbralAlerta(sku, requestDTO));
        verify(umbralAlertaRepository, times(1)).findBySku(sku);
        verify(umbralAlertaRepository, never()).save(any(UmbralAlerta.class));
    }

    /**
     * Prueba para verificar la consulta exitosa de un umbral por SKU.
     */
    @Test
    void testConsultarUmbralAlertaPorSku_Found() {
        String sku = "SKU001";
        UmbralAlerta umbral = crearUmbralAlerta(sku, TipoAlerta.BAJO_STOCK, 10, true);
        umbral.setId(1L);

        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.of(umbral));

        UmbralAlertaDTO found = umbralAlertaService.consultarUmbralAlertaPorSku(sku);

        assertNotNull(found);
        assertEquals(sku, found.getSku());
        assertEquals("BAJO_STOCK", found.getTipoAlerta());
        verify(umbralAlertaRepository, times(1)).findBySku(sku);
    }

    /**
     * Prueba para verificar que se lanza una excepción si el umbral no se encuentra por SKU.
     */
    @Test
    void testConsultarUmbralAlertaPorSku_NotFound() {
        String sku = "SKU001";
        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> umbralAlertaService.consultarUmbralAlertaPorSku(sku));
        verify(umbralAlertaRepository, times(1)).findBySku(sku);
    }

    /**
     * Prueba para verificar la consulta de todos los umbrales de alerta.
     */
    @Test
    void testConsultarTodosLosUmbrales() {
        UmbralAlerta umbral1 = crearUmbralAlerta("SKU001", TipoAlerta.BAJO_STOCK, 10, true);
        UmbralAlerta umbral2 = crearUmbralAlerta("SKU002", TipoAlerta.EXCESO_STOCK, 100, false);

        when(umbralAlertaRepository.findAll()).thenReturn(Arrays.asList(umbral1, umbral2));

        List<UmbralAlertaDTO> umbrales = umbralAlertaService.consultarTodosLosUmbrales();

        assertNotNull(umbrales);
        assertEquals(2, umbrales.size());
        assertEquals("SKU001", umbrales.get(0).getSku());
        assertEquals("SKU002", umbrales.get(1).getSku());
        verify(umbralAlertaRepository, times(1)).findAll();
    }

    /**
     * Prueba para verificar la consulta de umbrales activos por tipo.
     */
    @Test
    void testConsultarUmbralesActivosPorTipo() {
        TipoAlerta tipoAlerta = TipoAlerta.BAJO_STOCK;
        UmbralAlerta umbralActivo = crearUmbralAlerta("SKU001", TipoAlerta.BAJO_STOCK, 10, true);
        UmbralAlerta umbralInactivo = crearUmbralAlerta("SKU002", TipoAlerta.BAJO_STOCK, 5, false);
        UmbralAlerta umbralOtroTipo = crearUmbralAlerta("SKU003", TipoAlerta.EXCESO_STOCK, 50, true);

        // Mockeamos findAll y luego el servicio filtra en memoria
        when(umbralAlertaRepository.findAll()).thenReturn(Arrays.asList(umbralActivo, umbralInactivo, umbralOtroTipo));

        List<UmbralAlertaDTO> umbrales = umbralAlertaService.consultarUmbralesActivosPorTipo(tipoAlerta);

        assertNotNull(umbrales);
        assertEquals(1, umbrales.size());
        assertEquals("SKU001", umbrales.get(0).getSku());
        assertEquals("BAJO_STOCK", umbrales.get(0).getTipoAlerta());
        assertTrue(umbrales.get(0).getActivo());
        verify(umbralAlertaRepository, times(1)).findAll();
    }

    /**
     * Prueba para verificar la eliminación exitosa de un umbral de alerta.
     */
    @Test
    void testEliminarUmbralAlerta_Success() {
        String sku = "SKU001";
        UmbralAlerta umbral = crearUmbralAlerta(sku, TipoAlerta.BAJO_STOCK, 10, true);
        umbral.setId(1L);

        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.of(umbral));
        doNothing().when(umbralAlertaRepository).delete(umbral);

        umbralAlertaService.eliminarUmbralAlerta(sku);

        verify(umbralAlertaRepository, times(1)).findBySku(sku);
        verify(umbralAlertaRepository, times(1)).delete(umbral);
    }

    /**
     * Prueba para verificar que se lanza una excepción si el umbral no se encuentra al intentar eliminar.
     */
    @Test
    void testEliminarUmbralAlerta_NotFound() {
        String sku = "SKU001";
        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> umbralAlertaService.eliminarUmbralAlerta(sku));
        verify(umbralAlertaRepository, times(1)).findBySku(sku);
        verify(umbralAlertaRepository, never()).delete(any(UmbralAlerta.class));
    }


    /**
     * Método auxiliar para crear una instancia de UmbralAlertaDTO para las pruebas.
     */
    private UmbralAlertaDTO crearUmbralAlertaDTO(String sku, String tipoAlerta, Integer umbralCantidad, Boolean activo) {
        return UmbralAlertaDTO.builder()
                .sku(sku)
                .tipoAlerta(tipoAlerta)
                .umbralCantidad(umbralCantidad)
                .activo(activo)
                .build();
    }

    /**
     * Método auxiliar para crear una instancia de UmbralAlerta para las pruebas.
     */
    private UmbralAlerta crearUmbralAlerta(String sku, TipoAlerta tipoAlerta, Integer umbralCantidad, Boolean activo) {
        UmbralAlerta umbral = new UmbralAlerta();
        umbral.setSku(sku);
        umbral.setTipoAlerta(tipoAlerta);
        umbral.setUmbralCantidad(umbralCantidad);
        umbral.setActivo(activo);
        umbral.setFechaCreacion(LocalDateTime.now());
        umbral.setFechaUltimaActualizacion(LocalDateTime.now());
        return umbral;
    }
}
