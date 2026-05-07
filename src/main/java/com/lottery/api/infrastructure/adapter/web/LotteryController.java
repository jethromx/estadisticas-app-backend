package com.lottery.api.infrastructure.adapter.web;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.*;
import com.lottery.api.infrastructure.adapter.web.dto.response.*;
import com.lottery.api.infrastructure.adapter.web.mapper.LotteryWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la API de pronósticos de Lotería Nacional.
 *
 * <p>Todos los endpoints reciben el tipo de lotería como parte del path
 * ({@code MELATE, REVANCHA, REVANCHITA, GANA_GATO}).</p>
 */
@RestController
@RequestMapping("/api/v1/lottery")
@RequiredArgsConstructor
@Validated
@Tag(name = "Lotería Nacional", description = "API para análisis estadístico de juegos de pronósticos")
public class LotteryController {

    private final SyncHistoricalDataUseCase       syncUseCase;
    private final GetStatisticsUseCase            statisticsUseCase;
    private final GetNumberFrequenciesUseCase     frequenciesUseCase;
    private final GetHotNumbersUseCase            hotNumbersUseCase;
    private final GetPatternSuggestionsUseCase    patternSuggestionsUseCase;
    private final GetDueNumbersUseCase            dueNumbersUseCase;
    private final GetWindowedFrequenciesUseCase   windowedFrequenciesUseCase;
    private final GetBalanceAnalysisUseCase       balanceAnalysisUseCase;
    private final GetSumDistributionUseCase       sumDistributionUseCase;
    private final GetPairAnalysisUseCase          pairAnalysisUseCase;
    private final GetChiSquareUseCase             chiSquareUseCase;
    private final GetBacktestUseCase              backtestUseCase;
    private final GetBayesianAnalysisUseCase      bayesianAnalysisUseCase;
    private final GetDrawResultsUseCase           drawResultsUseCase;
    private final GetPositionAnalysisUseCase      positionAnalysisUseCase;
    private final GetConsecutiveAnalysisUseCase   consecutiveAnalysisUseCase;
    private final GetRichBacktestUseCase          richBacktestUseCase;
    private final GetTemporalWeightUseCase        temporalWeightUseCase;
    private final GetEntropyAnalysisUseCase       entropyAnalysisUseCase;
    private final GetClusterAnalysisUseCase       clusterAnalysisUseCase;
    private final GetSumStreakUseCase             sumStreakUseCase;
    private final GetEnsemblePredictionUseCase    ensemblePredictionUseCase;
    private final GetCalendarFrequencyUseCase     calendarFrequencyUseCase;
    private final GetNeuralPredictionUseCase      neuralPredictionUseCase;
    private final LotteryWebMapper                webMapper;

    // =========================================================================
    // Sincronización
    // =========================================================================

