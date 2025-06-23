package com.retailmax.inventario;

import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.UmbralAlerta;
import com.retailmax.inventario.model.enums.TipoAlerta;
import com.retailmax.inventario.repository.UmbralAlertaRepository;
import com.retailmax.inventario.service.UmbralAlertaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
}
