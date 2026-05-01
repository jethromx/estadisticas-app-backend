package com.lottery.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Lottery API.
 *
 * <p>Arquitectura hexagonal (Ports & Adapters):</p>
 * <ul>
 *   <li><b>domain</b> — modelos de dominio, excepciones y puertos (interfaces)</li>
 *   <li><b>application</b> — casos de uso / servicios de aplicación</li>
 *   <li><b>infrastructure</b> — adaptadores web (REST), persistencia (JPA) y descargador CSV</li>
 * </ul>
 *
 * <p>Documentación Swagger UI: <a href="http://localhost:8080/swagger-ui.html">swagger-ui.html</a></p>
 */
@SpringBootApplication
public class LotteryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryApiApplication.class, args);
    }
}
