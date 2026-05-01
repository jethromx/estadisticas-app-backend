package com.lottery.api.infrastructure.adapter.downloader.parser;

import com.lottery.api.domain.exception.CsvParsingException;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MelateCsvParser — Tests Unitarios")
class MelateCsvParserTest {

    private final MelateCsvParser parser = new MelateCsvParser();

    private InputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("supports solo MELATE")
    void supports_onlyMelate() {
        assertThat(parser.supports(LotteryType.MELATE)).isTrue();
        assertThat(parser.supports(LotteryType.REVANCHA)).isFalse();
        assertThat(parser.supports(LotteryType.REVANCHITA)).isFalse();
        assertThat(parser.supports(LotteryType.GANA_GATO)).isFalse();
    }

    @Test
    @DisplayName("debe parsear una fila correctamente")
    void parse_validRow_returnsCorrectDraw() {
        String content = """
                NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,R6,R7,BOLSA,FECHA
                40,4158,1,26,30,35,45,54,28,30100000,07/01/2026
                """;

        List<LotteryDraw> draws = parser.parse(csv(content), LotteryType.MELATE);

        assertThat(draws).hasSize(1);
        LotteryDraw draw = draws.get(0);
        assertThat(draw.getLotteryType()).isEqualTo(LotteryType.MELATE);
        assertThat(draw.getDrawNumber()).isEqualTo(4158);
        assertThat(draw.getDrawDate()).isEqualTo(LocalDate.of(2026, 1, 7));
        assertThat(draw.getNumbers()).containsExactly(1, 26, 30, 35, 45, 54);
        assertThat(draw.getAdditionalNumber()).isEqualTo(28);
        assertThat(draw.getJackpotAmount()).isEqualByComparingTo(new BigDecimal("30100000"));
    }

    @Test
    @DisplayName("debe parsear múltiples filas")
    void parse_multipleRows_returnsAllDraws() {
        String content = """
                NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,R6,R7,BOLSA,FECHA
                40,4158,1,26,30,35,45,54,28,30100000,07/01/2026
                40,4157,13,22,31,32,42,53,48,30000000,04/01/2026
                40,4156,11,25,33,39,45,50,9,272300000,02/01/2026
                """;

        List<LotteryDraw> draws = parser.parse(csv(content), LotteryType.MELATE);

        assertThat(draws).hasSize(3);
        assertThat(draws).extracting(LotteryDraw::getDrawNumber)
                .containsExactly(4158, 4157, 4156);
    }

    @Test
    @DisplayName("CSV vacío retorna lista vacía")
    void parse_emptyCsv_returnsEmptyList() {
        String content = "NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,R6,R7,BOLSA,FECHA\n";

        List<LotteryDraw> draws = parser.parse(csv(content), LotteryType.MELATE);

        assertThat(draws).isEmpty();
    }

    @Test
    @DisplayName("InputStream completamente vacío retorna lista vacía")
    void parse_emptyStream_returnsEmptyList() {
        List<LotteryDraw> draws = parser.parse(csv(""), LotteryType.MELATE);
        assertThat(draws).isEmpty();
    }

    @Test
    @DisplayName("filas con formato de fecha incorrecto se omiten con log de advertencia")
    void parse_badDateFormat_rowIsSkipped() {
        String content = """
                NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,R6,R7,BOLSA,FECHA
                40,4158,1,26,30,35,45,54,28,30100000,2026-01-07
                40,4157,13,22,31,32,42,53,48,30000000,04/01/2026
                """;

        List<LotteryDraw> draws = parser.parse(csv(content), LotteryType.MELATE);

        assertThat(draws).hasSize(1);
        assertThat(draws.get(0).getDrawNumber()).isEqualTo(4157);
    }
}
