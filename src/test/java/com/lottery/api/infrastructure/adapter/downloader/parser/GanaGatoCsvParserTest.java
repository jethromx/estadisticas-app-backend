package com.lottery.api.infrastructure.adapter.downloader.parser;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GanaGatoCsvParser — Tests Unitarios")
class GanaGatoCsvParserTest {

    private final GanaGatoCsvParser parser = new GanaGatoCsvParser();

    private InputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("supports solo GANA_GATO")
    void supports_onlyGanaGato() {
        assertThat(parser.supports(LotteryType.GANA_GATO)).isTrue();
        assertThat(parser.supports(LotteryType.MELATE)).isFalse();
    }

    @Test
    @DisplayName("debe parsear 8 números con posibles repetidos")
    void parse_eightNumbersWithDuplicates() {
        String content = """
                NPRODUCTO,CONCURSO,F1,F2,F3,F4,F5,F6,F7,F8,BOLSA,FECHA
                20,2981,3,5,3,1,5,5,5,2,302000,10/03/2026
                """;

        List<LotteryDraw> draws = parser.parse(csv(content), LotteryType.GANA_GATO);

        assertThat(draws).hasSize(1);
        LotteryDraw draw = draws.get(0);
        assertThat(draw.getNumbers()).hasSize(8);
        assertThat(draw.getNumbers()).containsExactly(3, 5, 3, 1, 5, 5, 5, 2);
        assertThat(draw.getLotteryType()).isEqualTo(LotteryType.GANA_GATO);
        assertThat(draw.getAdditionalNumber()).isNull();
    }

    @Test
    @DisplayName("todos los números deben estar en rango 1-5")
    void parse_allNumbersInValidRange() {
        String content = """
                NPRODUCTO,CONCURSO,F1,F2,F3,F4,F5,F6,F7,F8,BOLSA,FECHA
                20,2981,1,2,3,4,5,1,2,3,302000,10/03/2026
                20,2980,4,5,2,5,5,2,4,1,301000,07/03/2026
                """;

        List<LotteryDraw> draws = parser.parse(csv(content), LotteryType.GANA_GATO);

        assertThat(draws).hasSize(2);
        draws.forEach(d -> assertThat(d.getNumbers())
                .allMatch(n -> n >= 1 && n <= 5));
    }
}
