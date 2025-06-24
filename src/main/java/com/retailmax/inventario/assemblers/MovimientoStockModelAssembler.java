package com.retailmax.inventario.assemblers;

import com.retailmax.inventario.controller.MovimientoStockController;
import com.retailmax.inventario.controller.ProductoInventarioController; // Para enlazar al producto asociado
import com.retailmax.inventario.dto.MovimientoStockDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.lang.NonNull;
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
    @NonNull
    public EntityModel<MovimientoStockDTO> toModel(@NonNull MovimientoStockDTO movimientoStock) {
        // Construye el EntityModel para un MovimientoStockDTO individual
        return EntityModel.of(movimientoStock,
                // Enlace 'self' al movimiento individual por su ID
                linkTo(methodOn(MovimientoStockController.class).getMovimientoById(movimientoStock.getId())).withSelfRel(),
                // Enlace a la colección de movimientos para el mismo SKU
                linkTo(methodOn(MovimientoStockController.class).obtenerHistorialMovimientos(movimientoStock.getSku())).withRel("movimientos-del-sku"),
                // Enlace al producto de inventario asociado
                linkTo(methodOn(ProductoInventarioController.class).consultarProductoPorSku(movimientoStock.getSku())).withRel("producto-asociado"));
    }
}
