package jcog.memoize;

import jcog.data.map.CustomConcurrentHashMap;

import java.util.function.Function;

/** WARNING dont call get() directly; use apply().
 * allows storing null values
 * */
public class SoftMemoize<X, Y> extends CustomConcurrentHashMap<X, Object> implements Memoize<X, Y> {

    private final Function<X, Object> f;

    private final Object NULL = new Object();

    public SoftMemoize(Function<X, Y> g, int expSize, Strength strength) {
        this(g, expSize, strength, strength);
    }
    public SoftMemoize(Function<X, Y> g, int expSize, Strength keyStrength, Strength valueStrength) {
        super(keyStrength, EQUALS, valueStrength, EQUALS, expSize);
        this.f = (x) -> {
            Y y = g.apply(x);
            return y == null ?
                    NULL :
                    y;
        };
    }

    @Override
    public String summary() {
        return "size=" + super.size();
    }

    @Override
    public Y apply(X x) {
        Object y = computeIfAbsent(x, f);
		return y == NULL ? null : (Y) y;
    }


}
