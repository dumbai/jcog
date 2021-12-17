package jcog.tree.rtree;

import jcog.Util;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.locks.StampedLock;

public class RInsertion<X> {

    public final Spatialization<? super X, ?> model;
    public final X x;

    Set<X> reinserts;

    public final HyperRegion xBounds;

    public boolean addOrMerge;

    /** was the insertion merged */
    public boolean merged;
    /** was the add successful */
    public boolean added;

    /** additional removals that may have occurred as a result of consolidation */
    public int removals = 0;

    /** if merged, whether the merge resulted in stretch (recurse bounds update necessary on return) */
    public boolean stretched = false;

    /** stamped lock, used if concurrent */
    public long lock = 0;
    @Nullable public StampedLock _lock;

    /**
     * @param x      - value to add to index
     * @param parent - the callee which is the parent of this instance.
 *                  if parent is null, indicates it is in the 'merge attempt' stage
 *                  if parent is non-null, in the 'insertion attempt' stage
     * @param model
     * @param added
*              so when a callee Branch receives "bounds not changed"
*              it will know it doesnt need to update bounds
*
     */
    public RInsertion(X x, boolean addOrMerge, Spatialization<? super X, ?> model) {
        this.x = x;
        this.xBounds = model.bounds(x);
        this.addOrMerge = addOrMerge;
        this.model = model;
        this.added = false;
    }


    /**
	 * equality will have been tested first.
	 * if a merge is possible, either a or b or a new task will be returned.  null otherwise
	 * existing and incoming will not be the same instance.
	 * default implementation: test for equality and re-use existing item
	 */
    public @Nullable X merge(X existing) {
        return null;
//        merged = true;
//        return existing;
//        X z = model.merge(existing, x, this);
//        if (z!=null)
//            merged = true;
//        return z;
    }

    public void mergeEqual(X existing) {
        merged = true;
    }

    public final boolean maybeContainedBy(HyperRegion c) {
        return model.canMergeStretch() ? c.intersects(xBounds) : c.contains(xBounds);
    }

    public void write() {
        StampedLock LOCK = _lock;
        if (LOCK !=null)
            this.lock = Util.readToWrite(this.lock, LOCK);
    }

    public void read() {
        StampedLock LOCK = _lock;
        if (LOCK !=null)
            this.lock = Util.writeToRead(this.lock, LOCK);
    }

    public void reinsert(X x) {
        if (reinserts == null)
            reinserts = new UnifiedSet(1, 1f);
        reinserts.add(x);
    }


//    public final void start(Spatialization<X> t) {
//        this.space = t;
//    }
//    public final void end(Spatialization<X> xrTree) {
//        this.space = null;
//    }
//
//    public final Spatialization<X> space() {
//        return space;
//    }
}