package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SyncResult;

import java.io.InputStream;
import java.util.List;

/**
 * Puerto de entrada: sincronización del histórico CSV de un tipo de lotería.
 *
 * <p>Descarga el CSV desde la URL configurada, parsea los registros e inserta
 * los sorteos nuevos en el repositorio sin sobreescribir existentes.</p>
 */
public interface SyncHistoricalDataUseCase {

    /**
     * Sincroniza el histórico completo para el tipo de lotería indicado.
     *
     * @param lotteryType tipo de juego a sincronizar
     * @return resultado con contadores de registros procesados
     */
    SyncResult syncHistoricalData(LotteryType lotteryType);

    /**
     * Sincroniza los históricos de todos los tipos de lotería disponibles.
     *
     * @return lista de resultados, uno por tipo
     */
    List<SyncResult> syncAllHistoricalData();

    /**
     * Importa sorteos desde un {@link InputStream} de un CSV descargado manualmente.
     *
     * @param lotteryType tipo de juego al que pertenece el archivo
     * @param csvStream   flujo del archivo CSV local
     * @return resultado con contadores de registros procesados
     */
    SyncResult importFromStream(LotteryType lotteryType, InputStream csvStream);
}
