package com.lottery.api.application.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Red neuronal feedforward (MLP) implementada en Java puro, sin dependencias externas.
 *
 * <p>Arquitectura: input → Dense(hidden1, ReLU) → Dense(hidden2, ReLU) → Dense(1, Sigmoid)</p>
 * <p>Entrenamiento: mini-batch SGD con decaimiento de lr y regularización L2.
 * Pesos inicializados con He initialization (adecuada para ReLU).</p>
 * <p>Soporta peso de clase positiva para conjuntos desbalanceados (e.g. predicción de lotería
 * donde ~10% de los números aparecen en cada sorteo).</p>
 */
public class MLP {

    private final int inputSize;
    private final int h1;
    private final int h2;

    // Layer 1: w1[h1][inputSize], b1[h1]
    private final double[][] w1;
    private final double[]   b1;

    // Layer 2: w2[h2][h1], b2[h2]
    private final double[][] w2;
    private final double[]   b2;

    // Output layer: w3[h2], b3 (scalar)
    private final double[] w3;
    private double b3;

    private double validationHitRate = Double.NaN;

    public MLP(int inputSize, int hidden1, int hidden2) {
        this.inputSize = inputSize;
        this.h1 = hidden1;
        this.h2 = hidden2;

        Random rng = new Random(42);
        double s1 = Math.sqrt(2.0 / inputSize);
        double s2 = Math.sqrt(2.0 / hidden1);
        double s3 = Math.sqrt(2.0 / hidden2);

        w1 = new double[h1][inputSize];
        b1 = new double[h1];
        for (int i = 0; i < h1; i++)
            for (int j = 0; j < inputSize; j++)
                w1[i][j] = rng.nextGaussian() * s1;

        w2 = new double[h2][h1];
        b2 = new double[h2];
        for (int i = 0; i < h2; i++)
            for (int j = 0; j < h1; j++)
                w2[i][j] = rng.nextGaussian() * s2;

        w3 = new double[h2];
        b3 = 0.0;
        for (int j = 0; j < h2; j++)
            w3[j] = rng.nextGaussian() * s3;
    }

    // ── Activations ───────────────────────────────────────────────────────────

    private static double relu(double x)        { return x > 0.0 ? x : 0.0; }
    private static double reluD(double z)       { return z > 0.0 ? 1.0 : 0.0; }
    private static double sigmoid(double x)     { return 1.0 / (1.0 + Math.exp(-x)); }

    // ── Forward pass ──────────────────────────────────────────────────────────

    /**
     * Forward pass completo, guardando activaciones intermedias para backprop.
     * Devuelve la probabilidad de salida (sigmoid output).
     */
    private double forwardFull(double[] x,
                               double[] z1b, double[] h1b,
                               double[] z2b, double[] h2b) {
        for (int i = 0; i < h1; i++) {
            double s = b1[i];
            for (int j = 0; j < inputSize; j++) s += w1[i][j] * x[j];
            z1b[i] = s;
            h1b[i] = relu(s);
        }
        for (int i = 0; i < h2; i++) {
            double s = b2[i];
            for (int j = 0; j < h1; j++) s += w2[i][j] * h1b[j];
            z2b[i] = s;
            h2b[i] = relu(s);
        }
        double z3 = b3;
        for (int j = 0; j < h2; j++) z3 += w3[j] * h2b[j];
        return sigmoid(z3);
    }

    /** Inferencia (sin guardar intermedias). */
    public double predict(double[] x) {
        double[] z1b = new double[h1], h1b = new double[h1];
        double[] z2b = new double[h2], h2b = new double[h2];
        return forwardFull(x, z1b, h1b, z2b, h2b);
    }

    // ── Training ──────────────────────────────────────────────────────────────

