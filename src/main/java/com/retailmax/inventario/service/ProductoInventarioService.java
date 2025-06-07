package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.ActualizarStockRequestDTO;
import com.retailmax.inventario.dto.AgregarProductoInventarioRequestDTO;
import com.retailmax.inventario.dto.MovimientoStockDTO;
import com.retailmax.inventario.dto.ProductoInventarioDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoInventarioService {

    private final ProductoInventarioRepository productoInventarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public ProductoInventarioDTO agregarProductoInventario(AgregarProductoInventarioRequestDTO requestDTO) {
        if (productoInventarioRepository.existsBySku(requestDTO.getSku())) {
            throw new ProductoExistenteException("Ya existe un producto con el SKU: " + requestDTO.getSku() + " en el inventario.");
        }

        ProductoInventario nuevoProductoInventario = new ProductoInventario();
        nuevoProductoInventario.setSku(requestDTO.getSku());
        nuevoProductoInventario.setCantidadDisponible(requestDTO.getCantidadInicial());
        nuevoProductoInventario.setUbicacionAlmacen(requestDTO.getUbicacionAlmacen());
        nuevoProductoInventario.setCantidadMinimaStock(requestDTO.getCantidadMinimaStock() != null ? requestDTO.getCantidadMinimaStock() : 0);

        nuevoProductoInventario.setFechaCreacion(LocalDateTime.now());
        nuevoProductoInventario.setFechaUltimaActualizacion(LocalDateTime.now());
        nuevoProductoInventario.setCantidadReservada(0);
        nuevoProductoInventario.setCantidadEnTransito(0); // Inicializar cantidad en tránsito
        nuevoProductoInventario.setActivo(true); // Establecer como activo por defecto
        ProductoInventario savedProductoInventario = productoInventarioRepository.save(nuevoProductoInventario);

        registrarMovimiento(
            savedProductoInventario,
            TipoMovimiento.ENTRADA,
            requestDTO.getCantidadInicial(),
            "Entrada inicial al inventario"
        );

        return mapToProductoInventarioDTO(savedProductoInventario);
    }

    

    @Transactional(readOnly = true)
    public ProductoInventarioDTO consultarStockPorSku(String sku) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado en el inventario."));
        return mapToProductoInventarioDTO(productoInventario);
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> consultarTodoElStock() {
        return productoInventarioRepository.findAll().stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoInventarioDTO realizarAjusteManual(String sku, Integer cantidad, String motivo) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado para ajuste manual."));

        Integer oldCantidadDisponible = productoInventario.getCantidadDisponible();
        if (oldCantidadDisponible + cantidad < 0) {
            throw new IllegalArgumentException("El ajuste de " + cantidad + " unidades resultaría en stock negativo para el SKU " + sku + ". Stock actual: " + oldCantidadDisponible);
        }

        productoInventario.setCantidadDisponible(oldCantidadDisponible + cantidad);
        productoInventario.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario updatedProductoInventario = productoInventarioRepository.save(productoInventario);

        registrarMovimiento(
            updatedProductoInventario,
            TipoMovimiento.AJUSTE,
            cantidad,
            motivo
        );

        return mapToProductoInventarioDTO(updatedProductoInventario);
    }

    @Transactional
    public ProductoInventarioDTO decrementarStockPorOrdenConfirmada(String sku, Integer cantidad, String ordenId) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado para decrementar stock por orden confirmada."));

        if (productoInventario.getCantidadDisponible() < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente para la orden " + ordenId + " del SKU " + sku + ". Disponible: " + productoInventario.getCantidadDisponible() + ", Solicitado: " + cantidad);
        }

        productoInventario.setCantidadDisponible(productoInventario.getCantidadDisponible() - cantidad);
        productoInventario.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario updatedProductoInventario = productoInventarioRepository.save(productoInventario);

        registrarMovimiento(
            updatedProductoInventario,
            TipoMovimiento.SALIDA,
            cantidad,
            "Salida por orden confirmada: " + ordenId
        );

        return mapToProductoInventarioDTO(updatedProductoInventario);
    }

    @Transactional(readOnly = true)
    public boolean validarDisponibilidad(String sku, Integer cantidadRequerida) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado para validar disponibilidad."));

        return productoInventario.getCantidadDisponible() >= cantidadRequerida;
    }
    @Transactional
    public ProductoInventarioDTO actualizarStock(ActualizarStockRequestDTO request) {
        ProductoInventario producto = productoInventarioRepository.findBySku(request.getSku())
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + request.getSku() + " no encontrado."));

        TipoMovimiento tipoMovimiento = TipoMovimiento.valueOf(request.getTipoActualizacion());

        if (tipoMovimiento == TipoMovimiento.SALIDA && producto.getCantidadDisponible() < request.getCantidad()) {
            throw new StockInsuficienteException("Stock insuficiente para realizar la salida.");
        }

        int nuevaCantidad = tipoMovimiento == TipoMovimiento.ENTRADA
            ? producto.getCantidadDisponible() + request.getCantidad()
            : producto.getCantidadDisponible() - request.getCantidad();

        producto.setCantidadDisponible(nuevaCantidad);
        producto.setFechaUltimaActualizacion(LocalDateTime.now());

        productoInventarioRepository.save(producto);

        registrarMovimiento(producto, tipoMovimiento, request.getCantidad(), request.getReferenciaExterna());

        return mapToProductoInventarioDTO(producto);
    }
    @Transactional(readOnly = true)
    public ProductoInventario obtenerProductoPorSku(String sku) {
        return productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));
    }


    private void registrarMovimiento(ProductoInventario productoInventario, TipoMovimiento tipoMovimiento, Integer cantidad, String referenciaExterna) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoInventario(productoInventario);
        movimiento.setSku(productoInventario.getSku());
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setCantidadMovida(cantidad);
        movimiento.setStockFinalDespuesMovimiento(productoInventario.getCantidadDisponible()); // Stock después de la operación
        movimiento.setReferenciaExterna(referenciaExterna);
        movimiento.setMotivo(tipoMovimiento.getDescripcion()); // Usar la descripción del tipo de movimiento como motivo general
        movimiento.setFechaMovimiento(LocalDateTime.now()); // Establecer la fecha y hora actual del movimiento
        movimientoStockRepository.save(movimiento);
    }
    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> obtenerHistorialMovimientos(String sku, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado para obtener su historial de movimientos."));

        List<MovimientoStock> movimientos;
        if (fechaInicio != null && fechaFin != null) {
            movimientos = movimientoStockRepository.findByProductoInventarioIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                productoInventario.getId(), fechaInicio, fechaFin
            );
        } else {
            movimientos = movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(productoInventario.getId());
        }

        return movimientos.stream()
                .map(this::mapToMovimientoStockDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockBajo(Integer umbralCantidadMinima) {
        List<ProductoInventario> productosBajoStock = productoInventarioRepository.findByCantidadDisponibleLessThan(umbralCantidadMinima);
        System.out.println("Alertas de Stock Bajo generadas para: " + productosBajoStock.size() + " productos.");
        return productosBajoStock.stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> verificarYNotificarStockExcesivo(Integer umbralCantidadExcesiva) {
        List<ProductoInventario> productosConStockExcesivo = productoInventarioRepository.findByCantidadDisponibleGreaterThan(umbralCantidadExcesiva);
        System.out.println("Alertas de Stock Excesivo generadas para: " + productosConStockExcesivo.size() + " productos.");
        return productosConStockExcesivo.stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> generarReporteDeInventarioActual() {
        return consultarTodoElStock();
    }

    @Transactional
    public ProductoInventarioDTO recibirStockDeProveedor(String sku, Integer cantidad, String referenciaProveedor) {
        try {
            ProductoInventarioDTO productoActualizado = actualizarStock(
                new ActualizarStockRequestDTO(
                    sku,
                    cantidad, // Correcto: cantidad (Integer)
                    TipoMovimiento.ENTRADA.name(), // Correcto: tipoActualizacion (String)
                    "Recepción de proveedor: " + referenciaProveedor, // Correcto: referenciaExterna (String)
                    "Recepción de proveedor" // Falta: motivo (String)
                )
            );
            System.out.println("Stock actualizado para " + sku + " desde proveedor. Cantidad: " + cantidad);
            return productoActualizado;
        } catch (RecursoNoEncontradoException e) {
            System.out.println("Producto " + sku + " no encontrado, creándolo con stock inicial.");
            AgregarProductoInventarioRequestDTO newProductRequest = new AgregarProductoInventarioRequestDTO(
                sku,
                cantidad,
                "UbicacionDefecto",
                0
            );
            return agregarProductoInventario(newProductRequest);
        }
    }

    private ProductoInventarioDTO mapToProductoInventarioDTO(ProductoInventario productoInventario) {
        return ProductoInventarioDTO.builder()
                .id(productoInventario.getId())
                .sku(productoInventario.getSku())
                .cantidadDisponible(productoInventario.getCantidadDisponible())
                .cantidadReservada(productoInventario.getCantidadReservada() != null ? productoInventario.getCantidadReservada() : 0)
                .cantidadTotal(productoInventario.getCantidadDisponible() + (productoInventario.getCantidadReservada() != null ? productoInventario.getCantidadReservada() : 0))
                .ubicacionAlmacen(productoInventario.getUbicacionAlmacen())
                .cantidadMinimaStock(productoInventario.getCantidadMinimaStock() != null ? productoInventario.getCantidadMinimaStock() : 0)
                .fechaCreacion(productoInventario.getFechaCreacion())
                .fechaUltimaActualizacion(productoInventario.getFechaUltimaActualizacion())
                .build();
    }

    private MovimientoStockDTO mapToMovimientoStockDTO(MovimientoStock movimientoStock) {
        return MovimientoStockDTO.builder()
                .id(movimientoStock.getId())
                .productoInventarioId(movimientoStock.getProductoInventario() != null ? movimientoStock.getProductoInventario().getId() : null)
                .sku(movimientoStock.getSku())
                .tipoMovimiento(movimientoStock.getTipoMovimiento()) // ✅ Aquí va el enum, no .name()
                .cantidadMovida(movimientoStock.getCantidadMovida())
                .stockFinalDespuesMovimiento(movimientoStock.getStockFinalDespuesMovimiento())
                .referenciaExterna(movimientoStock.getReferenciaExterna())
                .motivo(movimientoStock.getMotivo())
                .fechaMovimiento(movimientoStock.getFechaMovimiento())
                .build();
    }
}