    @PostMapping("/{type}/sync")
    @Operation(summary = "Sincronizar histórico desde URL",
               description = "Descarga el CSV oficial de Lotería Nacional e inserta los sorteos nuevos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sincronización completada"),
        @ApiResponse(responseCode = "400", description = "Tipo de lotería inválido",
                     content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Error en descarga o parseo del CSV",
                     content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<SyncResultResponse> syncHistoricalData(
            @Parameter(description = "Tipo de lotería", example = "MELATE")
            @PathVariable String type) {
        LotteryType lotteryType = parseLotteryType(type);
        return ResponseEntity.ok(webMapper.toResponse(syncUseCase.syncHistoricalData(lotteryType)));
    }

    @PostMapping("/sync/all")
    @Operation(summary = "Sincronizar todos los tipos",
               description = "Ejecuta la sincronización para MELATE, REVANCHA, REVANCHITA y GANA_GATO")
    public ResponseEntity<List<SyncResultResponse>> syncAll() {
        return ResponseEntity.ok(webMapper.toSyncResponseList(syncUseCase.syncAllHistoricalData()));
    }

    @PostMapping(value = "/{type}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar CSV local",
               description = "Importa un archivo CSV descargado manualmente desde Lotería Nacional")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Importación completada"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o tipo incorrecto")
    })
    public ResponseEntity<SyncResultResponse> importCsv(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file) throws IOException {

        LotteryType lotteryType = parseLotteryType(type);
        var result = syncUseCase.importFromStream(lotteryType, file.getInputStream());
        return ResponseEntity.ok(webMapper.toResponse(result));
    }

    // =========================================================================
    // Estadísticas
    // =========================================================================

    @GetMapping("/{type}/statistics")
    @Operation(summary = "Estadísticas completas",
               description = "Retorna distribución de frecuencias, números calientes/fríos y nunca sorteados")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @PathVariable String type,
            @Parameter(description = "Fecha inicio (yyyy-MM-dd), opcional")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha fin (yyyy-MM-dd), opcional")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LotteryType lotteryType = parseLotteryType(type);
        var stats = (from != null && to != null)
                ? statisticsUseCase.getStatisticsByDateRange(lotteryType, from, to)
                : statisticsUseCase.getStatistics(lotteryType);
        return ResponseEntity.ok(webMapper.toResponse(stats));
    }

    // =========================================================================
    // Frecuencias
    // =========================================================================

    @GetMapping("/{type}/frequencies")
    @Operation(summary = "Frecuencia de todos los números",
               description = "Devuelve cuántas veces salió cada número en el histórico, ordenado por número")
    public ResponseEntity<List<NumberFrequencyResponse>> getFrequencies(
            @PathVariable String type) {
        return ResponseEntity.ok(
                webMapper.toFrequencyResponseList(
                        frequenciesUseCase.getNumberFrequencies(parseLotteryType(type))));
    }

    @GetMapping("/{type}/frequencies/{number}")
    @Operation(summary = "Frecuencia de un número específico")
    public ResponseEntity<NumberFrequencyResponse> getFrequency(
            @PathVariable String type,
            @Parameter(description = "Número a consultar")
            @PathVariable @Min(1) int number) {
        return ResponseEntity.ok(
                webMapper.toResponse(
                        frequenciesUseCase.getNumberFrequency(parseLotteryType(type), number)));
    }

    // =========================================================================
    // Números calientes / fríos
    // =========================================================================

    @GetMapping("/{type}/hot-numbers")
    @Operation(summary = "Números calientes",
               description = "Lista los números con mayor frecuencia histórica")
    public ResponseEntity<List<NumberFrequencyResponse>> getHotNumbers(
            @PathVariable String type,
            @Parameter(description = "Cantidad de resultados (default 10)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(56) int limit) {
        return ResponseEntity.ok(
                webMapper.toFrequencyResponseList(
                        hotNumbersUseCase.getHotNumbers(parseLotteryType(type), limit)));
    }

    @GetMapping("/{type}/cold-numbers")
    @Operation(summary = "Números fríos",
               description = "Lista los números con menor frecuencia histórica (teoría del retraso)")
    public ResponseEntity<List<NumberFrequencyResponse>> getColdNumbers(
            @PathVariable String type,
            @RequestParam(defaultValue = "10") @Min(1) @Max(56) int limit) {
        return ResponseEntity.ok(
                webMapper.toFrequencyResponseList(
                        hotNumbersUseCase.getColdNumbers(parseLotteryType(type), limit)));
    }

    @GetMapping("/{type}/recent-hot-numbers")
    @Operation(summary = "Números calientes recientes",
               description = "Números más frecuentes en los últimos N sorteos")
    public ResponseEntity<List<NumberFrequencyResponse>> getRecentHotNumbers(
            @PathVariable String type,
            @Parameter(description = "Cantidad de sorteos recientes a analizar (default 20)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int recentDraws,
            @RequestParam(defaultValue = "10") @Min(1) @Max(56) int limit) {
        return ResponseEntity.ok(
                webMapper.toFrequencyResponseList(
                        hotNumbersUseCase.getRecentHotNumbers(parseLotteryType(type), recentDraws, limit)));
    }

    // =========================================================================
    // Sugerencias de patrones
    // =========================================================================

    @GetMapping("/{type}/suggestions")
    @Operation(summary = "Sugerencias estadísticas",
               description = "Genera sugerencias de apuesta usando HOT_NUMBERS, COLD_NUMBERS, BALANCED y STATISTICAL_RANDOM")
    public ResponseEntity<List<PatternSuggestionResponse>> getSuggestions(
            @PathVariable String type) {
        return ResponseEntity.ok(
                webMapper.toPatternResponseList(
                        patternSuggestionsUseCase.getPatternSuggestions(parseLotteryType(type))));
    }

    @GetMapping("/{type}/suggestions/{methodology}")
    @Operation(summary = "Sugerencia por metodología",
               description = "HOT_NUMBERS | COLD_NUMBERS | BALANCED | STATISTICAL_RANDOM")
    public ResponseEntity<PatternSuggestionResponse> getSuggestionByMethodology(
            @PathVariable String type,
            @Parameter(description = "Metodología estadística")
            @PathVariable String methodology) {
        return ResponseEntity.ok(
                webMapper.toResponse(
                        patternSuggestionsUseCase.getSuggestionByMethodology(
                                parseLotteryType(type), methodology)));
    }

    // =========================================================================
    // Números pendientes
    // =========================================================================

    @GetMapping("/{type}/due-numbers")
    @Operation(summary = "Números pendientes de salir",
               description = "Top N números cuyo intervalo desde última aparición supera su promedio histórico. " +
                             "dueScore = sorteosSinSalir / intervaloPromedio. Mayor score = más pendiente.")
    public ResponseEntity<List<DueNumberResponse>> getDueNumbers(
            @PathVariable String type,
            @Parameter(description = "Cantidad de resultados (default 10)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(56) int limit) {
        return ResponseEntity.ok(
                webMapper.toDueNumberResponseList(
                        dueNumbersUseCase.getDueNumbers(parseLotteryType(type), limit)));
    }

    // =========================================================================
    // Ventana temporal
    // =========================================================================

    @GetMapping("/{type}/windowed-frequencies")
    @Operation(summary = "Frecuencias en ventana temporal",
               description = "Frecuencia de cada número en los últimos N sorteos, con indicador de tendencia vs histórico. " +
                             "trend > 0 = aparece más que su promedio histórico; < 0 = menos.")
    public ResponseEntity<List<WindowedFrequencyResponse>> getWindowedFrequencies(
            @PathVariable String type,
            @Parameter(description = "Tamaño de la ventana en sorteos (default 100)")
            @RequestParam(defaultValue = "100") @Min(10) @Max(2000) int window) {
        return ResponseEntity.ok(
                webMapper.toWindowedFrequencyResponseList(
                        windowedFrequenciesUseCase.getWindowedFrequencies(parseLotteryType(type), window)));
    }

    // =========================================================================
    // Balance par/impar y alto/bajo
    // =========================================================================

    @GetMapping("/{type}/balance-analysis")
    @Operation(summary = "Análisis de balance par/impar y alto/bajo",
               description = "Distribución histórica del balance de números pares/impares y altos/bajos por sorteo, " +
                             "con la combinación óptima observada.")
    public ResponseEntity<BalanceAnalysisResponse> getBalanceAnalysis(
            @PathVariable String type) {
        return ResponseEntity.ok(
                webMapper.toResponse(
                        balanceAnalysisUseCase.getBalanceAnalysis(parseLotteryType(type))));
    }

    // =========================================================================
    // Distribución de suma
    // =========================================================================

    @GetMapping("/{type}/sum-distribution")
    @Operation(summary = "Distribución de la suma de números sorteados",
               description = "Histograma de la suma de los números de cada sorteo, con media, desviación estándar " +
                             "y rango óptimo recomendado para seleccionar combinaciones.")
    public ResponseEntity<SumDistributionResponse> getSumDistribution(
            @PathVariable String type) {
        return ResponseEntity.ok(
                webMapper.toResponse(
                        sumDistributionUseCase.getSumDistribution(parseLotteryType(type))));
    }

    // =========================================================================
    // Análisis de pares (co-ocurrencia)
    // =========================================================================

    @GetMapping("/{type}/pair-analysis")
    @Operation(summary = "Análisis de co-ocurrencia de pares",
               description = "Top-N pares de números que aparecen juntos con más frecuencia en el mismo sorteo.")
    public ResponseEntity<List<NumberPairResponse>> getPairAnalysis(
            @PathVariable String type,
            @Parameter(description = "Cantidad de pares a retornar (default 20)")
            @RequestParam(defaultValue = "20") @Min(5) @Max(200) int limit) {
        return ResponseEntity.ok(
                webMapper.toPairResponseList(
                        pairAnalysisUseCase.getPairAnalysis(parseLotteryType(type), limit)));
    }

    // =========================================================================
    // Chi-cuadrado (uniformidad de frecuencias)
    // =========================================================================

    @GetMapping("/{type}/chi-square")
    @Operation(summary = "Test chi-cuadrado de uniformidad",
               description = "Prueba si la distribución de frecuencias de los números difiere significativamente " +
                             "de una distribución uniforme. p < 0.05 indica desviación estadísticamente significativa.")
    public ResponseEntity<ChiSquareResponse> getChiSquare(@PathVariable String type) {
        return ResponseEntity.ok(
                webMapper.toResponse(chiSquareUseCase.getChiSquare(parseLotteryType(type))));
    }

    // =========================================================================
    // Backtesting
    // =========================================================================

    @GetMapping("/{type}/backtest")
    @Operation(summary = "Backtesting de la estrategia de números calientes",
               description = "Evalúa cuántos números acertarías históricamente si siempre apostaras a los " +
                             "topK números más frecuentes. Incluye comparativa con una selección aleatoria.")
    public ResponseEntity<BacktestResponse> getBacktest(
            @PathVariable String type,
            @Parameter(description = "Cantidad de números a predecir (default = números por sorteo del juego)")
            @RequestParam(required = false) @Min(1) @Max(56) Integer topK,
            @Parameter(description = "Sorteos recientes a usar como conjunto de prueba (default 100)")
            @RequestParam(defaultValue = "100") @Min(10) @Max(2000) int testDraws) {
        LotteryType lotteryType = parseLotteryType(type);
        int k = topK != null ? topK : lotteryType.getNumbersCount();
        return ResponseEntity.ok(webMapper.toResponse(backtestUseCase.getBacktest(lotteryType, k, testDraws)));
    }

    // =========================================================================
    // Análisis bayesiano
    // =========================================================================

    @GetMapping("/{type}/bayesian-analysis")
    @Operation(summary = "Análisis bayesiano de números",
               description = "Combina la frecuencia histórica (prior) con la frecuencia reciente (likelihood) " +
                             "para obtener una probabilidad posterior Beta-Binomial por número. " +
                             "lift > 0 = el número aparece más de lo esperado en la ventana reciente.")
    public ResponseEntity<List<BayesianNumberResponse>> getBayesianAnalysis(
            @PathVariable String type,
            @Parameter(description = "Tamaño de la ventana reciente en sorteos (default 50)")
            @RequestParam(defaultValue = "50") @Min(10) @Max(500) int recentWindow) {
        return ResponseEntity.ok(
                webMapper.toBayesianResponseList(
                        bayesianAnalysisUseCase.getBayesianAnalysis(parseLotteryType(type), recentWindow)));
    }

    // =========================================================================
    // Sorteos históricos
    // =========================================================================

    @GetMapping("/{type}/draws")
    @Operation(summary = "Histórico de sorteos",
               description = "Sin parámetros de página devuelve todos los sorteos (hasta `limit`). " +
                             "Con `page` y `size` devuelve una página con metadatos de paginación.")
    public ResponseEntity<?> getDraws(
            @PathVariable String type,
            @Parameter(description = "Máximo de sorteos en modo sin paginación (default 5000)")
            @RequestParam(defaultValue = "5000") @Min(1) @Max(10000) int limit,
            @Parameter(description = "Número de página base-0 (activa paginación)")
            @RequestParam(required = false) @Min(0) Integer page,
            @Parameter(description = "Tamaño de página (default 100, máx 500)")
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int size) {

        LotteryType lotteryType = parseLotteryType(type);

        if (page != null) {
            Page<DrawResultResponse> result = drawResultsUseCase
                    .executePaged(lotteryType, PageRequest.of(page, size, Sort.by("drawNumber").descending()))
                    .map(d -> new DrawResultResponse(d.getDrawNumber(), d.getDrawDate(), d.getNumbers()));
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.ok(
                drawResultsUseCase.execute(lotteryType, limit)
                        .stream()
                        .map(d -> new DrawResultResponse(d.getDrawNumber(), d.getDrawDate(), d.getNumbers()))
                        .toList());
    }

    // =========================================================================
    // Análisis de posición ordenada
    // =========================================================================

    @GetMapping("/{type}/position-analysis")
    @Operation(summary = "Análisis de brechas por posición ordenada",
               description = "Para cada posición (1ª, 2ª… Nª número más pequeño del sorteo) muestra " +
                             "la distribución histórica: media, desviación, percentiles y rango recomendado.")
    public ResponseEntity<PositionAnalysisResponse> getPositionAnalysis(@PathVariable String type) {
        return ResponseEntity.ok(webMapper.toResponse(positionAnalysisUseCase.getPositionAnalysis(parseLotteryType(type))));
    }

    // =========================================================================
    // Análisis de números consecutivos
    // =========================================================================

    @GetMapping("/{type}/consecutive-analysis")
    @Operation(summary = "Análisis de números consecutivos",
               description = "Frecuencia de sorteos con pares consecutivos (n, n+1), distribución " +
                             "por cantidad de pares por sorteo y los pares más comunes.")
    public ResponseEntity<ConsecutiveAnalysisResponse> getConsecutiveAnalysis(
            @PathVariable String type,
            @RequestParam(defaultValue = "20") @Min(5) @Max(100) int topPairs) {
        return ResponseEntity.ok(webMapper.toResponse(
                consecutiveAnalysisUseCase.getConsecutiveAnalysis(parseLotteryType(type), topPairs)));
    }

    // =========================================================================
    // Backtest multi-estrategia (rico)
    // =========================================================================

    @GetMapping("/{type}/rich-backtest")
    @Operation(summary = "Backtest multi-estrategia",
               description = "Compara simultáneamente las estrategias HOT_NUMBERS, COLD_NUMBERS, " +
                             "DUE_NUMBERS y BALANCED en los sorteos de prueba. Incluye hit-rate, " +
                             "promedio de aciertos, comparativa vs azar y rolling hit-rate por ventana.")
    public ResponseEntity<RichBacktestResponse> getRichBacktest(
            @PathVariable String type,
            @RequestParam(required = false) @Min(1) @Max(56) Integer topK,
            @RequestParam(defaultValue = "100") @Min(10) @Max(2000) int testDraws) {
        LotteryType lotteryType = parseLotteryType(type);
        int k = topK != null ? topK : lotteryType.getNumbersCount();
        return ResponseEntity.ok(webMapper.toResponse(richBacktestUseCase.getRichBacktest(lotteryType, k, testDraws)));
    }

    // =========================================================================
    // Pesos temporales (decay exponencial)
    // =========================================================================

    @GetMapping("/{type}/temporal-weights")
    @Operation(summary = "Calibración de pesos por ventana temporal",
               description = "Calcula el score ponderado exponencialmente de cada número con factores " +
                             "de decay 0.99, 0.97, 0.95 y 0.90. Permite comparar ranking actual vs " +
                             "ranking histórico puro.")
    public ResponseEntity<TemporalWeightResponse> getTemporalWeights(@PathVariable String type) {
        return ResponseEntity.ok(webMapper.toResponse(temporalWeightUseCase.getTemporalWeights(parseLotteryType(type))));
    }

    // =========================================================================
    // Análisis de entropía
    // =========================================================================

    @GetMapping("/{type}/entropy-analysis")
    @Operation(summary = "Análisis de entropía de Shannon",
               description = "Mide si la distribución histórica es estadísticamente uniforme (aleatoria). " +
                             "entropyRatio cercano a 1.0 = muy uniforme, sin sesgo detectable. " +
                             "Incluye entropía por ventana de sorteos para ver tendencias temporales.")
    public ResponseEntity<EntropyAnalysisResponse> getEntropyAnalysis(
            @PathVariable String type,
            @RequestParam(defaultValue = "50") @Min(20) @Max(500) int windowSize) {
        return ResponseEntity.ok(webMapper.toResponse(
                entropyAnalysisUseCase.getEntropyAnalysis(parseLotteryType(type), windowSize)));
    }

    // =========================================================================
    // Clustering de combinaciones ganadoras
    // =========================================================================

    @GetMapping("/{type}/cluster-analysis")
    @Operation(summary = "Clusters de combinaciones ganadoras",
               description = "Agrupa los sorteos históricos en k clusters usando K-Means sobre " +
                             "(suma, cantidad de impares, amplitud). Cada cluster muestra sus números " +
                             "más frecuentes y qué porcentaje del histórico representa.")
    public ResponseEntity<ClusterAnalysisResponse> getClusterAnalysis(
            @PathVariable String type,
            @RequestParam(defaultValue = "5") @Min(2) @Max(10) int k) {
        return ResponseEntity.ok(webMapper.toResponse(
                clusterAnalysisUseCase.getClusterAnalysis(parseLotteryType(type), k)));
    }

    // =========================================================================
    // Racha de sumas (streak analysis)
    // =========================================================================

    @GetMapping("/{type}/sum-streak")
    @Operation(summary = "Análisis de rachas de sumas",
               description = "Detecta rachas consecutivas de sorteos con suma total por encima o por " +
                             "debajo de la media histórica. Muestra la racha actual, las más largas y " +
                             "el detalle de los últimos sorteos.")
    public ResponseEntity<SumStreakResponse> getSumStreak(
            @PathVariable String type,
            @RequestParam(defaultValue = "30") @Min(10) @Max(200) int recentDraws) {
        return ResponseEntity.ok(webMapper.toResponse(
                sumStreakUseCase.getSumStreak(parseLotteryType(type), recentDraws)));
    }

    // =========================================================================
    // Predicción ensemble (ML ponderado)
    // =========================================================================

    @GetMapping("/{type}/ensemble-prediction")
    @Operation(summary = "Predicción ensemble estadístico (ML)",
               description = "Combina cuatro señales (frecuencia histórica, recencia exponencial, " +
                             "due-score y co-ocurrencia de pares) en un modelo lineal cuyos pesos " +
                             "se optimizan por grid-search validado en los últimos sorteos. " +
                             "Devuelve los números más probables y 5 combinaciones sugeridas.")
    public ResponseEntity<EnsemblePredictionResponse> getEnsemblePrediction(
            @PathVariable String type,
            @RequestParam(defaultValue = "30") @Min(10) @Max(200) int validationDraws) {
        return ResponseEntity.ok(webMapper.toResponse(
                ensemblePredictionUseCase.getEnsemblePrediction(parseLotteryType(type), validationDraws)));
    }

    // Predicción red neuronal MLP (Java puro)
    // =========================================================================

    @GetMapping("/{type}/neural-prediction")
    @Operation(summary = "Predicción MLP — Red neuronal feedforward",
               description = "Entrena una red neuronal feedforward (MLP 8→16→8→1) en Java puro " +
                             "sobre el histórico del juego. Para cada número extrae 8 features " +
                             "(frecuencias recientes, due-score, tendencia, momentum) y predice la " +
                             "probabilidad de aparecer en el próximo sorteo. Devuelve scores por " +
                             "número y 3 combinaciones sugeridas. El resultado se cachea 6 horas.")
    public ResponseEntity<NeuralPredictionResponse> getNeuralPrediction(
            @PathVariable String type) {
        return ResponseEntity.ok(webMapper.toResponse(
                neuralPredictionUseCase.getNeuralPrediction(parseLotteryType(type))));
    }

    // =========================================================================
    // Frecuencia por día de la semana / mes
    // =========================================================================

    @GetMapping("/{type}/calendar-frequency")
    @Operation(summary = "Frecuencia por día de semana y mes",
               description = "Muestra qué números aparecen más en cada día de la semana y en cada mes " +
                             "del año, útil para detectar sesgos calendariales en el histórico.")
    public ResponseEntity<CalendarFrequencyResponse> getCalendarFrequency(@PathVariable String type) {
        return ResponseEntity.ok(webMapper.toResponse(
                calendarFrequencyUseCase.getCalendarFrequency(parseLotteryType(type))));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private LotteryType parseLotteryType(String type) {
        try {
            return LotteryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new com.lottery.api.domain.exception.LotteryException(
                    "Tipo de lotería inválido: '" + type + "'. Valores válidos: MELATE, REVANCHA, REVANCHITA, GANA_GATO");
        }
    }
}
