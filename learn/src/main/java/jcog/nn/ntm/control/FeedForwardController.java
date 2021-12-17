package jcog.nn.ntm.control;

import jcog.Util;
import jcog.activation.DiffableFunction;
import jcog.nn.ntm.learn.IWeightUpdater;
import jcog.nn.ntm.memory.ReadData;
import jcog.nn.ntm.memory.address.Head;

import java.util.function.IntToDoubleFunction;

public class FeedForwardController {
    public final NTMHiddenLayer hidden;
    public final OutputLayer output;

    public FeedForwardController(int controllerSize, int inputSize, int outputSize, int headCount, int memoryUnitSizeM, DiffableFunction act) {
        this(new NTMHiddenLayer(controllerSize, inputSize, headCount, memoryUnitSizeM, act),
                new OutputLayer(outputSize, controllerSize, headCount, memoryUnitSizeM));
    }

    private FeedForwardController(NTMHiddenLayer hidden, OutputLayer output) {
        this.hidden = hidden;
        this.output = output;
    }

    public int inputSize() {
        return hidden.inputs();
    }
    public int hiddens() {
        return hidden.outputs();
    }

    public int outputSize() {
        return output.size();
    }

    public double[] output() {
        return output.output();
    }


    @Override
    public FeedForwardController clone() {
        return new FeedForwardController(hidden.clone(), output.clone());
    }

    public void forward(double[] input, ReadData[] readDatas) {
        hidden.forward(input, readDatas);
        output.forward(hidden);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        output.update(weightUpdater);
        hidden.update(weightUpdater);
    }

    public final void backward(double[] knownOutput, double[] input, ReadData[] reads, boolean isDelta) {
        output.backward(knownOutput, hidden, isDelta);
        hidden.backward(input, reads);
    }

    public final Head[] outputHeads() {
        return output.heads;
    }

    private static final class OutputLayer {
        public final Head[] heads;
        private final int outs;
        private final int controllerSize;
        private final int memoryWidth;
        private final int headUnitSize;
        private final Unit[][] hiddenToOutputWeights;
        private final Unit[][][] hiddenToHeadWeights;
        public UVector output;

        OutputLayer(int outs, int ins, int headCount, int memoryUnitSizeM) {
            this.outs = outs;
            this.controllerSize = ins;
            memoryWidth = memoryUnitSizeM;
            headUnitSize = Head.unitSize(memoryUnitSizeM);
            hiddenToOutputWeights = UnitFactory.tensor2(outs, ins + 1);
            hiddenToHeadWeights = UnitFactory.tensor3(headCount, headUnitSize, ins + 1);
            heads = new Head[headCount];
            output = null;
        }

        private OutputLayer(Unit[][] hiddenToOutputWeights, Unit[][][] hiddenToHeadWeights, UVector output, Head[] heads, int outputSize, int controllerSize, int memoryWidth, int headUnitSize) {
            this.hiddenToOutputWeights = hiddenToOutputWeights;
            this.hiddenToHeadWeights = hiddenToHeadWeights;
            this.heads = heads;
            this.controllerSize = controllerSize;
            outs = outputSize;
            this.output = output;
            this.memoryWidth = memoryWidth;
            this.headUnitSize = headUnitSize;
        }

        public void forward(NTMHiddenLayer hiddenLayer) {

            double[] hiddenLayerNeurons = hiddenLayer.neurons.value;

            double[] out = output.value;

            DiffableFunction act = hiddenLayer.activation;

            for (int i = 0; i < outs; i++) {

                Unit[] weights = hiddenToOutputWeights[i];

                int n = controllerSize;
                out[i] = act.valueOf(weights[n].value +
                        Util.sum(n, (IntToDoubleFunction) j ->
                                weights[j].value * hiddenLayerNeurons[j]));
            }

            for (int i = 0; i < heads.length; i++) {


                Unit[][] headsWeights = hiddenToHeadWeights[i];
                Head head = heads[i];

                for (int j = 0; j < headsWeights.length; j++) {
                    Unit[] headWeights = headsWeights[j];

                    int s = controllerSize;
                    double sum = 0;
                    for (int k = 0; k < s; k++)
                        sum = jcog.Util.fma(headWeights[k].value, hiddenLayerNeurons[k], sum);

                    head.get(j).value += sum + headWeights[s].value;
                }
            }
        }

        @Override
        public OutputLayer clone() {
            Head[] heads = Head.vector(this.heads.length, i -> memoryWidth);
            return new OutputLayer(hiddenToOutputWeights, hiddenToHeadWeights,
                    new UVector(outs), heads, outs, controllerSize, memoryWidth, headUnitSize);
        }

