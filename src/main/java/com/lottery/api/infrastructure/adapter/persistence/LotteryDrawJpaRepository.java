package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.infrastructure.adapter.persistence.entity.LotteryDrawEntity;
import com.lottery.api.infrastructure.adapter.persistence.projection.BalanceProjection;
import com.lottery.api.infrastructure.adapter.persistence.projection.DueNumberProjection;
import com.lottery.api.infrastructure.adapter.persistence.projection.NumberFrequencyProjection;
import com.lottery.api.infrastructure.adapter.persistence.projection.PairFrequencyProjection;
import com.lottery.api.infrastructure.adapter.persistence.projection.SumHistogramProjection;
import com.lottery.api.infrastructure.adapter.persistence.projection.SumStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT d FROM LotteryDrawEntity d WHERE d.lotteryType = :type ORDER BY d.drawNumber DESC")
    Page<LotteryDrawEntity> findPageableByType(@Param("type") LotteryType type, Pageable pageable);

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

    /**
     * Calcula el "due score" de cada número: sorteos transcurridos desde su última
     * aparición dividido entre su intervalo promedio de aparición.
     * Un valor > 1.0 indica que el número ya superó su intervalo histórico.
     */
    @Query(value = """
        WITH all_numbers AS (
            SELECT draw_number, number_1 AS num FROM lottery_draws WHERE lottery_type = :type AND number_1 IS NOT NULL
            UNION ALL
            SELECT draw_number, number_2 FROM lottery_draws WHERE lottery_type = :type AND number_2 IS NOT NULL
            UNION ALL
            SELECT draw_number, number_3 FROM lottery_draws WHERE lottery_type = :type AND number_3 IS NOT NULL
            UNION ALL
            SELECT draw_number, number_4 FROM lottery_draws WHERE lottery_type = :type AND number_4 IS NOT NULL
            UNION ALL
            SELECT draw_number, number_5 FROM lottery_draws WHERE lottery_type = :type AND number_5 IS NOT NULL
            UNION ALL
            SELECT draw_number, number_6 FROM lottery_draws WHERE lottery_type = :type AND number_6 IS NOT NULL
            UNION ALL
            SELECT draw_number, number_7 FROM lottery_draws WHERE lottery_type = :type AND number_7 IS NOT NULL
            UNION ALL
            SELECT draw_number, number_8 FROM lottery_draws WHERE lottery_type = :type AND number_8 IS NOT NULL
        ),
        totals AS (
            SELECT MAX(draw_number)            AS max_draw,
                   COUNT(DISTINCT draw_number) AS total_draws
            FROM lottery_draws WHERE lottery_type = :type
        ),
        stats AS (
            SELECT num                  AS number,
                   COUNT(*)             AS frequency,
                   MAX(draw_number)     AS last_draw_number
            FROM all_numbers
            GROUP BY num
        )
        SELECT s.number,
               s.frequency,
               s.last_draw_number,
               t.max_draw - s.last_draw_number                                            AS draws_since_last,
               ROUND(CAST(t.total_draws AS numeric) / s.frequency, 2)                    AS avg_interval,
               ROUND(CAST(t.max_draw - s.last_draw_number AS numeric)
                     / NULLIF(CAST(t.total_draws AS numeric) / s.frequency, 0), 2)       AS due_score
        FROM stats s
        CROSS JOIN totals t
        ORDER BY due_score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<DueNumberProjection> findDueNumbers(
            @Param("type") String type,
            @Param("limit") int limit);

    /**
     * Frecuencia de cada número en los últimos {@code windowSize} sorteos del tipo dado.
     * Reusa la misma proyección que la query histórica para compatibilidad con el mapper.
     */
    @Query(value = """
        WITH bounds AS (
            SELECT MAX(draw_number) AS max_draw
            FROM lottery_draws WHERE lottery_type = :type
        ),
        window_count AS (
            SELECT COUNT(DISTINCT draw_number) AS cnt
            FROM lottery_draws, bounds
            WHERE lottery_type = :type AND draw_number > (bounds.max_draw - :windowSize)
        ),
        nums AS (
            SELECT draw_number, draw_date, number_1 AS n
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_1 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
            UNION ALL
            SELECT draw_number, draw_date, number_2
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_2 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
            UNION ALL
            SELECT draw_number, draw_date, number_3
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_3 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
            UNION ALL
            SELECT draw_number, draw_date, number_4
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_4 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
            UNION ALL
            SELECT draw_number, draw_date, number_5
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_5 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
            UNION ALL
            SELECT draw_number, draw_date, number_6
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_6 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
            UNION ALL
            SELECT draw_number, draw_date, number_7
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_7 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
            UNION ALL
            SELECT draw_number, draw_date, number_8
              FROM lottery_draws, bounds
             WHERE lottery_type = :type AND number_8 IS NOT NULL
               AND draw_number > (bounds.max_draw - :windowSize)
        )
        SELECT
            n                                                                  AS number,
            COUNT(*)::bigint                                                   AS frequency,
            ROUND(COUNT(*) * 100.0 / NULLIF((SELECT cnt FROM window_count), 0), 2) AS percentage,
            MAX(draw_date)                                                     AS lastDrawnDate,
            MAX(draw_number)::integer                                          AS lastDrawNumber
        FROM nums
        GROUP BY n
        ORDER BY n
        """, nativeQuery = true)
    List<NumberFrequencyProjection> findFrequenciesInWindow(
            @Param("type") String type,
            @Param("windowSize") int windowSize);

    /**
     * Devuelve el número real de sorteos distintos en los últimos {@code windowSize} draw_numbers.
     */
    @Query(value = """
        WITH bounds AS (SELECT MAX(draw_number) AS max_draw FROM lottery_draws WHERE lottery_type = :type)
        SELECT COUNT(DISTINCT draw_number)
        FROM lottery_draws, bounds
        WHERE lottery_type = :type AND draw_number > (bounds.max_draw - :windowSize)
        """, nativeQuery = true)
    long countDrawsInWindow(
            @Param("type") String type,
            @Param("windowSize") int windowSize);

    /**
     * Distribución de balance par/impar y alto/bajo por sorteo.
     * Agrupa por (odd_count, high_count) y retorna cuántos sorteos tienen esa combinación.
     */
    @Query(value = """
        WITH draw_analysis AS (
            SELECT
                draw_number,
                SUM(CASE WHEN MOD(n, 2) != 0 THEN 1 ELSE 0 END)::integer AS odd_count,
                SUM(CASE WHEN n > :midpoint THEN 1 ELSE 0 END)::integer AS high_count
            FROM (
                SELECT draw_number, number_1 AS n FROM lottery_draws WHERE lottery_type = :type AND number_1 IS NOT NULL
                UNION ALL
                SELECT draw_number, number_2 FROM lottery_draws WHERE lottery_type = :type AND number_2 IS NOT NULL
                UNION ALL
                SELECT draw_number, number_3 FROM lottery_draws WHERE lottery_type = :type AND number_3 IS NOT NULL
                UNION ALL
                SELECT draw_number, number_4 FROM lottery_draws WHERE lottery_type = :type AND number_4 IS NOT NULL
                UNION ALL
                SELECT draw_number, number_5 FROM lottery_draws WHERE lottery_type = :type AND number_5 IS NOT NULL
                UNION ALL
                SELECT draw_number, number_6 FROM lottery_draws WHERE lottery_type = :type AND number_6 IS NOT NULL
                UNION ALL
                SELECT draw_number, number_7 FROM lottery_draws WHERE lottery_type = :type AND number_7 IS NOT NULL
                UNION ALL
                SELECT draw_number, number_8 FROM lottery_draws WHERE lottery_type = :type AND number_8 IS NOT NULL
            ) nums
            GROUP BY draw_number
        )
        SELECT odd_count AS oddCount, high_count AS highCount, COUNT(*) AS drawCount
        FROM draw_analysis
        GROUP BY odd_count, high_count
        ORDER BY COUNT(*) DESC
        """, nativeQuery = true)
    List<BalanceProjection> findBalanceDistribution(
            @Param("type") String type,
            @Param("midpoint") int midpoint);

    /**
     * Histograma de la suma de los números principales por sorteo.
     */
    @Query(value = """
        SELECT
            (COALESCE(number_1,0) + COALESCE(number_2,0) + COALESCE(number_3,0)
             + COALESCE(number_4,0) + COALESCE(number_5,0) + COALESCE(number_6,0)
             + COALESCE(number_7,0) + COALESCE(number_8,0)) AS sumValue,
            COUNT(*) AS frequency
        FROM lottery_draws
        WHERE lottery_type = :type AND number_1 IS NOT NULL
        GROUP BY sumValue
        ORDER BY sumValue
        """, nativeQuery = true)
    List<SumHistogramProjection> findSumHistogram(@Param("type") String type);

    /**
     * Estadísticas descriptivas (media, desviación estándar, percentiles) de las sumas.
     */
    @Query(value = """
        WITH sums AS (
            SELECT (COALESCE(number_1,0) + COALESCE(number_2,0) + COALESCE(number_3,0)
                    + COALESCE(number_4,0) + COALESCE(number_5,0) + COALESCE(number_6,0)
                    + COALESCE(number_7,0) + COALESCE(number_8,0)) AS s
            FROM lottery_draws
            WHERE lottery_type = :type AND number_1 IS NOT NULL
        )
        SELECT
            ROUND(AVG(s)::numeric, 2)                                       AS mean,
            ROUND(STDDEV(s)::numeric, 2)                                    AS stdDev,
            MIN(s)::integer                                                  AS minSum,
            MAX(s)::integer                                                  AS maxSum,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY s)                 AS p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY s)                 AS p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY s)                 AS p75,
            COUNT(*)::bigint                                                 AS totalDraws
        FROM sums
        """, nativeQuery = true)
    SumStatsProjection findSumStats(@Param("type") String type);

    /**
     * Top-N pares de números que co-aparecen con más frecuencia en el mismo sorteo.
     * Usa LATERAL UNNEST para generar todos los pares (a < b) de cada sorteo.
     */
    @Query("SELECT d FROM LotteryDrawEntity d " +
           "WHERE d.lotteryType = :type AND d.drawDate > :afterDate " +
           "ORDER BY d.drawDate ASC")
    List<LotteryDrawEntity> findByTypeAndDrawDateAfter(
            @Param("type") LotteryType type,
            @Param("afterDate") LocalDate afterDate);

    @Query(value = """
        WITH nums AS (
            SELECT draw_number,
                   ARRAY_REMOVE(ARRAY[number_1, number_2, number_3, number_4,
                                      number_5, number_6, number_7, number_8], NULL) AS numbers
            FROM lottery_draws
            WHERE lottery_type = :type
        ),
        pairs AS (
            SELECT a.num AS number1, b.num AS number2
            FROM nums n,
                 LATERAL UNNEST(n.numbers) AS a(num),
                 LATERAL UNNEST(n.numbers) AS b(num)
            WHERE a.num < b.num
        )
        SELECT number1, number2, COUNT(*)::bigint AS frequency
        FROM pairs
        GROUP BY number1, number2
        ORDER BY frequency DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<PairFrequencyProjection> findTopPairs(
            @Param("type") String type,
            @Param("limit") int limit);
}
