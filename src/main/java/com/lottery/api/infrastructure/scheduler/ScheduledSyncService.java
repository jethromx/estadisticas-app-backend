package com.lottery.api.infrastructure.scheduler;

import com.lottery.api.domain.model.SyncResult;
import com.lottery.api.domain.port.in.SyncHistoricalDataUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sincronización automática nocturna de datos históricos.
 * Se ejecuta a las 3:00 AM hora del servidor para los tres juegos activos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledSyncService {

    private final SyncHistoricalDataUseCase syncUseCase;

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
}
