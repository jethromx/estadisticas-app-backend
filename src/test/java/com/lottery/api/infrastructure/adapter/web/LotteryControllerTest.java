package com.lottery.api.infrastructure.adapter.web;

import com.lottery.api.domain.exception.LotteryException;
import com.lottery.api.domain.model.*;
import com.lottery.api.domain.port.in.*;
import com.lottery.api.infrastructure.adapter.web.mapper.LotteryWebMapper;
import com.lottery.api.infrastructure.adapter.web.dto.response.*;
import com.lottery.api.infrastructure.config.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LotteryController.class)
@DisplayName("LotteryController — Tests Web")
@WithMockUser
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-minimum-32-characters-long",
    "app.jwt.expiry-ms=86400000"
})
class LotteryControllerTest {

    @Autowired private MockMvc mockMvc;

    // LotteryController dependencies
    @MockBean private SyncHistoricalDataUseCase syncUseCase;
    @MockBean private GetStatisticsUseCase statisticsUseCase;
    @MockBean private GetNumberFrequenciesUseCase frequenciesUseCase;
    @MockBean private GetHotNumbersUseCase hotNumbersUseCase;
    @MockBean private GetPatternSuggestionsUseCase patternSuggestionsUseCase;
    @MockBean private GetDueNumbersUseCase dueNumbersUseCase;
    @MockBean private GetWindowedFrequenciesUseCase windowedFrequenciesUseCase;
    @MockBean private GetBalanceAnalysisUseCase balanceAnalysisUseCase;
    @MockBean private GetSumDistributionUseCase sumDistributionUseCase;
    @MockBean private GetPairAnalysisUseCase pairAnalysisUseCase;
    @MockBean private GetChiSquareUseCase chiSquareUseCase;
    @MockBean private GetBacktestUseCase backtestUseCase;
    @MockBean private GetBayesianAnalysisUseCase bayesianAnalysisUseCase;
    @MockBean private GetDrawResultsUseCase drawResultsUseCase;
    @MockBean private GetPositionAnalysisUseCase positionAnalysisUseCase;
    @MockBean private GetConsecutiveAnalysisUseCase consecutiveAnalysisUseCase;
    @MockBean private GetRichBacktestUseCase richBacktestUseCase;
    @MockBean private GetTemporalWeightUseCase temporalWeightUseCase;
    @MockBean private GetEntropyAnalysisUseCase entropyAnalysisUseCase;
    @MockBean private GetClusterAnalysisUseCase clusterAnalysisUseCase;
    @MockBean private GetSumStreakUseCase sumStreakUseCase;
    @MockBean private GetEnsemblePredictionUseCase ensemblePredictionUseCase;
    @MockBean private GetCalendarFrequencyUseCase calendarFrequencyUseCase;
    @MockBean private LotteryWebMapper webMapper;
    // Security: mock dependencies of filters, not the filters themselves
    // (mocking filters stops chain.doFilter() from being called)
    @MockBean private JwtService jwtService;
    @MockBean private com.lottery.api.domain.port.out.UserActivityRepositoryPort userActivityRepositoryPort;
    @MockBean private com.lottery.api.infrastructure.config.security.UserDetailsServiceAdapter userDetailsServiceAdapter;

    // ---- helpers ----
    private SyncResultResponse syncResponse(String status) {
        return SyncResultResponse.builder().lotteryType("MELATE").status(status)
                .totalRecords(10).newRecords(2).skippedRecords(8)
                .syncedAt(LocalDateTime.now()).message("ok").build();
    }

    private StatisticsResponse statsResponse() {
        return StatisticsResponse.builder()
                .lotteryType("MELATE").totalDraws(4158L)
                .firstDrawDate(LocalDate.of(1990, 1, 1))
                .lastDrawDate(LocalDate.of(2026, 1, 7))
                .mostFrequent(List.of()).leastFrequent(List.of())
                .frequencyDistribution(Map.of()).averageFrequency(70.0)
                .numbersNeverDrawn(List.of()).build();
    }

