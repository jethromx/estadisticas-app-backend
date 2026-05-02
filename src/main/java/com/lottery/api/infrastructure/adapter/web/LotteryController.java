package com.lottery.api.infrastructure.adapter.web;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.*;
import com.lottery.api.infrastructure.adapter.web.dto.response.DueNumberResponse;
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

    private final SyncHistoricalDataUseCase    syncUseCase;
    private final GetStatisticsUseCase         statisticsUseCase;
    private final GetNumberFrequenciesUseCase  frequenciesUseCase;
    private final GetHotNumbersUseCase         hotNumbersUseCase;
    private final GetPatternSuggestionsUseCase patternSuggestionsUseCase;
    private final GetDueNumbersUseCase         dueNumbersUseCase;
    private final LotteryWebMapper             webMapper;

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
