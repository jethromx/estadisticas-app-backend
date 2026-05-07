package com.lottery.api.infrastructure.config;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.GetNeuralPredictionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Pre-calienta el caché de predicción neural en background al arrancar la aplicación,
 * para que la primera petición HTTP no espere el entrenamiento completo.
 */
@Slf4j
@Component
@Profile("local")   // solo activo en local; deshabilitado en Render (profile: default/prod)
@RequiredArgsConstructor
public class NeuralCacheWarmer {

    private final GetNeuralPredictionUseCase neuralPredictionUseCase;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmCache() {
        for (LotteryType type : LotteryType.values()) {
            try {
                log.info("[NeuralCacheWarmer] Pre-calentando caché neural para {}", type);
                neuralPredictionUseCase.getNeuralPrediction(type);
                log.info("[NeuralCacheWarmer] Caché neural listo para {}", type);
            } catch (Exception e) {
                log.warn("[NeuralCacheWarmer] No se pudo pre-calentar caché para {}: {}", type, e.getMessage());
            }
        }
    }
}
