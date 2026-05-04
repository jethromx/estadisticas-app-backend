package com.lottery.api.infrastructure.adapter.web;

import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.DeletePredictionUseCase;
import com.lottery.api.domain.port.in.GetPredictionsUseCase;
import com.lottery.api.domain.port.in.SavePredictionUseCase;
import com.lottery.api.infrastructure.adapter.web.dto.request.SavePredictionRequest;
import com.lottery.api.infrastructure.adapter.web.dto.response.SavedPredictionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Predictions", description = "Predicciones de combinaciones guardadas")
@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Validated
public class PredictionController {

    private final SavePredictionUseCase savePredictionUseCase;
    private final GetPredictionsUseCase getPredictionsUseCase;
    private final DeletePredictionUseCase deletePredictionUseCase;

    @Operation(summary = "Listar predicciones guardadas")
    @GetMapping
    public ResponseEntity<List<SavedPredictionResponse>> getAll() {
        List<SavedPredictionResponse> body = getPredictionsUseCase.execute()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Guardar una predicción")
    @PostMapping
    public ResponseEntity<SavedPredictionResponse> save(@RequestBody @Valid SavePredictionRequest request) {
        SavedPrediction saved = savePredictionUseCase.execute(
                request.label(),
                request.latestDrawDate(),
                request.combos().toString()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @Operation(summary = "Eliminar una predicción")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deletePredictionUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    private SavedPredictionResponse toResponse(SavedPrediction p) {
        return new SavedPredictionResponse(
                p.getId(),
                p.getLabel(),
                p.getSavedAt().toString(),
                p.getLatestDrawDate() != null ? p.getLatestDrawDate().toString() : null,
                p.getCombosJson()
        );
    }
}
