package com.retailmax.inventario.controller;

import org.springframework.web.bind.annotation.*;
import com.retailmax.inventario.dto.ProductoCatalogoDTO;
import com.retailmax.inventario.service.CatalogoProductoService;
import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoProductoController {

    private final CatalogoProductoService catalogoProductoService;

    public CatalogoProductoController(CatalogoProductoService catalogoProductoService) {
        this.catalogoProductoService = catalogoProductoService;
    }

    @GetMapping("/productos")
    public List<ProductoCatalogoDTO> obtenerCatalogo() {
        return catalogoProductoService.obtenerCatalogo();
    }
}
