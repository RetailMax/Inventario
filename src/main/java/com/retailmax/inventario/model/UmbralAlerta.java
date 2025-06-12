package com.retailmax.inventario.model;

import com.retailmax.inventario.model.enums.TipoAlerta; // Importar el Enum
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity // Indica que es una entidad JPA
@Table(name = "umbrales_alerta") // Define el nombre de la tabla en la DB
@Data // Genera getters, setters, toString, equals, hashCode (Lombok)
@NoArgsConstructor // Genera constructor sin argumentos (Lombok)
@AllArgsConstructor // Genera constructor con todos los argumentos (Lombok)
public class UmbralAlerta {

    @Id // Marca el campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Estrategia de generación de ID (autoincremental)
    private Long id;

    @Column(unique = true, nullable = false) // El SKU debe ser único para un umbral y no nulo
    private String sku; // El SKU al que se aplica este umbral

    @Enumerated(EnumType.STRING) // Almacena el nombre del Enum como String en la DB
    @Column(nullable = false)
    private TipoAlerta tipoAlerta; // Tipo de alerta (BAJO_STOCK, EXCESO_STOCK, SIN_MOVIMIENTO)

    @Column(nullable = false)
    private Integer umbralCantidad; // La cantidad que define el umbral (ej. 10 para bajo stock)

    @Column(nullable = false)
    private Boolean activo; // Indica si el umbral de alerta está activo o no

    // Campos de auditoría (serán gestionados por el servicio en un modelo anémico)
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    // Con @Data, @NoArgsConstructor y @AllArgsConstructor, no necesitas escribir
    // manualmente getters, setters ni constructores.
    // Recuerda que la lógica para establecer fechaCreacion y fechaUltimaActualizacion
    // debe residir en tu capa de servicio, siguiendo el modelo anémico.
}