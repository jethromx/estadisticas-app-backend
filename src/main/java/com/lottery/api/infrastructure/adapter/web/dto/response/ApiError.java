package com.lottery.api.infrastructure.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Respuesta de error estructurada")
public class ApiError {

    @Schema(description = "Código de estado HTTP", example = "400")
    private int status;

    @Schema(description = "Mensaje de error principal")
    private String message;

    @Schema(description = "Lista de errores de validación (si aplica)")
    private List<String> errors;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Momento en que ocurrió el error")
    private LocalDateTime timestamp;
}
