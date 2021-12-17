package jcog.classify;

import org.roaringbitmap.RoaringBitmap;

import java.util.function.Predicate;

public class BooleanClassifier<X, Y> extends AbstractClassifier<X> {

    final Predicate<X> pred;

    public BooleanClassifier(Y name, Predicate<X> pred) {
        super(name);
        this.pred = pred;
    }

    @Override
    public int dimension() {
        return 1;
    }

    @Override
    public void classify(X x, RoaringBitmap bmp, int offset) {
        if (pred.test(x)) {
            bmp.add(offset);

        }
    }
}
