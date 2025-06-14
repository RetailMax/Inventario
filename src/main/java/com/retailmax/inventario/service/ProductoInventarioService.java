package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.exception.StockInsuficienteException;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // Indicates that this class is a Spring service component
@RequiredArgsConstructor // Generates a constructor with 'final' fields for dependency injection
public class ProductoInventarioService {

    private final ProductoInventarioRepository productoInventarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    /**
     * Adds a new product to the inventory.
     * If the product already exists (based on SKU), throws ProductoExistenteException.
     * @param requestDTO Product data to add.
     * @return DTO of the added product.
     */
    @Transactional // Ensures the entire operation is atomic
    public ProductoInventarioDTO agregarProductoInventario(AgregarProductoInventarioRequestDTO requestDTO) {
        // Check if the product already exists by SKU
        if (productoInventarioRepository.existsBySku(requestDTO.getSku())) {
            throw new ProductoExistenteException("Product with SKU " + requestDTO.getSku() + " already exists.");
        }

        // Create a new ProductoInventario entity from the DTO
        ProductoInventario producto = new ProductoInventario();
        producto.setSku(requestDTO.getSku());
        producto.setCantidadDisponible(requestDTO.getCantidadInicial());
        producto.setCantidadReservada(0); // Initial value
        producto.setCantidadEnTransito(0); // Initial value
        producto.setCantidadMinimaStock(requestDTO.getCantidadMinimaStock());
        producto.setUbicacionAlmacen(requestDTO.getUbicacionAlmacen());
        producto.setActivo(true); // Active by default when created
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        // CORRECTION for ORA-01400: Ensure the 'stock' field is not NULL
        // Generally, total stock is equal to available quantity at the beginning.
        producto.setStock(requestDTO.getCantidadInicial()); // <--- CORRECTION HERE

        // Save the product to the database
        ProductoInventario savedProducto = productoInventarioRepository.save(producto);

        // Record the initial movement as an "ENTRY"
        MovimientoStock movimientoInicial = new MovimientoStock();
        movimientoInicial.setProductoInventario(savedProducto); // Associate with the saved product
        movimientoInicial.setSku(savedProducto.getSku());
        movimientoInicial.setCantidadMovida(savedProducto.getCantidadDisponible());
        movimientoInicial.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimientoInicial.setFechaMovimiento(LocalDateTime.now());
        movimientoInicial.setStockFinalDespuesMovimiento(savedProducto.getCantidadDisponible());
        movimientoInicial.setReferenciaExterna("Initial Load");
        movimientoInicial.setMotivo("New product creation in inventory.");
        movimientoStockRepository.save(movimientoInicial); // Save the movement

        // Map the saved entity to DTO and return it
        return mapToDTO(savedProducto);
    }

