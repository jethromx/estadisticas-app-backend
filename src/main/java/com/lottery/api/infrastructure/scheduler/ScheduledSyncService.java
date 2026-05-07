package com.lottery.api.infrastructure.scheduler;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SyncResult;
import com.lottery.api.domain.port.in.SyncHistoricalDataUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledSyncService {

    private final SyncHistoricalDataUseCase syncUseCase;
    private final LotteryDrawRepositoryPort drawRepository;

    @Value("${lottery.sync.stale-threshold-days:3}")
    private int staleThresholdDays;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        List<LotteryType> staleTypes = Arrays.stream(LotteryType.values())
                .filter(this::isStale)
                .toList();

        if (staleTypes.isEmpty()) {
            log.info("[Startup] Datos actualizados, no se requiere sincronización");
            return;
        }

        log.info("[Startup] Sincronizando {} juego(s) con datos desactualizados: {}", staleTypes.size(), staleTypes);
        staleTypes.forEach(type -> {
            SyncResult r = syncUseCase.syncHistoricalData(type);
            log.info("[Startup] Sync {}: estado={} nuevos={}", r.getLotteryType(), r.getStatus(), r.getNewRecords());
        });
        log.info("[Startup] Sincronización de inicio completada");
    }

    @Scheduled(cron = "${lottery.sync.cron:0 0 3 * * *}")
    public void syncAllLotteryData() {
        log.info("Iniciando sincronización automática nocturna");
        List<SyncResult> results = syncUseCase.syncAllHistoricalData();
        results.forEach(r -> log.info(
            "Sync {}: estado={} nuevos={} omitidos={}",
            r.getLotteryType(), r.getStatus(), r.getNewRecords(), r.getSkippedRecords()
        ));
        log.info("Sincronización nocturna completada");
    }

    private boolean isStale(LotteryType type) {
        return drawRepository.findLastDrawDateByType(type)
                .map(last -> last.isBefore(LocalDate.now().minusDays(staleThresholdDays)))
                .orElse(true);
    }
}
