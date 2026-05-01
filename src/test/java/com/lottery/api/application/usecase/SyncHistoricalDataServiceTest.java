package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SyncResult;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import com.lottery.api.domain.port.out.LotteryHistoricalDownloaderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncHistoricalDataService — Tests Unitarios")
class SyncHistoricalDataServiceTest {

    @Mock private LotteryHistoricalDownloaderPort downloaderPort;
    @Mock private LotteryDrawRepositoryPort repositoryPort;

    @InjectMocks private SyncHistoricalDataService service;

    private LotteryDraw sampleDraw;

    @BeforeEach
    void setUp() {
        sampleDraw = LotteryDraw.builder()
                .lotteryType(LotteryType.MELATE)
                .drawNumber(4158)
                .drawDate(LocalDate.of(2026, 1, 7))
                .numbers(List.of(1, 26, 30, 35, 45, 54))
                .additionalNumber(28)
                .build();
    }

    @Test
    @DisplayName("debe insertar sorteos nuevos y retornar SUCCESS")
    void syncHistoricalData_newDraws_returnsSuccess() {
        when(downloaderPort.downloadHistoricalData(LotteryType.MELATE)).thenReturn(List.of(sampleDraw));
        when(repositoryPort.findAllDrawNumbersByType(LotteryType.MELATE)).thenReturn(Set.of());
        when(repositoryPort.saveAll(List.of(sampleDraw))).thenReturn(List.of(sampleDraw));

        SyncResult result = service.syncHistoricalData(LotteryType.MELATE);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getNewRecords()).isEqualTo(1);
        assertThat(result.getSkippedRecords()).isEqualTo(0);
        assertThat(result.getTotalRecords()).isEqualTo(1);
        verify(repositoryPort, times(1)).saveAll(List.of(sampleDraw));
    }

    @Test
    @DisplayName("debe omitir sorteos ya existentes")
    void syncHistoricalData_existingDraws_skipsAndReturnsSuccess() {
        when(downloaderPort.downloadHistoricalData(LotteryType.MELATE)).thenReturn(List.of(sampleDraw));
        when(repositoryPort.findAllDrawNumbersByType(LotteryType.MELATE)).thenReturn(Set.of(4158));

        SyncResult result = service.syncHistoricalData(LotteryType.MELATE);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getNewRecords()).isEqualTo(0);
        assertThat(result.getSkippedRecords()).isEqualTo(1);
        verify(repositoryPort, never()).saveAll(any());
    }

    @Test
    @DisplayName("debe retornar ERROR cuando el downloader lanza excepción")
    void syncHistoricalData_downloaderThrows_returnsError() {
        when(downloaderPort.downloadHistoricalData(LotteryType.MELATE))
                .thenThrow(new RuntimeException("Conexión rechazada"));

        SyncResult result = service.syncHistoricalData(LotteryType.MELATE);

        assertThat(result.getStatus()).isEqualTo("ERROR");
        assertThat(result.getMessage()).contains("Conexión rechazada");
        assertThat(result.getNewRecords()).isEqualTo(0);
    }

    @Test
    @DisplayName("debe sincronizar todos los tipos de lotería")
    void syncAllHistoricalData_allTypes_callsEachType() {
        for (LotteryType type : LotteryType.values()) {
            when(downloaderPort.downloadHistoricalData(type)).thenReturn(List.of());
            when(repositoryPort.findAllDrawNumbersByType(type)).thenReturn(Set.of());
        }

        List<SyncResult> results = service.syncAllHistoricalData();

        assertThat(results).hasSize(LotteryType.values().length);
        assertThat(results).allMatch(r -> "SUCCESS".equals(r.getStatus()));
        verify(downloaderPort, times(LotteryType.values().length)).downloadHistoricalData(any());
    }

    @Test
    @DisplayName("syncedAt debe estar definido en el resultado")
    void syncHistoricalData_resultContainsSyncTime() {
        when(downloaderPort.downloadHistoricalData(LotteryType.REVANCHA)).thenReturn(List.of());
        when(repositoryPort.findAllDrawNumbersByType(LotteryType.REVANCHA)).thenReturn(Set.of());

        SyncResult result = service.syncHistoricalData(LotteryType.REVANCHA);

        assertThat(result.getSyncedAt()).isNotNull();
        assertThat(result.getLotteryType()).isEqualTo(LotteryType.REVANCHA);
    }
}