        public void backward(double[] knownOutput, NTMHiddenLayer hiddenLayer, boolean inputIsDelta) {

            Head[] heads = this.heads;

            if (inputIsDelta) {
                output.setDeltaDirect(knownOutput);
            } else {
                output.setDelta(knownOutput);
            }

            double[] hiddenGrad = hiddenLayer.neurons.grad;
            double[] outGrad = output.grad;

            int cs = this.controllerSize;

            Unit[][] hiddenToOutputLayerWeights = this.hiddenToOutputWeights;

            int os = outs;
            for (int j = 0; j < os; j++) {
                double unitGrad = outGrad[j];
                Unit[] weights = hiddenToOutputLayerWeights[j];
                for (int i = 0; i < cs; i++)
                    hiddenGrad[i] = jcog.Util.fma(weights[i].value, unitGrad, hiddenGrad[i]);
            }
            int hl = heads.length;
            for (int j = 0; j < hl; j++) {

                Head head = heads[j];
                Unit[][] weights = hiddenToHeadWeights[j];
                for (int k = 0; k < headUnitSize; k++) {
                    double unitGrad = head.get(k).grad;
                    Unit[] weightsK = weights[k];
                    for (int i = 0; i < cs; i++)
                        hiddenGrad[i] = jcog.Util.fma(weightsK[i].value, unitGrad, hiddenGrad[i]);
                }
            }

            double[] hiddenValue = hiddenLayer.neurons.value;

            int n = this.controllerSize;
            for (int i = 0; i < os; i++) {

                Unit[] wyh1I = this.hiddenToOutputWeights[i];
                double yGrad = outGrad[i];
                for (int j = 0; j < cs; j++)
                    wyh1I[j].grad = jcog.Util.fma(hiddenValue[j], yGrad, wyh1I[j].grad);

                wyh1I[n].grad += yGrad;
            }

            for (int i = 0; i < hl; i++) {
                Head head = heads[i];
                Unit[][] units = hiddenToHeadWeights[i];
                for (int j = 0; j < headUnitSize; j++) {
                    Unit[] unitJ = units[j];

                    double headUnitGrad = head.get(j).grad;
                    for (int k = 0; k < n; k++)
                        unitJ[k].grad += headUnitGrad * hiddenValue[k];

                    unitJ[n].grad += headUnitGrad;
                }
            }
        }

    //    public void updateWeights(Consumer<Unit> updateAction) {
    //        Consumer<Unit[][]> tensor2UpdateAction = Unit.tensor2UpdateAction(updateAction);
    //        Consumer<Unit[][][]> tensor3UpdateAction = Unit.tensor3UpdateAction(updateAction);
    //        tensor2UpdateAction.accept(_hiddenToOutputLayerWeights);
    //        tensor3UpdateAction.accept(_hiddenToHeadsWeights);
    //    }

        public void update(IWeightUpdater u) {
            u.update(hiddenToOutputWeights);
            u.update(hiddenToHeadWeights);
        }

        public double[] output() {
            return output.value;
        }

        public int size() {
            return outs;
        }
    }

    public static class NTMHiddenLayer {
        public final DiffableFunction activation;
        public final int inputs;
        public final int heads;
        final int memoryUnitSizeM;


        final UVector hiddenThresholds;


        public final UMatrix inputToHiddenWeights;


        final Unit[][][] readToHiddenWeights;


        public final UVector neurons;

        NTMHiddenLayer(int outs, int ins, int headCount, int memoryUnitSizeM, DiffableFunction activationFunction) {
            inputs = ins;
            heads = headCount;
            this.memoryUnitSizeM = memoryUnitSizeM;
            this.neurons = new UVector(outs);
            activation = activationFunction;
            readToHiddenWeights = UnitFactory.tensor3(outs, headCount, memoryUnitSizeM);
            inputToHiddenWeights = new UMatrix(outs, ins);
            hiddenThresholds = new UVector(outs);
        }

        private NTMHiddenLayer(Unit[][][] readToHiddenWeights, UMatrix inputToHiddenWeights, UVector hiddenThresholds, UVector hiddenLayer, int inputSize, int headCount, int memoryUnitSizeM, DiffableFunction activationFunction) {
            this.readToHiddenWeights = readToHiddenWeights;
            this.inputToHiddenWeights = inputToHiddenWeights;
            this.hiddenThresholds = hiddenThresholds;
            neurons = hiddenLayer;
            inputs = inputSize;
            heads = headCount;
            this.memoryUnitSizeM = memoryUnitSizeM;
            activation = activationFunction;
        }

