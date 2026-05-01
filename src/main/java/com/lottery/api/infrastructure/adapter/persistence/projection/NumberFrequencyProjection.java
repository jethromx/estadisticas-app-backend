package com.lottery.api.infrastructure.adapter.persistence.projection;

import java.time.LocalDate;

/**
 * Proyección Spring Data para la consulta de frecuencias de números.
 *
 * <p>Usada por la query nativa que hace UNION ALL de todas las columnas de números
 * y agrupa por valor para contar apariciones.</p>
 */
public interface NumberFrequencyProjection {

    Integer getNumber();

    Long getFrequency();

    LocalDate getLastDrawnDate();

    Integer getLastDrawNumber();
}
