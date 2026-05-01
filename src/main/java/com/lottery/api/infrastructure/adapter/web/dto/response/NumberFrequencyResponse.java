package com.lottery.api.infrastructure.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Frecuencia de aparición de un número en el histórico")
public class NumberFrequencyResponse {

    @Schema(description = "Número sorteado", example = "23")
    private Integer number;

    @Schema(description = "Cantidad de veces que apareció", example = "312")
    private long frequency;

    @Schema(description = "Porcentaje respecto al total de apariciones", example = "5.83")
    private double percentage;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha del último sorteo en que apareció")
    private LocalDate lastDrawnDate;

    @Schema(description = "Número de concurso en que apareció por última vez", example = "4158")
    private Integer lastDrawNumber;
}
