package jcog.signal.wave1d;

import jcog.Util;
import jcog.signal.Tensor;
import jcog.signal.tensor.ArrayTensor;

import java.util.Arrays;

/** TODO support pluggable models (DCT, DFT, Wavelet Transform, etc) with optional phase data in secondary dimension */
public class HaarWaveletTensor extends ArrayTensor {

    private final Tensor src;

    public HaarWaveletTensor(Tensor wave, int size) {
        super(Util.largestPowerOf2NoGreaterThan(size) /* specific to haar */);
        this.src = wave;
        update();
    }

    private float[] tmp;
    public void update() {
        int v = Util.largestPowerOf2NoGreaterThan(src.volume())*2;
        if(tmp==null || tmp.length!= v) {
            tmp = new float[v];
        } else {
            Arrays.fill(tmp, 0);
        }

        src.writeTo(tmp);

        OneDHaar.inPlaceFastHaarWaveletTransform(tmp);
        System.arraycopy(tmp, 0, data, 0, Math.min(data.length, tmp.length));
        //TODO fill 0s elsewhere
    }

}
