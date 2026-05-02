package com.lottery.api.domain.port.out;

import com.lottery.api.domain.model.DueNumber;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida: persistencia de sorteos de lotería.
 *
 * <p>Las implementaciones (adaptadores) en la capa de infraestructura son las únicas
 * responsables de la tecnología de persistencia concreta (JPA, etc.).</p>
 */
public interface LotteryDrawRepositoryPort {

    LotteryDraw save(LotteryDraw draw);

    List<LotteryDraw> saveAll(List<LotteryDraw> draws);

    Optional<LotteryDraw> findById(Long id);

    Optional<LotteryDraw> findByTypeAndDrawNumber(LotteryType type, Integer drawNumber);

    List<LotteryDraw> findByType(LotteryType type);

    List<LotteryDraw> findByTypeAndDateRange(LotteryType type, LocalDate from, LocalDate to);

    /**
     * Devuelve los {@code limit} sorteos más recientes del tipo indicado.
     *
     * @param type  tipo de juego
     * @param limit cantidad máxima de sorteos a retornar
     * @return lista ordenada de más reciente a más antiguo
     */
    List<LotteryDraw> findRecentByType(LotteryType type, int limit);

    long countByType(LotteryType type);

    /**
     * Calcula la frecuencia de cada número en todo el histórico del tipo.
     *
     * @param type tipo de juego
     * @return lista de frecuencias, ordenada de mayor a menor
     */
    List<NumberFrequency> getNumberFrequencies(LotteryType type);

    /**
     * Calcula la frecuencia de cada número en el rango de fechas indicado.
     */
    List<NumberFrequency> getNumberFrequenciesByDateRange(LotteryType type, LocalDate from, LocalDate to);

    Optional<LocalDate> findFirstDrawDateByType(LotteryType type);

    Optional<LocalDate> findLastDrawDateByType(LotteryType type);

    boolean existsByTypeAndDrawNumber(LotteryType type, Integer drawNumber);

    Set<Integer> findAllDrawNumbersByType(LotteryType type);

    /**
     * Devuelve los {@code limit} números con mayor {@code dueScore}
     * (sorteos transcurridos desde última aparición / intervalo promedio).
     */
    List<DueNumber> getDueNumbers(LotteryType type, int limit);
}
