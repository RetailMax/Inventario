package com.retailmax.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude; // Para omitir campos nulos en la respuesta JSON
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
@JsonInclude(JsonInclude.Include.NON_NULL) // Configura Jackson para no incluir campos con valor null en el JSON de respuesta
public class UmbralAlertaDTO {

    // --- Campos para RESPUESTA (Output) ---
    // El ID se genera por la DB y es de solo lectura. No se espera en solicitudes de creación/actualización.
    private Long id;

    // Las fechas de auditoría también son de solo lectura en respuestas.
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;


    // --- Campos que pueden ser usados en SOLICITUDES (Input) y RESPUESTAS (Output) ---

    // SKU: Es un identificador clave. Siempre debe estar presente en la solicitud.
    // Usamos @NotBlank para asegurar que no sea nulo ni vacío en la entrada.
    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    // Tipo de alerta: Recibido como String en solicitudes, convertido a Enum en el servicio.
    // No se marca como @NotNull/@NotBlank aquí para permitir actualizaciones parciales (PUT/PATCH)
    // donde este campo podría no enviarse si no se actualiza.
    private String tipoAlerta;

    // Cantidad del umbral: Requerido para la creación, opcional para la actualización.
    // No se marca como @NotNull aquí para permitir actualizaciones parciales.
    // @Min se aplica si el valor no es nulo.
    @Min(value = 0, message = "La cantidad del umbral no puede ser negativa")
    private Integer umbralCantidad;

    // Estado activo: Requerido para la creación, opcional para la actualización.
    // No se marca como @NotNull aquí para permitir actualizaciones parciales.
    private Boolean activo;
}