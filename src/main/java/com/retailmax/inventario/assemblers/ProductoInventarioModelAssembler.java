package com.retailmax.inventario.assemblers;

import com.retailmax.inventario.controller.ProductoInventarioController;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.lang.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler para convertir objetos ProductoInventarioDTO a EntityModel<ProductoInventarioDTO>
 * con enlaces HATEOAS.
 */
@Component
public class ProductoInventarioModelAssembler implements RepresentationModelAssembler<ProductoInventarioDTO, EntityModel<ProductoInventarioDTO>> {

    @Override
    @NonNull
    public EntityModel<ProductoInventarioDTO> toModel(@NonNull ProductoInventarioDTO productoInventario) {
        // Construye el EntityModel para un ProductoInventarioDTO individual
        // Agrega un enlace 'self' que apunta a la consulta del producto por su SKU
        return EntityModel.of(productoInventario,
                linkTo(methodOn(ProductoInventarioController.class).consultarProductoPorSku(productoInventario.getSku())).withSelfRel(),
                // Agrega un enlace 'productos' que apunta a la colección completa de productos
                linkTo(methodOn(ProductoInventarioController.class).consultarTodosLosProductos()).withRel("productos"));
    }
}
