package jcog.pri;

import jcog.Util;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * prioritized reference
 */
public interface PriReference<X> extends Supplier<X>, Prioritizable {

    static float[] histogram(Iterable<? extends Prioritized> pp, float[] x) {
        int bins = x.length;
        double[] total = {0};

        for (Prioritized y : pp) {
            if (y == null)
                continue;
            float p = y.priElseZero();
            if (p > 1f) p = 1f;
            int b = Util.bin(p, bins);
            x[b]++;
            total[0]++;
        }

        double t = total[0];
        if (t > 0) {
            for (int i = 0; i < bins; i++)
                x[i] /= t;
        }
        return x;
    }

    /**
     * double[histogramID][bin]
     */
    static <X, Y> double[][] histogram( Iterable<PriReference<Y>> pp,  BiConsumer<PriReference<Y>, double[][]> each,  double[][] d) {

        for (PriReference<Y> y : pp) {
            each.accept(y, d);
        }

        for (double[] e : d) {
            double total = Arrays.stream(e).sum();
            if (total > 0) {
                for (int i = 0, eLength = e.length; i < eLength; i++) {
                    double f = e[i];
                    e[i] /= total;
                }
            }
        }

        return d;
    }

    static boolean equals(PriReference x, Object y) {
        return (x == y) || (y instanceof PriReference && x.get().equals(((PriReference)y).get()));
    }

//    @Override
//    default float asFloat() {
//        return pri();
//    }
}