package jcog.learn;

import jcog.Fuzzy;
import jcog.Util;
import jcog.predict.LivePredictor;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.random.XorShift128PlusRandom;
import jcog.signal.anomaly.ewma.Ewma;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static jcog.Str.n4;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by me on 2/17/17.
 */
@Disabled
class NTMTest {

    @Test
    void xor2() {
        int cycles = 1;
        LivePredictor.NTMPredictor p = new LivePredictor.NTMPredictor(2, 1, cycles);
        p.alpha = 0.007;
        Random rng = new XoRoShiRo128PlusRandom(1);

        //XOR
        Ewma d = new Ewma().period(10);
        for (int i = 1; i < 4000; i++) {
            final float eps =
                    1;
            //0.5f;
            //0.25f;
            double[] x = {Util.round(rng.nextFloat(), eps), Util.round(rng.nextFloat(), eps)};
            double[] y = {Util.round(1- Fuzzy.xnr(x[0], x[1]), eps)};

            var Z = p.put(x, y, 1);

            double diff = Math.abs(y[0] - Z[0]);

            var diffMean = d.acceptAndGetMean(diff);

            System.out.println(n4(x) + " ->\t" + n4(Z) + "\t" + n4(diff) + "\t" + n4(diffMean));
            if (i%100==0)
                System.out.println("err: " + n4(diffMean));

        }
        assertTrue(d.mean() < 0.05f);
    }

    private static void testSimpleSequence(int v, int s, int iter, IntIntToObjectFunction<RecurrentSequenceLearning.TrainingSequence> seq) {

        RecurrentSequenceLearning l = new RecurrentSequenceLearning(
                v, s, v) {
            @Override
            protected RecurrentSequenceLearning.TrainingSequence next() {
                return seq.value(v, s);
            }
        };

        l.clear();

        double startError = l.run();
        assertTrue(startError > 0.1f);

        for (int i = 0; i < iter; i++)
            l.run();

        double err = l.run();
        assertTrue(err < 0.05f, ()->"err = " + err);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4})
    void testSimpleSequenceXOR(int vectorSize) {
        Random rng = new XorShift128PlusRandom(1);

        testSimpleSequence(vectorSize, vectorSize * 3, 200 * vectorSize,
            (v, s) -> {
                double[][] i = new double[s][v];
                double[][] o = new double[s][v];

                int j = Math.abs(rng.nextInt()) % Math.max(1, v / 2) + v / 2;

                for (int n = 0; n < s; n++) {
                    int index = ((j) ^ (n)) % v;


                    if (n < v / 2)
                        i[n][n] = 1;
                    i[n][j] = 1;
                    o[n][index] = 1;
                }

                return new RecurrentSequenceLearning.TrainingSequence(i, o);
            }
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 6, 9})
    void testSimpleSequenceSaw(int vectorSize) {
        Random rng = new XorShift128PlusRandom(1);

        testSimpleSequence(vectorSize, vectorSize * 3, 100 * vectorSize,
            (v, s) -> {
                int seed = Math.abs(rng.nextInt());
                int length = s * 2 + 2;

                double[][] input = new double[length][vectorSize];
                double[][] output = new double[length][vectorSize];

                boolean direction = seed % 2 == 0;

                int j = seed / 2;
                for (int i = 0; i < length; i++) {
                    int index = j % vectorSize;

                    input[i][index] = 1;
                    int reflected = (vectorSize - 1) - index;
                    output[i][reflected] = 1;

                    if (direction)
                        j++;
                    else
                        j--;

                }


                return new RecurrentSequenceLearning.TrainingSequence(input, output);
            }
        );
    }


//    public enum SequenceGenerator
//    {
////        public static TrainingSequence generateSequenceWTF(int length, int inputVectorSize) {
////            double[][] data = new double[length][inputVectorSize];
////            for (int i = 0;i < length;i++)
////            {
////
////                for (int j = 0;j < inputVectorSize;j++)
////                {
////                    data[i][j] = rng.nextInt(2);
////                }
////            }
////            int sequenceLength = (length * 2) + 2;
////            int vectorSize = inputVectorSize - 2;
////            double[][] input = new double[sequenceLength][inputVectorSize];
////
////            for (int i = 0;i < sequenceLength;i++)
////            {
////
////                if (i == 0) {
////
////                    input[0][vectorSize] = 1.0;
////                }
////                else if (i <= length) {
////
////                    System.arraycopy(data[i - 1], 0, input[i], 0, vectorSize);
////                }
////                else if (i == (length + 1)) {
////
////                    input[i][vectorSize + 1] = 1.0;
////                }
////
////            }
////            double[][] output = new double[sequenceLength][vectorSize];
////            for (int i = 0;i < sequenceLength;i++)
////            {
////
////                if (i >= (length + 2))
////                {
////                    System.arraycopy(data[i - (length + 2)], 0, output[i], 0, vectorSize);
////                }
////
////            }
////            return new TrainingSequence(input, output);
////        }
//
//    }

}