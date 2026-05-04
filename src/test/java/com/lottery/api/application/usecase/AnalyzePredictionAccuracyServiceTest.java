package com.lottery.api.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.api.domain.exception.PredictionNotFoundException;
import com.lottery.api.domain.exception.UnauthorizedPredictionAccessException;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.PredictionAccuracyResult;
import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.SyncHistoricalDataUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyzePredictionAccuracyService — Tests Unitarios")
class AnalyzePredictionAccuracyServiceTest {

    @Mock private SavedPredictionRepositoryPort predictionRepository;
    @Mock private LotteryDrawRepositoryPort drawRepository;
    @Mock private SyncHistoricalDataUseCase syncUseCase;

    @InjectMocks private AnalyzePredictionAccuracyService service;

    @BeforeEach
    void setUp() {
        // Inject real ObjectMapper
        var mapper = new ObjectMapper();
        service = new AnalyzePredictionAccuracyService(predictionRepository, drawRepository, syncUseCase, mapper);
    }

    private SavedPrediction buildPrediction(String userId, LotteryType type) {
        return SavedPrediction.builder()
                .id("pred-1")
                .label("Test")
                .savedAt(LocalDateTime.now())
                .latestDrawDate(LocalDate.of(2024, 1, 1))
                .combosJson("[[1,2,3,4,5,6],[7,8,9,10,11,12]]")
                .lotteryType(type)
                .userId(userId)
                .build();
    }

    private LotteryDraw buildDraw(LocalDate date, List<Integer> numbers) {
        return LotteryDraw.builder()
                .lotteryType(LotteryType.MELATE)
                .drawDate(date)
                .drawNumber(1000)
                .numbers(numbers)
                .build();
    }

    @Test
    @DisplayName("debe lanzar PredictionNotFoundException si no existe la predicción")
    void execute_predictionNotFound_throws() {
        when(predictionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing", "user-1", false))
                .isInstanceOf(PredictionNotFoundException.class);
    }

    @Test
    @DisplayName("debe lanzar UnauthorizedPredictionAccessException si el usuario no es dueño")
    void execute_wrongUser_throws() {
        when(predictionRepository.findById("pred-1"))
                .thenReturn(Optional.of(buildPrediction("owner-id", LotteryType.MELATE)));

        assertThatThrownBy(() -> service.execute("pred-1", "other-user", false))
                .isInstanceOf(UnauthorizedPredictionAccessException.class);
    }

    @Test
    @DisplayName("debe lanzar IllegalStateException si la predicción no tiene lotteryType")
    void execute_noLotteryType_throws() {
        SavedPrediction pred = buildPrediction("user-1", null);
        when(predictionRepository.findById("pred-1")).thenReturn(Optional.of(pred));

        assertThatThrownBy(() -> service.execute("pred-1", "user-1", false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("debe devolver resultado vacío si no hay sorteos posteriores")
    void execute_noDrawsAfterDate_returnsZeroDrawsAnalyzed() {
        when(predictionRepository.findById("pred-1"))
                .thenReturn(Optional.of(buildPrediction("user-1", LotteryType.MELATE)));
        when(drawRepository.findDrawsAfterDate(eq(LotteryType.MELATE), any()))
                .thenReturn(List.of());

        PredictionAccuracyResult result = service.execute("pred-1", "user-1", false);

        assertThat(result.getDrawsAnalyzed()).isZero();
        assertThat(result.getImprovementSuggestions()).isNotEmpty();
    }

    @Test
    @DisplayName("debe calcular correctamente los aciertos por combo")
    void execute_withMatchingDraws_calculatesAccurately() {
        when(predictionRepository.findById("pred-1"))
                .thenReturn(Optional.of(buildPrediction("user-1", LotteryType.MELATE)));
        // Draw shares [1,2,3] with first combo — 3 matches
        when(drawRepository.findDrawsAfterDate(eq(LotteryType.MELATE), any()))
                .thenReturn(List.of(buildDraw(LocalDate.of(2024, 2, 1), List.of(1, 2, 3, 20, 30, 40))));

        PredictionAccuracyResult result = service.execute("pred-1", "user-1", false);

        assertThat(result.getDrawsAnalyzed()).isEqualTo(1);
        assertThat(result.getBestMatchCount()).isEqualTo(3);
        assertThat(result.getComboDetails()).hasSize(2);
        assertThat(result.getComboDetails().get(0).getBestMatchCount()).isEqualTo(3);
        assertThat(result.getComboDetails().get(1).getBestMatchCount()).isZero();
    }

    @Test
    @DisplayName("syncFirst=true debe llamar a SyncHistoricalDataUseCase")
    void execute_syncFirst_callsSync() {
        when(predictionRepository.findById("pred-1"))
                .thenReturn(Optional.of(buildPrediction("user-1", LotteryType.MELATE)));
        when(drawRepository.findDrawsAfterDate(any(), any())).thenReturn(List.of());

        service.execute("pred-1", "user-1", true);

        verify(syncUseCase).syncHistoricalData(LotteryType.MELATE);
    }

    @Test
    @DisplayName("syncFirst=false no debe llamar a SyncHistoricalDataUseCase")
    void execute_noSync_doesNotCallSync() {
        when(predictionRepository.findById("pred-1"))
                .thenReturn(Optional.of(buildPrediction("user-1", LotteryType.MELATE)));
        when(drawRepository.findDrawsAfterDate(any(), any())).thenReturn(List.of());

        service.execute("pred-1", "user-1", false);

        verify(syncUseCase, never()).syncHistoricalData(any());
    }

    @Test
    @DisplayName("un usuario con userId null puede acceder a predicciones globales (userId null)")
    void execute_globalPrediction_accessibleByAnyUser() {
        SavedPrediction global = buildPrediction(null, LotteryType.MELATE);
        when(predictionRepository.findById("pred-1")).thenReturn(Optional.of(global));
        when(drawRepository.findDrawsAfterDate(any(), any())).thenReturn(List.of());

        PredictionAccuracyResult result = service.execute("pred-1", "any-user", false);

        assertThat(result).isNotNull();
    }
}
