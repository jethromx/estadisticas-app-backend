package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Resultado completo de la predicción por red neuronal MLP. */
@Value
@Builder
public class NeuralPrediction {
    LotteryType lotteryType;
    int totalDrawsAnalyzed;
    int trainingDraws;
    int validationDraws;
    double validationHitRate;   // promedio de aciertos/sorteo en el conjunto de validación
    int trainingEpochs;
    List<NeuralNumberScore> scoredNumbers;       // todos los números ordenados por probabilidad
    List<List<Integer>> suggestedCombos;         // combinaciones derivadas de los scores
    String methodDescription;
}
