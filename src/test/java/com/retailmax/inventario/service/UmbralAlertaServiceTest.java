package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.UmbralAlerta;
import com.retailmax.inventario.model.enums.TipoAlerta;
import com.retailmax.inventario.repository.UmbralAlertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UmbralAlertaServiceTest {

    @Mock
    private UmbralAlertaRepository umbralAlertaRepository;

    @InjectMocks
    private UmbralAlertaService umbralAlertaService;

    @BeforeEach
    void setUp() {
        reset(umbralAlertaRepository);
    }

    @Test
    void testCrearUmbralAlerta_Success() {
        UmbralAlertaDTO dto = crearDTO("SKU001", "BAJO_STOCK", 10, true);
        UmbralAlerta saved = crearEntidad("SKU001", TipoAlerta.BAJO_STOCK, 10, true);
        saved.setId(1L);

        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.empty());
        when(umbralAlertaRepository.save(any())).thenReturn(saved);

        UmbralAlertaDTO result = umbralAlertaService.crearUmbralAlerta(dto);

        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        verify(umbralAlertaRepository).findBySku("SKU001");
        verify(umbralAlertaRepository).save(any());
    }

    @Test
    void testCrearUmbralAlerta_AlreadyExists() {
        UmbralAlertaDTO dto = crearDTO("SKU001", "BAJO_STOCK", 10, true);
        when(umbralAlertaRepository.findBySku("SKU001"))
                .thenReturn(Optional.of(crearEntidad("SKU001", TipoAlerta.BAJO_STOCK, 10, true)));

        assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(dto));
        verify(umbralAlertaRepository).findBySku("SKU001");
        verify(umbralAlertaRepository, never()).save(any());
    }

    // --- Tests para aumentar cobertura de crearUmbralAlerta ---

    @Test
    void testCrearUmbralAlerta_CamposRequeridosNulosOEnBlanco_LanzaExcepcion() {
        // Caso 1: SKU es nulo
        UmbralAlertaDTO dtoSkuNull = crearDTO(null, "BAJO_STOCK", 10, true);
        var exSkuNull = assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(dtoSkuNull));
        assertTrue(exSkuNull.getMessage().contains("SKU es obligatorio"));

        // Caso 2: SKU está en blanco
        UmbralAlertaDTO dtoSkuBlank = crearDTO(" ", "BAJO_STOCK", 10, true);
        var exSkuBlank = assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(dtoSkuBlank));
        assertTrue(exSkuBlank.getMessage().contains("SKU es obligatorio"));

        // Caso 3: TipoAlerta es nulo
        UmbralAlertaDTO dtoTipoNull = crearDTO("SKU001", null, 10, true);
        var exTipoNull = assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(dtoTipoNull));
        assertTrue(exTipoNull.getMessage().contains("tipo de alerta es obligatorio"));

        // Caso 4: TipoAlerta está en blanco
        UmbralAlertaDTO dtoTipoBlank = crearDTO("SKU001", " ", 10, true);
        var exTipoBlank = assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(dtoTipoBlank));
        assertTrue(exTipoBlank.getMessage().contains("tipo de alerta es obligatorio"));

        // Caso 5: UmbralCantidad es nulo
        UmbralAlertaDTO dtoCantidadNull = crearDTO("SKU001", "BAJO_STOCK", null, true);
        var exCantidadNull = assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(dtoCantidadNull));
        assertTrue(exCantidadNull.getMessage().contains("cantidad del umbral es obligatoria"));

        // Caso 6: Activo es nulo
        UmbralAlertaDTO dtoActivoNull = crearDTO("SKU001", "BAJO_STOCK", 10, null);
        var exActivoNull = assertThrows(IllegalArgumentException.class, () -> umbralAlertaService.crearUmbralAlerta(dtoActivoNull));
        assertTrue(exActivoNull.getMessage().contains("estado activo es obligatorio"));

        // Verificamos que nunca se intentó guardar nada
        verify(umbralAlertaRepository, never()).save(any());
    }

    @Test
    void testActualizarUmbralAlerta_Success() {
        String sku = "SKU001";
        UmbralAlerta existente = crearEntidad(sku, TipoAlerta.BAJO_STOCK, 10, true);
        existente.setId(1L);
        UmbralAlertaDTO dto = crearDTO(sku, "EXCESO_STOCK", 50, false);

        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.of(existente));
        when(umbralAlertaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UmbralAlertaDTO result = umbralAlertaService.actualizarUmbralAlerta(sku, dto);

        assertNotNull(result);
        assertEquals(sku, result.getSku());
        assertEquals("EXCESO_STOCK", result.getTipoAlerta());
        verify(umbralAlertaRepository).findBySku(sku);
        verify(umbralAlertaRepository).save(any());
    }

    @Test
    void testActualizarUmbralAlerta_NotFound() {
        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                umbralAlertaService.actualizarUmbralAlerta("SKU001", crearDTO("SKU001", "EXCESO_STOCK", 50, false)));
        verify(umbralAlertaRepository).findBySku("SKU001");
    }

    // --- Test para aumentar cobertura de actualizarUmbralAlerta (ramas de if) ---

    @Test
    void testActualizarUmbralAlerta_PartialUpdate_SkipsNullOrBlankFields() {
        String sku = "SKU-PARTIAL";
        // Estado inicial de la entidad en la BD
        UmbralAlerta existente = crearEntidad(sku, TipoAlerta.BAJO_STOCK, 10, true);
        existente.setId(1L);

        // DTO de actualización solo con la cantidad. El resto de campos son nulos o en blanco.
        UmbralAlertaDTO dtoUpdate = UmbralAlertaDTO.builder()
                .sku(sku) // SKU se mantiene igual
                .umbralCantidad(99)
                .tipoAlerta("  ") // en blanco, no debería actualizar
                .activo(null)     // nulo, no debería actualizar
                .build();

        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.of(existente));
        ArgumentCaptor<UmbralAlerta> umbralCaptor = ArgumentCaptor.forClass(UmbralAlerta.class);
        when(umbralAlertaRepository.save(umbralCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        umbralAlertaService.actualizarUmbralAlerta(sku, dtoUpdate);

        // Verificamos que se llamó a save con la entidad actualizada
        verify(umbralAlertaRepository, times(1)).save(any(UmbralAlerta.class));
        UmbralAlerta savedUmbral = umbralCaptor.getValue();
        assertEquals(99, savedUmbral.getUmbralCantidad()); // Este se actualizó
        assertEquals(TipoAlerta.BAJO_STOCK, savedUmbral.getTipoAlerta()); // Este NO se actualizó (estaba en blanco)
        assertTrue(savedUmbral.getActivo()); // Este NO se actualizó (estaba nulo)
    }

    @Test
    void testActualizarUmbralAlerta_TipoAlertaBlank_SkipsUpdate() {
        // Caso: Se intenta actualizar el tipo de alerta con un valor en blanco.
        // Esperado: El tipo de alerta no se actualiza y se mantiene el valor original.
        String sku = "SKU-BLANK-TYPE";
        // Estado inicial de la entidad en la BD
        UmbralAlerta existente = crearEntidad(sku, TipoAlerta.EXCESO_STOCK, 50, true);
        existente.setId(2L);

        // DTO de actualización con tipoAlerta en blanco
        UmbralAlertaDTO dtoUpdate = UmbralAlertaDTO.builder()
                .sku(sku)
                .tipoAlerta("   ") // Valor en blanco
                .umbralCantidad(null)
                .activo(null)
                .build();

        when(umbralAlertaRepository.findBySku(sku)).thenReturn(Optional.of(existente));
        ArgumentCaptor<UmbralAlerta> umbralCaptor = ArgumentCaptor.forClass(UmbralAlerta.class);
        when(umbralAlertaRepository.save(umbralCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        umbralAlertaService.actualizarUmbralAlerta(sku, dtoUpdate);

        UmbralAlerta savedUmbral = umbralCaptor.getValue();
        assertEquals(TipoAlerta.EXCESO_STOCK, savedUmbral.getTipoAlerta()); // Verifica que no se actualizó
    }

    @Test
    void testConsultarUmbralAlertaPorSku_Found() {
        UmbralAlerta umbral = crearEntidad("SKU001", TipoAlerta.BAJO_STOCK, 10, true);
        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.of(umbral));

        UmbralAlertaDTO result = umbralAlertaService.consultarUmbralAlertaPorSku("SKU001");

        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        verify(umbralAlertaRepository).findBySku("SKU001");
    }

    @Test
    void testConsultarUmbralAlertaPorSku_NotFound() {
        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                umbralAlertaService.consultarUmbralAlertaPorSku("SKU001"));
        verify(umbralAlertaRepository).findBySku("SKU001");
    }

    @Test
    void testConsultarTodosLosUmbrales() {
        when(umbralAlertaRepository.findAll()).thenReturn(Arrays.asList(
                crearEntidad("SKU001", TipoAlerta.BAJO_STOCK, 10, true),
                crearEntidad("SKU002", TipoAlerta.EXCESO_STOCK, 100, false)
        ));

        List<UmbralAlertaDTO> result = umbralAlertaService.consultarTodosLosUmbrales();

        assertEquals(2, result.size());
        verify(umbralAlertaRepository).findAll();
    }

    @Test
    void testConsultarUmbralesActivosPorTipo() {
        when(umbralAlertaRepository.findAll()).thenReturn(Arrays.asList(
                crearEntidad("SKU001", TipoAlerta.BAJO_STOCK, 10, true),
                crearEntidad("SKU002", TipoAlerta.BAJO_STOCK, 10, false),
                crearEntidad("SKU003", TipoAlerta.EXCESO_STOCK, 50, true)
        ));

        List<UmbralAlertaDTO> result = umbralAlertaService.consultarUmbralesActivosPorTipo(TipoAlerta.BAJO_STOCK);

        assertEquals(1, result.size());
        assertEquals("SKU001", result.get(0).getSku());
        verify(umbralAlertaRepository).findAll();
    }

    @Test
    void testEliminarUmbralAlerta_Success() {
        UmbralAlerta umbral = crearEntidad("SKU001", TipoAlerta.BAJO_STOCK, 10, true);
        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.of(umbral));

        umbralAlertaService.eliminarUmbralAlerta("SKU001");

        verify(umbralAlertaRepository).findBySku("SKU001");
        verify(umbralAlertaRepository).delete(umbral);
    }

    @Test
    void testEliminarUmbralAlerta_NotFound() {
        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                umbralAlertaService.eliminarUmbralAlerta("SKU001"));
        verify(umbralAlertaRepository).findBySku("SKU001");
        verify(umbralAlertaRepository, never()).delete(any());
    }

    // Helpers
    private UmbralAlertaDTO crearDTO(String sku, String tipoAlerta, Integer cantidad, Boolean activo) {
        return UmbralAlertaDTO.builder()
                .sku(sku)
                .tipoAlerta(tipoAlerta)
                .umbralCantidad(cantidad)
                .activo(activo)
                .build();
    }

    private UmbralAlerta crearEntidad(String sku, TipoAlerta tipo, Integer cantidad, Boolean activo) {
        UmbralAlerta entidad = new UmbralAlerta();
        entidad.setSku(sku);
        entidad.setTipoAlerta(tipo);
        entidad.setUmbralCantidad(cantidad);
        entidad.setActivo(activo);
        entidad.setFechaCreacion(LocalDateTime.now());
        entidad.setFechaUltimaActualizacion(LocalDateTime.now());
        return entidad;
    }

    // --- Tests for TipoAlerta Enum to increase coverage ---

    @Test
    void testTipoAlerta_getDescripcion() {
        // This test covers the getDescripcion() method of the enum.
        assertEquals("Bajo Stock", TipoAlerta.BAJO_STOCK.getDescripcion());
        assertEquals("Exceso de Stock", TipoAlerta.EXCESO_STOCK.getDescripcion());
    }

    @Test
    void testTipoAlerta_fromDescripcion_Success() {
        // This test covers the successful path of fromDescripcion, including case-insensitivity.
        assertEquals(TipoAlerta.BAJO_STOCK, TipoAlerta.fromDescripcion("Bajo Stock"));
        assertEquals(TipoAlerta.EXCESO_STOCK, TipoAlerta.fromDescripcion("exceso de stock"));
    }

    @Test
    void testTipoAlerta_fromDescripcion_NotFound_ThrowsException() {
        // This test covers the failure path of fromDescripcion, ensuring an exception is thrown for invalid input.
        assertThrows(IllegalArgumentException.class, () -> TipoAlerta.fromDescripcion("Invalid Description"));
    }
}
