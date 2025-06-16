package com.retailmax.inventario.service;

import com.retailmax.inventario.dto.CrearProductoVariacionRequestDTO;
import com.retailmax.inventario.dto.ProductoVariacionDTO;
import com.retailmax.inventario.exception.ProductoExistenteException;
import com.retailmax.inventario.exception.RecursoNoEncontradoException;
import com.retailmax.inventario.model.ProductoVariacion;
import com.retailmax.inventario.repository.ProductoVariacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoVariacionService {

    private final ProductoVariacionRepository productoVariacionRepository;

    /**
     * Alias de registrarVariacion() para compatibilidad con el controlador.
     */
    @Transactional
    public ProductoVariacionDTO crearVariacion(CrearProductoVariacionRequestDTO dto) {
        return registrarVariacion(dto);
    }

    /**
     * Registra una nueva variación de producto, validando que no exista previamente.
     */
    @Transactional
    public ProductoVariacionDTO registrarVariacion(CrearProductoVariacionRequestDTO dto) {
        String sku = generarSku(dto); // SKU único basado en SKU base + talla + color

        if (productoVariacionRepository.existsBySku(sku)) {
            throw new ProductoExistenteException("Ya existe una variación con el SKU: " + sku);
        }

        ProductoVariacion variacion = ProductoVariacion.builder()
                .sku(sku)
                .skuProductoBase(dto.getSkuProductoBase())
                .talla(dto.getTalla())
                .color(dto.getColor())
                .stock(dto.getStock())
                .descripcion(dto.getDescripcion())
                .ubicacion(dto.getUbicacion())
                .fechaCreacion(LocalDateTime.now())
                .fechaUltimaActualizacion(LocalDateTime.now())
                .build();

        return mapToDTO(productoVariacionRepository.save(variacion));
    }

    private String generarSku(CrearProductoVariacionRequestDTO dto) {
        return dto.getSkuProductoBase() + "-" + dto.getTalla() + "-" + dto.getColor();
    }

    @Transactional(readOnly = true)
    public ProductoVariacionDTO obtenerPorSku(String sku) {
        ProductoVariacion variacion = productoVariacionRepository.findBySku(sku)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variación con SKU " + sku + " no encontrada."));
        return mapToDTO(variacion);
    }

    @Transactional(readOnly = true)
    public List<ProductoVariacionDTO> obtenerPorProductoBase(String skuProductoBase) {
        return productoVariacionRepository.findBySkuProductoBase(skuProductoBase).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoVariacionDTO> obtenerPorTallaYColor(String talla, String color) {
        return productoVariacionRepository.findByTallaAndColor(talla, color).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarVariacion(Long id) {
        ProductoVariacion variacion = productoVariacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la variación con ID: " + id));
        productoVariacionRepository.delete(variacion);
    }

    @Transactional
    public ProductoVariacionDTO ajustarStock(Long id, int cantidad) {
        ProductoVariacion variacion = productoVariacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la variación con ID: " + id));

        int nuevoStock = variacion.getStock() + cantidad;
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }

        variacion.setStock(nuevoStock);
        variacion.setFechaUltimaActualizacion(LocalDateTime.now());

        return mapToDTO(productoVariacionRepository.save(variacion));
    }

    private ProductoVariacionDTO mapToDTO(ProductoVariacion variacion) {
        return ProductoVariacionDTO.builder()
                .id(variacion.getId())
                .sku(variacion.getSku())
                .skuProductoBase(variacion.getSkuProductoBase())
                .talla(variacion.getTalla())
                .color(variacion.getColor())
                .cantidadDisponible(variacion.getStock()) // 👈 campo DTO
                .ubicacion(variacion.getUbicacion())
                .fechaCreacion(variacion.getFechaCreacion())
                .fechaUltimaActualizacion(variacion.getFechaUltimaActualizacion())
                .build();
    }
}
