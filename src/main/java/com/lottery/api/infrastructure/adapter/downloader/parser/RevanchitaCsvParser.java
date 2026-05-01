package com.lottery.api.infrastructure.adapter.downloader.parser;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Parser del CSV de Revanchita.
 *
 * <p>Formato esperado: {@code NPRODUCTO,CONCURSO,F1,F2,F3,F4,F5,F6,BOLSA,FECHA}</p>
 */
@Component
public class RevanchitaCsvParser extends AbstractCsvParser {

    @Override
    public boolean supports(LotteryType type) {
        return type == LotteryType.REVANCHITA;
    }

    @Override
    protected LotteryDraw parseRow(String[] row, List<String> headers, LotteryType lotteryType) {
        int idxConcurso = indexOf(headers, "CONCURSO");
        int idxF1       = indexOf(headers, "F1");
        int idxF2       = indexOf(headers, "F2");
        int idxF3       = indexOf(headers, "F3");
        int idxF4       = indexOf(headers, "F4");
        int idxF5       = indexOf(headers, "F5");
        int idxF6       = indexOf(headers, "F6");
        int idxBolsa    = indexOf(headers, "BOLSA");
        int idxFecha    = indexOf(headers, "FECHA");

        return LotteryDraw.builder()
                .lotteryType(LotteryType.REVANCHITA)
                .drawNumber(parseInt(row, idxConcurso))
                .drawDate(parseDate(row, idxFecha))
                .numbers(List.of(
                        parseInt(row, idxF1),
                        parseInt(row, idxF2),
                        parseInt(row, idxF3),
                        parseInt(row, idxF4),
                        parseInt(row, idxF5),
                        parseInt(row, idxF6)
                ))
                .jackpotAmount(parseBigDecimal(row, idxBolsa))
                .build();
    }
}