    /**
     * Updates the stock of an existing product.
     * @param requestDTO DTO with information to update stock.
     * @return DTO of the product with updated stock.
     */
    @Transactional
    public ProductoInventarioDTO actualizarStock(ActualizarStockRequestDTO requestDTO) {
        ProductoInventario producto = productoInventarioRepository.findBySku(requestDTO.getSku())
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + requestDTO.getSku() + " not found."));

        int cantidadActual = producto.getCantidadDisponible();
        int cantidadMovida = requestDTO.getCantidad();
        TipoMovimiento tipoMovimiento = TipoMovimiento.fromName(requestDTO.getTipoActualizacion());

        int nuevoStockDisponible;
        switch (tipoMovimiento) {
            case ENTRADA:
                nuevoStockDisponible = cantidadActual + cantidadMovida;
                break;
            case SALIDA:
                if (cantidadActual < cantidadMovida) {
                    throw new StockInsuficienteException("Insufficient stock for product with SKU " + requestDTO.getSku() + ". Available: " + cantidadActual + ", Requested: " + cantidadMovida);
                }
                nuevoStockDisponible = cantidadActual - cantidadMovida;
                break;
            case AJUSTE:
                nuevoStockDisponible = cantidadMovida; // Direct adjustment to the provided quantity
                break;
            default:
                throw new IllegalArgumentException("Invalid movement type: " + requestDTO.getTipoActualizacion());
        }

        producto.setCantidadDisponible(nuevoStockDisponible);
        // Also update the general 'stock' field if 'cantidadDisponible' is the main stock
        producto.setStock(nuevoStockDisponible); // <--- CORRECTION HERE
        producto.setFechaUltimaActualizacion(LocalDateTime.now());

        ProductoInventario updatedProducto = productoInventarioRepository.save(producto);

        // Record the stock movement
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoInventario(updatedProducto); // Associate with the updated product
        movimiento.setSku(updatedProducto.getSku());
        movimiento.setCantidadMovida(cantidadMovida);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setStockFinalDespuesMovimiento(updatedProducto.getCantidadDisponible());
        movimiento.setReferenciaExterna(requestDTO.getReferenciaExterna());
        movimiento.setMotivo(requestDTO.getMotivo());
        movimientoStockRepository.save(movimiento);

        return mapToDTO(updatedProducto);
    }

    /**
     * Queries a product by its SKU.
     * @param sku SKU of the product to query.
     * @return DTO of the found product.
     */
    @Transactional(readOnly = true) // Read-only, optimizes performance
    public ProductoInventarioDTO consultarProductoPorSku(String sku) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found."));
        return mapToDTO(producto);
    }

    /**
     * Deletes a product from the inventory by its SKU.
     * @param sku SKU of the product to delete.
     */
    @Transactional
    public void eliminarProducto(String sku) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found for deletion."));
        // Optional: Delete related stock movements if cascading or cleanup is desired
        // movimientoStockRepository.deleteByProductoInventario(producto); // If you have this method in your repo
        productoInventarioRepository.delete(producto);
    }

    /**
     * Queries all products in the inventory.
     * @return List of DTOs of all products.
     */
    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> consultarTodosLosProductos() {
        return productoInventarioRepository.findAll().stream()
                .map(this::mapToDTO) // Uses the mapping method
                .collect(Collectors.toList());
    }

    /**
     * Updates the details of an existing product.
     * @param sku SKU of the product to update.
     * @param requestDTO DTO with updated product data.
     * @return DTO of the updated product.
     */
    @Transactional
    public ProductoInventarioDTO actualizarProducto(String sku, AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found for update."));

        // Update allowed fields
        // producto.setCantidadDisponible(requestDTO.getCantidadInicial()); // Corrected field, but consider if this logic is desired here
        producto.setCantidadMinimaStock(requestDTO.getCantidadMinimaStock());
        producto.setUbicacionAlmacen(requestDTO.getUbicacionAlmacen());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        // CORRECTION for ORA-01400: Ensure the 'stock' field also updates if 'cantidadDisponible' changes
        // producto.setStock(requestDTO.getCantidadInicial()); // <--- CORRECTION HERE, but consider if this logic is desired here

        ProductoInventario updatedProducto = productoInventarioRepository.save(producto);
        return mapToDTO(updatedProducto);
    }

    /**
     * Checks if any product has stock below a given threshold and notifies.
     * @param umbralCantidadMinima The threshold to check against.
     * @return List of DTOs of products with low stock.
     */
    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockBajo(Integer umbralCantidadMinima) {
        // In a real scenario, this would send notifications (email, SMS, etc.)
        // For now, it just returns the list of products.
        return productoInventarioRepository.findByCantidadDisponibleLessThan(umbralCantidadMinima).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Checks if any product has excessive stock above a given threshold.
     * @param umbralCantidadExcesiva The threshold to check against.
     * @return List of DTOs of products with excessive stock.
     */
    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockExcesivo(Integer umbralCantidadExcesiva) {
        return productoInventarioRepository.findByCantidadDisponibleGreaterThan(umbralCantidadExcesiva).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets the movement history of a product by its SKU.
     * @param sku SKU of the product.
     * @param fechaInicio Start date to filter movements (optional).
     * @param fechaFin End date to filter movements (optional).
     * @return List of DTOs of stock movements.
     */
    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> obtenerHistorialMovimientos(String sku, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found, cannot retrieve its movement history."));

        List<MovimientoStock> movimientos;
        if (fechaInicio != null && fechaFin != null) {
            movimientos = movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                    producto.getId(), fechaInicio, fechaFin
            );
        } else {
            movimientos = movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(
                    producto.getId()
            );
        }
        return movimientos.stream()
                .map(this::mapMovimientoToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Queries an individual stock movement by ID.
     * @param id Movement ID.
     * @return DTO of the found movement.
     */
    @Transactional(readOnly = true)
    public MovimientoStockDTO consultarMovimientoPorId(Long id) {
        MovimientoStock movimiento = movimientoStockRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Stock movement with ID " + id + " not found."));
        return mapMovimientoToDTO(movimiento);
    }

    // --- Mapping Methods (can be in a separate Mapper class in large projects) ---

    private ProductoInventarioDTO mapToDTO(ProductoInventario producto) {
        if (producto == null) {
            return null;
        }
        return ProductoInventarioDTO.builder()
                .id(producto.getId())
                .sku(producto.getSku())
                .cantidadDisponible(producto.getCantidadDisponible())
                .cantidadReservada(producto.getCantidadReservada())
                // .cantidadEnTransito(producto.getCantidadEnTransito()) // This field is not in ProductoInventarioDTO
                .cantidadTotal(producto.getCantidadDisponible() + producto.getCantidadReservada()) // Calculate cantidadTotal
                .cantidadMinimaStock(producto.getCantidadMinimaStock())
                .ubicacionAlmacen(producto.getUbicacionAlmacen())
                // .activo(producto.getActivo()) // This field is not in ProductoInventarioDTO
                .fechaCreacion(producto.getFechaCreacion())
                .fechaUltimaActualizacion(producto.getFechaUltimaActualizacion())
                .build();
    }

    private MovimientoStockDTO mapMovimientoToDTO(MovimientoStock movimiento) {
        if (movimiento == null) {
            return null;
        }
        return MovimientoStockDTO.builder()
                .id(movimiento.getId())
                .productoInventarioId(movimiento.getProductoInventario() != null ? movimiento.getProductoInventario().getId() : null)
                .sku(movimiento.getSku())
                .cantidadMovida(movimiento.getCantidadMovida())
                .tipoMovimiento(movimiento.getTipoMovimiento().name()) // Convert Enum to String
                .fechaMovimiento(movimiento.getFechaMovimiento())
                .stockFinalDespuesMovimiento(movimiento.getStockFinalDespuesMovimiento())
                .referenciaExterna(movimiento.getReferenciaExterna())
                .motivo(movimiento.getMotivo())
                .build();
    }
}
