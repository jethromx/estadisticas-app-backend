package com.lottery.api.domain.port.out;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;

import java.io.InputStream;
import java.util.List;

/**
 * Puerto de salida: descarga y parseo del CSV histórico de la Lotería Nacional.
 *
 * <p>Abstrae el mecanismo de obtención del archivo (HTTP, sistema de ficheros, etc.)
 * del resto del dominio y la capa de aplicación.</p>
 */
public interface LotteryHistoricalDownloaderPort {

    /**
     * Descarga el CSV desde la URL configurada para el tipo dado y retorna los sorteos parseados.
     *
     * @param lotteryType tipo de juego a descargar
     * @return lista de sorteos parseados; vacía si no hay registros
     * @throws com.lottery.api.domain.exception.CsvParsingException si la descarga o el parseo fallan
     */
    List<LotteryDraw> downloadHistoricalData(LotteryType lotteryType);

    /**
     * Parsea los sorteos directamente desde un {@link InputStream} de un CSV.
     * Útil para importar un archivo local sin necesidad de descarga HTTP.
     *
     * @param lotteryType tipo de juego que corresponde al archivo
     * @param csvStream   flujo del archivo CSV
     * @return lista de sorteos parseados
     * @throws com.lottery.api.domain.exception.CsvParsingException si el parseo falla
     */
    List<LotteryDraw> parseFromStream(LotteryType lotteryType, InputStream csvStream);
}
