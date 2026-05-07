package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.ClusterAnalysis;
import com.lottery.api.domain.model.DrawCluster;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.GetClusterAnalysisUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GetClusterAnalysisService implements GetClusterAnalysisUseCase {

    private static final int MAX_ITERATIONS = 100;
    private static final long SEED = 42L;

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-cluster", key = "#type.name() + '-' + #k")
    @Transactional(readOnly = true)
    public ClusterAnalysis getClusterAnalysis(LotteryType type, int k) {
        List<LotteryDraw> draws = repositoryPort.findByType(type);
        List<LotteryDraw> valid = draws.stream()
                .filter(d -> d.getNumbers() != null && !d.getNumbers().isEmpty())
                .toList();

        if (valid.size() < k) {
            return ClusterAnalysis.builder()
                    .lotteryType(type).totalDraws(valid.size()).k(k)
                    .clusters(List.of())
                    .interpretation("Datos insuficientes para clustering con k=" + k)
                    .build();
        }

        int mid = (type.getMinNumber() + type.getMaxNumber()) / 2;
        double[][] features = valid.stream().map(d -> toFeature(d, mid)).toArray(double[][]::new);

        // Normalize features
        double[] mins = new double[4], maxs = new double[4];
        Arrays.fill(mins, Double.MAX_VALUE);
        Arrays.fill(maxs, Double.MIN_VALUE);
        for (double[] f : features) {
            for (int i = 0; i < 4; i++) {
                if (f[i] < mins[i]) mins[i] = f[i];
                if (f[i] > maxs[i]) maxs[i] = f[i];
            }
        }
        double[][] normalized = new double[features.length][4];
        for (int r = 0; r < features.length; r++) {
            for (int c = 0; c < 4; c++) {
                double range = maxs[c] - mins[c];
                normalized[r][c] = range > 0 ? (features[r][c] - mins[c]) / range : 0;
            }
        }

        // K-means
        int[] assignments = kMeans(normalized, k);

        // Build clusters
        List<DrawCluster> clusters = new ArrayList<>();
        for (int cid = 0; cid < k; cid++) {
            List<double[]> clusterFeatures = new ArrayList<>();
            Map<Integer, Long> numFreq = new HashMap<>();
            for (int r = 0; r < valid.size(); r++) {
                if (assignments[r] != cid) continue;
                clusterFeatures.add(features[r]);
                for (int n : valid.get(r).getNumbers()) numFreq.merge(n, 1L, (a, b) -> a + b);
            }
            if (clusterFeatures.isEmpty()) continue;

            double avgSum     = clusterFeatures.stream().mapToDouble(f -> f[0]).average().orElse(0);
            double avgOdd     = clusterFeatures.stream().mapToDouble(f -> f[1]).average().orElse(0);
            double avgSpread  = clusterFeatures.stream().mapToDouble(f -> f[2]).average().orElse(0);
            int count = clusterFeatures.size();

            List<Integer> topNums = numFreq.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(10).map(Map.Entry::getKey).toList();

            clusters.add(DrawCluster.builder()
                    .clusterId(cid)
                    .drawCount(count)
                    .centroidSum(Math.round(avgSum * 10.0) / 10.0)
                    .centroidOddCount(Math.round(avgOdd * 100.0) / 100.0)
                    .centroidSpread(Math.round(avgSpread * 10.0) / 10.0)
                    .mostCommonNumbers(topNums)
                    .pctOfTotal(Math.round((double) count / valid.size() * 10000.0) / 100.0)
                    .build());
        }
        clusters.sort((a, b) -> Integer.compare(b.getDrawCount(), a.getDrawCount()));

        return ClusterAnalysis.builder()
                .lotteryType(type)
                .totalDraws(valid.size())
                .k(k)
                .clusters(clusters)
                .interpretation(buildInterpretation(clusters))
                .build();
    }

    private double[] toFeature(LotteryDraw draw, int mid) {
        List<Integer> nums = draw.getNumbers();
        int sum    = nums.stream().mapToInt(Integer::intValue).sum();
        int odd    = (int) nums.stream().filter(n -> n % 2 != 0).count();
        int spread = Collections.max(nums) - Collections.min(nums);
        int high   = (int) nums.stream().filter(n -> n > mid).count();
        return new double[]{sum, odd, spread, high};
    }

    private int[] kMeans(double[][] data, int k) {
        int n = data.length;
        int dim = data[0].length;
        Random rng = new Random(SEED);

        // Init centroids from random data points
        double[][] centroids = new double[k][dim];
        List<Integer> chosen = new ArrayList<>();
        while (chosen.size() < k) {
            int idx = rng.nextInt(n);
            if (!chosen.contains(idx)) { chosen.add(idx); centroids[chosen.size() - 1] = data[idx].clone(); }
        }

        int[] assignments = new int[n];
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            boolean changed = false;
            for (int r = 0; r < n; r++) {
                int best = 0;
                double bestDist = Double.MAX_VALUE;
                for (int c = 0; c < k; c++) {
                    double dist = euclidean(data[r], centroids[c]);
                    if (dist < bestDist) { bestDist = dist; best = c; }
                }
                if (assignments[r] != best) { assignments[r] = best; changed = true; }
            }
            if (!changed) break;
            // Recompute centroids
            double[][] sums = new double[k][dim];
            int[] counts = new int[k];
            for (int r = 0; r < n; r++) {
                for (int d = 0; d < dim; d++) sums[assignments[r]][d] += data[r][d];
                counts[assignments[r]]++;
            }
            for (int c = 0; c < k; c++) {
                if (counts[c] == 0) continue;
                for (int d = 0; d < dim; d++) centroids[c][d] = sums[c][d] / counts[c];
            }
        }
        return assignments;
    }

    private double euclidean(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) s += (a[i] - b[i]) * (a[i] - b[i]);
        return Math.sqrt(s);
    }

    private String buildInterpretation(List<DrawCluster> clusters) {
        if (clusters.isEmpty()) return "Sin clusters disponibles.";
        DrawCluster largest = clusters.get(0);
        return String.format(
                "El cluster dominante (%d%% de sorteos) tiene suma promedio %.1f y %.2f números impares. " +
                "Los %d números más frecuentes de ese cluster son los mejores candidatos estadísticos.",
                (int) largest.getPctOfTotal(), largest.getCentroidSum(),
                largest.getCentroidOddCount(), Math.min(6, largest.getMostCommonNumbers().size()));
    }
}
