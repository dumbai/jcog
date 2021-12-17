package jcog.math;

import jcog.sort.FloatRank;
import org.eclipse.collections.api.block.HashingStrategy;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectFloatHashMapWithHashingStrategy;
import org.jetbrains.annotations.Nullable;

public class CachedFloatFunction<X> extends ObjectFloatHashMapWithHashingStrategy<X> implements FloatRank<X> {



    private static final HashingStrategy IDENTITY_STRATEGY = new HashingStrategy() {
        @Override
        public int computeHashCode(Object object) {
            return object.hashCode();
        }

        @Override
        public boolean equals(Object object1, Object object2) {
            return object1 == object2;
        }
    };
    private final int compactionLimit;

    protected FloatFunction<X> f;


    public CachedFloatFunction(int sizeMin) {
        this(sizeMin, null);
    }

    public CachedFloatFunction(int sizeMin, @Nullable FloatFunction<X> f) {
        this(sizeMin, sizeMin * 2, f);
    }
    public CachedFloatFunction(int sizeMin, int compactionLimit, @Nullable FloatFunction<X> f) {
        super(IDENTITY_STRATEGY, sizeMin);
        this.compactionLimit = compactionLimit;
        value(f);
    }

    /** resets the value function */
    public CachedFloatFunction<X> value(FloatFunction<X> f) {
        clear();
        this.f = f;
        return this;
    }


    @Override
    public float rank(X x, float min) {
        return f.floatValueOf(x);
    }

    @Override
    public final float floatValueOf(X x) {
        return getIfAbsentPutWithKey(x, f);
    }

    @Override
    public void clear() {
        int s = size();
        if (s > 0) {
            super.clear();

            if (s > compactionLimit) {
                compact();
            }
        }
    }

}
