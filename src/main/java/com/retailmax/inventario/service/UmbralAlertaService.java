package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.UmbralAlertaDTO; // Ahora usamos solo este DTO
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
    public UmbralAlertaDTO crearUmbralAlerta(UmbralAlertaDTO requestDTO) { // Usamos el DTO unificado
        // Validaciones específicas para la creación (que el DTO unificado no puede imponer con @NotNull
        // si queremos flexibilidad para la actualización).
        if (requestDTO.getSku() == null || requestDTO.getSku().isBlank()) {
            throw new IllegalArgumentException("El SKU es obligatorio para crear un umbral de alerta.");
        }
        if (requestDTO.getTipoAlerta() == null || requestDTO.getTipoAlerta().isBlank()) {
            throw new IllegalArgumentException("El tipo de alerta es obligatorio para crear un umbral de alerta.");
        }
        if (requestDTO.getUmbralCantidad() == null) {
            throw new IllegalArgumentException("La cantidad del umbral es obligatoria para crear un umbral de alerta.");
        }
        if (requestDTO.getActivo() == null) {
            throw new IllegalArgumentException("El estado activo es obligatorio para crear un umbral de alerta.");
        }


        Optional<UmbralAlerta> existingUmbral = umbralAlertaRepository.findBySku(requestDTO.getSku());
        if (existingUmbral.isPresent()) {
            throw new IllegalArgumentException("Ya existe un umbral de alerta configurado para el SKU: " + requestDTO.getSku());
        }

        UmbralAlerta nuevoUmbral = new UmbralAlerta();
        nuevoUmbral.setSku(requestDTO.getSku());
        nuevoUmbral.setTipoAlerta(TipoAlerta.fromName(requestDTO.getTipoAlerta()));
        nuevoUmbral.setUmbralCantidad(requestDTO.getUmbralCantidad());
        nuevoUmbral.setActivo(requestDTO.getActivo());

        nuevoUmbral.setFechaCreacion(LocalDateTime.now());
        nuevoUmbral.setFechaUltimaActualizacion(LocalDateTime.now());

        UmbralAlerta savedUmbral = umbralAlertaRepository.save(nuevoUmbral);
        return mapToDTO(savedUmbral);
    }

    @Transactional
    public UmbralAlertaDTO actualizarUmbralAlerta(String sku, UmbralAlertaDTO requestDTO) { // Usamos el DTO unificado
        UmbralAlerta umbralExistente = umbralAlertaRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Umbral de alerta para SKU " + sku + " no encontrado."));

        // Solo actualizamos si los campos están presentes en el DTO de la solicitud
        if (requestDTO.getTipoAlerta() != null && !requestDTO.getTipoAlerta().isBlank()) {
            umbralExistente.setTipoAlerta(TipoAlerta.fromName(requestDTO.getTipoAlerta()));
        }
        if (requestDTO.getUmbralCantidad() != null) {
            umbralExistente.setUmbralCantidad(requestDTO.getUmbralCantidad());
        }
        if (requestDTO.getActivo() != null) {
            umbralExistente.setActivo(requestDTO.getActivo());
        }

        umbralExistente.setFechaUltimaActualizacion(LocalDateTime.now());

        UmbralAlerta updatedUmbral = umbralAlertaRepository.save(umbralExistente);
        return mapToDTO(updatedUmbral);
    }

    @Transactional(readOnly = true)
    public UmbralAlertaDTO consultarUmbralAlertaPorSku(String sku) {
        UmbralAlerta umbral = umbralAlertaRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Umbral de alerta para SKU " + sku + " no encontrado."));
        return mapToDTO(umbral);
    }

    @Transactional(readOnly = true)
    public List<UmbralAlertaDTO> consultarTodosLosUmbrales() {
        return umbralAlertaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UmbralAlertaDTO> consultarUmbralesActivosPorTipo(TipoAlerta tipoAlerta) {
        // Asumiendo que TipoAlerta ya es el Enum correcto
        // Si necesitas un método en el repo, lo podrías añadir:
        // return umbralAlertaRepository.findByTipoAlertaAndActivoTrue(tipoAlerta).stream()
        //        .map(this::mapToDTO)
        //        .collect(Collectors.toList());

        // O, si no tienes el método en el repo y solo filtras en memoria (menos eficiente para grandes datasets):
        return umbralAlertaRepository.findAll().stream()
                .filter(u -> u.getActivo() && u.getTipoAlerta().equals(tipoAlerta))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public void eliminarUmbralAlerta(String sku) {
        UmbralAlerta umbral = umbralAlertaRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Umbral de alerta para SKU " + sku + " no encontrado para eliminar."));
        umbralAlertaRepository.delete(umbral);
    }

    // Método de mapeo de entidad a DTO (ahora siempre usa el mismo DTO)
    private UmbralAlertaDTO mapToDTO(UmbralAlerta umbralAlerta) {
        return UmbralAlertaDTO.builder()
                .id(umbralAlerta.getId())
                .sku(umbralAlerta.getSku())
                .tipoAlerta(umbralAlerta.getTipoAlerta().name()) // Convertir Enum a String para el DTO
                .umbralCantidad(umbralAlerta.getUmbralCantidad())
                .activo(umbralAlerta.getActivo())
                .fechaCreacion(umbralAlerta.getFechaCreacion())
                .fechaUltimaActualizacion(umbralAlerta.getFechaUltimaActualizacion())
                .build();
    }
}