        @Override
        public NTMHiddenLayer clone() {
    //		try {
            return new NTMHiddenLayer(readToHiddenWeights, inputToHiddenWeights, hiddenThresholds,
                    new UVector(neurons()),
                    inputs, heads, memoryUnitSizeM, activation);
    //		} catch (RuntimeException __dummyCatchVar0) {
    //			throw __dummyCatchVar0;
    //		} catch (Exception __dummyCatchVar0) {
    //			throw new RuntimeException(__dummyCatchVar0);
    //		}

        }

        final int neurons() {
            return neurons.size();
        }

        public int inputs() {
            return inputs;
        }
        public int outputs() {
            return neurons.size();
        }


        public void forward(double[] i, ReadData[] d) {

            double[] nv = neurons.value;

            double[] hlt = hiddenThresholds.value;

            int N = neurons();

            for (int n = 0; n < N; n++) {
                nv[n] = activation.valueOf(
                        readContributionToHidden(n, d) +
                                inputContributionToHidden(n, i) +
                                hlt[n]);
            }
        }

        private double readContributionToHidden(int neuronIndex, ReadData[] readData) {
            Unit[][] readWeightsForEachHead = readToHiddenWeights[neuronIndex];
            double s = 0;
            int memoryUnitSizeM = this.memoryUnitSizeM;
            for (int i = 0; i < heads; i++) {
                Unit[] h = readWeightsForEachHead[i];
                Unit[] r = readData[i].read;
                for (int j = 0; j < memoryUnitSizeM; j++)
                    s += h[j].value * r[j].value;
            }
            return s;
        }

        private double inputContributionToHidden(int neuronIndex, double[] input) {
            return inputToHiddenWeights.row[neuronIndex].sumDotSafe(input);
        }


        public void update(IWeightUpdater u) {
            u.update(readToHiddenWeights);
            u.update(inputToHiddenWeights);
            u.update(hiddenThresholds);
        }

        public void backward(double[] input, ReadData[] reads) {
            double[] h = hiddenGrads();
            updateReadGradient(h, reads);
            updateInputToHiddenWeightsGradients(h, input);
            updateHiddenThresholdsGradients(h);
        }

        private double[] hiddenGrads() {
            double[] g = this.neurons.grad;
            double[] v = this.neurons.value;
            DiffableFunction a = this.activation;
            return Util.arrayOf(i -> g[i] * a.derivative(v[i]), new double[neurons()]);
        }

        private void updateReadGradient(double[] hiddenLayerGradients, ReadData[] reads) {
            int n = neurons(), h = heads, m = memoryUnitSizeM;
            for (int neuronIndex = 0; neuronIndex < n; neuronIndex++) {
                Unit[][] neuronToReadDataWeights = readToHiddenWeights[neuronIndex];
                double hiddenLayerGradient = hiddenLayerGradients[neuronIndex];
                for (int headIndex = 0; headIndex < h; headIndex++) {
                    ReadData readData = reads[headIndex];
                    Unit[] neuronToHeadReadDataWeights = neuronToReadDataWeights[headIndex];
                    Unit[] r = readData.read;
                    for (int memoryCellIndex = 0; memoryCellIndex < m; memoryCellIndex++) {
                        r[memoryCellIndex].grad += hiddenLayerGradient * neuronToHeadReadDataWeights[memoryCellIndex].value;
                        neuronToHeadReadDataWeights[memoryCellIndex].grad += hiddenLayerGradient * r[memoryCellIndex].value;
                    }
                }
            }
        }

        private void updateInputToHiddenWeightsGradients(double[] hiddenLayerGradients, double[] input) {
            int n = neurons();
            UVector[] inputToHiddenLayerWeights = this.inputToHiddenWeights.row;
            for (int i = 0; i < n; i++)
                updateInputGradient(hiddenLayerGradients[i], inputToHiddenLayerWeights[i], input);
        }

        private void updateInputGradient(double hiddenLayerGradient, UVector inputToHiddenNeuronWeights, double[] input) {
            double[] g = inputToHiddenNeuronWeights.grad;
            int n = this.inputs;
            for (int i = 0; i < n; i++)
                g[i] += hiddenLayerGradient * input[i];
        }

        private void updateHiddenThresholdsGradients(double[] hiddenLayerGradients) {
            double[] hgrad = hiddenThresholds.grad;
            int n = neurons();
            for (int i = 0; i < n; i++)
                hgrad[i] += hiddenLayerGradients[i];
        }

    }
}