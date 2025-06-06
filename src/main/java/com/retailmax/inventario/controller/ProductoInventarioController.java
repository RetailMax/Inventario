package com.retailmax.inventario.controller;

import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.service.ProductoInventarioService;
import jakarta.validation.Valid; // Para validar los DTOs de entrada
import lombok.RequiredArgsConstructor; // Para inyección de dependencias vía constructor
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/inventario") // Prefijo para todas las rutas en este controlador
@RequiredArgsConstructor // Genera un constructor con los campos 'final' para inyección
public class ProductoInventarioController {

    private final ProductoInventarioService productoInventarioService; // Inyección del servicio

    /**
     * RF1: Permite la adición de nuevos productos al inventario.
     * POST /api/inventario/productos
     * @param requestDTO Datos del nuevo producto a agregar.
     * @return ResponseEntity con el ProductoInventarioDTO creado y estado HTTP 201 Created.
     */
    @PostMapping("/productos")
    public ResponseEntity<ProductoInventarioDTO> agregarProducto(
            @Valid @RequestBody AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventarioDTO nuevoProducto = productoInventarioService.agregarProductoInventario(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    /**
     * RF2, RF4, RF5, RF10, RF16: Permite actualizar el stock (incremento, decremento, reserva, liberación, ajuste).
     * El tipo de operación se define en el campo 'tipoActualizacion' del requestDTO.
     * PUT /api/inventario/stock
     * @param requestDTO Datos de la actualización de stock.
     * @return ResponseEntity con el ProductoInventarioDTO actualizado y estado HTTP 200 OK.
     */
    @PutMapping("/stock")
    public ResponseEntity<ProductoInventarioDTO> actualizarStock(
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO) {
        ProductoInventarioDTO updatedProducto = productoInventarioService.actualizarStock(requestDTO);
        return ResponseEntity.ok(updatedProducto);
    }

    /**
     * RF3: Consulta la cantidad de stock disponible para un producto específico por SKU.
     * GET /api/inventario/stock/{sku}
     * @param sku SKU del producto a consultar.
     * @return ResponseEntity con el ProductoInventarioDTO del producto y estado HTTP 200 OK.
     */
    @GetMapping("/stock/{sku}")
    public ResponseEntity<ProductoInventarioDTO> consultarStockPorSku(@PathVariable String sku) {
        ProductoInventarioDTO producto = productoInventarioService.consultarStockPorSku(sku);
        return ResponseEntity.ok(producto);
    }

    /**
     * RF3: Consulta la cantidad de stock disponible para todos los productos.
     * GET /api/inventario/stock
     * @return ResponseEntity con una lista de ProductoInventarioDTO y estado HTTP 200 OK.
     */
    @GetMapping("/stock")
    public ResponseEntity<List<ProductoInventarioDTO>> consultarTodoElStock() {
        List<ProductoInventarioDTO> stockList = productoInventarioService.consultarTodoElStock();
        return ResponseEntity.ok(stockList);
    }
}