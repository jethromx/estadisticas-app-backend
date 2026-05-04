package com.lottery.api.infrastructure.adapter.persistence.projection;

public interface ActionCountProjection {
    String getAction();
    Long getCount();
}
