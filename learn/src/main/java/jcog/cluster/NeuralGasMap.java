package jcog.cluster;

import jcog.Util;
import jcog.data.Centroid;
import jcog.deep.Autoencoder;
import jcog.random.XorShift128PlusRandom;

import static java.lang.System.arraycopy;

/**
 * dimension reduction applied to a neural gasnet
 * Untested
 */
public class NeuralGasMap extends NeuralGasNet<NeuralGasMap.AECentroid> {

    private final Autoencoder enc;
    private final int outs;

    /** call this before retrieving values */
    public void update() {

        enc.forget(0.001f);

        forEachCentroid(n -> {

            if (n.center==null)
                n.center = new float[outs];

            float[] x1 = Util.toFloat(n.getDataRef());
            if (x1[0] == x1[0]) {
                enc.noise.set(0.001f);
                enc.put(x1, 0.02f);
                arraycopy(enc.output(), 0, n.center, 0, outs);




                
            }
        });
    }

    @Override
    public void clear() {
        super.clear();
        if (enc!=null)
            enc.clear();
    }

    public static class AECentroid extends Centroid {

        public float[] center;

        public AECentroid(int id, int dimensions) {
            super(id, dimensions);
            randomizeUniform(-1, 1);
        }

        public float[] center() {
            return center;
        }

    }

    

    public NeuralGasMap(int in, int maxNodes, int out) {
        super(in, maxNodes, null);
        this.outs = out;
        this.enc = new Autoencoder(in, out, new XorShift128PlusRandom(1));
        
        
    }


    @Override
    public AECentroid put(double[] x) {
        return super.put(x);
    }

    @Override
    public AECentroid newCentroid(int i, int dims) {
        return new AECentroid(i, dims);
    }
}