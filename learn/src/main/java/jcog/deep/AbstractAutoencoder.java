package jcog.deep;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static jcog.Util.toDouble;
import static jcog.Util.toFloat;

public abstract class AbstractAutoencoder {
    public final AtomicBoolean normalize = new AtomicBoolean(true);

    public Random rng;

    public abstract void clear(Random rng);

    protected void clear(float[] wi, float a) {
        int n = wi.length;
        for (int j = 0; j < n; j++)
            wi[j] = uniform(-a, +a);
//        Arrays.sort(wi);
    }
    protected void clear(double[] wi, float a) {
        int n = wi.length;
        for (int j = 0; j < n; j++)
            wi[j] = uniform(-a, +a);
//        Arrays.sort(wi);
    }

    protected void clear(double[][] wi, float a) {
        for (double[] x : wi)
            clear(x, a);
    }

    private float uniform(float min, float max) {
        return rng.nextFloat() * (max - min) + min;
    }



    public abstract float[] get(float[] x);

    public void put(double[] x, float pri) {
        put(toFloat(x), pri); //HACK
    }
    public void put(float[] x, float pri) {
        put(toDouble(x), pri); //HACK
    }

    /** callback after encoding */
    protected void latent(float[] x, float[] y, float[] z) {

    }
    /** preprocessing filter, applied to each x[]'s value
     * TODO double
     */
    public float pre(float x) {
        return Float.isFinite(x) ? x : 0;
        //return x;
    }

    /** TODO double */
    public float encodePost(float x) {
        return pre(x);
        //return x;
    }

    protected void preprocess(float[] x, float noiseLevel, float corruptionRate) {


        Random r = this.rng;
        int ins = x.length;

        for (int i = 0; i < ins; i++) {
            float v = pre(x[i]);
            if ((corruptionRate > 0) && (r.nextFloat() < corruptionRate)) {
                v = 0;
            }
            if (noiseLevel > 0) {
                v +=

                        (r.nextFloat() - 0.5f) * 2 * noiseLevel;


            }
            x[i] = v;
        }

//        for (int i = 0, inputLength = xx.length; i < inputLength; i++)
//            xx[i] = Util.clamp(xx[i], 0, 1f);
    }
}