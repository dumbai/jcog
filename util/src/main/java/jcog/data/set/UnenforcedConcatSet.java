package jcog.data.set;

import com.google.common.base.Joiner;
import jcog.data.iterator.Concaterator;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO make a caching subclass which
 * generates an arraylist copy during the first iteration for fast
 * subsequent iterations. this copy can be linked via SoftReference<>
 */
public class UnenforcedConcatSet<X> extends AbstractSet<X> {

    final Set<X> a;
    final Set<X> b;
    final int size;

    UnenforcedConcatSet(Set<X> a, Set<X> b) {
        this.a = a;
        this.b = b;
        this.size = a.size() + b.size();
    }
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return a.contains(o) || b.contains(o);
    }

    @Override
    public boolean isEmpty() {
        return size==0;
    }

    @Override
    public Iterator<X> iterator() {
        return Concaterator.concat(a, b);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return Joiner.on(',').join(iterator());
    }


    /** if a or b are null, they are considered empty sets */
    public static <X> Set<X> concat(@Nullable Set<X> a, @Nullable Set<X> b) {
        Set<X> nextFree;
        boolean aEmpty = a == null || a.isEmpty();
        boolean bEmpty = b == null || b.isEmpty();
        if (bEmpty && aEmpty) {
            nextFree = Collections.EMPTY_SET;
        } else if (bEmpty) {
            nextFree = a;
        } else if (aEmpty) {
            nextFree = b;
        } else {
            nextFree = new UnenforcedConcatSet<>(a, b);
        }
        return nextFree;
    }

}
