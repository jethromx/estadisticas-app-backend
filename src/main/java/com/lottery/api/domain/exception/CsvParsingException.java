package com.lottery.api.domain.exception;

/**
 * Se lanza cuando falla el parseo o la descarga del archivo CSV histórico.
 */
public class CsvParsingException extends LotteryException {

    public CsvParsingException(String message) {
        super(message);
    }

    public CsvParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
