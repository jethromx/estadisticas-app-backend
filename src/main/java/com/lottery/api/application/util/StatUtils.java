package com.lottery.api.application.util;

public final class StatUtils {

    private StatUtils() {}

    private static final double[] LANCZOS_C = {
        76.18009172947146, -86.50532032941677, 24.01409824083091,
        -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5
    };

    public static double lnGamma(double x) {
        double y = x, tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (double c : LANCZOS_C) { ser += c / ++y; }
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

    private static double gammaSeriesP(double a, double x) {
        double ap = a, sum = 1.0 / a, del = sum;
        for (int i = 0; i < 300; i++) {
            del *= x / ++ap;
            sum += del;
            if (Math.abs(del) < Math.abs(sum) * 1e-12) break;
        }
        return sum * Math.exp(-x + a * Math.log(x) - lnGamma(a));
    }

    private static double gammaCFQ(double a, double x) {
        double b = x + 1 - a, c = 1.0 / 1e-300, d = 1.0 / b, h = d;
        for (int i = 1; i <= 300; i++) {
            double an = -i * (i - a);
            b += 2;
            d = an * d + b; if (Math.abs(d) < 1e-300) d = 1e-300;
            c = b + an / c; if (Math.abs(c) < 1e-300) c = 1e-300;
            d = 1.0 / d;
            double del = d * c;
            h *= del;
            if (Math.abs(del - 1.0) < 1e-12) break;
        }
        return Math.exp(-x + a * Math.log(x) - lnGamma(a)) * h;
    }

    /** Chi-square survival function: P(X > chi2) for X ~ chi2(df). */
    public static double chiSquarePValue(double chi2, int df) {
        if (chi2 <= 0) return 1.0;
        double a = df / 2.0, x = chi2 / 2.0;
        return x < a + 1 ? 1.0 - gammaSeriesP(a, x) : gammaCFQ(a, x);
    }
}
