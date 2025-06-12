package com.retailmax.inventario.assemblers;

import com.retailmax.inventario.controller.UmbralAlertaControllerV2;
import com.retailmax.inventario.dto.UmbralAlertaDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler para convertir objetos UmbralAlertaDTO a EntityModel<UmbralAlertaDTO>
 * con enlaces HATEOAS.
 */
@Component
public class UmbralAlertaModelAssembler implements RepresentationModelAssembler<UmbralAlertaDTO, EntityModel<UmbralAlertaDTO>> {

    @Override
    public EntityModel<UmbralAlertaDTO> toModel(UmbralAlertaDTO umbralAlerta) {
        // Construye el EntityModel para un UmbralAlertaDTO individual
        // Agrega un enlace 'self' que apunta a la consulta del umbral por su SKU
        return EntityModel.of(umbralAlerta,
                linkTo(methodOn(UmbralAlertaControllerV2.class).getUmbralBySku(umbralAlerta.getSku())).withSelfRel(),
                // Agrega un enlace 'umbrales' que apunta a la colección completa de umbrales
                linkTo(methodOn(UmbralAlertaControllerV2.class).getAllUmbrales()).withRel("umbrales"));
    }
}
