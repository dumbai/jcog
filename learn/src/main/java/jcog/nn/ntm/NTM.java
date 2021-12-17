package jcog.nn.ntm;

import jcog.activation.DiffableFunction;
import jcog.activation.SigmoidActivation;
import jcog.nn.ntm.control.FeedForwardController;
import jcog.nn.ntm.learn.IWeightUpdater;
import jcog.nn.ntm.learn.RandomWeightInitializer;
import jcog.nn.ntm.memory.MemoryState;
import jcog.nn.ntm.memory.NTMMemory;
import jcog.nn.ntm.memory.address.Head;
import jcog.random.XorShift128PlusRandom;

import java.util.concurrent.ThreadLocalRandom;

/** https://www.niklasschmidinger.com/posts/2019-12-25-neural-turing-machines/ */
public class NTM {

    public final FeedForwardController control;
    public final NTMMemory memory;

    private MemoryState prev, now;

    /** previously input */
    public double[] input;

    private NTM(NTM old) {
        control = old.control.clone();
        memory = old.memory;
        now = old.now;
        prev = old.prev;
        input = null;
    }

    public NTM(int inputSize, int outputSize, int controllerSize, int headCount, int memoryHeight) {
        this(inputSize, outputSize, controllerSize, headCount, inputSize, memoryHeight );
    }
    public NTM(int inputSize, int outputSize, int controllerSize, int headCount, int memoryWidth, int memoryHeight) {
        this(inputSize, outputSize, controllerSize, headCount, memoryWidth, memoryHeight,
                new RandomWeightInitializer(new XorShift128PlusRandom()), SigmoidActivation.the);
    }

    public NTM(int inputSize, int outputSize, int controllerSize, int headCount, int memoryWidth, int memoryHeight, IWeightUpdater initializer, DiffableFunction act) {
        memory = new NTMMemory(memoryHeight, memoryWidth, headCount, act);
        control = new FeedForwardController(controllerSize, inputSize, outputSize, headCount, memoryWidth, act);
        now = prev = null;
        input = null;
        update(initializer);
    }




    public final void update(IWeightUpdater u) {
        memory.update(u);
        control.updateWeights(u);
    }

    public void forward(double[] input) {
        this.input = input;
        prev = now;

        //HACK patch NaN's in input
        for (int i = 0, inputLength = input.length; i < inputLength; i++) {
            double x = input[i];
            if (!Double.isFinite(x))
                input[i] = ThreadLocalRandom.current().nextFloat();
        }

        control.forward(input, prev.read);
        now = prev.forward(control.outputHeads());
    }

    public double[] output() {
        return control.output();
    }

    public void clear() {
        now = new MemoryState(memory);
        IWeightUpdater.GradientReset.apply(this);
    }

    private void backward(double[] knownOutput, boolean isDelta) {
        now.backward();
        control.backward(knownOutput, input, prev.read, isDelta);
    }

    public final int heads() {
        return memory.heads();
    }
    public final int inputs() {
        return control.inputSize();
    }

    public final int outputs() {
        return control.outputSize();
    }

    public final int w() {
        return memory.width;
    }

    public final int h() {
        return memory.height;
    }

    public final int c() {
        return control.hiddens();
    }

    public NTM[] put(double[] x, double[] y, int iters, IWeightUpdater learn) {
        return put(new double[][] {x} , new double[][] {y}, false, iters, learn);
    }

    /** for sequences */
    public NTM[] put(double[][] x, double[][] y, IWeightUpdater learn) {
        return put(x, y, false, x.length, learn);
    }

    public NTM[] put(double[][] x, double[][] y, boolean yIsDelta, int n, IWeightUpdater learn) {
        int yl = y.length;
        NTM[] m = forward(x, n);

        for (int i = n - 1; i >= 0; i--)
            m[i].backward(y[i%yl], yIsDelta);

        now.backwardFinal();

        learn.apply(this);

        return m;
    }

    public double[] get(double[][] x, int n) {
        NTM[] y = forward(x, n);
        return y[y.length-1].output();
    }

    private NTM[] forward(double[][] x, int n) {
        this.clear();
        int xl = x.length;

        NTM[] m = new NTM[n];

        for (int i = 0; i < n; i++) {
            double[] xi = x[i % xl];
            (m[i] = new NTM(i == 0 ? this : m[i - 1])).forward(xi);
        }
        return m;
    }

    public int weightCount() {
        int heads = heads();
        int w = w();
        int h = h();
        int c = c();
        return
            (heads * h) +
            (h * w) +
            (c * heads * w) +
            (c * inputs()) +
            (c) +
            (outputs() * (c + 1)) +
            (heads * Head.unitSize(w) * (c + 1));
    }

}