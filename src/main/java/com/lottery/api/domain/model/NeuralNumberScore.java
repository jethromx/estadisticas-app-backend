package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

/** Score de un número individual calculado por la red neuronal. */
@Value
@Builder(toBuilder = true)
public class NeuralNumberScore {
    int    number;
    double probability;    // sigmoid output de la red [0, 1]
    double recentFreq50;   // frecuencia normalizada en los últimos 50 sorteos
    double dueScore;       // due-score normalizado [0, 1]
    double trend;          // tendencia reciente [-1, 1] (positivo = en alza)
    int    rank;
}