    @Test
    @DisplayName("POST /sync devuelve 200 con resultado de sincronización")
    void syncHistoricalData_returns200() throws Exception {
        when(syncUseCase.syncHistoricalData(LotteryType.MELATE))
                .thenReturn(SyncResult.builder().lotteryType(LotteryType.MELATE).status("SUCCESS").build());
        when(webMapper.toResponse(any(SyncResult.class))).thenReturn(syncResponse("SUCCESS"));

        mockMvc.perform(post("/api/v1/lottery/MELATE/sync").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.lotteryType").value("MELATE"));
    }

    @Test
    @DisplayName("POST /sync con tipo inválido devuelve 400")
    void syncHistoricalData_invalidType_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/lottery/INVALID/sync").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /sync/all devuelve lista de resultados")
    void syncAll_returns200WithList() throws Exception {
        when(syncUseCase.syncAllHistoricalData()).thenReturn(List.of());
        when(webMapper.toSyncResponseList(any())).thenReturn(List.of(syncResponse("SUCCESS")));

        mockMvc.perform(post("/api/v1/lottery/sync/all").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /statistics devuelve estadísticas completas")
    void getStatistics_returns200() throws Exception {
        when(statisticsUseCase.getStatistics(LotteryType.MELATE))
                .thenReturn(LotteryStatistics.builder().lotteryType(LotteryType.MELATE).totalDraws(4158L).build());
        when(webMapper.toResponse(any(LotteryStatistics.class))).thenReturn(statsResponse());

        mockMvc.perform(get("/api/v1/lottery/MELATE/statistics")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDraws").value(4158));
    }

    @Test
    @DisplayName("GET /statistics con rango de fechas usa el método correspondiente")
    void getStatistics_withDateRange_usesDateRangeMethod() throws Exception {
        when(statisticsUseCase.getStatisticsByDateRange(eq(LotteryType.MELATE), any(), any()))
                .thenReturn(LotteryStatistics.builder().lotteryType(LotteryType.MELATE).totalDraws(100L).build());
        when(webMapper.toResponse(any(LotteryStatistics.class))).thenReturn(statsResponse());

        mockMvc.perform(get("/api/v1/lottery/MELATE/statistics")
                .param("from", "2025-01-01")
                .param("to", "2025-12-31"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /frequencies devuelve lista de frecuencias")
    void getFrequencies_returns200() throws Exception {
        when(frequenciesUseCase.getNumberFrequencies(LotteryType.REVANCHA))
                .thenReturn(List.of(NumberFrequency.builder().number(10).frequency(100L).build()));
        when(webMapper.toFrequencyResponseList(any())).thenReturn(
                List.of(NumberFrequencyResponse.builder().number(10).frequency(100L).build()));

        mockMvc.perform(get("/api/v1/lottery/REVANCHA/frequencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(10));
    }

    @Test
    @DisplayName("GET /hot-numbers devuelve números calientes")
    void getHotNumbers_returns200() throws Exception {
        when(hotNumbersUseCase.getHotNumbers(LotteryType.MELATE, 5)).thenReturn(List.of());
        when(webMapper.toFrequencyResponseList(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lottery/MELATE/hot-numbers").param("limit", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /suggestions devuelve 4 sugerencias")
    void getSuggestions_returns200() throws Exception {
        when(patternSuggestionsUseCase.getPatternSuggestions(LotteryType.MELATE)).thenReturn(List.of());
        when(webMapper.toPatternResponseList(any())).thenReturn(
                List.of(PatternSuggestionResponse.builder().methodology("HOT_NUMBERS").build()));

        mockMvc.perform(get("/api/v1/lottery/MELATE/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].methodology").value("HOT_NUMBERS"));
    }

    @Test
    @DisplayName("GET /suggestions/{methodology} devuelve sugerencia específica")
    void getSuggestionByMethodology_returns200() throws Exception {
        when(patternSuggestionsUseCase.getSuggestionByMethodology(LotteryType.MELATE, "BALANCED"))
                .thenReturn(PatternSuggestion.builder().methodology("BALANCED").build());
        when(webMapper.toResponse(any(PatternSuggestion.class)))
                .thenReturn(PatternSuggestionResponse.builder().methodology("BALANCED").build());

        mockMvc.perform(get("/api/v1/lottery/MELATE/suggestions/BALANCED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.methodology").value("BALANCED"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler: LotteryException → 400")
    void globalHandler_lotteryException_returns400() throws Exception {
        when(statisticsUseCase.getStatistics(any()))
                .thenThrow(new LotteryException("Error de dominio"));

        mockMvc.perform(get("/api/v1/lottery/MELATE/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de dominio"));
    }
}
