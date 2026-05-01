package com.lottery.api.infrastructure.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "Estadísticas agregadas del histórico de un tipo de lotería")
public class StatisticsResponse {

    @Schema(description = "Tipo de lotería", example = "MELATE")
    private String lotteryType;

    @Schema(description = "Total de sorteos en el histórico", example = "4158")
    private long totalDraws;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha del primer sorteo disponible")
    private LocalDate firstDrawDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha del sorteo más reciente")
    private LocalDate lastDrawDate;

    @Schema(description = "Top 10 números más frecuentes")
    private List<NumberFrequencyResponse> mostFrequent;

    @Schema(description = "Top 10 números menos frecuentes")
    private List<NumberFrequencyResponse> leastFrequent;

    @Schema(description = "Distribución completa: número → total de apariciones")
    private Map<Integer, Long> frequencyDistribution;

    @Schema(description = "Promedio de apariciones por número", example = "448.7")
    private double averageFrequency;

    @Schema(description = "Números del rango válido que nunca han sido sorteados")
    private List<Integer> numbersNeverDrawn;
}
