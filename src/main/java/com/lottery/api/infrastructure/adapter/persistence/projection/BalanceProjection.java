package com.lottery.api.infrastructure.adapter.persistence.projection;

public interface BalanceProjection {
    Integer getOddCount();
    Integer getHighCount();
    Long getDrawCount();
}
