package jcog.signal.wave1d;

import jcog.signal.Tensor;
import jcog.signal.tensor.ArrayTensor;
import org.eclipse.collections.api.block.function.primitive.IntFloatToFloatFunction;

public class SlidingDFTTensor extends ArrayTensor {
    final SlidingDFT dft;

    //private final boolean realOrComplex;
    float[] timeDomainSamplesFloat;

    public SlidingDFTTensor(int fftSize) {
        super(fftSize*2);
        this.dft = new SlidingDFT(fftSize, 1);
        //this.realOrComplex = realOrComplex;
    }

    public void update(Tensor timeDomainSamples) {
        int sv = timeDomainSamples.volume();
        if(timeDomainSamplesFloat ==null || timeDomainSamplesFloat.length!= sv) {
            timeDomainSamplesFloat = new float[sv];
        }

        timeDomainSamples.writeTo(timeDomainSamplesFloat);

        dft.next(timeDomainSamplesFloat, 0, sv, 0, null);
        dft.decode(0, data);
    }

    //    public void normalize(int from, int to) {
//        float[] minmax = Util.minmax(data, from, to);
//        if (minmax[1] - minmax[0] > Float.MIN_NORMAL*(to-from)) {
//            Util.normalize(data, from, to, minmax[0], minmax[1]);
//        } else {
//            Arrays.fill(data, from, to,0);
//        }
//    }

    /** multiplicative filter, by freq index */
    public void transform(IntFloatToFloatFunction f) {
        float[] d = this.data;
        for (int i = 0; i < d.length; i++)
            d[i] *= f.valueOf(i, d[i]);
    }
}