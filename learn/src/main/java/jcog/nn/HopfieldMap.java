package jcog.nn;

import jcog.Str;
import jcog.Util;
import jcog.data.graph.AdjGraph;
import jcog.signal.MutableFloat;
import jcog.signal.NumberX;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.eclipse.collections.api.block.procedure.primitive.FloatObjectProcedure;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** untested */
public class HopfieldMap<X> {

    private final X[] x;
    private final FloatFunction<X> in;
    private final FloatObjectProcedure<X> out;

    
    private final AdjGraph<X, Float> weight = new AdjGraph(false);

    private final Random rng = new Random();

    /**
     * TODO generalize to Iterable
     */
    @SafeVarargs
    public HopfieldMap(FloatFunction<X> in, FloatObjectProcedure<X> out, X... x) {
        assert (x.length > 1);
        this.x = x;
        this.in = in;
        this.out = out;
    }

    public X random() {
        return x[randomIndex()];
    }

    public int randomIndex() {
        return rng.nextInt(x.length);
    }

    public float randomWeight(float min, float max) {
        return Util.lerp(rng.nextFloat(), min, max);
    }

    public HopfieldMap<X> randomWeights(float connectivity) {
        int edges = (int) Math.ceil(x.length * x.length * connectivity);
        for (int i = 0; i < edges; i++) {
            X a = random();
            X b = random();
            if (a != b) {
                weight.addNode(a);
                weight.addNode(b);
                weight.setEdge(a, b, randomWeight(-1, +1));
            }
        }
        return this;
    }

    @Override
    public String toString() {
        String result = Arrays.stream(x).map(xx -> Str.n4(in.floatValueOf(xx)) + ',').collect(Collectors.joining());
        String sb = result;
        return sb;
    }

    public HopfieldMap<X> learn(int cycles) {
        for (int i = 0; i < cycles; i++) {
            learn();
        }
        return this;
    }

    public static float alpha() {
        return 0.2f;
    }

    /**
     * https:
     */
    public void learn() {
        int p = randomIndex();

        float alpha = alpha();

        for (int i = 0; i < x.length; i++) {

            X a = x[p];

            float[] aOut = {0};
            weight.neighborEdges(a, (b, w) -> {
                float bIn = in.floatValueOf(b);

                aOut[0] += bIn * w;

                return Util.tanhFast(w + (alpha * aOut[0] * bIn)); 
            });

            this.out.value(out(aOut[0]), a);

            if (++p == x.length) p = 0;
        }
    }

    public HopfieldMap<X> get() {
        for (X value : x) {
            float[] aOut = {0};

            X a = value;

            weight.neighborEdges(a, (b, w) -> {
                float bIn = in.floatValueOf(b);

                aOut[0] += bIn * w;
            });

            this.out.value(out(aOut[0]), a);
        }
        return this;
    }

    protected static float out(float v) {
        
        return v >= 0 ? 1 : -1;
        
    }

    public HopfieldMap<X> set(float... v) {
        assert (v.length == x.length);
        for (int i = 0; i < v.length; i++)
            out.value(v[i], x[i]);
        return this;
    }

    public static void main(String[] args) {
        int n = 8;
        NumberX[] m = IntStream.range(0, n).mapToObj(i1 -> new MutableFloat()).toArray(NumberX[]::new);

        HopfieldMap<NumberX> h = new HopfieldMap<>(NumberX::floatValue,
                (v, x) -> x.set(v), m);
        h.randomWeights(0.9f);
        for (int i = 0; i < 16; i++) {
            h.set(+1, +1, +1, +1, -1, -1, -1, -1).learn(1);
            h.set(-1, -1, -1, -1, +1, +1, +1, +1).learn(1);
        }

        h.set(+1, +1, +1, +1, -1, 0, -1, -1).get();
        System.out.println(h);
        System.out.println(h.weight);


    }



}