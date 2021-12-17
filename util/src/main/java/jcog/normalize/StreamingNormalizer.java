//package jcog.normalize;
//
//
//import jcog.Util;
//import org.hipparchus.linear.ArrayRealVector;
///**
// * normalizes each dimension in a vector seperately,
// * determined by iterative vector inputs which stretch each
// * separate dimension's bounds
// *
// * TODO write decay(d) function to collapse gradually */
//public class StreamingNormalizer {
//    private final float[] n;
//    final int dim;
//
//    public StreamingNormalizer(int s) {
//        this.dim = s;
//        this.n = new float[dim * 2 /* min, max */];
//        clear();
//    }
//
//    public void clear() {
//
//        for (int i = 0; i < dim * 2; i++) {
//            n[i] = (
//                    i % 2 == 0 ? Float.NEGATIVE_INFINITY /* for max */ : Float.POSITIVE_INFINITY /* for min */
//            );
//        }
//    }
//
//    /**
//     * y may be the same as x, it will just get destructively modified
//     */
//    public float[] normalize(float[] x, float[] y, float epsilon) {
//        for (int i = 0; i < dim; i++) {
//            float v = x[i];
//            float max = n[i * 2];
//            if (v > max)
//                n[i * 2] = v;
//            float min = n[i * 2 + 1];
//            if (v < min)
//                n[i * 2 + 1] = v;
//
//            y[i] = (!Float.isFinite(max) || !Float.isFinite(min) || !Util.equals(Math.abs(max - min),0, epsilon)) ? v : (v - min) / (max - min);
//        }
//        return y;
//    }
//    public float[] unnormalize(ArrayRealVector v) {
//        double[] vv = v.getDataRef();
//        float[] f = new float[vv.length];
//        for (int i = 0; i < dim; i++) {
//            float max = n[i * 2];
//            float min = n[i * 2 + 1];
//            f[i] = ((float)vv[i]) * (max-min) + min;
//        }
//        return f;
//    }
//
//    float maxmin(boolean maxOrMin) {
//        float z = maxOrMin ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
//        for (int i = 0; i < dim; i++) {
//            float m = n[i * 2 + (maxOrMin ? 0 : 1)];
//            if (maxOrMin ? m > z : m < z)
//                z = m;
//        }
//        return z;
//    }
//
//    public void decay(float v) {
//        for (int i = 0; i < dim*2; i++) {
//            n[i] *= v;
//        }
//    }
//
//
//}