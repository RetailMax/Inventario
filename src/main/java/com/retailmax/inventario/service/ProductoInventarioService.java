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

     @Transactional
    public ProductoInventarioDTO actualizarStock(ActualizarStockRequestDTO requestDTO) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(requestDTO.getSku())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + requestDTO.getSku() + " no encontrado en el inventario."));

        Integer cantidadMovida = requestDTO.getCantidad();
        TipoMovimiento tipoMovimiento = TipoMovimiento.fromName(requestDTO.getTipoActualizacion());

        Integer oldCantidadDisponible = productoInventario.getCantidadDisponible();
        Integer oldCantidadReservada = productoInventario.getCantidadReservada();

        switch (tipoMovimiento) {
            case ENTRADA:
                productoInventario.setCantidadDisponible(oldCantidadDisponible + cantidadMovida);
                break;
            case SALIDA:
                if (oldCantidadDisponible < cantidadMovida) {
                    throw new StockInsuficienteException("No hay suficiente stock disponible para salida de " + cantidadMovida + " unidades del SKU " + requestDTO.getSku() + ". Stock actual: " + oldCantidadDisponible);
                }
                productoInventario.setCantidadDisponible(oldCantidadDisponible - cantidadMovida);
                break;
             case RESERVA:
                if (oldCantidadDisponible < cantidadMovida) {
                    throw new StockInsuficienteException("No hay suficiente stock disponible para reservar " + cantidadMovida + " unidades del SKU " + requestDTO.getSku() + ". Stock actual: " + oldCantidadDisponible);
                }
                productoInventario.setCantidadDisponible(oldCantidadDisponible - cantidadMovida);
                productoInventario.setCantidadReservada(oldCantidadReservada + cantidadMovida);
                break;
            case LIBERACION:
                if (oldCantidadReservada < cantidadMovida) {
                    throw new StockInsuficienteException("No hay suficiente stock reservado para liberar " + cantidadMovida + " unidades del SKU " + requestDTO.getSku() + ". Stock reservado: " + oldCantidadReservada);
                }
                productoInventario.setCantidadDisponible(oldCantidadDisponible + cantidadMovida);
                productoInventario.setCantidadReservada(oldCantidadReservada - cantidadMovida);
                break;
            case AJUSTE:
                // Para AJUSTE, la cantidadMovida puede ser positiva (incremento) o negativa (decremento)
                if (oldCantidadDisponible + cantidadMovida < 0) {
                     throw new IllegalArgumentException("El ajuste resultaría en stock negativo para el SKU " + requestDTO.getSku());
                }
                productoInventario.setCantidadDisponible(oldCantidadDisponible + cantidadMovida);
                break;
            case DEVOLUCION_CLIENTE: // Una devolución de cliente incrementa el stock disponible
                productoInventario.setCantidadDisponible(oldCantidadDisponible + cantidadMovida);
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento '" + tipoMovimiento + "' no soportado para esta operación de actualización.");
        }

        productoInventario.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario updatedProductoInventario = productoInventarioRepository.save(productoInventario);

        registrarMovimiento(updatedProductoInventario, tipoMovimiento, cantidadMovida, requestDTO.getReferenciaExterna());
        return mapToProductoInventarioDTO(updatedProductoInventario);
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



    @Transactional
    public void eliminarProducto(String sku) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado, no se puede eliminar."));

        // Opcional: Antes de eliminar el producto, podrías querer eliminar o archivar los movimientos de stock asociados
        // List<MovimientoStock> movimientosAsociados = movimientoStockRepository.findByProductoInventarioIdOrderByFechaMovimientoDesc(productoInventario.getId());
        // movimientoStockRepository.deleteAll(movimientosAsociados); // Ejemplo de eliminación de movimientos
        productoInventarioRepository.delete(productoInventario);
    }

    @Transactional(readOnly = true)
    public List<ProductoInventarioDTO> consultarTodosLosProductos() {
        return productoInventarioRepository.findAll().stream()
                .map(this::mapToProductoInventarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoInventarioDTO consultarProductoPorSku(String sku) {
        ProductoInventario productoInventario = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado."));
        return mapToProductoInventarioDTO(productoInventario);
    }

    @Transactional
    public ProductoInventarioDTO actualizarProducto(String sku, AgregarProductoInventarioRequestDTO requestDTO) {
        ProductoInventario productoExistente = productoInventarioRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado, no se puede actualizar."));

        // Actualizar los campos permitidos. SKU generalmente no se actualiza, o si se hace, requiere cuidado.
        // Aquí asumimos que el SKU en el path es el identificador y el SKU en el DTO es el nuevo valor si se permite cambiar.
        // Por simplicidad, no actualizaremos el SKU aquí, sino otros campos.
        if (requestDTO.getUbicacionAlmacen() != null) {
            productoExistente.setUbicacionAlmacen(requestDTO.getUbicacionAlmacen());
        }
        if (requestDTO.getCantidadMinimaStock() != null) {
            productoExistente.setCantidadMinimaStock(requestDTO.getCantidadMinimaStock());
        }
        // No se actualiza la cantidad inicial directamente aquí, eso se maneja vía actualizarStock.

        productoExistente.setFechaUltimaActualizacion(LocalDateTime.now());
        ProductoInventario productoActualizado = productoInventarioRepository.save(productoExistente);
        return mapToProductoInventarioDTO(productoActualizado);
    }    

    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> obtenerHistorialMovimientos(String sku, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Primero, verificar que el producto exista.
        if (!productoInventarioRepository.existsBySku(sku)) {
            throw new RecursoNoEncontradoException("Producto con SKU " + sku + " no encontrado, no se puede obtener su historial de movimientos.");
        }

        List<MovimientoStock> movimientos;
        // Aquí podrías tener una lógica más compleja si necesitas filtrar por fechaInicio y fechaFin.
        // Por ahora, simplemente obtenemos todos los movimientos para el SKU ordenados por fecha.
        // Asegúrate de que tu MovimientoStockRepository tenga un método adecuado, como findBySkuOrderByFechaMovimientoDesc.
        movimientos = movimientoStockRepository.findBySkuOrderByFechaMovimientoDesc(sku);

        return movimientos.stream()
                .map(this::mapToMovimientoStockDTO)
                .collect(Collectors.toList());
    }

    // Método auxiliar para mapear MovimientoStock a MovimientoStockDTO
    // Este método podría estar en una clase Mapper dedicada si prefieres.
    private MovimientoStockDTO mapToMovimientoStockDTO(MovimientoStock movimiento) {
        return MovimientoStockDTO.builder()
                // Asume que MovimientoStockDTO tiene los campos correspondientes y un builder.
                .id(movimiento.getId())
                .sku(movimiento.getSku())
                .tipoMovimiento(movimiento.getTipoMovimiento().name())
                .cantidadMovida(movimiento.getCantidadMovida())
                .stockFinalDespuesMovimiento(movimiento.getStockFinalDespuesMovimiento())
                .referenciaExterna(movimiento.getReferenciaExterna())
                .motivo(movimiento.getMotivo())
                .fechaMovimiento(movimiento.getFechaMovimiento())
                .productoInventarioId(movimiento.getProductoInventario() != null ? movimiento.getProductoInventario().getId() : null)
                .build();
    }

}