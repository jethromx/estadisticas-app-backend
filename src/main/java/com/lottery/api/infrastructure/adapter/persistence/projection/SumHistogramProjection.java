package com.lottery.api.infrastructure.adapter.persistence.projection;

public interface SumHistogramProjection {
    Integer getSumValue();
    Long getFrequency();
}
