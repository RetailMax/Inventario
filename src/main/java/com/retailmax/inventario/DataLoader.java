package com.retailmax.inventario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID; // Para generar SKUs únicos

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.retailmax.inventario.model.MovimientoStock;
import com.retailmax.inventario.model.ProductoInventario;
import com.retailmax.inventario.model.UmbralAlerta;
import com.retailmax.inventario.model.enums.TipoAlerta;
import com.retailmax.inventario.model.enums.TipoMovimiento;

import com.retailmax.inventario.repository.MovimientoStockRepository;
import com.retailmax.inventario.repository.ProductoInventarioRepository;
import com.retailmax.inventario.repository.UmbralAlertaRepository;

import net.datafaker.Faker;

@Profile("dev") // Esto asegura que solo se ejecute en el perfil "dev"
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;
    @Autowired
    private ProductoInventarioRepository productoInventarioRepository;
    @Autowired
    private UmbralAlertaRepository umbralAlertaRepository;

    @Override
    public void run(String... args) throws Exception {
        // Limpiar datos existentes para asegurar una carga limpia en el perfil dev
        System.out.println("Limpiando datos existentes antes de la carga...");
        movimientoStockRepository.deleteAll();
        umbralAlertaRepository.deleteAll();
        productoInventarioRepository.deleteAll();
        System.out.println("Datos existentes limpiados.");

       
        Faker faker = new Faker();
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now(); // Para auditoría

        System.out.println("Cargando datos de prueba para Inventario RetailMax...");

        // 1. Generar ProductoInventario
        // Vamos a generar productos con stock inicial
        System.out.println("-> Cargando ProductoInventario...");
        for (int i = 0; i < 20; i++) {
            ProductoInventario producto = new ProductoInventario();
            producto.setSku(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            producto.setStock(faker.number().numberBetween(50, 500)); // Stock total en el almacén
            producto.setCantidadReservada(faker.number().numberBetween(0, producto.getStock() / 5)); // Una parte del stock está reservada
            producto.setCantidadEnTransito(faker.number().numberBetween(0, producto.getStock() / 10)); // Una parte en tránsito
            
            // Cantidad disponible = stock - cantidadReservada - cantidadEnTransito (asegurando que no sea negativo)
            int stockDisponibleCalculado = producto.getStock() - producto.getCantidadReservada() - producto.getCantidadEnTransito();
            producto.setCantidadDisponible(Math.max(0, stockDisponibleCalculado)); 
            
            producto.setCantidadMinimaStock(faker.number().numberBetween(10, 50)); // Umbral para alerta
            producto.setUbicacionAlmacen(faker.address().city() + " Bodega " + faker.number().digit());
            producto.setActivo(faker.bool().bool());
            producto.setFechaCreacion(now.minusDays(faker.number().numberBetween(0, 365)));
            producto.setFechaUltimaActualizacion(now);

            productoInventarioRepository.save(producto);
        }
        List<ProductoInventario> productos = productoInventarioRepository.findAll();
        System.out.println("-> " + productos.size() + " ProductosInventario cargados.");

        // 2. Generar UmbralesAlerta
        // Estos umbrales ahora están asociados a un SKU específico.
        // Aseguramos que se genere un UmbralAlerta por cada SKU de ProductoInventario
        System.out.println("-> Cargando UmbralesAlerta...");
        for (ProductoInventario producto : productos) {
            UmbralAlerta umbral = new UmbralAlerta();
            umbral.setSku(producto.getSku()); // Asociar al SKU del producto
            umbral.setTipoAlerta(faker.options().option(TipoAlerta.class));
            umbral.setUmbralCantidad(faker.number().numberBetween(5, 25)); // Cantidad específica para el umbral
            umbral.setActivo(true); // Asumimos que los umbrales de alerta son activos al crearse
            umbral.setFechaCreacion(now.minusDays(faker.number().numberBetween(0, 180)));
            umbral.setFechaUltimaActualizacion(now);

            umbralAlertaRepository.save(umbral);
        }
        System.out.println("-> " + umbralAlertaRepository.count() + " UmbralesAlerta cargados.");


        // 3. Generar MovimientosStock
        // Los movimientos de stock requieren un producto asociado y un SKU
        System.out.println("-> Cargando MovimientosStock...");
        if (!productos.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                MovimientoStock movimiento = new MovimientoStock();
                ProductoInventario productoAleatorio = productos.get(random.nextInt(productos.size()));

                movimiento.setProductoInventario(productoAleatorio);
                movimiento.setSku(productoAleatorio.getSku()); // SKU del producto asociado
                // Asegurar que solo se seleccionen tipos de movimiento permitidos por la restricción de BD
                List<TipoMovimiento> tiposPermitidosPorConstraint = List.of(
                    TipoMovimiento.ENTRADA,
                    TipoMovimiento.SALIDA,
                    TipoMovimiento.AJUSTE,
                    TipoMovimiento.RESERVA,
                    TipoMovimiento.LIBERACION,
                    TipoMovimiento.DEVOLUCION_CLIENTE
                );
                TipoMovimiento tipoSeleccionado = faker.options().option(tiposPermitidosPorConstraint.toArray(new TipoMovimiento[0]));


                movimiento.setTipoMovimiento(tipoSeleccionado);

                int cantidadMovida;
                if (tipoSeleccionado == TipoMovimiento.RESERVA || tipoSeleccionado == TipoMovimiento.LIBERACION) {
                    // Para RESERVA y LIBERACION, la cantidad física movida es 0 (solo cambia el estado del stock).
                
                cantidadMovida = 0;
                } else {
                    cantidadMovida = faker.number().numberBetween(1, 20); // Cantidad base del movimiento
                    // De los tipos permitidos, solo SALIDA implica una cantidad negativa.
                    if (tipoSeleccionado == TipoMovimiento.SALIDA) {
                        
                    cantidadMovida *= -1; // Negar para movimientos que disminuyen stock físico
                    }
                }
                movimiento.setCantidadMovida(cantidadMovida);

                // El stock final después del movimiento normalmente lo calcularía el servicio
                // Aquí lo simulamos de forma básica.
                // En un escenario real, esto se manejaría en la lógica de negocio.
                int stockAntes = productoAleatorio.getStock(); // Simular stock antes del movimiento
                movimiento.setStockFinalDespuesMovimiento(Math.max(0, stockAntes + cantidadMovida)); // Asegurar que no sea negativo


                movimiento.setReferenciaExterna(faker.bothify("ORD#####")); // Número de orden, factura, etc.
                movimiento.setMotivo(faker.lorem().sentence(3));
                movimiento.setFechaMovimiento(now.minusHours(faker.number().numberBetween(0, 720))); // Movimientos recientes

                movimientoStockRepository.save(movimiento);
            }
            System.out.println("-> " + movimientoStockRepository.count() + " MovimientosStock cargados.");
        } else {
            System.out.println("-> No se pudieron generar MovimientosStock porque no hay ProductosInventario.");
        }

        System.out.println("Carga inicial de datos de inventario completada.");
    }
}