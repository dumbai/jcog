package jcog.signal.tensor;

import jcog.deep.Autoencoder;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.Tensor;

/**
 * applies stacked autoencoder as a filter function of an input tensor
 */
public enum AutoTranscodeTensor { ;

    /**  TODO AutodecodeTensor (reverse) - generate from encoded. and possibly share autoencoder instance */

//    public static class AutoDecodeTensor extends ArrayTensor {
//
//    }

    public static class AutoEncodeTensor extends ArrayTensor {
        private final Tensor in;
        //private final dA a;
        public final Autoencoder ae;

        /** TODO FloatRange */
        float noise = 0.0005f;

        public AutoEncodeTensor(Tensor input, int outputs) {
            this(input, new Autoencoder(input.volume(), outputs, new XoRoShiRo128PlusRandom(1)));
        }

        public AutoEncodeTensor(Tensor input, Autoencoder ae) {
            super(ae.outputs());
            this.in = input;
            this.ae = ae;
            ae.clear();
        }

        @Override
        public float[] snapshot() {

            synchronized (ae) {
                float[] ii = in.snapshot();


                //setAt(a.train(dii, learnRate, 0));
                /* TODO FloatRange */
                double learnRate = 0.01f;
                ae.put(ii, (float) learnRate);
                set(ae.y);

                if (noise > 0) {
                    float max = Float.NEGATIVE_INFINITY;
                    float min = Float.POSITIVE_INFINITY;
                    for (int i = 0; i < data.length; i++) {
                        double v = (data[i] += (((ae.rng.nextFloat()) - 0.5f) * 2f) * noise);
                        max = Math.max((float) v, max);
                        min = Math.min((float) v, min);
                    }

                    if (max != min) {
                        for (int i = 0; i < data.length; i++) {
                            data[i] = (data[i] - min) / (max - min);
                        }
                    }
                }
                return super.snapshot();
            }
        }
    }
}