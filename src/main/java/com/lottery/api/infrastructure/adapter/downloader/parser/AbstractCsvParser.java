package com.lottery.api.infrastructure.adapter.downloader.parser;

import com.lottery.api.domain.exception.CsvParsingException;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase base con lógica de parseo CSV compartida entre todos los tipos de juego.
 *
 * <p>El formato de fecha esperado en los CSVs de Lotería Nacional es {@code dd/MM/yyyy}.
 * El separador de columnas es la coma.</p>
 */
@Slf4j
public abstract class AbstractCsvParser implements LotteryCsvParser {

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final Charset CSV_CHARSET = StandardCharsets.UTF_8;

    @Override
    public List<LotteryDraw> parse(InputStream inputStream, LotteryType lotteryType) {
        List<LotteryDraw> draws = new ArrayList<>();
        try (var reader = new CSVReaderBuilder(new InputStreamReader(inputStream, CSV_CHARSET))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .build()) {

            String[] headers = reader.readNext(); // primera fila = encabezados
            if (headers == null) {
                log.warn("CSV vacío para tipo {}", lotteryType);
                return draws;
            }

            List<String> headerList = Arrays.stream(headers)
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .toList();

            String[] row;
            int lineNum = 1;
            while ((row = reader.readNext()) != null) {
                lineNum++;
                if (isBlankRow(row)) continue;
                try {
                    LotteryDraw draw = parseRow(row, headerList, lotteryType);
                    if (draw != null) draws.add(draw);
                } catch (Exception e) {
                    log.warn("Error al parsear fila {} ({}): {}", lineNum, Arrays.toString(row), e.getMessage());
                }
            }
            log.info("Parseados {} sorteos de {}", draws.size(), lotteryType);
        } catch (CsvParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvParsingException("Error al parsear CSV de " + lotteryType + ": " + e.getMessage(), e);
        }
        return draws;
    }

    /**
     * Parsea una fila del CSV a un {@link LotteryDraw}.
     * Subclases implementan la lógica específica de columnas de cada juego.
     *
     * @param row        valores de la fila
     * @param headers    encabezados normalizados a mayúsculas
     * @param lotteryType tipo de juego
     * @return sorteo parseado, o {@code null} para omitir la fila
     */
    protected abstract LotteryDraw parseRow(String[] row, List<String> headers, LotteryType lotteryType);

    // -------- utilidades -------------------------------------------------

    protected int indexOf(List<String> headers, String name) {
        int idx = headers.indexOf(name.toUpperCase());
        if (idx < 0) throw new CsvParsingException("Columna no encontrada en CSV: " + name);
        return idx;
    }

    protected Integer parseInt(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return null;
        String val = row[idx].trim();
        if (val.isEmpty()) return null;
        return Integer.parseInt(val);
    }

    protected BigDecimal parseBigDecimal(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return null;
        String val = row[idx].trim().replace(",", "");
        if (val.isEmpty()) return null;
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected LocalDate parseDate(String[] row, int idx) {
        String val = row[idx].trim();
        return LocalDate.parse(val, DATE_FORMAT);
    }

    private boolean isBlankRow(String[] row) {
        return row == null || Arrays.stream(row).allMatch(s -> s == null || s.trim().isEmpty());
    }
}
