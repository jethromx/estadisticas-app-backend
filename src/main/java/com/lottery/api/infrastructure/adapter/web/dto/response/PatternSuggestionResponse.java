package com.lottery.api.infrastructure.adapter.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Sugerencia de apuesta basada en análisis estadístico")
public class PatternSuggestionResponse {

    @Schema(description = "Tipo de lotería", example = "MELATE")
    private String lotteryType;

    @Schema(description = "Números sugeridos como apuesta principal", example = "[7, 15, 23, 34, 45, 50]")
    private List<Integer> suggestedNumbers;

    @Schema(description = "Número adicional sugerido (solo Melate)", example = "28")
    private Integer suggestedAdditional;

    @Schema(description = "Metodología aplicada", example = "HOT_NUMBERS",
            allowableValues = {"HOT_NUMBERS", "COLD_NUMBERS", "BALANCED", "STATISTICAL_RANDOM"})
    private String methodology;

    @Schema(description = "Descripción de la metodología")
    private String description;

    @Schema(description = "Puntuación orientativa de confianza estadística (0.0–1.0)", example = "0.65")
    private double confidenceScore;
}
