package jcog.pri;

import jcog.Util;

import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * Numerically re-prioritizable reference.
 * Repurposes SoftReference.timestamp field as a priority value.
 * <p>
 * Intended to re-use all SoftReference-related VM features
 * except its time-as-priority behavior.
 *
 * Experimental, needs testing
 */
public class FuzzyReference<T> extends SoftReference<T> {

    private static final VarHandle PRI = Util.VAR(SoftReference.class, "timestamp", long.class);
    private static final VarHandle REF = Util.VAR(Reference.class, "referent", Object.class);

    /**
     * default constructor.  priority initialized to 0
     *
     * @param referent reference
     */
    public FuzzyReference(T referent) {
        this(referent, 0);
    }

    /**
     * default constructor
     *
     * @param referent reference
     * @param pri initial priority
     */
    public FuzzyReference(T referent, long pri) {
        super(referent);
        pri(pri);
    }

    /**
     * default constructor, with ReferenceQueue.  see SoftReference constructor for details
     *
     * @param referent reference
     * @param q        queue
     */
    public FuzzyReference(T referent, ReferenceQueue<T> q) {
        super(referent, q);
        pri(0);
    }

    /**
     * @return reference priority
     */
    public final long pri() {
        return (long) PRI.get(this);
    }

    /**
     * sets reference priority
     *
     * @param p new priority value
     */
    public final void pri(long p) {
        PRI.set(this, p);
    }

    /**
     * @return reference, without triggering: SoftReference.timestamp=clock
     */
    @Override
    public T get() {
        return (T) REF.get(this);
    }

}