package com.retailmax.inventario.controller;

import org.springframework.web.bind.annotation.*;
import com.retailmax.inventario.dto.ProductoCatalogoDTO;
import com.retailmax.inventario.service.CatalogoProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "CatalogoProducto", description = "Operaciones relacionadas con el catálogo de productos")
@RequestMapping("/api/catalogo")
public class CatalogoProductoController {

    private final CatalogoProductoService catalogoProductoService;

    public CatalogoProductoController(CatalogoProductoService catalogoProductoService) {
        this.catalogoProductoService = catalogoProductoService;
    }

    @GetMapping("/productos")
    @Operation(
        summary = "Obtener catálogo de productos",
        description = "Permite consultar el catálogo de productos disponibles en el sistema."
    )
    public List<ProductoCatalogoDTO> obtenerCatalogo() {
        return catalogoProductoService.obtenerCatalogo();
    }
}
