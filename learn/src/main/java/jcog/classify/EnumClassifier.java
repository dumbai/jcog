package jcog.classify;

import org.roaringbitmap.RoaringBitmap;

import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class EnumClassifier<X> extends AbstractClassifier<X> {

    
    private final ToIntFunction<X> which;
    private final int dim;
    private final String[] names;
    private final boolean dynamic;

    public EnumClassifier(Object id, int n, ToIntFunction<X> which) {
        this(id, IntStream.range(0, n).mapToObj(String::valueOf).toArray(String[]::new), which);
    }

    public EnumClassifier(Object id, String[] names, ToIntFunction<X> which) {
        this(id, names, which, false);
    }

    public EnumClassifier(Object id, String[] names, ToIntFunction<X> which, boolean isDynamic) {
        super(id);
        this.dim = names.length;
        this.names = names;
        this.which = which;
        this.dynamic = isDynamic;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public String name(int i) {
        return names[i];
    }

    @Override
    public int dimension() {
        return dim;
    }

    @Override
    public void classify(X x, RoaringBitmap bmp, int offset) {

        if (dynamic) {
            bmp.remove(offset, offset+dim);
        }

        int w = which.applyAsInt(x);
        if (w >= 0) {
            assert (w < dim);
            bmp.add(offset + w);
        }
    }

}
