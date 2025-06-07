package com.retailmax.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UmbralAlertaDTO {

    // --- Campos para RESPUESTA (Output) ---
    private Long id;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;

    // --- Campos de SOLICITUD y RESPUESTA ---

    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    // Tipo de alerta: se recibe como String y se convierte a Enum en el servicio
    private String tipoAlerta;

    @Min(value = 0, message = "La cantidad del umbral no puede ser negativa")
    private Integer umbralCantidad;

    private Boolean activo;
}
