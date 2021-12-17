package jcog.learn;

import com.google.common.math.PairedStatsAccumulator;
import jcog.activation.SigmoidActivation;
import jcog.lstm.LSTM;
import jcog.nn.MLP;
import jcog.predict.LivePredictor;
import jcog.predict.Predictor;
import jcog.random.XoRoShiRo128PlusRandom;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import org.eclipse.collections.api.block.function.primitive.LongToFloatFunction;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import static jcog.Util.assertFinite;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivePredictorTest {

    private static void assertCorrect(IntToFloatFunction ii, IntToFloatFunction oo, IntIntToObjectFunction<Predictor> model, int iHistory, int totalTime, float maxMeanError) {
        int m = 0;
        LongToFloatFunction[] in = {(w) -> ii.valueOf((int) w), (w) -> oo.valueOf((int) w - 1)};
        LongToFloatFunction[] out = {(w) -> oo.valueOf((int) w)};

        LivePredictor.DenseFramer ih = new LivePredictor.DenseFramer(in, iHistory, ()->1, out);
        LivePredictor l = new LivePredictor(ih, model);
        l.learningRate = 0.01f;

        int numSnapshots = 16;
        assert (totalTime > numSnapshots * 2);
        int errorWindow = totalTime / numSnapshots;

        PairedStatsAccumulator errortime = new PairedStatsAccumulator();
        DescriptiveStatistics error = new DescriptiveStatistics(errorWindow);

        for (int t = 0; t < totalTime; t++, m++) {

            double[] prediction = l.next(m);

            double predicted = prediction[0];
            assertFinite(predicted);
            double actual = oo.valueOf(m + 1);
            assertFinite(actual);

            double e = Math.abs(actual - predicted);
            error.addValue(e);

            System.out.println(e);
            if (t % errorWindow == errorWindow - 1) {
                errortime.add(t, error.getMean());
            }

        }

        double eMean = error.getMean();

        System.out.println(model);
        System.out.println("\tmean error: " + eMean);

        double learningSlope = errortime.leastSquaresFit().slope();

        System.out.println("\terror rate: " + learningSlope);
        assertTrue(learningSlope < -1E-8);

        assertTrue(eMean < maxMeanError, () -> "mean error: " + eMean);
    }

    @Test
    void test1() {
        IntToFloatFunction ii = x -> (float) (0.5f + 0.5f * Math.sin(x / 4f));
        IntToFloatFunction oo = x -> (float) (0.5f + 0.5f * Math.cos(x / 4f));
        IntIntToObjectFunction<Predictor> model = (i, o) -> {
            LSTM l = new LSTM(i, o, 4);
            l.clear(new XoRoShiRo128PlusRandom(1));
            l.alpha(40);
            return l;
        };

        int iHistory = 1;
        int totalTime = 64 * 2;
        float maxMeanError = 0.2f;
        assertCorrect(ii, oo, model, iHistory, totalTime, maxMeanError);
    }

    @Test
    void test21_LSTM() {
        IntToFloatFunction ii = x -> (float) (0.5f + 0.5f * Math.sin(x / 4f));
        IntToFloatFunction oo = x -> (float) (0.5f + 0.5f * Math.cos(x / 8f));
        IntIntToObjectFunction<Predictor> model = (i, o) -> {
            final LSTM l = new LSTM(i, o, 4);
            l.alpha(20);
            l.clear(new XoRoShiRo128PlusRandom(1));
            return l;
        };
        int iHistory = 1;
        int totalTime = 64 * 2;
        float maxMeanError = 0.1f;


        assertCorrect(ii, oo, model, iHistory, totalTime, maxMeanError);
    }

    @Test
    void test12_MLP() {

        IntToFloatFunction ii = x -> (1 + (float) Math.sin(x / 6f)) / 2;
        IntToFloatFunction oo = x -> (1 + (float) Math.cos(x / 9f)) / 2;
        IntIntToObjectFunction<Predictor> model = (i, o) ->
        {
            MLP m = new MLP(i,
//                new MLP.FC(i+o, TanhActivation.the),
                    //new MLP.FC((i+o), SigmoidActivation.the),
                    //new MLP.FC(i, SigmoidActivation.the),
                    new MLP.Dense(i + o, SigmoidActivation.the),
                    new MLP.Dense(o, SigmoidActivation.the)
            );
            m.clear(new XoRoShiRo128PlusRandom(1));
//            m.momentum = 0f;
            return m;
        };
        int iHistory = 4;
        int totalTime = 1024 * 64;
        float maxMeanError = 0.13f;

        assertCorrect(ii, oo, model, iHistory, totalTime, maxMeanError);
    }


//    static double[] d(FloatSupplier[] f) {
//        double[] d = new double[f.length];
//        int i = 0;
//        for (FloatSupplier g : f)
//            d[i++] = g.asFloat();
//        return d;
//    }

}