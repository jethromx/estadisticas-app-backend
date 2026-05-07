package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SyncResult;
import com.lottery.api.domain.port.in.SyncHistoricalDataUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import com.lottery.api.domain.port.out.LotteryHistoricalDownloaderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Servicio que coordina la descarga del CSV y la persistencia de sorteos nuevos.
 *
 * <p>No sobreescribe sorteos existentes: si el concurso ya está en BD se cuenta
 * como {@code skipped} para preservar datos corregidos manualmente.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncHistoricalDataService implements SyncHistoricalDataUseCase {

    private final LotteryHistoricalDownloaderPort downloaderPort;
    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Caching(evict = {
        @CacheEvict(value = "analysis-due",      allEntries = true),
        @CacheEvict(value = "analysis-sum",      allEntries = true),
        @CacheEvict(value = "analysis-balance",  allEntries = true),
        @CacheEvict(value = "analysis-windowed", allEntries = true),
        @CacheEvict(value = "analysis-pairs",    allEntries = true),
        @CacheEvict(value = "analysis-chi",      allEntries = true),
        @CacheEvict(value = "analysis-backtest",      allEntries = true),
        @CacheEvict(value = "analysis-bayesian",      allEntries = true),
        @CacheEvict(value = "draws",                  allEntries = true),
        @CacheEvict(value = "analysis-position",      allEntries = true),
        @CacheEvict(value = "analysis-consecutive",   allEntries = true),
        @CacheEvict(value = "analysis-rich-backtest", allEntries = true),
        @CacheEvict(value = "analysis-temporal",      allEntries = true),
        @CacheEvict(value = "analysis-entropy",       allEntries = true),
        @CacheEvict(value = "analysis-cluster",       allEntries = true),
        @CacheEvict(value = "analysis-streak",        allEntries = true),
        @CacheEvict(value = "analysis-ensemble",      allEntries = true),
        @CacheEvict(value = "analysis-calendar",      allEntries = true),
        @CacheEvict(value = "analysis-neural",        allEntries = true),
    })
    public SyncResult syncHistoricalData(LotteryType lotteryType) {
        log.info("Iniciando sincronización para: {}", lotteryType);
        try {
            List<LotteryDraw> draws = downloaderPort.downloadHistoricalData(lotteryType);
            log.info("Descargados {} registros para {}", draws.size(), lotteryType);
            return persistNewDraws(lotteryType, draws, "Sincronización completada");
        } catch (Exception e) {
            log.error("Error sincronizando {}: {}", lotteryType, e.getMessage(), e);
            return buildResult(lotteryType, 0, 0, 0, "ERROR", e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<SyncResult> syncAllHistoricalData() {
        return Arrays.stream(LotteryType.values())
                .map(this::syncHistoricalData)
                .toList();
    }

    @Override
    public SyncResult importFromStream(LotteryType lotteryType, InputStream csvStream) {
        log.info("Importando CSV local para: {}", lotteryType);
        try {
            List<LotteryDraw> draws = downloaderPort.parseFromStream(lotteryType, csvStream);
            return persistNewDraws(lotteryType, draws, "Importación completada");
        } catch (Exception e) {
            log.error("Error importando {}: {}", lotteryType, e.getMessage(), e);
            return buildResult(lotteryType, 0, 0, 0, "ERROR", e.getMessage());
        }
    }

    @Transactional
    SyncResult persistNewDraws(LotteryType lotteryType, List<LotteryDraw> draws, String successMessage) {
        Set<Integer> existing = repositoryPort.findAllDrawNumbersByType(lotteryType);
        List<LotteryDraw> newDraws = draws.stream()
                .filter(d -> !existing.contains(d.getDrawNumber()))
                .toList();
        if (!newDraws.isEmpty()) {
            repositoryPort.saveAll(newDraws);
        }
        int skipped = draws.size() - newDraws.size();
        log.info("Sync {} completado: {} nuevos, {} omitidos", lotteryType, newDraws.size(), skipped);
        return buildResult(lotteryType, draws.size(), newDraws.size(), skipped, "SUCCESS", successMessage);
    }

    private SyncResult buildResult(LotteryType type, int total, int newRec, int skipped,
                                   String status, String message) {
        return SyncResult.builder()
                .lotteryType(type)
                .totalRecords(total)
                .newRecords(newRec)
                .updatedRecords(0)
                .skippedRecords(skipped)
                .syncedAt(LocalDateTime.now())
                .status(status)
                .message(message)
                .build();
    }
}
