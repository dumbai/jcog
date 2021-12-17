package jcog.math;

import com.google.common.base.Joiner;
import com.google.common.math.PairedStatsAccumulator;
import jcog.data.list.Lst;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * TODO use TableSaw
 */
public class MultiStatistics<X> implements Consumer<X> {


    public static class BooleanClassifierWithStatistics<X> extends RecycledSummaryStatistics implements Consumer<X> /* implements AbstractClassifier */ {
        public final Predicate<X> filter;
        public final String id;

        public BooleanClassifierWithStatistics(String id, Predicate<X> filter) {
            super();
            this.id = id;
            this.filter = filter;
        }

        @Override
        public String toString() {
            long N = getN();
            return id + ": " +
                    (N > 0 ? (N +":" + getMin() + ".." + getMax() + ", avg=" + getMean() + ", sum=" + getSum() + ':' + super.toString())
                        :
                        "none")
                    ;
        }


        @Override
        public void accept(X x) {
            accept(x, 1);
        }

        void accept(X parameter, float v) {
            if (filter.test(parameter)) {
                accept(v);
            }
        }
    }

    public final List<Consumer<X>> cond;


    public MultiStatistics() {
        this.cond = new Lst();

        /** wildcard catch-all; by default will not collect all the unique values but other added conditions will */
        cond.add(new BooleanClassifierWithStatistics<>("*", x -> true) );
    }


    public void clear() {
        for (Consumer<X> c : cond) {


            if (c instanceof BooleanClassifierWithStatistics)
                ((BooleanClassifierWithStatistics)c).clear();
            else if (c instanceof ScalarStats)
                ((ScalarStats)c).clear();
            else
                throw new UnsupportedOperationException();
        }
    }


    public MultiStatistics<X> classify(String name, Predicate<X> test) {
        cond.add(new BooleanClassifierWithStatistics<>(name, test));
        return this;
    }

    static class ScalarStats<X> extends RecycledSummaryStatistics implements Consumer<X> {
        private final String id;
        private final FloatFunction<X> test;

        ScalarStats(String id, FloatFunction<X> test) {
            this.id = id;
            this.test = test;
        }
        @Override
        public String toString() {
            return id + ": #" + getN() + ':' + getMin() + ".." + getMax() + ", avg=" + getMean() + ", sum=" + getSum();
        }

        @Override
        public void accept(X x) {
            float v = test.floatValueOf(x);
            if (v == v)
                accept(v);
        }
    }

    public MultiStatistics<X> value(String name, FloatFunction<X> test) {
        cond.add(new ScalarStats(name, test));
        return this;
    }

    public MultiStatistics<X> value2D(String name, Function<X,float[]> test) {
        cond.add(new TwoDStats(name, test));
        return this;
    }

    @Override
    public final void accept(X x) {
        for (Consumer<X> cc : cond) {
            cc.accept(x);
        }
    }

    public MultiStatistics<X> add(Iterable<X> ii) {
        ii.forEach(this);
        return this;
    }

    @Override
    public String toString() {
        return Joiner.on('\n').join(cond);
    }

    public void print() {
        print(System.out);
    }

    public void print(Appendable out) {
        try {
            Joiner.on('\n').appendTo(out, cond);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class TwoDStats<X> implements Consumer<X> {
        private final PairedStatsAccumulator stats;
        private final Function<X, float[]> func;
        private final String id;

        TwoDStats(String name, Function<X,float[]> val) {
            this.id = name;
            this.stats = new PairedStatsAccumulator();
            this.func = val;
        }

        @Override
        public String toString() {
            return id + ' ' + stats.snapshot();
        }

        @Override
        public void accept(X z) {
            float[] xy = func.apply(z);
            assert(xy.length==2);
            stats.add(xy[0], xy[1]);
        }
    }
}