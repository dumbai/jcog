package jcog.nn.optimizer;

import jcog.Util;
import jcog.nn.layer.DenseLayer;
import jcog.util.ArrayUtil;

import static jcog.Util.fma;
import static jcog.Util.lerpSafe;

/** TODO https://arxiv.org/pdf/1709.07417.pdf */
public class AdamOptimizer extends BatchWeightUpdater {


    public double beta1 = 0.9;
    public double beta2 =
        0.99f;
        //0.999;

    //        private double beta1PowerT = Double.NaN, beta2PowerT = Double.NaN;
    public static final double epsilon = 1.0E-12;
    public float lr;

    /** mean - running estimate of gradient */
    private double[] momentM = ArrayUtil.EMPTY_DOUBLE_ARRAY;

    /** variance - running estimate of gradient^2 */
    private double[] momentV = ArrayUtil.EMPTY_DOUBLE_ARRAY;

    private final boolean minimizing = false;
    private int pAbs;


    /**
     * https://arxiv.org/pdf/1711.05101.pdf
     * https://cs.stanford.edu/people/karpathy/reinforcejs/puckworld.html
     * https://github.com/pytorch/pytorch/blob/c371542efc31b1abfe6f388042aa3ab0cef935f2/torch/optim/_multi_tensor/adamw.py
     */
    float weightDecay =
            //1;
            //0.5f;
            //0.1f;
            //0.05f;
            //0.001f;
            0;


    //        protected void minimizeDeltas(Map<VariableNode, TensorNode> var) {
//            minimizing = true;
//            imize(var);
//        }
//
//        protected void maximizeDeltas(Map<VariableNode, TensorNode> var) {
//            minimizing = false;
//            imize(var);
//        }

    @Override
    public void reset(int weights, float alpha) {
        super.reset(weights,alpha);
        if (momentM.length != weights) {
//                beta1PowerT = beta1;
//                beta2PowerT = beta2;
            momentM = new double[weights];
            momentV = new double[weights];
        }
        this.pAbs = 0;
        this.lr = alpha;
    }


    @Override
    protected void updateWeights(DenseLayer l, double[] dW, double[] dWPrev, double[] W) {
        int pAbs = this.pAbs;
        double[] mm = this.momentM, vv = this.momentV;

        double lr = this.lr;
        int dir = minimizing ? -1 : +1;
        double alphaT =
                lr * dir
                //* Math.sqrt(1 - beta2) / (1 - beta1)
                ;

        double weightDecay =
                Math.min(1, Util.sumAbs(dW)/ (dW.length)) * this.weightDecay;
                //this.weightDecay;

        double weightDecayFactor = 1 - weightDecay;


        int n = l.ins() * l.outs();
        for (int pRel = 0; pRel < n; pRel++, pAbs++) {
            double g = dW[pRel];

            //update the moving averages of the gradient
            double m = mm[pAbs] =
                    //beta1 * mm[pAbs] + (1-beta1)*g;
                    lerpSafe(beta1, g, mm[pAbs]);

            //update the moving averages of the squared gradient
            double v = vv[pAbs] =
                    //beta2 * vv[pAbs] + (1-beta2)*(g*g);
                    lerpSafe(beta2, g * g, vv[pAbs]);

            double Wnext =
                    m / (Math.sqrt(v) + epsilon);

            double Wprev = W[pRel];
            W[pRel] = fma(alphaT, Wnext, Wprev * weightDecayFactor);

//                    double m_cap = m/(1-(Math.pow(beta1,t)))		#calculates the bias-corrected estimates
//                            v_cap = v/(1-(Math.pow(beta2,t))		#calculates the bias-corrected estimates

        }
        this.pAbs = pAbs;

//            if (this.pAbs == 0) {
//                beta1PowerT *= beta1PowerT;
//                beta2PowerT *= beta2PowerT;
//            }
    }

    public final AdamOptimizer alpha(float v) {
        lr = v;
        return this;
    }

    public final AdamOptimizer momentum(double b1, double b2) {
        this.beta1 = b1;
        this.beta2 = b2;
        return this;
    }

    public final WeightUpdater weightDecay(float w) {
        this.weightDecay = w;
        return this;
    }
}