    /**
     * Entrena la red con mini-batch SGD.
     *
     * @param X         lista de vectores de entrada
     * @param y         targets (0.0 o 1.0)
     * @param epochs    épocas de entrenamiento
     * @param lr        learning rate inicial
     * @param batchSize tamaño del mini-batch
     * @param l2        coeficiente de regularización L2
     * @param posWeight peso para la clase positiva (y=1) en BCE ponderada
     */
    public void train(List<double[]> X, List<Double> y,
                      int epochs, double lr, int batchSize,
                      double l2, double posWeight) {

        int n = X.size();
        Random rng = new Random(42);
        List<Integer> idx = new ArrayList<>(n);
        for (int i = 0; i < n; i++) idx.add(i);

        // Acumuladores de gradiente (reutilizados entre batches)
        double[][] gw1 = new double[h1][inputSize];
        double[]   gb1 = new double[h1];
        double[][] gw2 = new double[h2][h1];
        double[]   gb2 = new double[h2];
        double[]   gw3 = new double[h2];

        // Buffers temporales por muestra
        double[] z1 = new double[h1], ah1 = new double[h1];
        double[] z2 = new double[h2], ah2 = new double[h2];
        double[] dz2 = new double[h2];

        double currentLr = lr;

        for (int epoch = 0; epoch < epochs; epoch++) {
            Collections.shuffle(idx, rng);

            for (int start = 0; start < n; start += batchSize) {
                int end = Math.min(start + batchSize, n);
                int bs  = end - start;

                // Resetear gradientes
                for (int i = 0; i < h1; i++) {
                    gb1[i] = 0.0;
                    for (int j = 0; j < inputSize; j++) gw1[i][j] = 0.0;
                }
                for (int i = 0; i < h2; i++) {
                    gb2[i] = 0.0;
                    for (int j = 0; j < h1; j++) gw2[i][j] = 0.0;
                }
                for (int j = 0; j < h2; j++) gw3[j] = 0.0;
                double gb3 = 0.0;

                // Acumular gradientes del mini-batch
                for (int bi = start; bi < end; bi++) {
                    int     si   = idx.get(bi);
                    double[] xi  = X.get(si);
                    double   ti  = y.get(si);

                    double pred = forwardFull(xi, z1, ah1, z2, ah2);

                    // dL/dz3 para BCE ponderada:  -posWeight*t*(1-pred) + (1-t)*pred
                    double dz3 = (-posWeight * ti * (1.0 - pred) + (1.0 - ti) * pred) / bs;

                    // Capa de salida
                    gb3 += dz3;
                    for (int j = 0; j < h2; j++) gw3[j] += dz3 * ah2[j];

                    // Capa oculta 2
                    for (int i = 0; i < h2; i++) {
                        double dhi = dz3 * w3[i];
                        dz2[i] = dhi * reluD(z2[i]);
                        gb2[i] += dz2[i];
                        for (int j = 0; j < h1; j++) gw2[i][j] += dz2[i] * ah1[j];
                    }

                    // Capa oculta 1
                    for (int i = 0; i < h1; i++) {
                        double dhi = 0.0;
                        for (int k = 0; k < h2; k++) dhi += dz2[k] * w2[k][i];
                        double dz1i = dhi * reluD(z1[i]);
                        gb1[i] += dz1i;
                        for (int j = 0; j < inputSize; j++) gw1[i][j] += dz1i * xi[j];
                    }
                }

                // Actualizar pesos con SGD + L2
                for (int i = 0; i < h1; i++) {
                    b1[i] -= currentLr * gb1[i];
                    for (int j = 0; j < inputSize; j++)
                        w1[i][j] -= currentLr * (gw1[i][j] + l2 * w1[i][j]);
                }
                for (int i = 0; i < h2; i++) {
                    b2[i] -= currentLr * gb2[i];
                    for (int j = 0; j < h1; j++)
                        w2[i][j] -= currentLr * (gw2[i][j] + l2 * w2[i][j]);
                }
                for (int j = 0; j < h2; j++)
                    w3[j] -= currentLr * (gw3[j] + l2 * w3[j]);
                b3 -= currentLr * gb3;
            }

            // Decaimiento del lr cada 20 épocas
            if ((epoch + 1) % 20 == 0) currentLr *= 0.5;
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Evalúa el modelo sobre un conjunto de validación.
     * Los samples deben estar ordenados: numNumbers muestras consecutivas = un sorteo.
     *
     * @param valX       vectores de features de validación
     * @param valY       targets de validación
     * @param numNumbers rango de números del juego (e.g. 56)
     * @param topK       cuántos números se seleccionan por sorteo
     */
    public void validate(List<double[]> valX, List<Double> valY, int numNumbers, int topK) {
        int draws = valX.size() / numNumbers;
        if (draws == 0) return;

        int totalHits = 0;

        for (int d = 0; d < draws; d++) {
            int offset = d * numNumbers;
            double[] scores = new double[numNumbers];
            for (int n = 0; n < numNumbers; n++)
                scores[n] = predict(valX.get(offset + n));

            // Selección de los topK por score (selection sort parcial)
            int[] pickedIdx = new int[topK];
            boolean[] used = new boolean[numNumbers];
            for (int k = 0; k < topK; k++) {
                int best = -1;
                for (int n = 0; n < numNumbers; n++) {
                    if (!used[n] && (best == -1 || scores[n] > scores[best])) best = n;
                }
                pickedIdx[k] = best;
                used[best]   = true;
            }

            for (int k = 0; k < topK; k++) {
                if (valY.get(offset + pickedIdx[k]) > 0.5) totalHits++;
            }
        }

        this.validationHitRate = (double) totalHits / draws;
    }

    public double getValidationHitRate() { return validationHitRate; }
}
