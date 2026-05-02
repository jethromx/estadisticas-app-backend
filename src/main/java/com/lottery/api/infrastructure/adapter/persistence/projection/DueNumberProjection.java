package com.lottery.api.infrastructure.adapter.persistence.projection;

public interface DueNumberProjection {
    Integer getNumber();
    Long    getFrequency();
    Integer getLastDrawNumber();
    Integer getDrawsSinceLast();
    Double  getAvgInterval();
    Double  getDueScore();
}
