package jcog.memoize;

import jcog.Log;
import jcog.TODO;
import jcog.data.map.ConcurrentFastIteratingHashSet;
import jcog.io.BinTxt;
import jcog.memoize.byt.ByteHijackMemoize;
import jcog.memoize.byt.ByteKeyExternal;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.function.Function;

/** manager for multiple (pre-registered) memoizeByte instances, each with its own key/value type.
 *
 *  TODO each separate memoizer can be managed centrally
 *      * memory consumption
 *      * access patterns
 *
 *  TODO shutdown hook print stats
 *
 *  TODO labels for each memoization function
 *
 *  TODO byte key convenience method
 * */

public class Memoizers {


    public static final int DEFAULT_HIJACK_REPROBES = 5;
    //1gb -> 64k?
    public static final int DEFAULT_MEMOIZE_CAPACITY = (int) (Runtime.getRuntime().maxMemory()/6_000);

    private final Collection<MemoizationStatistics> memoize = new ConcurrentFastIteratingHashSet<>(new MemoizationStatistics[0]);

    public static final Memoizers the = new Memoizers();

    private Memoizers() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::log));
    }

    protected void log() {
        for (MemoizationStatistics m : memoize)
            m.log();
    }

    public <X,B extends ByteKeyExternal,Y> Function<X,Y> memoizeByte(String id, Function<X,B> byter, Function<B,Y> computation, int capacity) {
        Function<B, Y> c = memoizeByte(id, computation, capacity);
        return x -> c.apply(byter.apply(x));
    }

    /** auto-tune the capacity heuristically from performance archives from previous executions */
    public <X,Y> Function<X,Y> memoize(String id, Function<X, Y> computation) {
        throw new TODO();
    }

    /** registers a new memoizer with a default memoization implementation */
    public <X,Y> Function<X,Y> memoize(String id, Function<X, Y> computation, int capacity) {
        return add(id, computation, memoizer(computation, capacity));
    }

    private static String id(String id, Object instance) {
        return id + "_" + BinTxt.toString(System.identityHashCode(instance));
    }

    public <X, Y, M extends Memoize<X,Y>> Function<X, Y> add(String id, Object instance, M m) {
        memoize.add(new MemoizationStatistics(id(id, instance), m));
        return m;
    }

    /** provides default memoizer implementation */
    private static <X, Y> Memoize<X,Y> memoizer(Function<X, Y> computation, int capacity) {
        return new HijackMemoize<>(computation, capacity, DEFAULT_HIJACK_REPROBES);
    }

    public <X extends ByteKeyExternal, Y> ByteHijackMemoize<X, Y> memoizeByte(String id, Function<X, Y> computation, int capacity) {
        ByteHijackMemoize<X, Y> c = new ByteHijackMemoize<>(computation, capacity, DEFAULT_HIJACK_REPROBES, false);
        add(id, computation, c);
        return c;
    }

    static final Logger logger = Log.log(Memoizers.class);

    private static class MemoizationStatistics {
        public final String name;
        public final WeakReference<Memoize> memoize;

        MemoizationStatistics(String name, Memoize memoize) {
            this.name = name;
            this.memoize = new WeakReference<>(memoize);
        }

        public void log() {
            Memoize m = memoize.get();
            if (m != null && logger.isInfoEnabled())
                logger.info("{} {}", name, m.summary());
//            else
//                System.out.println(name + " DELETED");
        }
    }
}