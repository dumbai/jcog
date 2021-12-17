//package jcog.lstm;
//
//
//import jcog.Util;
//
//import java.util.Random;
//import java.util.function.Consumer;
//
//public abstract class AbstractTraining {
//
//    public AbstractTraining(Random random, int inputs, int outputs) {
//        this.random = random;
//        this.inputs = inputs;
//        this.outputs = outputs;
//    }
//
//    @Deprecated public double scoreSupervised(LSTM agent, float learningRate)  {
//
//        double[] err = {0};
//
//        this.interact(inter -> {
//            if (inter.forget > 0)
//                agent.forget(inter.forget);
//
//            if (inter.expected == null) {
//                agent.get(inter.actual);
//            } else {
//                double[] predicted;
//                predicted = validation_mode ?
//                        agent.get(inter.actual) :
//                        agent.put(inter.actual, inter.expected, learningRate);
//
//                err[0] += Util.sum(predicted.length, (int i)->Math.abs(predicted[i] - inter.expected[i]));
//            }
//        });
//
//        return err[0];
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    protected final Random random;
//    protected int batches;
//    protected boolean validation_mode;
//
//    @Deprecated protected abstract void interact(Consumer<ExpectedVsActual> each);
//
//    public final int inputs;
//    public final int outputs;
//}