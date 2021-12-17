package jcog.sort;

import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@FunctionalInterface public interface FloatRank<X> extends FloatFunction<X> {
    /**
     *
     * @param min this value which may be NEGATIVE_INFINITY, is a value that the rank must exceed to matter.
     *            so if a scoring function can know that, before completing,
     *            it wont meet this threshold, it can fail fast (by returning NaN).
     */
    float rank(X x, float min);

    default float rank(X x) {
        return rank(x, Float.NEGATIVE_INFINITY);
    }

    /** adapter which ignores the minimum */
    static <X> FloatRank<X> the(FloatFunction<X> f) {
        return f instanceof FloatRank ? (FloatRank) f : ((x, min) -> f.floatValueOf(x));
    }


    @Override
    default float floatValueOf(X x) {
        return rank(x, Float.NEGATIVE_INFINITY);
    }

    default FloatRank<X> filter(@Nullable Predicate<X> filter) {
        return filter == null ? this : new FilteredFloatRank<>(filter, this);

    }

    default FloatRank<X> negative() {
        return (x,min) -> -rank(x);
    }

    default FloatRank<X> mul(FloatRank<X> f) {
        return (x, min) -> {
            float y = rank(x, min);
            return y != y ? Float.NaN : y * f.rank(x, min);
        };
    }

    /** assumes f returns values in 0..1, to enable early exit condition */
    default FloatRank<X> mulUnit(FloatFunction<? super X> f) {
        return (x, min) -> {
            float y = rank(x, min);
            return y != y || y < min ? Float.NaN : y * f.floatValueOf(x);
        };
    }
    default FloatRank<X> mulUnitPow(@Nullable FloatFunction<? super X> base, float power) {
        if (base == null)
            return this;

        if (power==1)
            return mulUnit(base);

        return (x, min) -> {
            float y = rank(x, min);
            return y != y /*|| y < min <- only valid if power>1 */ ?
                    Float.NaN :
                    (float) (y * Math.pow(base.floatValueOf(x), power));
        };
    }


    final class FilteredFloatRank<X> implements FloatRank<X> {

        private final @Nullable Predicate<X> filter;
        private final FloatRank<X> rank;

        FilteredFloatRank(@Nullable Predicate<X> filter, FloatRank<X> rank) {
            this.filter = filter;
            this.rank = rank;
        }

        @Override
        public float rank(X t, float m) {
            return filter != null && !filter.test(t) ? Float.NaN : rank.rank(t,m);
        }
    }
}