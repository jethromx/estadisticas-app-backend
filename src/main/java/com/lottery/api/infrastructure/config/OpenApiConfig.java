package com.lottery.api.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lotteryOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtenido en /api/v1/auth/login")))
                .info(new Info()
                        .title("Lottery API — Pronósticos Lotería Nacional México")
                        .description("""
                                API REST para análisis estadístico de los juegos de pronósticos de Lotería Nacional:
                                **Melate**, **Revancha**, **Revanchita** y **GanaGato**.

                                Funcionalidades:
                                - Autenticación JWT con registro e inicio de sesión de usuarios
                                - Sincronización del histórico de sorteos desde la fuente oficial (CSV)
                                - Estadísticas agregadas: frecuencias, promedios, números nunca sorteados
                                - Números calientes y fríos (histórico completo y últimos N sorteos)
                                - Predicciones guardadas por usuario con análisis de precisión
                                - Sugerencias de mejora basadas en comparación con sorteos reales

                                **Aviso:** Las sugerencias son puramente estadísticas y no garantizan resultados.
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Lottery API")
                                .email("contacto@lottery-api.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Históricos Lotería Nacional")
                        .url("https://www.loterianacional.gob.mx"));
    }
}
