package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.infrastructure.adapter.persistence.entity.LotteryDrawEntity;
import com.lottery.api.infrastructure.adapter.persistence.projection.NumberFrequencyProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repositorio Spring Data JPA para {@link LotteryDrawEntity}.
 *
 * <p>La query de frecuencias usa UNION ALL sobre todas las columnas numéricas para
 * contar cuántas veces aparece cada valor en el histórico del tipo indicado.</p>
 */
public interface LotteryDrawJpaRepository extends JpaRepository<LotteryDrawEntity, Long> {

    Optional<LotteryDrawEntity> findByLotteryTypeAndDrawNumber(LotteryType type, Integer drawNumber);

    List<LotteryDrawEntity> findByLotteryTypeOrderByDrawDateDesc(LotteryType type);

    @Query("SELECT d FROM LotteryDrawEntity d " +
           "WHERE d.lotteryType = :type AND d.drawDate BETWEEN :from AND :to " +
           "ORDER BY d.drawDate DESC")
    List<LotteryDrawEntity> findByTypeAndDateRange(
            @Param("type") LotteryType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query(value = "SELECT * FROM lottery_draws WHERE lottery_type = :type " +
                   "ORDER BY draw_date DESC LIMIT :limit",
           nativeQuery = true)
    List<LotteryDrawEntity> findRecentByType(
            @Param("type") String type,
            @Param("limit") int limit);

    long countByLotteryType(LotteryType type);

    @Query("SELECT MIN(d.drawDate) FROM LotteryDrawEntity d WHERE d.lotteryType = :type")
    Optional<LocalDate> findMinDrawDateByType(@Param("type") LotteryType type);

    @Query("SELECT MAX(d.drawDate) FROM LotteryDrawEntity d WHERE d.lotteryType = :type")
    Optional<LocalDate> findMaxDrawDateByType(@Param("type") LotteryType type);

    boolean existsByLotteryTypeAndDrawNumber(LotteryType type, Integer drawNumber);

    @Query("SELECT d.drawNumber FROM LotteryDrawEntity d WHERE d.lotteryType = :type")
    Set<Integer> findAllDrawNumbersByType(@Param("type") LotteryType type);

    /**
     * Cuenta apariciones de cada número en todas las columnas de números para el tipo dado.
     * La fecha de filtro {@code minDate} = epoch si no se aplica filtro por rango.
     */
    @Query(value = """
        SELECT t.number          AS number,
               COUNT(*)          AS frequency,
               MAX(t.draw_date)  AS lastDrawnDate,
               MAX(t.draw_number) AS lastDrawNumber
        FROM (
            SELECT number_1 AS number, draw_date, draw_number
            FROM lottery_draws WHERE lottery_type = :type AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT number_2, draw_date, draw_number
            FROM lottery_draws WHERE lottery_type = :type AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT number_3, draw_date, draw_number
            FROM lottery_draws WHERE lottery_type = :type AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT number_4, draw_date, draw_number
            FROM lottery_draws WHERE lottery_type = :type AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT number_5, draw_date, draw_number
            FROM lottery_draws WHERE lottery_type = :type AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT number_6, draw_date, draw_number
            FROM lottery_draws
            WHERE lottery_type = :type AND number_6 IS NOT NULL AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT number_7, draw_date, draw_number
            FROM lottery_draws
            WHERE lottery_type = :type AND number_7 IS NOT NULL AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT number_8, draw_date, draw_number
            FROM lottery_draws
            WHERE lottery_type = :type AND number_8 IS NOT NULL AND draw_date BETWEEN :from AND :to
            UNION ALL
            SELECT additional_number, draw_date, draw_number
            FROM lottery_draws
            WHERE lottery_type = :type AND additional_number IS NOT NULL AND draw_date BETWEEN :from AND :to
        ) t
        WHERE t.number IS NOT NULL
        GROUP BY t.number
        ORDER BY frequency DESC
        """, nativeQuery = true)
    List<NumberFrequencyProjection> getNumberFrequenciesInRange(
            @Param("type") String type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
