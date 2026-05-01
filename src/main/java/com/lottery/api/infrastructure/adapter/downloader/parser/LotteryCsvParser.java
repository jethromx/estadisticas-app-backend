package com.lottery.api.infrastructure.adapter.downloader.parser;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;

import java.io.InputStream;
import java.util.List;

/**
 * Estrategia de parseo de archivos CSV históricos de Lotería Nacional.
 *
 * <p>Cada implementación es responsable de un tipo de juego (o familia de tipos)
 * cuyo CSV tiene una estructura de columnas diferente.</p>
 */
public interface LotteryCsvParser {

    /**
     * Indica si esta estrategia puede procesar el tipo de juego dado.
     *
     * @param type tipo de lotería a verificar
     * @return {@code true} si el parser soporta este tipo
     */
    boolean supports(LotteryType type);

    /**
     * Parsea el CSV del {@code InputStream} y retorna la lista de sorteos.
     *
     * @param inputStream flujo del archivo CSV (se cierra internamente)
     * @param lotteryType tipo de juego para construir correctamente los objetos de dominio
     * @return lista de sorteos parseados; vacía si el CSV no tiene datos
     * @throws com.lottery.api.domain.exception.CsvParsingException ante cualquier error de formato
     */
    List<LotteryDraw> parse(InputStream inputStream, LotteryType lotteryType);
}
