package com.retailmax.inventario.config; // Assuming this package based on previous context, adjust if different

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API 2025 Microservicio de Inventario")
                        .version("1.0")
                        .description("Documentación de la API del microservicio de inventario para el proyecto de Duoc"));
    }
}
