package com.lottery.api.infrastructure.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Resultado de la sincronización del histórico CSV")
public class SyncResultResponse {

    @Schema(description = "Tipo de lotería sincronizado", example = "MELATE")
    private String lotteryType;

    @Schema(description = "Total de registros encontrados en el CSV", example = "4158")
    private int totalRecords;

    @Schema(description = "Registros nuevos insertados", example = "10")
    private int newRecords;

    @Schema(description = "Registros omitidos por ya existir en BD", example = "4148")
    private int skippedRecords;

    @Schema(description = "Estado: SUCCESS o ERROR", example = "SUCCESS")
    private String status;

    @Schema(description = "Mensaje descriptivo del resultado")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora de la sincronización")
    private LocalDateTime syncedAt;
}
