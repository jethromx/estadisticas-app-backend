package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

@Data
public class NumberPairResponse {
    private int number1;
    private int number2;
    private long frequency;
    private double percentage;
}
