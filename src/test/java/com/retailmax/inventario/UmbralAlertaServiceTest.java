package com.retailmax.inventario;

import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.model.UmbralAlerta;
import com.retailmax.inventario.model.enums.TipoAlerta;
import com.retailmax.inventario.repository.UmbralAlertaRepository;
import com.retailmax.inventario.service.UmbralAlertaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UmbralAlertaServiceTest {

    @Mock
    private UmbralAlertaRepository umbralAlertaRepository;

    @InjectMocks
    private UmbralAlertaService umbralAlertaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCrearUmbralAlerta() {
        UmbralAlertaDTO dto = UmbralAlertaDTO.builder()
                .sku("SKU001")
                .tipoAlerta("BAJO_STOCK")
                .umbralCantidad(10)
                .activo(true)
                .build();

        when(umbralAlertaRepository.findBySku("SKU001")).thenReturn(Optional.empty());

        UmbralAlerta mockEntity = new UmbralAlerta();
        mockEntity.setSku("SKU001");
        mockEntity.setTipoAlerta(TipoAlerta.BAJO_STOCK);
        mockEntity.setUmbralCantidad(10);
        mockEntity.setActivo(true);

        when(umbralAlertaRepository.save(any(UmbralAlerta.class))).thenReturn(mockEntity);

        UmbralAlertaDTO result = umbralAlertaService.crearUmbralAlerta(dto);

        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        assertEquals("BAJO_STOCK", result.getTipoAlerta());
        assertTrue(result.getActivo());
    }

    @Test
    void testConsultarPorSku() {
        UmbralAlerta mockEntity = new UmbralAlerta();
        mockEntity.setSku("SKU123");
        mockEntity.setTipoAlerta(TipoAlerta.BAJO_STOCK);
        mockEntity.setUmbralCantidad(5);
        mockEntity.setActivo(true);

        when(umbralAlertaRepository.findBySku("SKU123")).thenReturn(Optional.of(mockEntity));

        UmbralAlertaDTO result = umbralAlertaService.consultarUmbralAlertaPorSku("SKU123");

        assertNotNull(result);
        assertEquals("SKU123", result.getSku());
        assertEquals("BAJO_STOCK", result.getTipoAlerta());
    }
}
