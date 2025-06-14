package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.*;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.exception.StockInsuficienteException;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.enums.TipoMovimiento;
import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.repository.MovimientoStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoInventarioService {

    private final ProductoInventarioRepository productoInventarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public ProductoInventarioDTO agregarProductoInventario(AgregarProductoInventarioRequestDTO requestDTO) {
        if (productoInventarioRepository.existsBySku(requestDTO.getSku())) {
            throw new ProductoExistenteException("Ya existe un producto con el SKU: " + requestDTO.getSku());
        }

        ProductoInventario producto = new ProductoInventario();
        producto.setSku(requestDTO.getSku());
        producto.setCantidadDisponible(requestDTO.getCantidadInicial());
        producto.setUbicacionAlmacen(requestDTO.getUbicacionAlmacen());
        producto.setCantidadMinimaStock(Optional.ofNullable(requestDTO.getCantidadMinimaStock()).orElse(0));
        producto.setCantidadReservada(0);
        producto.setCantidadEnTransito(0);
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());

        ProductoInventario saved = productoInventarioRepository.save(producto);
        registrarMovimiento(saved, TipoMovimiento.ENTRADA, requestDTO.getCantidadInicial(), "Entrada inicial");

        return mapToProductoInventarioDTO(saved);
    }

    @Transactional
    public ProductoInventarioDTO actualizarStock(ActualizarStockRequestDTO requestDTO) {
        ProductoInventario producto = productoInventarioRepository.findBySku(requestDTO.getSku())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + requestDTO.getSku() + " no encontrado."));

        int cantidad = requestDTO.getCantidad();
        TipoMovimiento tipo = TipoMovimiento.fromName(requestDTO.getTipoActualizacion());

        int disponible = producto.getCantidadDisponible();
        int reservada = producto.getCantidadReservada();

        switch (tipo) {
            case ENTRADA -> producto.setCantidadDisponible(disponible + cantidad);
            case SALIDA -> {
                if (disponible < cantidad) throw new StockInsuficienteException("Stock insuficiente.");
                producto.setCantidadDisponible(disponible - cantidad);
            }
            case RESERVA -> {
                if (disponible < cantidad) throw new StockInsuficienteException("Stock insuficiente para reserva.");
                producto.setCantidadDisponible(disponible - cantidad);
                producto.setCantidadReservada(reservada + cantidad);
            }
            case LIBERACION -> {
                if (reservada < cantidad) throw new StockInsuficienteException("Stock reservado insuficiente.");
                producto.setCantidadDisponible(disponible + cantidad);
                producto.setCantidadReservada(reservada - cantidad);
            }
            case AJUSTE -> {
                if (disponible + cantidad < 0) throw new IllegalArgumentException("Ajuste resultaría en stock negativo.");
                producto.setCantidadDisponible(disponible + cantidad);
            }
            case DEVOLUCION_CLIENTE -> producto.setCantidadDisponible(disponible + cantidad);
            default -> throw new IllegalArgumentException("Tipo de movimiento no soportado.");
        }

        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario actualizado = productoInventarioRepository.save(producto);

        registrarMovimiento(actualizado, tipo, cantidad, requestDTO.getReferenciaExterna());

        return mapToProductoInventarioDTO(actualizado);
    }

    @Transactional
    public ProductoInventarioDTO actualizarUbicacionPorSku(ActualizarUbicacionDTO dto) {
        ProductoInventario producto = productoInventarioRepository.findBySku(dto.getSku())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + dto.getSku() + " no encontrado."));

        producto.setUbicacionAlmacen(dto.getNuevaUbicacion());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());

        ProductoInventario actualizado = productoInventarioRepository.save(producto);
        return mapToProductoInventarioDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> buscarPorUbicacion(String ubicacion) {
        return productoInventarioRepository.findByUbicacionAlmacenIgnoreCase(ubicacion).stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoInventarioDTO consultarStockPorSku(String sku) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));
        return mapToProductoInventarioDTO(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> consultarTodoElStock() {
        return productoInventarioRepository.findAll().stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoInventarioDTO realizarAjusteManual(String sku, Integer cantidad, String motivo) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));

        if (producto.getCantidadDisponible() + cantidad < 0) {
            throw new IllegalArgumentException("Ajuste negativo inválido.");
        }

        producto.setCantidadDisponible(producto.getCantidadDisponible() + cantidad);
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario actualizado = productoInventarioRepository.save(producto);

        registrarMovimiento(actualizado, TipoMovimiento.AJUSTE, cantidad, motivo);
        return mapToProductoInventarioDTO(actualizado);
    }

    @Transactional
    public ProductoInventarioDTO decrementarStockPorOrdenConfirmada(String sku, Integer cantidad, String ordenId) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));

        if (producto.getCantidadDisponible() < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente para orden confirmada.");
        }

        producto.setCantidadDisponible(producto.getCantidadDisponible() - cantidad);
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario actualizado = productoInventarioRepository.save(producto);

        registrarMovimiento(actualizado, TipoMovimiento.SALIDA, cantidad, "Orden confirmada: " + ordenId);
        return mapToProductoInventarioDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public boolean validarDisponibilidad(String sku, Integer cantidad) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));
        return producto.getCantidadDisponible() >= cantidad;
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> obtenerHistorialMovimientos(String sku, LocalDateTime desde, LocalDateTime hasta) {
        ProductoInventario producto = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));

        List<MovimientoStock> movimientos = (desde != null && hasta != null)
                ? movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(producto.getId(), desde, hasta)
                : movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(producto.getId());

        return movimientos.stream().map(this::mapToMovimientoStockDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockBajo(Integer umbral) {
        return productoInventarioRepository.findByCantidadDisponibleLessThan(umbral).stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockExcesivo(Integer umbral) {
        return productoInventarioRepository.findByCantidadDisponibleGreaterThan(umbral).stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> generarReporteDeInventarioActual() {
        return consultarTodoElStock();
    }

    @Transactional
    public ProductoInventarioDTO recibirStockDeProveedor(String sku, Integer cantidad, String referencia) {
        try {
            return actualizarStock(new ActualizarStockRequestDTO(sku, cantidad, TipoMovimiento.ENTRADA.name(), "Recepción proveedor: " + referencia, "Recepción"));
        } catch (RecursoNoEncontradoException e) {
            return agregarProductoInventario(new AgregarProductoInventarioRequestDTO(sku, cantidad, "UbicacionDefecto", 0));
        }
    }

    @Transactional
    public void eliminarProductoPorId(Long id) {
        ProductoInventario producto = productoInventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado."));
        productoInventarioRepository.delete(producto);
        registrarMovimiento(producto, TipoMovimiento.AJUSTE, 0, "Eliminación del producto del sistema");
    }

    private void registrarMovimiento(ProductoInventario producto, TipoMovimiento tipo, Integer cantidad, String referencia) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoInventario(producto);
        movimiento.setSku(producto.getSku());
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidadMovida(cantidad);
        movimiento.setStockFinalDespuesMovimiento(producto.getCantidadDisponible());
        movimiento.setReferenciaExterna(referencia);
        movimiento.setMotivo(tipo.getDescripcion());
        movimiento.setFechaMovimiento(LocalDateTime.now());

        movimientoStockRepository.save(movimiento);
    }

    private ProductoInventarioDTO mapToProductoInventarioDTO(ProductoInventario producto) {
        return ProductoInventarioDTO.builder()
                .id(producto.getId())
                .sku(producto.getSku())
                .cantidadDisponible(producto.getCantidadDisponible())
                .cantidadReservada(Optional.ofNullable(producto.getCantidadReservada()).orElse(0))
                .cantidadTotal(producto.getCantidadDisponible() + Optional.ofNullable(producto.getCantidadReservada()).orElse(0))
                .ubicacionAlmacen(producto.getUbicacionAlmacen())
                .cantidadMinimaStock(Optional.ofNullable(producto.getCantidadMinimaStock()).orElse(0))
                .fechaCreacion(producto.getFechaCreacion())
                .fechaUltimaActualizacion(producto.getFechaUltimaActualizacion())
                .build();
    }

    private MovimientoStockDTO mapToMovimientoStockDTO(MovimientoStock movimiento) {
        return MovimientoStockDTO.builder()
                .id(movimiento.getId())
                .productoInventarioId(movimiento.getProductoInventario() != null ? movimiento.getProductoInventario().getId() : null)
                .sku(movimiento.getSku())
                .tipoMovimiento(movimiento.getTipoMovimiento().name())
                .cantidadMovida(movimiento.getCantidadMovida())
                .stockFinalDespuesMovimiento(movimiento.getStockFinalDespuesMovimiento())
                .referenciaExterna(movimiento.getReferenciaExterna())
                .motivo(movimiento.getMotivo())
                .fechaMovimiento(movimiento.getFechaMovimiento())
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<ProductoInventario> buscarPorSku(String sku) {
        return productoInventarioRepository.findBySku(sku);
    }
}
