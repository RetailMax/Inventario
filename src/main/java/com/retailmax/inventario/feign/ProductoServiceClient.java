package com.retailmax.inventario.feign;

import com.retailmax.inventario.dto.ProductoCatalogoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "producto-service", url = "${producto.service.url}")
public interface ProductoServiceClient {
    @GetMapping("/api/productos/{sku}")
    ProductoCatalogoDTO getProductoBySku(@PathVariable("sku") String sku);
} 