package com.lottery.api.infrastructure.adapter.downloader.parser;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Parser del CSV de Revancha.
 *
 * <p>Formato esperado: {@code NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,R6,BOLSA,FECHA}
 * (sin número adicional).</p>
 */
@Component
public class RevanchaCsvParser extends AbstractCsvParser {

    @Override
    public boolean supports(LotteryType type) {
        return type == LotteryType.REVANCHA;
    }

    @Override
    protected LotteryDraw parseRow(String[] row, List<String> headers, LotteryType lotteryType) {
        int idxConcurso = indexOf(headers, "CONCURSO");
        int idxR1       = indexOf(headers, "R1");
        int idxR2       = indexOf(headers, "R2");
        int idxR3       = indexOf(headers, "R3");
        int idxR4       = indexOf(headers, "R4");
        int idxR5       = indexOf(headers, "R5");
        int idxR6       = indexOf(headers, "R6");
        int idxBolsa    = indexOf(headers, "BOLSA");
        int idxFecha    = indexOf(headers, "FECHA");

        return LotteryDraw.builder()
                .lotteryType(LotteryType.REVANCHA)
                .drawNumber(parseInt(row, idxConcurso))
                .drawDate(parseDate(row, idxFecha))
                .numbers(List.of(
                        parseInt(row, idxR1),
                        parseInt(row, idxR2),
                        parseInt(row, idxR3),
                        parseInt(row, idxR4),
                        parseInt(row, idxR5),
                        parseInt(row, idxR6)
                ))
                .additionalNumber(null)
                .jackpotAmount(parseBigDecimal(row, idxBolsa))
                .build();
    }
}
