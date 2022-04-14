package jcog.deep;

import jcog.Util;
import jcog.activation.DiffableFunction;
import jcog.activation.SigmoidActivation;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.FloatRange;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.fill;
import static jcog.Util.fma;

/**
 * Denoising Autoencoder (from DeepLearning.net)
 */
public class Autoencoder extends AbstractAutoencoder {

    /**
     * input vector after preprocessing (noise, corruption, etc..)
     */
    public float[] x;

    /**
     * encoded vector
     */
    public final float[] y;

    /**
     * decoded vector
     */
    public final float[] z;

    public final float[][] W;

    private final float[] hbias, vbias;
    private final float[] L_hbias;

    private final double[] delta /*"L_vbias"*/;

    /** domain and range must be non-negative */
    public DiffableFunction activation =
        SigmoidActivation.the;
        //TanhActivation.the;
        //new SigLinearActivation();
        //ReluActivation.the;

    @Deprecated public final FloatRange noise = new FloatRange(0, 0, 1);
    public final FloatRange corruption = new FloatRange(0, 0, 1);

    public Autoencoder(int ins, int outs) {
        this(ins, outs, null);
    }

    public Autoencoder(int ins, int outs, @Nullable Random rng) {

        x = new float[ins];
        z = new float[ins];
        delta = new double[ins];
        vbias = new float[ins];

        W = new float[outs][ins];

        hbias = new float[outs];
        L_hbias = new float[outs];
        y = new float[outs];

        clear(rng);
    }

    public void clear() {
        clear(this.rng);
    }

    @Override
    public void clear(Random rng) {

        if (rng == null)
            rng = new XoRoShiRo128PlusRandom();

        if (!(rng instanceof ThreadLocalRandom))
            this.rng = rng;

        fill(hbias, 0);
        fill(L_hbias, 0);
        fill(vbias, 0);
        fill(delta, 0);

        float a = 1f / W[0].length;
        for (float[] wi : W)
            clear(wi, a);

    }

    @Override
    public final float[] get(float[] x) {
        return get(x, noise.floatValue(), corruption.floatValue(), normalize.getOpaque(), this.y);
    }

    @Deprecated private float[] get(float[] x, float noise, float corruption, @Deprecated boolean normalize, float[] y) {

        this.x = x;

        preprocess(x, noise, corruption);

        float[][] W = this.W;

        int ins = x.length;
        int outs = y.length;

        float[] hbias = this.hbias;

        for (int i = 0; i < ins; i++)
            x[i] = pre(x[i]); //HACK

        for (int o = 0; o < outs; o++) {
            double yi = hbias[o];
            if (yi!=yi)
                hbias[o] = (float) (yi = 0); //corrupted hbias

            float[] wi = W[o];

            for (int i = 0; i < ins; i++)
                yi = fma(wi[i], x[i], yi);


            y[o] = (float) yi;
        }

        activation.applyTo(y, 0, outs);

        if (normalize) {
            //Util.normalizeCartesian(y, NORMALIZATION_EPSILON);
            Util.normalize(y);
        }

        for (int o = 0; o < outs; o++)
            y[o] = encodePost(y[o]);

        return y;
    }

    /**
     * TODO some or all of the bias vectors may need modified too here
     */
    public void forget(float rate) {
        float mult = 1f - rate;
        for (float[] floats : this.W)
            Util.mul(mult, floats);
        Util.mul(mult, hbias);
        Util.mul(mult, L_hbias);
        Util.mul(mult, vbias);
        Util.mul(mult, delta);
    }

    public final float[] decode(float[] y) {
        return decode(y, this.z);
    }

    @Deprecated public float[] decode(float[] y, float[] z) {

        float[][] w = W;

        float[] vbias = this.vbias;

        int ii = z.length, oo = y.length;

        for (int i = 0; i < ii; i++) {
            double vbiasI = vbias[i];

            double zi = vbiasI;
            for (int o = 0; o < oo; o++)
                zi = fma(w[o][i], y[o], zi);  //vi += w[j][i] * y[j];

            z[i] = pre((float)zi);
        }

        return z;
    }


    public int outputs() {
        return y.length;
    }

    public float[] output() {
        return y;
    }

    @Override
    @Deprecated public void put(float[] x, float pri) {

        int ii = this.inputs();

        double[] delta = this.delta;

        float[] y = get(x);

        float[] z = decode(y, this.z);

        latent(x, y, z);

        for (int i = 0; i < ii; i++)
            delta[i] = ((double) x[i]) - z[i];

        vBiasAccum(pri);

        learn(pri);
    }

    private void vBiasAccum(float pri) {
        for (int i = 0, ii = inputs(); i < ii; i++)
            this.vbias[i] = (float) fma(delta[i], pri, this.vbias[i]);
    }

    public double loss(double[] delta) {
        return Util.sumAbs(delta);
    }


    private void learn(float pri) {

        int ii = inputs(), oo = outputs();

        DiffableFunction a = this.activation;
        for (int o = 0; o < oo; o++) {
            float[] wo = this.W[o];

            double lbi = 0;
            for (int i = 0; i < ii; i++)
                lbi = fma(wo[i], this.delta[i], lbi);

            double dho = a.derivative(y[o]) * lbi;
            this.L_hbias[o] = (float) dho;
              this.hbias[o] = (float) fma(pri, dho, hbias[o]);
        }

        for (int o = 0; o < oo; o++) {
            double yo = y[o];
            double lho = L_hbias[o];
            float[] wo = W[o];
            for (int i = 0; i < ii; i++)
                wo[i] = (float) fma(pri, fma(delta[i], yo, lho * x[i]), wo[i]);
        }
    }

    public float[] reconstruct(float[] x) {
        return decode(get(x, 0, 0, false, new float[this.y.length]), z);
    }

    public int inputs() {
        return x.length;
    }


}