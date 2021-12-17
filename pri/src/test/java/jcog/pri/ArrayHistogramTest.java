package jcog.pri;

import com.google.common.collect.Streams;
import jcog.random.XoRoShiRo128PlusRandom;
import org.hipparchus.stat.Frequency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayHistogramTest {


//    @ValueSource(ints = {4, 8, 15, 16, 32})
//    @ParameterizedTest void flat(int bins) {
//        BiConsumer<DistributionApproximator, float[]> writer = (w, pdf) -> {
//            for (int i = 0; i < bins*64; i++)
//                w.accept((float) (0.5f + Math.random() * 0.05f));
//        };
//        Frequency f = assertSampling(bins, 200 * bins, writer);
//        System.out.println(f);
//        assertEquals(0, variance(f),  0.1f, "flat");
//    }

    @ValueSource(ints = {4, 8, 15, 16, 32})
    @ParameterizedTest void topHeavy(int bins) {
        double exp = 2;
        Random rng = new XoRoShiRo128PlusRandom(1);
        BiConsumer<DistributionApproximator, float[]> writer = (w, pdf) -> {
            for (int i = 0; i < bins*64; i++) {
                w.accept(
                     (float) Math.pow(rng.nextFloat(), exp)
                );
            }
        };
        Frequency f = assertSampling(bins, 200 * bins, writer);
        //System.out.println(f);
        assertTrue(f.getPct(0) > f.getPct(bins-1));
    }

    @ValueSource(ints = { 3,4, 8, 15, 16, 32})
    @ParameterizedTest void linear(int bins) {
        BiConsumer<DistributionApproximator, float[]> writer = (w, pdf) -> {
            int repeat = 16;
            for (int k = 0; k < repeat; k++) {
                for (int i = 0; i < bins; i++) {
                    w.accept(((float) i) / (bins - 1));
                }
            }
        };
        int iters = bins*1000;
        Frequency f = assertSampling(bins, iters * bins, writer);
        //System.out.println(f);
        for (int i = 1; i < bins; i++)
            assertTrue(f.getCount(i-1) - f.getCount(i) > -(iters/4), "monotonically decreasing");

    }

    private static double variance(Frequency<?> f) {
        int bins = f.getUniqueCount();
        long iters = f.getSumFreq();
        double avg = ((double)iters)/bins;
        return Streams.stream(f.entrySetIterator()).map(Map.Entry::getValue).mapToDouble(x -> Math.abs(avg - x)).average().getAsDouble() / avg;
    }

    private static Frequency assertSampling(int bins, int iters, BiConsumer<DistributionApproximator, /*depr*/float[]> writer) {
        DistributionApproximator a = new ArrayHistogram();
        a.start(bins);
        writer.accept(a, null);
        a.commit(0, bins, bins);

//        System.out.println(a);
//        System.out.println(a.chart());

        Random rng = new XoRoShiRo128PlusRandom(1);

        Frequency f = new Frequency();
        for (int i = 0; i < iters; i++)
            f.addValue( a.sampleInt(rng) );
        //System.out.println(f);
        assertEquals(bins, f.getUniqueCount());
        return f;
    }

}