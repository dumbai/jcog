package jcog.learn;

import jcog.Util;
import jcog.nn.ntm.NTM;
import jcog.nn.ntm.learn.IWeightUpdater;
import jcog.nn.ntm.learn.RMSPropWeightUpdater;
import jcog.random.XorShift128PlusRandom;

import java.util.Arrays;
import java.util.Random;

/** sequence learning process for RNN-like learners (ex: NTM) */
public abstract class RecurrentSequenceLearning {

    /** TODO generalize to RNN superclass */
    public final NTM model;

    private final IWeightUpdater learn;

    /** iteration */
    protected int i;

    protected final Random rand = new XorShift128PlusRandom();

    private boolean trace = false;

    protected static final int statisticsWindow = 16;
    protected double[] errors = new double[statisticsWindow];
    protected double[] times = new double[statisticsWindow];

    public RecurrentSequenceLearning(int vocabularySize, int seqLen, int heads) {

//        DoubleDiffableFunction activation =
//                SigmoidActivation.the;
//                //SoftPlusActivation.the; //<- doesnt work

        this.model = new NTM(vocabularySize, vocabularySize,
            vocabularySize,
            heads, seqLen);


        this.learn = new RMSPropWeightUpdater(model.weightCount(), 0.001);
    }

    /**
     * shift all items in an array down 1 index, leaving the last element ready for a new item
     */
    private static void pop(double[] x) {
        System.arraycopy(x, 0, x, 1, x.length - 1);
    }

    private static void pop(Object[] x) {
        System.arraycopy(x, 0, x, 1, x.length - 1);
    }

    private static void push(double[] x, double v) {
        x[0] = v;
    }

    private static void popPush(double[] x, double v) {
        pop(x);
        push(x, v);
    }


    public static double lossLog(double[][] _ideal, NTM[] _actual) {
        double totalLoss = 0;
        for (int t = 0; t < _ideal.length; t++) {

            double[] ideal = _ideal[t];
            double[] actual = _actual[t].output();

            double rowLoss = 0;
            for (int i = 0; i < ideal.length; i++) {

                double expected = ideal[i];
                double real = actual[i];

                rowLoss += (     expected  * (Math.log(real      ) / log2)) +
                           ((1 - expected) * (Math.log(1.0 - real) / log2));
            }
            totalLoss += rowLoss;
        }
        return -totalLoss;
    }

    private static final double log2 = Math.log(2);

    private static double lossAbsolute(double[][] knownOutput, NTM[] machines) {
        double totalLoss = 0.0;
//        int okt = knownOutput.length - ((knownOutput.length - 2) / 2);
        final int tMax = knownOutput.length;
        final int iMax = knownOutput[0].length;
        for (int t = 0; t < tMax; t++) {

//            if (t < okt) continue;

            double[] knownOutputT = knownOutput[t];
            double[] actual = machines[t].output();


            double rowLoss = 0;
            for (int i = 0; i < iMax; i++) {

                double expected = knownOutputT[i];
                double real = actual[i];
                double diff = Math.abs(expected - real);

                rowLoss += diff;
            }
            totalLoss += rowLoss;
        }
        return totalLoss/(iMax*tMax);
    }

    /**
     * train the next sequence
     */
    public double run() {

        TrainingSequence s = next();
        double[][] input = s.input();
        double[][] ideal = s.ideal();

        long tBefore = System.nanoTime();

        NTM[] output = model.put(input, ideal, learn);

        long trainTimeNS = System.nanoTime() - tBefore;

        double loss = //lossLog(ideal, output);
                lossAbsolute(ideal, output);

        popPush(errors, loss);
        popPush(times, trainTimeNS);

        trained(i++, s, output, trainTimeNS, loss);

        return loss;
    }

    protected abstract TrainingSequence next();

    public void trained(int sequenceNum, TrainingSequence sequence, NTM[] output, long trainTimeNS, double avgError) {
        if (trace) {
//                double[][] ideal = sequence.ideal();
//                int slen = ideal.length;
//        if (printSequences) {
//            for (int t = 0; t < slen; t++) {
//                double[] actual = output[t].getOutput();
//                System.out.println("\t" + sequenceNum + '#' + t + ":\t" + toNiceString(ideal[t]) + " =?= " + toNiceString(actual));
//            }
//        }


            if ((sequenceNum + 1) % RecurrentSequenceLearning.statisticsWindow == 0) {
                System.out.format("@ %d :       avgErr: %f       time(s): %f", i,
                        Util.mean(errors), Util.mean(times) / 1.0e9);
                System.out.println();
            }
        }
    }

    public void clear() {
        Arrays.fill(errors, 1.0);
    }

    public static record TrainingSequence(double[][] input, double[][] ideal) {
    }
}