package com.lottery.api.infrastructure.adapter.persistence.projection;

public interface SumStatsProjection {
    Double getMean();
    Double getStdDev();
    Integer getMinSum();
    Integer getMaxSum();
    Double getP25();
    Double getP50();
    Double getP75();
    Long getTotalDraws();
}
