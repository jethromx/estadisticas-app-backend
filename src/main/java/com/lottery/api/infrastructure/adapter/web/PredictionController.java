package com.lottery.api.infrastructure.adapter.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.api.domain.model.ComboMatchDetail;
import com.lottery.api.domain.model.PredictionAccuracyResult;
import com.lottery.api.domain.model.SavePredictionCommand;
import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.AnalyzePredictionAccuracyUseCase;
import com.lottery.api.domain.port.in.DeletePredictionUseCase;
import com.lottery.api.domain.port.in.GetPredictionsUseCase;
import com.lottery.api.domain.port.in.SavePredictionUseCase;
import com.lottery.api.infrastructure.adapter.web.dto.request.SavePredictionRequest;
import com.lottery.api.infrastructure.adapter.web.dto.response.ComboMatchDetailResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.PredictionAccuracyResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SavedPredictionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "Predictions", description = "Predicciones de combinaciones guardadas")
@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class PredictionController {

    private final SavePredictionUseCase savePredictionUseCase;
    private final GetPredictionsUseCase getPredictionsUseCase;
    private final DeletePredictionUseCase deletePredictionUseCase;
    private final AnalyzePredictionAccuracyUseCase analyzePredictionAccuracyUseCase;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Listar predicciones del usuario autenticado")
    @GetMapping
    public ResponseEntity<List<SavedPredictionResponse>> getAll(Authentication auth) {
        String userId = extractUserId(auth);
        List<SavedPredictionResponse> body = getPredictionsUseCase.execute(userId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Guardar una predicción")
    @PostMapping
    public ResponseEntity<SavedPredictionResponse> save(
            @RequestBody @Valid SavePredictionRequest request,
            Authentication auth) {
        String userId = extractUserId(auth);
        String generationParamsJson = request.generationParams() != null
                ? request.generationParams().toString()
                : null;
        SavePredictionCommand command = new SavePredictionCommand(
                request.label(),
                request.latestDrawDate(),
                request.combos().toString(),
                request.lotteryType(),
                generationParamsJson,
                userId
        );
        SavedPrediction saved = savePredictionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @Operation(summary = "Analizar precisión de una predicción contra sorteos posteriores")
    @PostMapping("/{id}/analyze")
    public ResponseEntity<PredictionAccuracyResponse> analyze(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean syncFirst,
            Authentication auth) {
        String userId = extractUserId(auth);
        PredictionAccuracyResult result = analyzePredictionAccuracyUseCase.execute(id, userId, syncFirst);
        return ResponseEntity.ok(toAccuracyResponse(result));
    }

    @Operation(summary = "Eliminar una predicción")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication auth) {
        String userId = extractUserId(auth);
        deletePredictionUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return principal.toString();
    }

    private SavedPredictionResponse toResponse(SavedPrediction p) {
        JsonNode combosNode = parseJson(p.getCombosJson(), p.getId());
        JsonNode paramsNode = p.getGenerationParamsJson() != null
                ? parseJson(p.getGenerationParamsJson(), p.getId())
                : null;
        return new SavedPredictionResponse(
                p.getId(),
                p.getLabel(),
                p.getSavedAt().toString(),
                p.getLatestDrawDate() != null ? p.getLatestDrawDate().toString() : null,
                combosNode,
                p.getLotteryType() != null ? p.getLotteryType().name() : null,
                paramsNode,
                p.getUserId()
        );
    }

    private PredictionAccuracyResponse toAccuracyResponse(PredictionAccuracyResult r) {
        List<ComboMatchDetailResponse> details = r.getComboDetails().stream()
                .map(this::toComboDetailResponse)
                .toList();
        return new PredictionAccuracyResponse(
                r.getPredictionId(),
                r.getPredictionLabel(),
                r.getLotteryType() != null ? r.getLotteryType().name() : null,
                r.getDrawsAnalyzed(),
                r.getBestMatchCount(),
                r.getWorstMatchCount(),
                r.getAverageMatchCount(),
                details,
                r.getImprovementSuggestions()
        );
    }

    private ComboMatchDetailResponse toComboDetailResponse(ComboMatchDetail d) {
        java.util.Map<String, Integer> matchesStr = new java.util.LinkedHashMap<>();
        d.getMatchesPerDraw().forEach((date, count) -> matchesStr.put(date.toString(), count));
        return new ComboMatchDetailResponse(
                d.getComboNumbers(),
                d.getBestMatchCount(),
                d.getAverageMatchCount(),
                matchesStr
        );
    }

    private JsonNode parseJson(String json, String predictionId) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("Error parsing JSON for prediction {}: {}", predictionId, e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
}
