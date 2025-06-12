package com.retailmax.inventario.assemblers;

import com.retailmax.inventario.controller.MovimientoStockControllerV2;
import com.retailmax.inventario.controller.ProductoInventarioControllerV2; // Para enlazar al producto asociado
import com.retailmax.inventario.dto.MovimientoStockDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler para convertir objetos MovimientoStockDTO a EntityModel<MovimientoStockDTO>
 * con enlaces HATEOAS.
 */
@Component
public class MovimientoStockModelAssembler implements RepresentationModelAssembler<MovimientoStockDTO, EntityModel<MovimientoStockDTO>> {

    @Override
    public EntityModel<MovimientoStockDTO> toModel(MovimientoStockDTO movimientoStock) {
        // Construye el EntityModel para un MovimientoStockDTO individual
        return EntityModel.of(movimientoStock,
                // Enlace 'self' al movimiento individual por su ID
                linkTo(methodOn(MovimientoStockControllerV2.class).getMovimientoById(movimientoStock.getId())).withSelfRel(),
                // Enlace a la colección de movimientos para el mismo SKU
                linkTo(methodOn(MovimientoStockControllerV2.class).obtenerHistorialMovimientos(movimientoStock.getSku())).withRel("movimientos-del-sku"),
                // Enlace al producto de inventario asociado
                linkTo(methodOn(ProductoInventarioControllerV2.class).getProductoBySku(movimientoStock.getSku())).withRel("producto-asociado"));
    }
}
