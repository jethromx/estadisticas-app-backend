package com.lottery.api.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la documentación OpenAPI 3 de la API de Lotería Nacional.
 *
 * <p>Acceder a la UI en: <a href="http://localhost:8080/swagger-ui.html">swagger-ui.html</a></p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lotteryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lottery API — Pronósticos Lotería Nacional México")
                        .description("""
                                API REST para análisis estadístico de los juegos de pronósticos de Lotería Nacional:
                                **Melate**, **Revancha**, **Revanchita** y **GanaGato**.

                                Funcionalidades:
                                - Sincronización del histórico de sorteos desde la fuente oficial (CSV)
                                - Estadísticas agregadas: frecuencias, promedios, números nunca sorteados
                                - Números calientes y fríos (histórico completo y últimos N sorteos)
                                - Sugerencias de apuesta basadas en patrones estadísticos

                                **Aviso:** Las sugerencias son puramente estadísticas y no garantizan resultados.
                                """)
                        .version("1.0.0")
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
