package com.lottery.api.infrastructure.adapter.downloader;

import com.lottery.api.domain.exception.CsvParsingException;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.out.LotteryHistoricalDownloaderPort;
import com.lottery.api.infrastructure.adapter.downloader.parser.LotteryCsvParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Adaptador que descarga el CSV histórico desde la URL de Lotería Nacional
 * usando WebClient (reactivo, con reintentos) y delega el parseo al
 * {@link LotteryCsvParser} correspondiente.
 *
 * <p>Si la descarga HTTP falla, se puede usar {@link #parseFromStream} con
 * un archivo local descargado manualmente.</p>
 */
@Slf4j
@Component
public class LotteryHistoricalDownloaderAdapter implements LotteryHistoricalDownloaderPort {

    private final WebClient webClient;
    private final List<LotteryCsvParser> parsers;
    private final Map<LotteryType, String> urls;
    private final int maxRetries;

    public LotteryHistoricalDownloaderAdapter(
            WebClient webClient,
            List<LotteryCsvParser> parsers,
            @Value("${lottery.urls.melate}")     String melateUrl,
            @Value("${lottery.urls.revancha}")   String revanchaUrl,
            @Value("${lottery.urls.revanchita}") String revanchitaUrl,
            @Value("${lottery.download.max-retries:3}") int maxRetries) {

        this.webClient  = webClient;
        this.parsers    = parsers;
        this.maxRetries = maxRetries;
        this.urls = Map.of(
                LotteryType.MELATE,      melateUrl,
                LotteryType.REVANCHA,    revanchaUrl,
                LotteryType.REVANCHITA,  revanchitaUrl
        );
    }

    @Override
    public List<LotteryDraw> downloadHistoricalData(LotteryType lotteryType) {
        String url = urls.get(lotteryType);
        log.info("Descargando histórico de {} desde: {}", lotteryType, url);

        byte[] csvBytes = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(2)))
                .block();

        if (csvBytes == null || csvBytes.length == 0) {
            throw new CsvParsingException("Respuesta vacía al descargar histórico de " + lotteryType);
        }

        log.debug("Descargados {} bytes para {}", csvBytes.length, lotteryType);
        return parseFromStream(lotteryType, new ByteArrayInputStream(csvBytes));
    }

    @Override
    public List<LotteryDraw> parseFromStream(LotteryType lotteryType, InputStream csvStream) {
        LotteryCsvParser parser = parsers.stream()
                .filter(p -> p.supports(lotteryType))
                .findFirst()
                .orElseThrow(() -> new CsvParsingException(
                        "No hay parser disponible para el tipo: " + lotteryType));

        return parser.parse(csvStream, lotteryType);
    }
}
