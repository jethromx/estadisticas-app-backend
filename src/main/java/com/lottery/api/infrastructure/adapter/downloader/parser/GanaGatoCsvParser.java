package com.lottery.api.infrastructure.adapter.downloader.parser;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser del CSV de GanaGato.
 *
 * <p>Formato esperado: {@code NPRODUCTO,CONCURSO,F1,F2,F3,F4,F5,F6,F7,F8,BOLSA,FECHA}
 * Los 8 números están en el rango 1-5 y pueden repetirse.</p>
 */
@Component
public class GanaGatoCsvParser extends AbstractCsvParser {

    @Override
    public boolean supports(LotteryType type) {
        return type == LotteryType.GANA_GATO;
    }

    @Override
    protected LotteryDraw parseRow(String[] row, List<String> headers, LotteryType lotteryType) {
        int idxConcurso = indexOf(headers, "CONCURSO");
        int idxBolsa    = indexOf(headers, "BOLSA");
        int idxFecha    = indexOf(headers, "FECHA");

        // GanaGato usa F1-F8; se leen dinámicamente según las columnas presentes
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            String colName = "F" + i;
            if (headers.contains(colName)) {
                Integer num = parseInt(row, headers.indexOf(colName));
                if (num != null) numbers.add(num);
            }
        }

        return LotteryDraw.builder()
                .lotteryType(LotteryType.GANA_GATO)
                .drawNumber(parseInt(row, idxConcurso))
                .drawDate(parseDate(row, idxFecha))
                .numbers(numbers)
                .jackpotAmount(parseBigDecimal(row, idxBolsa))
                .build();
    }
}
