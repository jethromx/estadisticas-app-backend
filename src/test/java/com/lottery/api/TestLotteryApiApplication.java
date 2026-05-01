package com.lottery.api;

import org.springframework.boot.SpringApplication;

/**
 * Punto de entrada para pruebas de integración con Testcontainers.
 */
public class TestLotteryApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(LotteryApiApplication::main)
                .with(TestcontainersConfig.class)
                .run(args);
    }
}
