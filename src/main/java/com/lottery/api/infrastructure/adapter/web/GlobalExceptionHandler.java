package com.lottery.api.infrastructure.adapter.web;

import com.lottery.api.domain.exception.CsvParsingException;
import com.lottery.api.domain.exception.DrawNotFoundException;
import com.lottery.api.domain.exception.InvalidCredentialsException;
import com.lottery.api.domain.exception.LotteryException;
import com.lottery.api.domain.exception.PredictionNotFoundException;
import com.lottery.api.domain.exception.UnauthorizedPredictionAccessException;
import com.lottery.api.domain.exception.UserAlreadyExistsException;
import com.lottery.api.domain.exception.UserNotFoundException;
import com.lottery.api.infrastructure.adapter.web.dto.response.ApiError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Manejador global de excepciones: convierte las excepciones del dominio en respuestas HTTP estructuradas.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DrawNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(DrawNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PredictionNotFoundException.class)
    public ResponseEntity<ApiError> handlePredictionNotFound(PredictionNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedPredictionAccessException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedPredictionAccessException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LotteryException.class)
    public ResponseEntity<ApiError> handleLotteryException(LotteryException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        log.warn("Estado inválido: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CsvParsingException.class)
    public ResponseEntity<ApiError> handleCsvParsing(CsvParsingException ex) {
        log.error("Error al parsear CSV: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el archivo CSV: " + ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = StreamSupport.stream(ex.getConstraintViolations().spliterator(), false)
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Parámetros inválidos", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + ex.getName());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado: " + ex.getRequestURL());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleThrowable(Throwable ex) {
        log.error("Error crítico ({}): {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message) {
        return buildResponse(status, message, null);
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, List<String> errors) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
