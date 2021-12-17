//package jcog.decide;
//
//import jcog.WTF;
//import jcog.math.FloatSupplier;
//import jcog.sort.RankedN;
//import org.eclipse.collections.api.tuple.primitive.FloatObjectPair;
//
//import java.util.List;
//import java.util.Random;
//import java.util.function.Function;
//
///** weighted sampler */
//public class RouletteMix<X> extends RankedN<X> implements Function<FloatSupplier/*RNG*/,X> {
//
//    public static <X> RouletteMix<X> the(List<FloatObjectPair<X>> items) {
//
//        int s = items.size();
//        if (s == 0)
//            return null;
//        //TODO else if (s == 1)
//
//        RouletteMix<X> r = new RouletteMix(s);
//        for (FloatObjectPair<X> p : items)
//            r.addUnsorted(p.getTwo(), p.getOne());
//
//        return r;
//    }
//
//    private boolean addUnsorted(X x, float value) {
//        int s = size();
//        if (s + 1 > capacity())
//            throw new WTF("capacity exceeded");
//
//        return addEnd(x, value)!=-1;
//    }
//
//    private RouletteMix(int n) {
//        super((X[])new Object[n]);
//
//    }
//
//    public final X get(Random random) {
//        return get(random::nextFloat);
//    }
//
//    public final X get(FloatSupplier random) {
//        return apply(random);
//    }
//
//    @Override
//    public X apply(FloatSupplier floatSupplier) {
//        return getRoulette(floatSupplier);
//    }
//}
