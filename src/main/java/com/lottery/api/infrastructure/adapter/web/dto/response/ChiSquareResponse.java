package com.lottery.api.infrastructure.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChiSquareResponse {
    private String lotteryType;
    private double chiSquare;
    private int degreesOfFreedom;
    @JsonProperty("pValue")
    private double pValue;
    private long totalObservations;
    private double expectedFrequency;
    private String interpretation;
}
