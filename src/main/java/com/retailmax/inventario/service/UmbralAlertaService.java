package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.UmbralAlertaDTO;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.UmbralAlerta;
import com.retailmax.inventario.model.enums.TipoAlerta;
import com.retailmax.inventario.repository.UmbralAlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UmbralAlertaService {

    private final UmbralAlertaRepository umbralAlertaRepository;

    @Transactional
    public UmbralAlertaDTO crearUmbralAlerta(UmbralAlertaDTO requestDTO) {
        if (requestDTO.getSku() == null || requestDTO.getSku().isBlank()) {
            throw new IllegalArgumentException("El SKU es obligatorio para crear un umbral de alerta.");
        }
        if (requestDTO.getTipoAlerta() == null || requestDTO.getTipoAlerta().isBlank()) {
            throw new IllegalArgumentException("El tipo de alerta es obligatorio para crear un umbral de alerta.");
        }
        if (requestDTO.getUmbralCantidad() == null) {
            throw new IllegalArgumentException("La cantidad del umbral es obligatoria.");
        }
        if (requestDTO.getActivo() == null) {
            throw new IllegalArgumentException("El campo activo es obligatorio.");
        }

        Optional<UmbralAlerta> existente = umbralAlertaRepository.findBySku(requestDTO.getSku());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un umbral para el SKU: " + requestDTO.getSku());
        }

        UmbralAlerta nuevo = new UmbralAlerta();
        nuevo.setSku(requestDTO.getSku());
        nuevo.setTipoAlerta(TipoAlerta.fromName(requestDTO.getTipoAlerta()));
        nuevo.setUmbralCantidad(requestDTO.getUmbralCantidad());
        nuevo.setActivo(requestDTO.getActivo());
        nuevo.setFechaCreacion(LocalDateTime.now());
        nuevo.setFechaUltimaActualizacion(LocalDateTime.now());

        return mapToDTO(umbralAlertaRepository.save(nuevo));
    }

    @Transactional
    public UmbralAlertaDTO actualizarUmbralAlerta(String sku, UmbralAlertaDTO dto) {
        UmbralAlerta existente = umbralAlertaRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el umbral para SKU: " + sku));

        if (dto.getTipoAlerta() != null && !dto.getTipoAlerta().isBlank()) {
            existente.setTipoAlerta(TipoAlerta.fromName(dto.getTipoAlerta()));
        }
        if (dto.getUmbralCantidad() != null) {
            existente.setUmbralCantidad(dto.getUmbralCantidad());
        }
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        existente.setFechaUltimaActualizacion(LocalDateTime.now());
        return mapToDTO(umbralAlertaRepository.save(existente));
    }

    @Transactional(readOnly = true)
    public UmbralAlertaDTO consultarUmbralAlertaPorSku(String sku) {
        UmbralAlerta umbral = umbralAlertaRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el umbral para SKU: " + sku));
        return mapToDTO(umbral);
    }

    @Transactional(readOnly = true)
    public List<UmbralAlertaDTO> consultarTodosLosUmbrales() {
        return umbralAlertaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UmbralAlertaDTO> consultarUmbralesActivosPorTipo(TipoAlerta tipoAlerta) {
        return umbralAlertaRepository.findAll()
                .stream()
                .filter(u -> u.getActivo() && u.getTipoAlerta().equals(tipoAlerta))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarUmbralAlerta(String sku) {
        UmbralAlerta umbral = umbralAlertaRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el umbral para eliminar con SKU: " + sku));
        umbralAlertaRepository.delete(umbral);
    }

    private UmbralAlertaDTO mapToDTO(UmbralAlerta u) {
        return UmbralAlertaDTO.builder()
                .id(u.getId())
                .sku(u.getSku())
                .tipoAlerta(u.getTipoAlerta().name())
                .umbralCantidad(u.getUmbralCantidad())
                .activo(u.getActivo())
                .fechaCreacion(u.getFechaCreacion())
                .fechaUltimaActualizacion(u.getFechaUltimaActualizacion())
                .build();
    }
}
