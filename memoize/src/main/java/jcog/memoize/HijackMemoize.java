package jcog.memoize;

import jcog.Str;
import jcog.pri.PriProxy;
import jcog.pri.Prioritized;
import jcog.pri.bag.impl.hijack.PriHijackBag;
import jcog.pri.op.PriMerge;
import jcog.signal.NumberX;
import org.eclipse.collections.api.block.procedure.primitive.ObjectLongProcedure;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * a wrapper of HijackBag
 * adds a condition to decrease the priority of a cell which does not get replaced.  this gradually erodes the priority of existing cells and the rate this occurrs determines the aggregate lifespan of entries in the cache.  they can also be prioritized differently on insert, so in a memoization situation it can be helpful to increase the priority of a more expensive work item so it is more likely to replace a less expensive or less used existing entry.
 *
 * TODO add an instrumentation wrapper to collect statistics
 * about cache efficiency and also processing time of the calculations
 *
 *
 */
public class HijackMemoize<X, Y> extends AbstractMemoize<X,Y> {

    static final boolean unsafe = false;

    protected final MemoizeHijackBag bag;
    private final Function<X, Y> func;
    private final boolean soft;

    protected float DEFAULT_VALUE;
    protected float CACHE_HIT_BOOST;
    protected float CACHE_SURVIVE_COST;

    public HijackMemoize(Function<X, Y> f, int initialCapacity, int reprobes) {
        this(f, initialCapacity, reprobes, false);
    }


    public HijackMemoize(Function<X, Y> f, int initialCapacity, int reprobes, boolean soft) {
        this.soft = soft;
        this.func = f;

        bag = new MemoizeHijackBag(initialCapacity, reprobes);
        bag.resize(initialCapacity);
    }


    @Override
    public final void clear() {
        bag.clear();
    }

    private float statReset(ObjectLongProcedure<String> eachStat) {

        long H;
        eachStat.accept("H" /* hit */, H = hit.getAndSet(0));
        long M;
        eachStat.accept("M" /* miss */, M = miss.getAndSet(0));
        long R;
        eachStat.accept("R" /* reject */, R = reject.getAndSet(0));
        long E;
        eachStat.accept("E" /* evict */, E = evict.getAndSet(0));
        return (H / ((float) (H + M + R /* + E */)));
    }

    /**
     * estimates the value of computing the input.
     * easier/frequent items will introduce lower priority, allowing
     * harder/infrequent items to sustain longer
     */
    public float value(X x, Y y) {
        return DEFAULT_VALUE;
    }

    public final @Nullable Y getIfPresent(Object k) {
        PriProxy<X, Y> exists = bag.get(k);
        if (exists != null) {
            Y e = exists.y();
            if (e != null) {
                boost(exists);
                return e;
            } else
                throw new NullPointerException();
        }
        return null;
    }

    /** gain of priority on cache hit */
    protected void boost(PriProxy<X, Y> p) {
        p.priAdd(CACHE_HIT_BOOST);
    }

    protected void cut(PriProxy<X, Y> p) {
        p.priAdd(-CACHE_SURVIVE_COST);
    }

    public @Nullable Y removeIfPresent(X x) {
        @Nullable PriProxy<X, Y> exists = bag.remove(x);
        return exists != null ? exists.y() : null;
    }

    protected PriProxy<X, Y> put(X x, Y y) {
        return bag.put(computation(x, y));
    }

    @Override
    public @Nullable Y apply(X x) {
        Y y = getIfPresent(x);
        if (y != null) {
            hit.getAndIncrement();
            return y;
        } else
            return compute(x);
    }

    private Y compute(X x) {
        Y y;
        y = func.apply(x);
        PriProxy<X, Y> input = computation(x, y);
        PriProxy<X, Y> output = bag.put(input);
        boolean interned = (output == input);
        if (interned) {
            miss.getAndIncrement();
            internedNew(x, input);
        } else {
            if (output!=null) {
                //result obtained before inserting ours, use that it is more likely to be shared
                y = output.y();
                miss.getAndIncrement(); //technically, this is a combination of a hit and a miss
            } else {
                //output = null
                reject.getAndIncrement();
            }
        }
        return y;
    }

    protected void internedNew(X x, PriProxy<X, Y> input) {

    }

//
//    /**
//     * can be overridden in implementations to compact or otherwise react to the interning of an input key
//     */
//    private void onIntern(X x) {
//
//    }

