package com.lottery.api.application.usecase;

import com.lottery.api.application.util.MLP;
import com.lottery.api.domain.exception.LotteryException;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NeuralNumberScore;
import com.lottery.api.domain.model.NeuralPrediction;
import com.lottery.api.domain.port.in.GetNeuralPredictionUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de predicción mediante red neuronal feedforward (MLP) implementada en Java puro.
 *
 * <p>Para cada número del rango del juego extrae 8 features del histórico y entrena
 * una red MLP (input→16→8→1) con mini-batch SGD, regularización L2 y peso de clase
 * positiva para manejar el desbalance (~10% de los números aparecen por sorteo).</p>
 *
 * <p>Features por número:
 * <ol>
 *   <li>Frecuencia normalizada en últimos 10 sorteos</li>
 *   <li>Frecuencia normalizada en últimos 50 sorteos</li>
 *   <li>Frecuencia normalizada en últimos 100 sorteos</li>
 *   <li>Frecuencia histórica global normalizada</li>
 *   <li>Due-score normalizado (sorteos sin salir / intervalo promedio)</li>
 *   <li>Tendencia reciente (último-50 vs anterior-50), mapeada a [0,1]</li>
 *   <li>Posición del número en el rango (feature estructural)</li>
 *   <li>Ratio momentum muy-reciente/reciente</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetNeuralPredictionService implements GetNeuralPredictionUseCase {

    private static final int    INPUT_SIZE        = 8;
    private static final int    HIDDEN1           = 16;
    private static final int    HIDDEN2           = 8;
    private static final int    EPOCHS            = 60;
    private static final int    BATCH_SIZE        = 256;
    private static final double LR                = 0.01;
    private static final double L2                = 0.001;
    private static final int    MIN_HISTORY       = 200;
    private static final int    VALIDATION_DRAWS  = 50;

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-neural", key = "#lotteryType")
    @Transactional(readOnly = true)
    public NeuralPrediction getNeuralPrediction(LotteryType lotteryType) {
        List<LotteryDraw> draws = repositoryPort.findByType(lotteryType).stream()
                .sorted(Comparator.comparing(LotteryDraw::getDrawDate))
                .collect(Collectors.toList());

        int N          = draws.size();
        int numNumbers = lotteryType.getMaxNumber();
        int numsPerDraw = lotteryType.getNumbersCount();

        if (N < MIN_HISTORY + VALIDATION_DRAWS + 50) {
            throw new LotteryException(
                "Datos insuficientes para entrenar la red neuronal: se requieren al menos " +
                (MIN_HISTORY + VALIDATION_DRAWS + 50) + " sorteos, hay " + N + ".");
        }

        log.info("Iniciando entrenamiento MLP para {} ({} sorteos)", lotteryType, N);

        // 1. Precomputar tablas de frecuencias acumuladas y última aparición
        int[][] cumFreq  = computeCumFreq(draws, numNumbers);
        int[][] lastSeen = computeLastSeen(draws, numNumbers);

        // Peso de clase positiva: compensa el desbalance (solo numsPerDraw de numNumbers aparecen)
        double posWeight = (double) (numNumbers - numsPerDraw) / numsPerDraw;

        // 2. Construir conjunto de entrenamiento (draws MIN_HISTORY..N-VALIDATION_DRAWS)
        int trainStart = MIN_HISTORY;
        int trainEnd   = N - VALIDATION_DRAWS;
        int trainSamples = (trainEnd - trainStart) * numNumbers;

        List<double[]> trainX = new ArrayList<>(trainSamples);
        List<Double>   trainY = new ArrayList<>(trainSamples);

        for (int d = trainStart; d < trainEnd; d++) {
            Set<Integer> drawn = new HashSet<>(draws.get(d).getNumbers());
            for (int n = 1; n <= numNumbers; n++) {
                trainX.add(extractFeatures(n, d, numNumbers, cumFreq, lastSeen));
                trainY.add(drawn.contains(n) ? 1.0 : 0.0);
            }
        }

        // 3. Construir conjunto de validación (últimos VALIDATION_DRAWS sorteos)
        List<double[]> valX = new ArrayList<>(VALIDATION_DRAWS * numNumbers);
        List<Double>   valY = new ArrayList<>(VALIDATION_DRAWS * numNumbers);

        for (int d = trainEnd; d < N; d++) {
            Set<Integer> drawn = new HashSet<>(draws.get(d).getNumbers());
            for (int n = 1; n <= numNumbers; n++) {
                valX.add(extractFeatures(n, d, numNumbers, cumFreq, lastSeen));
                valY.add(drawn.contains(n) ? 1.0 : 0.0);
            }
        }

        // 4. Entrenar MLP
        log.info("Entrenando MLP {}: {} muestras, {} épocas, posWeight={}",
                 lotteryType, trainX.size(), EPOCHS, String.format("%.1f", posWeight));
        MLP mlp = new MLP(INPUT_SIZE, HIDDEN1, HIDDEN2);
        mlp.train(trainX, trainY, EPOCHS, LR, BATCH_SIZE, L2, posWeight);

        // 5. Validar
        mlp.validate(valX, valY, numNumbers, numsPerDraw);
        log.info("Validación {}: {} aciertos/sorteo", lotteryType, String.format("%.2f", mlp.getValidationHitRate()));

        // 6. Predecir usando todos los N sorteos como contexto
        double[] scores = new double[numNumbers + 1];  // 1-indexed
        for (int n = 1; n <= numNumbers; n++) {
            scores[n] = mlp.predict(extractFeatures(n, N, numNumbers, cumFreq, lastSeen));
        }

        // 7. Construir lista de números con scores
        List<NeuralNumberScore> scoredNumbers = new ArrayList<>(numNumbers);
        for (int n = 1; n <= numNumbers; n++) {
            double[] feat = extractFeatures(n, N, numNumbers, cumFreq, lastSeen);
            double   trend = feat[5] * 2.0 - 1.0;  // deshacer el mapeo [0,1] → [-1,1]
            scoredNumbers.add(NeuralNumberScore.builder()
                    .number(n)
                    .probability(Math.round(scores[n] * 1_000_000.0) / 1_000_000.0)
                    .recentFreq50(Math.round(feat[1] * 1_000_000.0) / 1_000_000.0)
                    .dueScore(Math.round(feat[4] * 1_000_000.0) / 1_000_000.0)
                    .trend(Math.round(trend * 1_000_000.0) / 1_000_000.0)
                    .rank(0)
                    .build());
        }
        scoredNumbers.sort(Comparator.comparingDouble(NeuralNumberScore::getProbability).reversed());
        for (int i = 0; i < scoredNumbers.size(); i++) {
            scoredNumbers.set(i, scoredNumbers.get(i).toBuilder().rank(i + 1).build());
        }

        // 8. Generar combinaciones sugeridas
        List<List<Integer>> combos = buildCombinations(scoredNumbers, numsPerDraw, scores, numNumbers);

        String desc = String.format(
            "MLP %d→%d→%d→1 · %d épocas · lr=%.3f (decay×0.5 cada 20) · L2=%.4f · " +
            "peso positivo=%.1fx · features: freq10/50/100, freq_hist, due, tendencia, posición, momentum",
            INPUT_SIZE, HIDDEN1, HIDDEN2, EPOCHS, LR, L2, posWeight);

        return NeuralPrediction.builder()
                .lotteryType(lotteryType)
                .totalDrawsAnalyzed(N)
                .trainingDraws(trainEnd - trainStart)
                .validationDraws(VALIDATION_DRAWS)
                .validationHitRate(Math.round(mlp.getValidationHitRate() * 10000.0) / 10000.0)
                .trainingEpochs(EPOCHS)
                .scoredNumbers(scoredNumbers)
                .suggestedCombos(combos)
                .methodDescription(desc)
                .build();
    }

    // ── Feature extraction ────────────────────────────────────────────────────

    /**
     * Extrae 8 features para el número {@code number} dado {@code drawIdx} sorteos de contexto.
     * Usa las tablas precomputadas para eficiencia O(1).
     */
    private double[] extractFeatures(int number, int drawIdx, int numNumbers,
                                     int[][] cumFreq, int[][] lastSeen) {
        int ni = number - 1;  // índice 0-based

        // f0: frecuencia muy reciente (últimos 10)
        int cnt10 = cumFreq[ni][drawIdx] - cumFreq[ni][Math.max(0, drawIdx - 10)];
        double f0 = cnt10 / 10.0;

        // f1: frecuencia reciente (últimos 50)
        int cnt50 = cumFreq[ni][drawIdx] - cumFreq[ni][Math.max(0, drawIdx - 50)];
        double f1 = cnt50 / 50.0;

        // f2: frecuencia media (últimos 100)
        int cnt100 = cumFreq[ni][drawIdx] - cumFreq[ni][Math.max(0, drawIdx - 100)];
        double f2 = cnt100 / 100.0;

        // f3: frecuencia histórica global
        double f3 = drawIdx > 0 ? (double) cumFreq[ni][drawIdx] / drawIdx : 0.0;

        // f4: due-score normalizado
        int    totalAppearances = cumFreq[ni][drawIdx];
        double avgInterval = totalAppearances > 0 ? (double) drawIdx / totalAppearances : drawIdx;
        int    ls = lastSeen[ni][drawIdx];
        int    drawsSinceLast = ls >= 0 ? drawIdx - ls : drawIdx;
        double f4 = Math.min(drawsSinceLast / Math.max(avgInterval, 1.0), 4.0) / 4.0;

        // f5: tendencia (últimos-50 vs previos-50), mapeada a [0,1]
        int cntPrev50 = drawIdx >= 50
            ? cumFreq[ni][drawIdx - 50] - cumFreq[ni][Math.max(0, drawIdx - 100)]
            : 0;
        double prevFreq50 = cntPrev50 / 50.0;
        double trend = (f1 - prevFreq50) / 0.5;  // diferencia normalizada
        double f5 = Math.max(0.0, Math.min(1.0, trend * 0.5 + 0.5));

        // f6: posición del número en el rango [0, 1] (feature estructural)
        double f6 = (number - 1.0) / (numNumbers - 1.0);

        // f7: ratio momentum muy-reciente vs reciente
        double f7 = f1 > 1e-6 ? Math.min(f0 / f1, 3.0) / 3.0 : 0.5;

        return new double[]{f0, f1, f2, f3, f4, f5, f6, f7};
    }

    // ── Precomputation ────────────────────────────────────────────────────────

    /**
     * Tabla de frecuencias acumuladas.
     * {@code cumFreq[n][d]} = número de veces que el número (n+1) apareció en draws[0..d-1].
     */
    private int[][] computeCumFreq(List<LotteryDraw> draws, int numNumbers) {
        int N = draws.size();
        int[][] cf = new int[numNumbers][N + 1];
        for (int d = 0; d < N; d++) {
            for (int n = 0; n < numNumbers; n++) cf[n][d + 1] = cf[n][d];
            for (int number : draws.get(d).getNumbers()) {
                if (number >= 1 && number <= numNumbers) {
                    cf[number - 1][d + 1]++;
                }
            }
        }
        return cf;
    }

    /**
     * Tabla de última aparición.
     * {@code lastSeen[n][d]} = índice del sorteo más reciente (en [0..d-1]) donde apareció (n+1),
     * o -1 si nunca ha aparecido.
     */
    private int[][] computeLastSeen(List<LotteryDraw> draws, int numNumbers) {
        int N = draws.size();
        int[][] ls = new int[numNumbers][N + 1];
        for (int n = 0; n < numNumbers; n++) ls[n][0] = -1;
        for (int d = 0; d < N; d++) {
            Set<Integer> drawn = new HashSet<>(draws.get(d).getNumbers());
            for (int n = 0; n < numNumbers; n++) {
                ls[n][d + 1] = drawn.contains(n + 1) ? d : ls[n][d];
            }
        }
        return ls;
    }

    // ── Combination building ──────────────────────────────────────────────────

    private List<List<Integer>> buildCombinations(List<NeuralNumberScore> scored,
                                                   int k, double[] scores, int numNumbers) {
        List<List<Integer>> combos = new ArrayList<>();

        // Combo 1: Top-k greedy por probabilidad
        List<Integer> greedy = scored.stream()
                .limit(k)
                .map(NeuralNumberScore::getNumber)
                .sorted()
                .collect(Collectors.toList());
        combos.add(greedy);

        // Combo 2: Balanceada 3I+3P (o mitad/mitad) desde el top del ranking
        int nOdd  = k / 2 + (k % 2);
        int nEven = k / 2;
        List<Integer> oddNums  = new ArrayList<>();
        List<Integer> evenNums = new ArrayList<>();
        for (NeuralNumberScore s : scored) {
            if (s.getNumber() % 2 != 0 && oddNums.size()  < nOdd)  oddNums.add(s.getNumber());
            if (s.getNumber() % 2 == 0 && evenNums.size() < nEven) evenNums.add(s.getNumber());
            if (oddNums.size() == nOdd && evenNums.size() == nEven) break;
        }
        List<Integer> balanced = new ArrayList<>();
        balanced.addAll(oddNums);
        balanced.addAll(evenNums);
        Collections.sort(balanced);
        combos.add(balanced);

        // Combo 3: Híbrida — 0.55*probabilidad + 0.45*dueScore, pick top-k balanceado
        List<NeuralNumberScore> hybridRanked = scored.stream()
                .sorted(Comparator.comparingDouble(
                    s -> -(0.55 * s.getProbability() + 0.45 * s.getDueScore())))
                .collect(Collectors.toList());
        List<Integer> hybridOdd  = new ArrayList<>();
        List<Integer> hybridEven = new ArrayList<>();
        for (NeuralNumberScore s : hybridRanked) {
            if (s.getNumber() % 2 != 0 && hybridOdd.size()  < nOdd)  hybridOdd.add(s.getNumber());
            if (s.getNumber() % 2 == 0 && hybridEven.size() < nEven) hybridEven.add(s.getNumber());
            if (hybridOdd.size() == nOdd && hybridEven.size() == nEven) break;
        }
        List<Integer> hybrid = new ArrayList<>();
        hybrid.addAll(hybridOdd);
        hybrid.addAll(hybridEven);
        Collections.sort(hybrid);
        combos.add(hybrid);

        return combos;
    }
}
