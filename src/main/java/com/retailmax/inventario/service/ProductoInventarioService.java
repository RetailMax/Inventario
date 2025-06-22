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
import com.retailmax.inventario.model.enums.EstadoStock;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoInventarioService {

    private final ProductoInventarioRepository productoInventarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public ProductoInventarioDTO agregarProductoInventario(AgregarProductoInventarioRequestDTO requestDTO) {
        if (productoInventarioRepository.existsBySku(requestDTO.getSku())) {
            throw new ProductoExistenteException("Product with SKU " + requestDTO.getSku() + " already exists.");
        }

        ProductoInventario producto = new ProductoInventario();
        producto.setSku(requestDTO.getSku());
        producto.setCantidadDisponible(requestDTO.getCantidadInicial());
        producto.setCantidadReservada(0);
        producto.setCantidadEnTransito(0);
        producto.setCantidadMinimaStock(requestDTO.getCantidadMinimaStock());
        producto.setUbicacionAlmacen(requestDTO.getUbicacionAlmacen());
        producto.setActivo(true);
        producto.setProductoBaseSku(requestDTO.getProductoBaseSku());
        producto.setTalla(requestDTO.getTalla());
        producto.setColor(requestDTO.getColor());
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setStock(requestDTO.getCantidadInicial());

        if (requestDTO.getEstado() != null) {
            producto.setEstado(requestDTO.getEstado());
        } else {
            producto.setEstado(EstadoStock.DISPONIBLE);
        }

        ProductoInventario savedProducto = productoInventarioRepository.save(producto);

        MovimientoStock movimientoInicial = new MovimientoStock();
        movimientoInicial.setProductoInventario(savedProducto);
        movimientoInicial.setSku(savedProducto.getSku());
        movimientoInicial.setCantidadMovida(savedProducto.getCantidadDisponible());
        movimientoInicial.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimientoInicial.setFechaMovimiento(LocalDateTime.now());
        movimientoInicial.setStockFinalDespuesMovimiento(savedProducto.getCantidadDisponible());
        movimientoInicial.setReferenciaExterna("Initial Load");
        movimientoInicial.setMotivo("New product creation in inventory.");
        movimientoStockRepository.save(movimientoInicial);

        return mapToDTO(savedProducto);
    }

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
                    throw new StockInsuficienteException("Insufficient stock for product with SKU " + requestDTO.getSku());
                }
                nuevoStockDisponible = cantidadActual - cantidadMovida;
                break;
            case AJUSTE:
                nuevoStockDisponible = cantidadMovida;
                break;
            default:
                throw new IllegalArgumentException("Invalid movement type: " + requestDTO.getTipoActualizacion());
        }

        producto.setCantidadDisponible(nuevoStockDisponible);
        producto.setStock(nuevoStockDisponible);
        producto.setFechaUltimaActualizacion(LocalDateTime.now());

        ProductoInventario updatedProducto = productoInventarioRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoInventario(updatedProducto);
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

    @Transactional(readOnly = true)
    public ProductoInventarioDTO consultarProductoPorSku(String sku) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found."));
        return mapToDTO(producto);
    }

    @Transactional
    public void eliminarProducto(String sku) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found for deletion."));
        productoInventarioRepository.delete(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> consultarTodosLosProductos() {
        return productoInventarioRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoInventarioDTO actualizarProducto(String sku, AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found for update."));

        producto.setCantidadMinimaStock(requestDTO.getCantidadMinimaStock());
        producto.setUbicacionAlmacen(requestDTO.getUbicacionAlmacen());
        producto.setProductoBaseSku(requestDTO.getProductoBaseSku());
        producto.setTalla(requestDTO.getTalla());
        producto.setColor(requestDTO.getColor());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());

        ProductoInventario updatedProducto = productoInventarioRepository.save(producto);
        return mapToDTO(updatedProducto);
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockBajo(Integer umbralCantidadMinima) {
        return productoInventarioRepository.findByCantidadDisponibleLessThan(umbralCantidadMinima).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockExcesivo(Integer umbralCantidadExcesiva) {
        return productoInventarioRepository.findByCantidadDisponibleGreaterThan(umbralCantidadExcesiva).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> obtenerHistorialMovimientos(String sku, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Product with SKU " + sku + " not found, cannot retrieve its movement history."));

        List<MovimientoStock> movimientos;
        if (fechaInicio != null && fechaFin != null) {
            movimientos = movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                    producto.getId(), fechaInicio, fechaFin);
        } else {
            movimientos = movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(
                    producto.getId());
        }
        return movimientos.stream()
                .map(this::mapMovimientoToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovimientoStockDTO consultarMovimientoPorId(Long id) {
        MovimientoStock movimiento = movimientoStockRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Stock movement with ID " + id + " not found."));
        return mapMovimientoToDTO(movimiento);
    }

    @Transactional
    public void actualizarEstado(Long id, EstadoStock nuevoEstado) {
        ProductoInventario producto = productoInventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado."));
        producto.setEstado(nuevoEstado);
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        productoInventarioRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> buscarPorEstado(EstadoStock estado) {
        return productoInventarioRepository.findByEstado(estado).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ProductoInventarioDTO mapToDTO(ProductoInventario producto) {
        if (producto == null) return null;

        return ProductoInventarioDTO.builder()
                .id(producto.getId())
                .sku(producto.getSku())
                .cantidadDisponible(producto.getCantidadDisponible())
                .cantidadReservada(producto.getCantidadReservada())
                .cantidadTotal(producto.getCantidadDisponible() + producto.getCantidadReservada())
                .cantidadMinimaStock(producto.getCantidadMinimaStock())
                .ubicacionAlmacen(producto.getUbicacionAlmacen())
                .fechaCreacion(producto.getFechaCreacion())
                .fechaUltimaActualizacion(producto.getFechaUltimaActualizacion())
                .productoBaseSku(producto.getProductoBaseSku())
                .talla(producto.getTalla())
                .color(producto.getColor())
                .estado(producto.getEstado())
                .build();
    }

    private MovimientoStockDTO mapMovimientoToDTO(MovimientoStock movimiento) {
        if (movimiento == null) return null;

        return MovimientoStockDTO.builder()
                .id(movimiento.getId())
                .productoInventarioId(movimiento.getProductoInventario() != null ? movimiento.getProductoInventario().getId() : null)
                .sku(movimiento.getSku())
                .cantidadMovida(movimiento.getCantidadMovida())
                .tipoMovimiento(movimiento.getTipoMovimiento().name())
                .fechaMovimiento(movimiento.getFechaMovimiento())
                .stockFinalDespuesMovimiento(movimiento.getStockFinalDespuesMovimiento())
                .referenciaExterna(movimiento.getReferenciaExterna())
                .motivo(movimiento.getMotivo())
                .build();
    }
    // 🚩 RF10 - Reserva de Stock

    @Transactional(readOnly = true)
    public boolean validarDisponibilidad(String sku, Integer cantidadSolicitada) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));
        
        return producto.getCantidadDisponible() >= cantidadSolicitada;
    }

}
