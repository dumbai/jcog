package jcog.pri;

import jcog.Is;
import jcog.signal.tensor.ArrayTensor;
import jcog.signal.tensor.AtomicArrayTensor;
import jcog.signal.tensor.WritableTensor;

import java.util.Arrays;
import java.util.Random;

import static jcog.Str.n2;
import static jcog.Util.lerpSafe;

/**
 * dead-simple fixed range continuous histogram with fixed # and size of bins. supports PDF sampling
 * https://www.scratchapixel.com/lessons/mathematics-physics-for-computer-graphics/monte-carlo-methods-mathematical-foundations/inverse-transform-sampling-method
 *
 * TODO pluggable function-approximation back-end
 *   either:
 *      empirical distribution (current)
 *      polynomial fit
 *      ...
 */
@Is("Inverse_transform_sampling")
public class ArrayHistogram extends DistributionApproximator {


    /**
     * for single writer
     */
    //private WritableTensor dataPre;
    float[] dataPre;

    private transient float[] pdf;

    /**
     * for readers
     */
    private volatile WritableTensor dataOut;

    public ArrayHistogram() {
        //clear();
    }

    private static int binFloor(float x, int bins) {
        return (int)(x * (bins-1));
    }
    private static float unbin(int b, int bins) {
        return b / (bins-1f);
    }

    private static int bin(float x, int bins) {
        //return Math.min(bins - 1, (int) (x * bins));
        return Math.round(x * (bins-1));
        //return (int) (x * (bins - 0.5f));
    }

    @Override
    public String toString() {
        return n2(dataOut.floatArray());
    }

    private void resize(int nextBins) {
        WritableTensor prevDataOut = this.dataOut;
        if (dataPre==null || (prevDataOut !=null ? prevDataOut.volume() : -1) != nextBins) {
            this.dataPre = new float[nextBins];
            this.dataOut = dataAtomic(nextBins);
        }
    }

    private static ArrayTensor dataPlain(int bins) {
        return bins == 0 ? ArrayTensor.Zero : new ArrayTensor(bins);
    }

    private static AtomicArrayTensor dataAtomic(int bins) {
        return bins == 0 ? AtomicArrayTensor.Empty : new AtomicArrayTensor(bins);
    }

//    public String chart() {
//        return SparkLine.renderFloats(new FloatArrayList(dataOut.floatArrayShared()));
//    }

    public void start(int inBins) {
        float[] _pdf = this.pdf;
        if (_pdf!=null && _pdf.length == inBins)
            Arrays.fill(this.pdf, 0);
        else {
            assert (inBins > 0);
            this.pdf = new float[inBins];
        }
    }


    @Override public void accept(float pri) {
        addRaw(pri);

        addRawBi(pri, 1, pdf, pdf.length, 0);
    }


    private float[] addRaw(float pri) {
        float[] pdf = this.pdf;
        pdf[bin(pri, pdf.length)] += 1;
        return pdf;
    }


    /**
     * inverse transform sampling
     */
    @Override public void commit(float lo, float hi, int outBins) {
        assert(outBins > 2);
        float[] pdf = this.pdf;
        int inBins = pdf.length;

        //1. convert pdf to cdf
        for (int i = 1 /* skip 0 */; i < inBins; i++)
            pdf[i] += pdf[i - 1];
        
        @SuppressWarnings("UnnecessaryLocalVariable")
        float[] cdf = pdf; //rename only

        //2. rotate
        float min = cdf[0], max = cdf[inBins - 1];
        float range = max - min;
        if (range < outBins * Prioritized.EPSILON) {
            commitFlat(lo, hi);
        } else {
            commitCurve(lo, hi, outBins, cdf, 0, inBins, min, range);
        }
    }

    private void commitCurve(float lo, float hi, int outBins, float[] cdf, int bs, int be, float cdfMin, float cdfRange) {
        resize(outBins);

        var data = dataPre;
        Arrays.fill(data, 0);

        int bw = be - bs;

        double sum = 0;
        for (int i = bs; i < be; i++) {
            float cx = (cdf[i] - cdfMin) / cdfRange;
            float w = ((i+0.5f) - bs) / bw;
            addRawBi(cx, w, data, outBins, 0);
            sum += w;
        }

        //cumulative
        for (int i = 1; i < outBins - 1; i++)
            data[i] += data[i-1];

        data[0] = lo;

        //normalize to indices
        float hilo = hi - lo;
        for (int i = 1; i < outBins - 1; i++)
            data[i] = (float)((data[i] / sum) * hilo + lo);

        data[outBins - 1] = hi;

        dataOut.setAll(data);
    }

    private static void addRaw(float x, float dy, float[] data, int outBins) {
        data[bin(x, outBins)] += dy;
    }

    /** 2-ary supersample */
    private static void addRawBi(float cx, float w, float[] data, int outBins, int offset) {
        int yMin = binFloor(cx, outBins);
        int yMax = Math.min(yMin + 1, outBins-1);
        if (yMin == yMax)
            data[yMin + offset] += w;
        else {
            float cMin = unbin(yMin, outBins);
            float cMax = unbin(yMax, outBins);
            float pMax = (cx - cMin)/(cMax - cMin);
            data[yMin + offset] += w * (1-pMax);
            data[yMax + offset] += w * pMax;
        }
    }

    //}

    @Override public void commitFlat(float lo, float hi) {

        WritableTensor o = this.dataOut;
        if (o == null)
            this.dataOut = o = dataAtomic(2);

        o.setAt(0, lo);
        o.setAt(1, hi);
    }

    @Override
    public final int sampleInt(Random r) {
        return sampleInt(r.nextFloat());
    }

    @Override
    public final int sampleInt(float uniform) { return (int)sample(uniform); }

    /**
     * TODO use the rng to generate one 64-bit or one 32-bit integer and use half of its bits for each random value needed
     */
    @Override
    public float sample(float q) {
        WritableTensor data = this.dataOut;
        if (data == null)
            return q;

        int bins = data.volume();
        int lower;
        float p;
        if (bins == 2) {
            p = q;
            lower = 0;
        } else {
            p = q * (bins - 1);
            lower = (int) p;
        }
//            System.out.println(n2(q) + " " + below + "," + above);

        return lerpSafe(p - lower, data.getAt(lower), data.getAt(lower + 1));
    }


    @Override
    public void clear() {
        commitFlat(0,0);
    }
}