    /**
     * produces the memoized computation instance for insertion into the bag.
     * here it can choose the implementation to use: strong, soft, weak, etc..
     */
    public PriProxy<X, Y> computation(X x, Y y) {
        float pri = value(x,y);
        return soft ?
                new PriProxy.SoftProxy<>(x, y, pri) :
                new PriProxy.StrongProxy<>(x, y, pri);
    }

    /**
     * clears the statistics
     */
    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(" N=").append(bag.size()).append(' ');
        float rate = statReset((k, v) -> sb.append(k).append('=').append(v).append(' '));
        sb.setLength(sb.length() - 1);
        sb.append(" D=").append(Str.n2percent(bag.density()));
        sb.insert(0, Str.n2percent(rate));
        return sb.toString();
    }

    public Iterator<PriProxy<X, Y>> iterator() {
        return bag.iterator();
    }

    /** warning: increase EVICT meter */
    public boolean remove(X x) {
        return bag.remove(x)!=null;
    }


    protected class MemoizeHijackBag extends PriHijackBag<X, PriProxy<X,Y>> {

        MemoizeHijackBag(int cap, int reprobes) {
            super(PriMerge.plus, cap, reprobes);
        }

        @Override
        protected boolean unsafe() {
            return unsafe;
        }

        @Override
        protected PriProxy<X, Y> merge(PriProxy<X, Y> existing, PriProxy<X, Y> incoming, NumberX overflowing) {
            if (existing.isDeleted())
                return incoming;
            return super.merge(existing, incoming, overflowing);
        }

        @Override
        protected void resize(int newSpace) {
            if (space() > newSpace)
                return;

            super.resize(newSpace);
        }

        @Override
        protected void _setCapacity(int oldCap, int c) {

//
//            float boost = i > 0 ?
//                    (float) (1f / Math.sqrt(capacity()))
//                    : 0;
//
//
//            float cut = boost / (reprobes / 2f);
//
//            assert (cut > ScalarValue.EPSILON);
//            HijackMemoize.this.DEFAULT_VALUE = 0.5f / reprobes;
//            HijackMemoize.this.CACHE_HIT_BOOST = boost;

            float sc = (float) Math.sqrt(c);
            DEFAULT_VALUE =
                    //0.5f / reprobes;
                    1f / sc;
            CACHE_HIT_BOOST = 0.5f/sc;
            CACHE_SURVIVE_COST = CACHE_HIT_BOOST / reprobes;

            assert(DEFAULT_VALUE > Prioritized.EPSILON);
            assert(CACHE_HIT_BOOST > Prioritized.EPSILON);
            assert(CACHE_SURVIVE_COST > Prioritized.EPSILON);
        }

        @Override
        public int spaceMin() {
            return capacity();
        }

        @Override
        protected boolean reshrink(int length) {
            return false; //maintain capacity
        }

        @Override
        protected boolean regrowForSize(int s, int c) {
            return false;
        }

        @Override
        public void pressurize(float f) {

        }

        @Override
        public void depressurize(float toRemove) {

        }

        @Override
        public float depressurizePct(float percentToRemove) {
            return 0;
        }

        @Override
        public void commit(@Nullable Consumer<? super PriProxy<X, Y>> update) {

        }

        @Override
        public X key(PriProxy<X, Y> value) {
            return value.x();
        }

        @Override
        protected boolean replace(PriProxy<X, Y> incoming, float inPri, PriProxy<X, Y> existing, float exPri) {
            if (super.replace(incoming, inPri, existing, exPri)) {
                return true;
            } else {
                //remains, gradually weaken
                cut(existing);
                return false;
            }
        }

        /** loss of priority if survives */
        protected void cut(PriProxy<X,Y> p) {
            p.priAdd(-CACHE_SURVIVE_COST);
        }


        @Override
        public Consumer<PriProxy<X, Y>> forget(float rate) {
            /* TODO */ return null;
        }

        @Override
        public void onReject(PriProxy<X, Y> value) {
            rejected(value);
        }

        @Override
        public void onRemove(PriProxy<X, Y> value) {
            removed(value);
            value.delete();
            evict.getAndIncrement();
        }
    }

    /** subclasses can implement removal handler here */
    protected void removed(PriProxy<X,Y> value) {

    }
    /** subclasses can implement removal handler here */
    protected void rejected(PriProxy<X,Y> value) {

    }
}