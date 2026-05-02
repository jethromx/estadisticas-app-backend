package com.lottery.api.infrastructure.adapter.persistence.projection;

public interface PairFrequencyProjection {
    Integer getNumber1();
    Integer getNumber2();
    Long getFrequency();
}
