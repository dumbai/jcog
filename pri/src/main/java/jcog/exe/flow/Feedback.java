package jcog.exe.flow;

import jcog.signal.meter.FastCounter;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectFloatHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * records Feedback events
 *
 * @param <K> kontext - thread-local kontext object
 * @param <C> cause   - cause
 * @param <X> result  - effect/outcome
 */
public interface Feedback<K, C, X> {


    ThreadLocal<Feedback> model = ThreadLocal.withInitial(() -> null);
    ThreadLocal<FeedbackThread> ctx = ThreadLocal.withInitial(() -> {
        Feedback m = m();
        if (m != null) return new FeedbackThread(m.newContext());
        return null;
    });

    static void start(Supplier<Object> id) {
        var m = m();
        if (m != null)
            ctx().start(m, id.get());
    }

    static boolean start(@Nullable Object id) {
        return id != null && _start(id);
    }

    private static boolean _start(Object id) {
        var m = m();
        if (m != null) {
            ctx().start(m, id);
            return true;
        }
        return false;
    }

    static void end() {
        _end();
    }

    private static void _end() {
        var m = m();
        if (m != null)
            ctx().end(m);
    }

    /**
     * provide feedback
     */
    static void is(Object tag) {
        var m = m();
        if (m != null) ctx().is(m, tag);
    }

    static void is(Object tag, FastCounter counter) {
        counter.increment();
        is(tag);
    }

    static void set(@Nullable Feedback next) {
        Feedback prev = m(); if (prev == next) return;

//        if (prev != null) {
            //throw new TODO("change " + Feedback.class.getName() + " models");
            //TODO add feedback.end() method
//        }

        model.set(next);
    }

    private static FeedbackThread ctx() {
        return ctx.get();
    }

    private static <X> double plus(Map<X, Double> qq, X x, double p) {
        return qq.merge(x, p, Double::sum);
    }

    @Nullable
    private static <K, C> Feedback<K, C, ?> m() {
        return (Feedback<K, C, ?>) model.get();
    }

    /**
     * ctx -> cause -> result
     */
    void accept(K ctx, C cause, X result);

    default void start(K kontext, C cause) {
    }

    default void end(K kontext, C cause) {
    }

    /**
     * creates a new
     */
    K newContext();

    /**
     * the combination of a short[] set and a partial stacktrace provide the context for a recursive
     * feedback credit assignment system
     */
    final class FeedbackThread<K, C> {

        private final K kontext;

        @Nullable
        private C cause;

        private FeedbackThread(K k) {
            this.kontext = k;
        }

        private <X> void is(Feedback<K, C, X> m, X result) {
            m.accept(kontext, cause, result);
        }

        /**
         * set current cause
         */
        private void start(Feedback<K, C, ?> m, C next) {
            C prev = this.cause;
            if (prev == next) return;

            if (prev != null)
                m.end(kontext, prev);

            this.cause = next;

            if (next != null)
                m.start(kontext, next);
        }

        private void end(Feedback<K, C, ?> m) {

            m.end(kontext, cause);

            cause = null;
        }

    }


    /**
     * immediate commit
     */
    class PriFeedback<C, X> implements Feedback<Object, C, X> {
        final Map<C, Map<X, Double>> PRI = new ConcurrentHashMap<>();

        @Override
        public void accept(Object ignored, C cause, X result) {
            plus(PRI.computeIfAbsent(cause, c -> new ConcurrentHashMap<>()), result, 1);
        }

        @Override
        public Object newContext() {
            return null;
        }
    }

    /**
     * buffers results and commits when the cause changes
     */
    class BufferedPriFeedback<C, X> implements Feedback<ObjectFloatHashMap<X>, C, X> {
        final Map<C, Map<X, Double>> PRI = new ConcurrentHashMap<>();

        @Override
        public void accept(ObjectFloatHashMap<X> pp, C cause, X result) {
            pp.addToValue(result, 1 /* TODO PLink */);
        }

        @Override
        public void end(ObjectFloatHashMap<X> kontext, C cause) {
            Map<X, Double> qq = PRI.computeIfAbsent(cause, (c) -> new ConcurrentHashMap<>());
            kontext.forEachKeyValue((x, p) -> plus(qq, x, p));
            kontext.clear();
        }

        @Override
        public ObjectFloatHashMap<X> newContext() {
            return new ObjectFloatHashMap<>();
        }
    }


    //    private StackFrame start;

    //    private static final AtomicInteger nextCause = new AtomicInteger(0);
    //    public static synchronized short newCause() {
    //        int s = nextCause.incrementAndGet();
    //        if (s > Short.MAX_VALUE)
    //            throw new IndexOutOfBoundsException(Feedback.class + " cause overflow");
    //        return (short) s;
    //    }


    //    /** get the current state */
    //    public <C> C cause() {
    //        return (C) cause;
    //    }

    //    public StackFrame[] stack() {
    //        return StackWalker.getInstance().walk(s -> {
    //            assert(start!=null);
    //            //TODO drop first N to actual callee
    //            return s.takeWhile(z->z!=start).toArray(StackFrame[]::new);
    //        });
    //    }

    //    public static void start() {
    //        ctx().start = StackWalker.getInstance().walk(s -> (StackFrame)(s.findFirst().get()));
    //    }
}